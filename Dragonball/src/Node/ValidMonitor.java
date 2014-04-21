package Node;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import structInfo.LogInfo;
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
				Runnable messageSender = new UnSubscribeMessageSender(this.getServerOwner(),targetUnit);
				new Thread(messageSender).start();
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
