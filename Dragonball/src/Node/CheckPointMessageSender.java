package Node;

import interfaces.ServerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import messages.MessageType;
import messages.ServerServerMessage;
import structInfo.ServerInfo;

public class CheckPointMessageSender implements Runnable{

	public CheckPointMessageSender(){
		
	}
	
	public void run(){
		for(ServerInfo serverInfo: Server.getServerList().values()){
			if(!serverInfo.isRunsGame() || !serverInfo.isProblematicServer())
					continue;
			
			 ServerServerMessage sendCheckPoint = new ServerServerMessage(
									MessageType.NewCheckPoint,
									Server.getMyInfo().getName(),
									Server.getMyInfo().getIP(),
									serverInfo.getName(),
									serverInfo.getIP());
			 sendCheckPoint.setBattlefield(Server.getBattlefield());
			 sendCheckPoint.setValidActionsLog(Server.getValidActions());
			 
			 
			//sending the PendingInvalid message to subscribed client
			 ServerServer serverRMI=null;
			 serverRMI = Server.getServerReg(new Node(serverInfo.getName(),serverInfo.getIP()));
			 
			 if(serverRMI == null) return;
			
			 try {
				 serverRMI.onMessageReceived(sendCheckPoint);
			 } catch (RemoteException | NotBoundException e) {
				//e.printStackTrace();
			 }
			
			 System.out.println("Server: sendCheckPoint sent to Server"+ serverInfo.getName()+ "serverIP: "+ serverInfo.getIP());
		}

	}
	
}
