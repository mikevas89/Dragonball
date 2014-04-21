package Node;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import structInfo.Constants;
import structInfo.LogInfo;

public class PendingMonitor implements Runnable {

		Map<String, LogInfo> PendingActions;
		private BlockingQueue<LogInfo> validBlockQueue;

		
		public PendingMonitor(Map<String, LogInfo> pendinglist, BlockingQueue<LogInfo> validBlockQueue)
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
				    
				    if(Server.getBattlefield().getUnit(action.getSenderX(), action.getSenderY()).getUnitID()!=action.getSenderUnitID() ||
				    		Server.getBattlefield().getUnit(action.getTargetX(), action.getTargetY()).getUnitID()!=action.getTargetUnitID()){
				    				System.err.println("Pending Monitor found an inconsistency from the updated Battlefield");
				    				iter.remove();
				    				continue;
				    }			    
				    
				    // check for the timeout!!
				    Long curtime = System.currentTimeMillis();
				    if(curtime - action.getTimestamp() > Constants.PENDING_TIMEOUT)
				    {
				    	System.out.println("Pending -> VALID action: " + action.toString());
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
