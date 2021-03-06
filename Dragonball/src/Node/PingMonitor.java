package Node;

import java.util.Iterator;
import java.util.Map.Entry;

import messages.MessageType;

import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import structInfo.ServerInfo;
import units.Unit;

//checks the Timeouts of Ping Messages between Server-Client and Server-Server
public class PingMonitor implements Runnable{
	
	public PingMonitor(){
		
	}

	@Override
	public void run() {
		
		while(!Server.killServer){
			
			try {
				Thread.sleep(Constants.PING_MONITOR_CHECKING_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//System.out.println("PingMonitor is checking..");
			
			// checks the Server2Server Connections according to the update of Ping messages
			for(Iterator<Entry<Node, ServerInfo>> it= Server.getServerList().entrySet().iterator();it.hasNext();){
				Entry<Node, ServerInfo> entry = it.next();
				long currentTime = System.nanoTime();
			//	System.out.println("Ping Checking: CurrentTime:"+ System.nanoTime() 
		//							+ " NodePing:"+ entry.getValue().getRemoteNodeTimeLastPingSent()+
			//						" Difference:"+ (System.nanoTime() - entry.getValue().getRemoteNodeTimeLastPingSent()) +
				//					"2*PingPeriod:"+ 2* Constants.SERVER2SERVER_PING_PERIOD);
				//regular communication
				if((currentTime - entry.getValue().getRemoteNodeTimeLastPingSent()) < (2* Constants.SERVER2SERVER_PING_PERIOD
																										*Constants.NANO)){
					//System.out.println("No worries");
					continue;
				}
				//broadcast "ProblematicServer" message to all servers except the problematic one 
				if(((currentTime - entry.getValue().getRemoteNodeTimeLastPingSent()) < Constants.SERVER2SERVER_TIMEOUT*Constants.NANO)
																						&& 	!entry.getValue().isProblematicServer()){
					System.err.println("Problematic Server: "+ entry.getKey().getName());
					Runnable pingMonitorSender=new PingMonitorSender(entry.getKey(),MessageType.ProblematicServer,
																	PingMonitorSender.DecisionType.Undefined);
					entry.getValue().setProblematicServer(true);
					entry.getValue().setNumAnswersAgreeRemovingServer(entry.getValue().getNumAnswersAgreeRemovingServer()+1);
					entry.getValue().setTotalNumAnswersAggreement(entry.getValue().getNumAnswersAgreeRemovingServer()+
														entry.getValue().getNumAnswersNotAgreeRemovingServer());
					entry.getValue().setNodeFoundTheProblematicServer(new Node(Server.getMyInfo().getName(),
																		Server.getMyInfo().getName()));
					Server.getServerList().replace(entry.getKey(), entry.getValue());
					Server.printlist();
					new Thread(pingMonitorSender).start();
				}
				
			}
			
			
			
			//checks the Client2Server Connections according to the update of Ping messages from the Client
			
			for(Iterator<Entry<Node, ClientPlayerInfo>> it= Server.getClientList().entrySet().iterator();it.hasNext();){
				Entry<Node, ClientPlayerInfo> entry = it.next();
				long currentTime = System.nanoTime();
				//regular communication 
				if((currentTime - entry.getValue().getTimeLastPingSent()) < Constants.CLIENT2SERVER_PING_PERIOD*Constants.NANO)
					continue;
				
			/*	if((currentTime - entry.getValue().getTimeLastPingSent()) < (2* Constants.CLIENT2SERVER_PING_PERIOD*Constants.NANO) &&
						!entry.getValue().isServerHasSentPingForCheckingClient()){
					
					Runnable pingMonitorSender=new PingMonitorSender(entry.getKey(),MessageType.ServerClientPing,
																	PingMonitorSender.DecisionType.Undefined);
					new Thread(pingMonitorSender).start();
					
					entry.getValue().setServerHasSentPingForCheckingClient(true);
					entry.getValue().setLastPingFromServer(System.nanoTime());
					entry.getValue().setRegularCommunicationFromClient(false);
					it.remove();
					Server.getClientList().put(entry.getKey(),entry.getValue());	
					continue;
				}*/
				
				if((currentTime - entry.getValue().getTimeLastPingSent()) >= Constants.SERVER2CLIENT_TIMEOUT*Constants.NANO){
					Unit clientUnit = Server.getBattlefield().getUnitByUnitID(entry.getValue().getUnitID());
					System.out.println("Ping Monitor will unsubscribe client "+ entry.getKey().getName()+
							" diff:"+(System.nanoTime() - entry.getValue().getTimeLastPingSent())+
							" timeout:"+Constants.SERVER2CLIENT_TIMEOUT*Constants.NANO);
					Runnable messageSender = new UnSubscribeMessageSender(Server.getPendingActions(),clientUnit);
					new Thread(messageSender).start();
					//logs the removal of the client- player
					LogInfo playerDown = new LogInfo(Action.Removed,clientUnit.getUnitID(), clientUnit.getX(),clientUnit.getY(),
														clientUnit.getType(clientUnit.getX(),clientUnit.getY()),
														clientUnit.getUnitID(), 
														clientUnit.getX(),clientUnit.getY(),
														clientUnit.getType(clientUnit.getX(),clientUnit.getY()),
														System.nanoTime(), Server.getMyInfo().getName());
					Server.getValidActions().add(playerDown);
					
					Runnable validActionPlayerDownSender = new ValidActionSender(playerDown);
					new Thread(validActionPlayerDownSender).start();
				}
				
			}
			
		}
	}
	

}
