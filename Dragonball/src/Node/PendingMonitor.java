package Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import structInfo.LogInfo;

public class PendingMonitor implements Runnable {

		HashMap<String, LogInfo> PendingActions;
		ArrayList<LogInfo> ValidActions;
		public static final int PENDING_TIMEOUT = 2000;
		public static final int CHECK_FREQUENCY = 1000;

		
		public PendingMonitor(HashMap<String, LogInfo> pendinglist, ArrayList<LogInfo> validlist)
		{
			this.PendingActions=pendinglist;
			this.ValidActions=validlist;
		}

		@Override
		public void run() {
		
			while (true)
			{
				Iterator<String> iter = PendingActions.keySet().iterator();
				while(iter.hasNext()) {
					String key = (String)iter.next();
				    LogInfo val = (LogInfo)PendingActions.get(key);
				    // check for the timeout!!
				    Long curtime = System.currentTimeMillis();
				    if(curtime - val.getTimestamp() > PENDING_TIMEOUT)
				    {
				    	System.out.println("VALID key,val: " + key + "," + val.getSenderIP());
				    	ValidActions.add(val);
				    	iter.remove();	
				    	//TODO !+++SEND message
				    }
				}
				try {
					Thread.sleep(CHECK_FREQUENCY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
}
