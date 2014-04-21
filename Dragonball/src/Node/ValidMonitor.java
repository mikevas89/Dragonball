package Node;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import structInfo.LogInfo;
import units.Unit;

public class ValidMonitor implements Runnable{

	private ArrayList<LogInfo> validActions;
	private BlockingQueue<LogInfo> validBlockQueue;
	
	public ValidMonitor(ArrayList<LogInfo> validActions, BlockingQueue<LogInfo> validBlockQueue){
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
			Unit senderUnit= Server.getBattlefield().getUnitByUnitID(newAction.getUnitID());
			switch (newAction.getTargetType()) {
			case undefined:
				// There is no unit in the square. Move the player to this square
				Server.getBattlefield().moveUnit(senderUnit, newAction.getTargetX(), newAction.getTargetY());
				//this.serverOwner.setPendingActions(x*y, new LogInfo(client.getIP(), LogInfo.Action.Move, x, y, message.getTimeIssuedFromServer()));
				break;
			case player:
				// There is a player in the square, attempt a healing
				Server.getBattlefield().healDamage(newAction.getTargetX(), newAction.getTargetY(), senderUnit.getAttackPoints());
				//this.serverOwner.setPendingActions(x*y, new LogInfo(client.getIP(), LogInfo.Action.Heal, x, y, message.getTimeIssuedFromServer()));
				break;
			case dragon:
				// There is a dragon in the square, attempt a dragon slaying
				Server.getBattlefield().dealDamage(newAction.getTargetX(), newAction.getTargetY(), senderUnit.getAttackPoints());
				//this.serverOwner.setPendingActions(x*y, new LogInfo(client.getIP(), LogInfo.Action.Damage, x, y, message.getTimeIssuedFromServer()));
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

}
