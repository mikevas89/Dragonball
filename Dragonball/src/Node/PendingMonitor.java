package Node;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import structInfo.Constants;
import structInfo.LogInfo;
import units.Unit;

public class PendingMonitor implements Runnable {

		Map<String, LogInfo> PendingActions;
		private LinkedBlockingQueue<LogInfo> validBlockQueue;

		
		public PendingMonitor(Map<String, LogInfo> pendinglist, LinkedBlockingQueue<LogInfo> validBlockQueue)
		{
			this.PendingActions=pendinglist;
			this.validBlockQueue=validBlockQueue;
		}

		@Override
		public void run() {
			//if Server is killed, then finish
			while (!Server.killServer)
			{
				Iterator<String> iter = PendingActions.keySet().iterator();
				while(iter.hasNext()) {
					String key = (String)iter.next();
				    LogInfo action = (LogInfo)PendingActions.get(key);
				    //print Pending actions
				    action.toString();
				    //check if the action can still be played according to an update of the battlefield
				    Unit existingTargetUnit=Server.getBattlefield().getUnit(action.getTargetX(), action.getTargetY());
				    int existingTargetUnitID;
				    if(existingTargetUnit==null)
				    	existingTargetUnitID=-1;
				    else
				    	existingTargetUnitID=existingTargetUnit.getUnitID();
				    if(Server.getBattlefield().getUnit(action.getSenderX(), action.getSenderY()).getUnitID()!=action.getSenderUnitID() ||
				    		existingTargetUnitID!=action.getTargetUnitID()){
				    				System.err.println("Pending Monitor found an inconsistency from the updated Battlefield");
				    				iter.remove();
				    				continue;
				    }			    
				    
				    // check for the timeout!!
				    long curtime = System.nanoTime();
				    if((curtime - action.getTimestamp()) > Constants.PENDING_TIMEOUT*Constants.NANO)
				    {
				    	//System.out.println("Pending -> VALID action: " + action.toString());
				    	try {
							this.validBlockQueue.put(action);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				    	//remove it from pending list
				    	iter.remove();	
				    }
				}
				try {
					Thread.sleep(Constants.CHECK_PENDING_LIST_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
}
