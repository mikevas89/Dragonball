package communication;

import interfaces.ServerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import structInfo.ServerInfo;
import structInfo.UnitType;

import messages.Message;
import messages.MessageType;
import messages.ServerServerMessage;
import Node.Node;
import Node.Server;

public class Server2ServerRMI extends UnicastRemoteObject implements ServerServer { // implements
	// Server2Server

	private static final long serialVersionUID = 1L;

	private Server serverOwner; // server instance of this communication
	

	public Server2ServerRMI(Server server) throws RemoteException {
		this.setServerOwner(server);
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException,
			NotBoundException {
		// if the message is not a proper ClientServerMessage, it is discarded
		if (!(message instanceof ServerServerMessage) || !message.getReceiver().equals(this.serverOwner.getName()))
			return;

		// issued time of Message in the Server;
		message.setTimeIssuedFromServer(System.currentTimeMillis());
		ServerServerMessage serverServerMessage = (ServerServerMessage) message;

		System.out.println("Server: Received Message from "+ message.getSender()+ " to "+ message.getReceiver()
									+" Time: "+ message.getTimeIssuedFromServer());
		// TODO: spawn a thread to handle the message

		switch (message.getMessageTypeRequest()) {
		case ServerServerPing:
			onServerServerPingMessageReceived(serverServerMessage);
			break;
		case CheckPending:
			onCheckPendingMessageReceived(serverServerMessage);
			break;
		case PendingMoveInvalid:
			onPendingMoveInvalidMessageReceived(serverServerMessage);
			break;
		case SendValidAction:
			onSendValidActionMessageReceived(serverServerMessage);
			break;
		case Subscribe2Server:
			onServerSubscribe2ServerMessageReceived(serverServerMessage);
			break;
		case ServerSubscribedAck://same method with Subscribe2Server but not Ack back 
			onServerSubscribe2ServerMessageReceived(serverServerMessage);
			break;
		case RequestBattlefield:
			onRequestBattlefieldMessageReceived(serverServerMessage);
		case GetBattlefield:
			onGetBattlefieldMessageReceived(serverServerMessage);
			break;
		default:
			break;
		}
	}
	

	

	private void onPendingMoveInvalidMessageReceived(ServerServerMessage message) {
		System.out.println("onPendingMoveInvalidMessageReceived");
		Map<String, LogInfo> pendingLog = this.serverOwner.getPendingActions();
		//iterate and remove the pending invalid move
		for(Iterator<Entry<String, LogInfo>> it= pendingLog.entrySet().iterator();it.hasNext();){
			Map.Entry<String, LogInfo> entry = it.next();
			
			if(message.getActionToBeChecked().equals(entry.getValue())){
				it.remove();
				break;
			}
			
		}
		
	}

	private void onRequestBattlefieldMessageReceived(ServerServerMessage message) {
		System.out.println("onRequestBattlefieldMessageReceived");
		Node serverSender=new Node(message.getSender(),message.getSender());
		//ServerInfo serverInfo= Server.getServerList().get((serverSender));
		ServerInfo serverInfo = null;
		for(ServerInfo resultInfo : Server.getServerList().values()){
//		ServerInfo serverInfo = Server.getServerList().get(serverSender);
			//System.out.println("ServerInfo Entry :"+ serverInfo.getName()+" "+serverInfo.getIP());
			if(resultInfo.getName().equals(serverSender.getName()) && resultInfo.getIP().equals(serverSender.getIP()))
				serverInfo = resultInfo;
			if(resultInfo==null)
				System.err.println("resultInfo is NULL");
		}
		if(serverInfo==null){
			System.err.println("Unknown Server: "+ message.getSender() +" tries to request the Battlefield");
			return;
		}
		ServerServerMessage sendBattleFieldMessage = new ServerServerMessage(
					MessageType.GetBattlefield,
					this.serverOwner.getName(),
					this.serverOwner.getIP(),
					serverSender.getName(),
					serverSender.getIP());
		sendBattleFieldMessage.setBattlefield(Server.getBattlefield());
		if(sendBattleFieldMessage.getBattlefield()==null)
			System.err.println("Battlefield on the Message is null");
		
		//send the BattleField to the server
		ServerServer serverComm= Server.getServerReg(new Node(serverInfo.getName(),serverInfo.getIP()));
		try {
			serverComm.onMessageReceived(sendBattleFieldMessage);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}	
		
	}

	private void onGetBattlefieldMessageReceived(ServerServerMessage message) {
		System.out.println("onGetBattlefieldMessageReceived");
		
		System.out.println("BattleFieled updated from the Server "+ message.getSender()+" to Server "+ message.getReceiver());
		//update the battlefield of the subscribed client
		Server.getBattlefield().copyBattleField(message.getBattlefield());
		Server.getMyInfo().setRunsGame(true);

	}

	private void onServerSubscribe2ServerMessageReceived(ServerServerMessage message) throws RemoteException {
		
		System.out.println("Server: onSubscribe2ServerMessageReceived from "+ message.getSender()+ " "+ message.getSender());
		Node serverSender=new Node(message.getSender(),message.getSender());
	//	ServerInfo serverInfo = null;
	//	for(ServerInfo resultInfo : Server.getServerList().values()){
			ServerInfo serverInfo = Server.getServerList().get(serverSender);
			//System.out.println("ServerInfo Entry :"+ serverInfo.getName()+" "+serverInfo.getIP());
	//		if(resultInfo.getName().equals(serverSender.getName()) && resultInfo.getIP().equals(serverSender.getIP()))
	//			serverInfo = resultInfo;
	//		if(resultInfo==null)
	//			System.err.println("resultInfo is NULL");
	//	}
		//Server.printlist();
		//ServerInfo serverInfo = Server.getServerList().get(serverSender);
		if(serverInfo==null)
			System.err.println("serverInfo is NULL");
		//new timestamp
		serverInfo.setTimeLastPingSent(message.getTimeIssuedFromServer());
		serverInfo.setAlive(true);
		serverInfo.setRunsGame(message.isSenderRunsGame());
		serverInfo.setNumClients(message.getNumClients());
		//Server.getServerList().remove(serverSender);
		//update the serverList
		Server.getServerList().put(serverSender, serverInfo);
		Server.printlist();
		
		System.err.println("ServerSubscribedAck has to be sent");
		
		if(message.getMessageTypeRequest().equals(MessageType.ServerSubscribedAck))
			return;
		
		//With this message, Server sends the OK subscription to the Server
		 ServerServerMessage sendSubscribed = new ServerServerMessage(
												MessageType.ServerSubscribedAck,
												this.serverOwner.getName(),
												this.serverOwner.getIP(),
												serverSender.getName(),
												serverSender.getIP());
		 
		 System.err.println("ServerSubscribedAck is sent");
		 
		 sendSubscribed.setContent("serverID", String.valueOf(Server.getMyServerID()));
		 //send my number of clients
		 sendSubscribed.setNumClients(Server.getMyInfo().getNumClients());
		 //send if server runs the game
		 sendSubscribed.setSenderRunsGame(Server.getMyInfo().isRunsGame());
		 //sending the ACK message to subscribed client
		 ServerServer serverRMI=null;
		 serverRMI = Server.getServerReg(serverSender);
		
		 try {
			 serverRMI.onMessageReceived(sendSubscribed);
		 } catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		 }
		
		 System.out.println("Server: ACK sent to Server"+ serverSender.getName()+ "serverID: "+ serverInfo.getServerID());
		
	}

	private void onSendValidActionMessageReceived(
			ServerServerMessage serverServerMessage) {
	}

	private void onCheckPendingMessageReceived(ServerServerMessage message) {
		
		boolean hasToReplyPendingInvalid=false;
		
		LogInfo messagePendingMove = message.getActionToBeChecked();
		Map<String, LogInfo> pendingLog = this.serverOwner.getPendingActions();
		
		for(Iterator<Entry<String, LogInfo>> it= pendingLog.entrySet().iterator();it.hasNext();){
			Map.Entry<String, LogInfo> entry = it.next();
			//check contradictions according to game rules
			if((entry.getValue().getTargetUnitID()==messagePendingMove.getSenderUnitID() 
										&& messagePendingMove.getAction().equals(Action.Move)) ||
				(messagePendingMove.getTargetUnitID()==entry.getValue().getSenderUnitID() 
										&& entry.getValue().getAction().equals(Action.Move))){
				
				if(messagePendingMove.getTimestamp()<= entry.getValue().getTimestamp())
					it.remove();
				else
					hasToReplyPendingInvalid=true;
			}
			
			if(entry.getValue().getTargetX() == messagePendingMove.getTargetX() 	&&
					entry.getValue().getTargetY() == messagePendingMove.getTargetY() &&
					messagePendingMove.getTargetType().equals(UnitType.undefined)){
				
				if(messagePendingMove.getTimestamp()<= entry.getValue().getTimestamp())
					it.remove();
				else
					hasToReplyPendingInvalid=true;	
			}	
		}
		
		if(!hasToReplyPendingInvalid) return;
		
		//With this message, Server sends the OK subscription to the Server
		 ServerServerMessage sendPendingMoveInvalid = new ServerServerMessage(
												MessageType.PendingMoveInvalid,
												this.serverOwner.getName(),
												this.serverOwner.getIP(),
												message.getSender(),
												message.getSender());
		 sendPendingMoveInvalid.setActionToBeChecked(message.getActionToBeChecked());
		 
		 //sending the PendingInvalid message to subscribed client
		 ServerServer serverRMI=null;
		 serverRMI = Server.getServerReg(new Node(message.getSender(),message.getSender()));
		 
		 if(serverRMI == null) return;
		
		 try {
			 serverRMI.onMessageReceived(sendPendingMoveInvalid);
		 } catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		 }
		
		 System.out.println("Server: PendingMoveInvalid sent to Server"+ message.getSender()+ "serverIP: "+ message.getSender());
	}

	private void onServerServerPingMessageReceived(ServerServerMessage message) {
		System.out.println("onServerServerPingMessageReceived");
		Node server = new Node(message.getSender(), message.getSender());
		ServerInfo result = Server.getServerList().get(server);
		if (result == null)
			return; // client is not player in my database
		result.setTimeLastPingSent(System.currentTimeMillis());
		Server.getServerList().remove(server);
		Server.getServerList().put(server, result);
		

	}

	public Server getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}

}
