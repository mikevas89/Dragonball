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
			
			//TODO: checks the Server2Server Connections according to the update of Ping messages
			for(Iterator<Entry<Node, ServerInfo>> it= Server.getServerList().entrySet().iterator();it.hasNext();){
				Entry<Node, ServerInfo> entry = it.next();
				
				//regular communication
				if(System.currentTimeMillis() - entry.getValue().getRemoteNodeTimeLastPingSent() < 2* Constants.SERVER2SERVER_PING_PERIOD)
					continue;
				//broadcast "ProblematicServer" message to all servers except the problematic one 
				if(System.currentTimeMillis() - entry.getValue().getRemoteNodeTimeLastPingSent() < Constants.SERVER2SERVER_TIMEOUT && 
																!entry.getValue().isProblematicServer()){
					Runnable pingMonitorSender=new PingMonitorSender(entry.getKey(),MessageType.ProblematicServer,
																	PingMonitorSender.DecisionType.Undefined);
					new Thread(pingMonitorSender).start();
					entry.getValue().setProblematicServer(true);
					it.remove();
					Server.getServerList().put(entry.getKey(), entry.getValue());
				}
				
			}
			
			
			
			//checks the Client2Server Connections according to the update of Ping messages from the Client
			
			for(Iterator<Entry<Node, ClientPlayerInfo>> it= Server.getClientList().entrySet().iterator();it.hasNext();){
				Entry<Node, ClientPlayerInfo> entry = it.next();
				//regular communication 
				if(System.currentTimeMillis() - entry.getValue().getTimeLastPingSent() < Constants.CLIENT2SERVER_PING_PERIOD)
					continue;
				
				if(System.currentTimeMillis() - entry.getValue().getTimeLastPingSent() < 2* Constants.CLIENT2SERVER_PING_PERIOD &&
						!entry.getValue().isServerHasSentPingForCheckingClient()){
					
					Runnable pingMonitorSender=new PingMonitorSender(entry.getKey(),MessageType.ServerClientPing,
																	PingMonitorSender.DecisionType.Undefined);
					new Thread(pingMonitorSender).start();
					
					entry.getValue().setServerHasSentPingForCheckingClient(true);
					entry.getValue().setLastPingFromServer(System.currentTimeMillis());
					entry.getValue().setRegularCommunicationFromClient(false);
					it.remove();
					Server.getClientList().put(entry.getKey(),entry.getValue());	
					continue;
				}
				
				if(System.currentTimeMillis() - entry.getValue().getTimeLastPingSent() >= 2* Constants.CLIENT2SERVER_PING_PERIOD){
					Unit clientUnit = Server.getBattlefield().getUnitByUnitID(entry.getValue().getUnitID());
					System.err.println("Ping Monitor will unsubscribe client "+ entry.getKey().getName());
					Runnable messageSender = new UnSubscribeMessageSender(Server.getPendingActions(),clientUnit);
					new Thread(messageSender).start();
					//logs the removal of the client- player
					LogInfo playerDown = new LogInfo(Action.Removed,clientUnit.getUnitID(), clientUnit.getX(),clientUnit.getY(),
														clientUnit.getType(clientUnit.getX(),clientUnit.getY()),
														clientUnit.getUnitID(), 
														clientUnit.getX(),clientUnit.getY(),
														clientUnit.getType(clientUnit.getX(),clientUnit.getY()),
														System.currentTimeMillis(), "0.0.0.0");
					Server.getValidActions().add(playerDown);
					
					Runnable validActionPlayerDownSender = new ValidActionSender(playerDown);
					new Thread(validActionPlayerDownSender).start();
				}
				
			}
			
		}
	}
	

}
