package Node;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import units.Player;
import units.Unit;

public class ValidMonitor implements Runnable{

	private Server serverOwner;
	private ArrayList<LogInfo> validActions;
	private BlockingQueue<LogInfo> validBlockQueue;
	
	public ValidMonitor(Server serverOwner, ArrayList<LogInfo> validActions, BlockingQueue<LogInfo> validBlockQueue){
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
			//TODO,TODO : check for rollback here? after this, the action is valid and it WILL BE played (targetUnitID may also be -1)
			Unit existingTargetUnit=Server.getBattlefield().getUnit(newAction.getTargetX(), newAction.getTargetY());
		    int existingTargetUnitID;
		    if(existingTargetUnit==null)
		    	existingTargetUnitID=-1;
		    else
		    	existingTargetUnitID=existingTargetUnit.getUnitID();
		    if(Server.getBattlefield().getUnit(newAction.getSenderX(), newAction.getSenderY()).getUnitID()!=newAction.getSenderUnitID() ||
		    		existingTargetUnitID!=newAction.getTargetUnitID()){
					    	System.err.println("Valid Monitor found an inconsistency from the updated Battlefield");
							continue; 	
		    }
			Unit senderUnit= Server.getBattlefield().getUnitByUnitID(newAction.getSenderUnitID());
			if(senderUnit==null){
				System.err.println("ValidMonitor: Move from unit which is not in the BattleField");
				continue;
			}
			switch (newAction.getTargetType()) {
			case undefined:
				// There is no unit in the square. Move the player to this square
				Server.getBattlefield().moveUnit(senderUnit, newAction.getTargetX(), newAction.getTargetY());
				break;
			case player:
				// There is a player in the square, attempt a healing
				Server.getBattlefield().healDamage(newAction.getTargetX(), newAction.getTargetY(), senderUnit.getAttackPoints());
				break;
			case dragon:
				// There is a dragon in the square, attempt a dragon slaying
				Server.getBattlefield().dealDamage(newAction.getTargetX(), newAction.getTargetY(), senderUnit.getAttackPoints());
				break;
		}
			//logs the new Valisd Action
			this.validActions.add(newAction);			
			
			//broadcast to servers
			new Thread(new Runnable(){

				@Override
				public void run() {
					//TODO: create message ServerServerMessage
					//put battlefield in the message
					//send to all servers
				}
				
			}).start();
			
			
			Unit targetUnit= Server.getBattlefield().getUnitByUnitID(newAction.getTargetUnitID());
			if((targetUnit instanceof Player) && (targetUnit.getHitPoints()<=0)){
				System.err.println("Valid Monitor went to unsubscribed");
				Runnable messageSender = new UnSubscribeMessageSender(this.getServerOwner(),targetUnit);
				new Thread(messageSender).start();
				LogInfo playerDown = new LogInfo(Action.Removed,targetUnit.getUnitID(), targetUnit.getX(),targetUnit.getY(),
													targetUnit.getType(targetUnit.getX(),targetUnit.getY()),
													targetUnit.getUnitID(), 
													targetUnit.getX(),targetUnit.getY(),
													targetUnit.getType(targetUnit.getX(),targetUnit.getY()),
													System.currentTimeMillis(), "0.0.0.0");
				this.validActions.add(playerDown);
			}
			
			
		}
	}

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

	public Server getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}

}
