package Node;

import interfaces.ServerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import messages.MessageType;
import messages.ServerServerMessage;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import structInfo.UnitType;
import structInfo.LogInfo.Action;

public class ValidActionSender implements Runnable {
	
	private LogInfo actionToBeSent;

	
	
	public ValidActionSender(LogInfo action){
		this.actionToBeSent=new LogInfo(action.getAction(),action.getSenderUnitID(), action.getSenderX(), action.getSenderY(),
				action.getSenderType(), action.getTargetUnitID(), action.getTargetX(), action.getTargetY(),
				action.getTargetType(), action.getTimestamp(), action.getSenderName());
	}

	@Override
	public void run() {
		for(ServerInfo serverInfo: Server.getServerList().values()){
			if(!serverInfo.isRunsGame()) continue;
			
			 ServerServerMessage sendValidAction = new ServerServerMessage(
									MessageType.NewValidAction,
									Server.getMyInfo().getName(),
									Server.getMyInfo().getIP(),
									serverInfo.getName(),
									serverInfo.getIP());
			 sendValidAction.setActionToBeChecked(this.actionToBeSent);
			 
			//sending the PendingInvalid message to subscribed client
			 ServerServer serverRMI=null;
			 serverRMI = Server.getServerReg(new Node(serverInfo.getName(),serverInfo.getIP()));
			 
			 if(serverRMI == null) return;
			
			 try {
				 serverRMI.onMessageReceived(sendValidAction);
			 } catch (RemoteException | NotBoundException e) {
				//se.printStackTrace();
			 }
			
			 System.out.println("Server: sendValidAction sent to Server"+ serverInfo.getName()+ "serverIP: "+ serverInfo.getIP());
		}
		
	}


}
