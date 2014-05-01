package Node;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import structInfo.UnitType;
import units.Dragon;
import units.Player;
import units.Unit;

public class ValidMonitor implements Runnable{

	private Map<String,LogInfo> pendingActions;
	private ArrayList<LogInfo> validActions;
	private BlockingQueue<LogInfo> validBlockQueue;
	
	public ValidMonitor(Map<String,LogInfo> pendingActions, ArrayList<LogInfo> validActions, BlockingQueue<LogInfo> validBlockQueue){
		this.setPendingActions(pendingActions);
		this.setValidActions(validActions);
		this.setValidBlockQueue(validBlockQueue);
	}
	
	@Override
	public void run() {
		LogInfo newAction = null;
		while (!Server.killServer){
			try {
				newAction = validBlockQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("ValidMonitor : ActionToValid-> "+ newAction.toString());
			synchronized (Server.lock) {
				// A Removed Action has received from other Server
				if (newAction.getAction().equals(Action.Removed)) {
					ListIterator<Unit> it = Server.getBattlefield().getUnits()
							.listIterator();
					while (it.hasNext()) {
						Unit unit = it.next();
						if (unit.getUnitID() == newAction.getSenderUnitID()) {
							Server.getBattlefield().removeUnit(unit.getX(),
									unit.getY(), it);
							break;
						}
					}
					// logs the new Valid Action "Removed"
					this.validActions.add(newAction);
					continue;
				}
			}
			
			if (!newAction.getSenderType().equals(UnitType.dragon)) {

				// after this, the action is valid and it WILL BE played
				// (targetUnitID may also be -1)
				Unit existingTargetUnit = Server.getBattlefield().getUnit(
						newAction.getTargetX(), newAction.getTargetY());
				int existingTargetUnitID;
				if (existingTargetUnit == null)
					existingTargetUnitID = -1;
				else
					existingTargetUnitID = existingTargetUnit.getUnitID();

				if (Server.getBattlefield().getUnit(newAction.getSenderX(), newAction.getSenderY()).getUnitID() != newAction.getSenderUnitID() || 
						existingTargetUnitID != newAction.getTargetUnitID()) {
					System.err.println("Valid Monitor found an inconsistency from the updated Battlefield");
					
					//clear the pending/validBlockQueue
					Server.getPendingActions().clear();
					Server.getValidBlockQueue().clear();
					//copy checkpoint state to running battlefield
					Server.getBattlefield().copyMap(Server.getCheckPoint().getMap());
					Server.getBattlefield().copyListUnits(Server.getCheckPoint().getUnits());
					Server.copyValidActions(Server.getCheckPoint().getCheckPointValidActions());
					//broadcast the checkpoint battlefield to all
					Runnable checkPointMessage = new CheckPointMessageSender();
					new Thread(checkPointMessage).start();
					return;
				}

				Unit senderUnit = Server.getBattlefield().getUnitByUnitID(
						newAction.getSenderUnitID());
				if (senderUnit == null) {
					System.err
							.println("ValidMonitor: Move from unit which is not in the BattleField");
					continue;
				}

				switch (newAction.getTargetType()) {
				case undefined:
					// There is no unit in the square. Move the player to this
					// square
					Server.getBattlefield().moveUnit(senderUnit,
							newAction.getTargetX(), newAction.getTargetY());
					break;
				case player:
					// There is a player in the square, attempt a healing
					Server.getBattlefield().healDamage(newAction.getTargetX(),
							newAction.getTargetY(),
							senderUnit.getAttackPoints());
					break;
				case dragon:
					// There is a dragon in the square, attempt a dragon slaying
					Server.getBattlefield().dealDamage(newAction.getTargetX(),
							newAction.getTargetY(),
							senderUnit.getAttackPoints());
					break;
				}
				// logs the new Valid Action
				this.validActions.add(newAction);

				// server sends ONLY his valid actions
				if (!newAction.getSenderName().equals(
						Server.getMyInfo().getName()))
					continue;

			}
			else{ //dragon's action
				this.validActions.add(newAction);
			}
			/*----------------------------------------------------
			Create thread for sending Valid Action
			----------------------------------------------------		
			 */
			System.err.println(Server.getMyInfo().getName()+": sends Valid Action - on Valid Monitor");
			
			Runnable validActionSender = new ValidActionSender(newAction);
			new Thread(validActionSender).start();
			
			//server checks for removal
			Server.checkIfUnitIsDead(newAction);
		}
	}

	/*----------------------------------------------------
		GETTERS AND SETTERS
	----------------------------------------------------		
*/
	
	public ArrayList<LogInfo> getValidActions() {
		return validActions;
	}

	public void setValidActions(ArrayList<LogInfo> validActions) {
		this.validActions = validActions;
	}

	public BlockingQueue<LogInfo> getValidBlockQueue() {
		return validBlockQueue;
	}

	public void setValidBlockQueue(BlockingQueue<LogInfo> validBlockQueue) {
		this.validBlockQueue = validBlockQueue;
	}

	public Map<String,LogInfo> getPendingActions() {
		return pendingActions;
	}

	public void setPendingActions(Map<String,LogInfo> pendingActions) {
		this.pendingActions = pendingActions;
	}

}
