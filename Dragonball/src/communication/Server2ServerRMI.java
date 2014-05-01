package communication;

import interfaces.ServerServer;








import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;









import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import structInfo.Constants;
import structInfo.ServerInfo;
import structInfo.UnitType;
import units.Player;
import units.Unit;

import messages.Message;
import messages.MessageType;
import messages.ServerServerMessage;
import Node.CheckPointMessageSender;
import Node.Node;
import Node.PingMonitorSender;
import Node.Server;

public class Server2ServerRMI extends UnicastRemoteObject implements ServerServer { // implements
	// Server2Server

	private static final long serialVersionUID = 1L;

	private Server serverOwner; // server instance of this communication
	

	public Server2ServerRMI(Server server) throws RemoteException {
		this.setServerOwner(server);
	}

	@Override
	public synchronized void onMessageReceived(Message message) {
		// if the message is not a proper ClientServerMessage, it is discarded
		if (!(message instanceof ServerServerMessage)
				|| !message.getReceiver().equals(this.serverOwner.getName()))
			return;

		// issued time of Message in the Server;
		message.setTimeIssuedFromServer(System.nanoTime());
		ServerServerMessage serverServerMessage = (ServerServerMessage) message;

		System.out.println("Server: Received Message from "
				+ message.getSender() + " to " + message.getReceiver()
				+ " Time: " + message.getTimeIssuedFromServer());

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
		case NewPlayer:
			onNewPlayerMessageReceived(serverServerMessage);
			break;
		case NewValidAction:
			onSendValidActionMessageReceived(serverServerMessage);
			break;
		case NewCheckPoint:
			onNewCheckPointMessageReceived(serverServerMessage);
			break;
		case ProblematicServer:
			onProblematicServerMessageReceived(serverServerMessage);
			break;
		case ResponseProblematicServer:
			onResponseProblematicServerMessageReceived(serverServerMessage);
			break;
		case Subscribe2Server:
			onServerSubscribe2ServerMessageReceived(serverServerMessage);
			break;
		case ServerSubscribedAck:
			onServerSubscribe2ServerMessageReceived(serverServerMessage);
			break;
		case RequestBattlefield:
			onRequestBattlefieldMessageReceived(serverServerMessage);
			break;
		case GetBattlefield:
			onGetBattlefieldMessageReceived(serverServerMessage);
			break;
		default:
			break;
		}

	}

	




	private void onPendingMoveInvalidMessageReceived(ServerServerMessage message) {
		System.out.println("SS3 "+System.nanoTime()+" onPendingMoveInvalidMessageReceived");
		Map<String, LogInfo> pendingLog = Server.getPendingActions();
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
		System.out.println("SS8 "+System.nanoTime()+" onRequestBattlefieldMessageReceived");
		Node serverSender=new Node(message.getSender(),message.getSenderIP());
		ServerInfo serverInfo= Server.getServerList().get(serverSender);

		if(serverInfo==null){
			System.err.println("Unknown Server: "+ message.getSender() +" requests the Battlefield");
			return;
		}
		ServerServerMessage sendBattleFieldMessage = new ServerServerMessage(
					MessageType.GetBattlefield,
					this.serverOwner.getName(),
					this.serverOwner.getIP(),
					serverSender.getName(),
					serverSender.getIP());
		sendBattleFieldMessage.setBattlefield(Server.getBattlefield());
		
		//send the BattleField to the server
		ServerServer serverComm= Server.getServerReg(new Node(serverInfo.getName(),serverInfo.getIP()));
		try {
			serverComm.onMessageReceived(sendBattleFieldMessage);
		} catch (RemoteException e) {
			//e.printStackTrace();
		} catch (NotBoundException e) {
			//e.printStackTrace();
		}	
		
	}

	private void onGetBattlefieldMessageReceived(ServerServerMessage message) {
		System.out.println("SS9 "+System.nanoTime()+" onGetBattlefieldMessageReceived");
		
		System.out.println("BattleFieled updated from the Server "+ message.getSender()+" to Server "+ message.getReceiver());
		//update the battlefield of the subscribed client
		Server.getBattlefield().copyBattleField(message.getBattlefield());
		Server.getMyInfo().setRunsGame(true);

	}

	private void onServerSubscribe2ServerMessageReceived(ServerServerMessage message){
		
		System.out.println("Server: onSubscribe2ServerMessageReceived from "+ message.getSender()+ " "+ message.getSenderIP());
		Node serverSender=new Node(message.getSender(),message.getSenderIP());

		ServerInfo serverInfo = Server.getServerList().get(serverSender);

		if(serverInfo==null) {
			System.err.println("Server that requests Subscription is not listed on Server List");
			return;
		}
		//new timestamp
		serverInfo.setRemoteNodeTimeLastPingSent(message.getTimeIssuedFromServer());
		serverInfo.setAlive(true);
		serverInfo.setRunsGame(message.isSenderRunsGame());
		serverInfo.setNumClients(message.getNumClients());
		//Server.getServerList().remove(serverSender);
		//update the serverList
		Server.getServerList().put(serverSender, serverInfo);
		Server.printlist();
		
		
		if(message.getMessageTypeRequest().equals(MessageType.ServerSubscribedAck))
			return;
		
		System.out.println("ServerSubscribedAck has to be sent");
		
		//With this message, Server sends the OK subscription to the Server
		 ServerServerMessage sendSubscribed = new ServerServerMessage(
												MessageType.ServerSubscribedAck,
												this.serverOwner.getName(),
												this.serverOwner.getIP(),
												serverSender.getName(),
												serverSender.getIP());
		 
		 System.out.println("ServerSubscribedAck is sent");
		 
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
		
		 System.out.println("SS7 "+System.nanoTime()+" Server: ACK sent to Server"+ serverSender.getName()+ "serverID: "+ serverInfo.getServerID());
		
	}
	

	private void onNewPlayerMessageReceived(ServerServerMessage message) {
		System.out.println("onNewPlayerMessageReceived");
		int playerX = Integer.valueOf(message.getContent().get("x"));
		int playerY = Integer.valueOf(message.getContent().get("y"));
		int playerUnitID = Integer.valueOf(message.getContent().get("unitID"));
		int playerServerOwnerID = Integer.valueOf(message.getContent().get("serverOwnerID"));
		//create player to battlefield from other Server
		Player newPlayer = new Player(playerX, playerY,Server.getBattlefield(), playerUnitID,playerServerOwnerID);
	}
	
	
	

	private void onSendValidActionMessageReceived(ServerServerMessage message) {
		long currentTime = System.nanoTime();
		int totalNumClients = Server.getMyInfo().getNumClients();
		for(ServerInfo serverInfo: Server.getServerList().values()){
			totalNumClients+=serverInfo.getNumClients();
		}
		System.out.println("SS4 "+currentTime+" "+(currentTime - message.getActionToBeChecked().getTimestamp())+" "+totalNumClients );
		LogInfo newRemoteAction = message.getActionToBeChecked();
		
		
		// Removed Action was received
		synchronized (Server.lock) {
			if (newRemoteAction.getAction().equals(Action.Removed)) {
				ListIterator<Unit> it = Server.getBattlefield().getUnits()
						.listIterator();
				while (it.hasNext()) {
					Unit unit = it.next();
					if (unit.getUnitID() == newRemoteAction.getSenderUnitID()) {
						Server.getBattlefield().removeUnit(unit.getX(),
								unit.getY(), it);
						//put action to the local log file
						Server.getValidActions().add(newRemoteAction);
						return;
					}
				}
			}
		}
		
		 //after this, the action is valid and it WILL BE played (targetUnitID may also be -1)
		Unit existingTargetUnit=Server.getBattlefield().getUnit(newRemoteAction.getTargetX(), newRemoteAction.getTargetY());
	    int existingTargetUnitID;
	    if(existingTargetUnit==null)
	    	existingTargetUnitID=-1;
	    else
	    	existingTargetUnitID=existingTargetUnit.getUnitID();
	    
	    if(Server.getBattlefield().
	    		getUnit(newRemoteAction.getSenderX(), newRemoteAction.getSenderY()).getUnitID()!=newRemoteAction.getSenderUnitID() ||
	    		existingTargetUnitID!=newRemoteAction.getTargetUnitID()){
				    	System.err.println("onSendValidAction found an inconsistency from the Remote Action");
						//clear the pending/validBlockQueue
						Server.getPendingActions().clear();
						Server.getValidBlockQueue().clear();
						//copy checkpoint state to running battlefield
						Server.getBattlefield().copyMap(Server.getCheckPoint().getMap());
						Server.getBattlefield().copyListUnits(Server.getCheckPoint().getUnits());
						Server.copyValidActions(Server.getCheckPoint().getCheckPointValidActions());
						//broadcast the checkpoint battlefield to all
						Runnable checkPointMessage = new CheckPointMessageSender();
						new Thread(checkPointMessage).start();
						return;
	    }
	    
	    if(newRemoteAction.getSenderType().equals(UnitType.dragon)){
	    	Unit dragonUnit = Server.getBattlefield().getUnit(newRemoteAction.getSenderX(),newRemoteAction.getSenderY());
	    	Unit targetUnit = Server.getBattlefield().getUnit(newRemoteAction.getTargetX(),newRemoteAction.getTargetY());
	    	Server.getBattlefield().dealDamage(targetUnit.getX(), targetUnit.getY(), dragonUnit.getAttackPoints());
	    	//check if the targetUnit is mine and perhaps wants removal
	    	Server.checkIfUnitIsDead(newRemoteAction);
	    	Server.getValidActions().add(newRemoteAction);
	    }
	    else {//senderType is Player
	    	Unit senderUnit = Server.getBattlefield().getUnit(newRemoteAction.getSenderX(), newRemoteAction.getSenderY());
	    	
	    	switch (newRemoteAction.getTargetType()) {
			case undefined:
				// There is no unit in the square. Move the player to this
				// square
				Server.getBattlefield().moveUnit(senderUnit,
						newRemoteAction.getTargetX(), newRemoteAction.getTargetY());
				break;
			case player:
				// There is a player in the square, attempt a healing
				Server.getBattlefield().healDamage(newRemoteAction.getTargetX(),
						newRemoteAction.getTargetY(),
						senderUnit.getAttackPoints());
				break;
			case dragon:
				// There is a dragon in the square, attempt a dragon slaying
				Server.getBattlefield().dealDamage(newRemoteAction.getTargetX(),
						newRemoteAction.getTargetY(),
						senderUnit.getAttackPoints());
				//check if this server handles the dragons and remove him if dead
		    	Server.checkIfUnitIsDead(newRemoteAction);
				break;
			}
			// logs the new Valid Action
			Server.getValidActions().add(newRemoteAction);
	    }

	}
	

	private void onNewCheckPointMessageReceived(ServerServerMessage message) {
		System.out.println("onNewCheckPointMessageReceived");
		Server.getPendingActions().clear();
		Server.getValidBlockQueue().clear();
		//update battlefield and validActions due to an inconsistency to another Server
		Server.getBattlefield().copyBattleField(message.getBattlefield());
		Server.copyValidActions(message.getValidActionsLog());
		//update server's checkpoint to the received one
		Server.getCheckPoint().captureCheckPoint(message.getBattlefield(), message.getValidActionsLog());		
	}



	private void onProblematicServerMessageReceived(ServerServerMessage message) {
		System.out.println("onProblematicServerMessageReceived");
		Node problematicServer = message.getProblematicServerToCheck();
		
		ServerInfo serverInfo = Server.getServerList().get(problematicServer);
		if(serverInfo.isProblematicServer())
			return;
		//update the info for that server
		serverInfo.setProblematicServer(true);
		serverInfo.setNodeFoundTheProblematicServer(new Node(message.getSender(),message.getSenderIP()));
		//first vote from the sender
		serverInfo.setNumAnswersAgreeRemovingServer(serverInfo.getNumAnswersAgreeRemovingServer()+1);
		
		Runnable pingMonitorSender=null;
		//current server decides about problematicServer 
		if((System.nanoTime() - serverInfo.getRemoteNodeTimeLastPingSent()) > Constants.SERVER2SERVER_PING_PERIOD*Constants.NANO){
			System.out.println(Server.getMyInfo().getName()+" Agree for "+ serverInfo.getName());
			serverInfo.setNumAnswersAgreeRemovingServer(serverInfo.getNumAnswersAgreeRemovingServer()+1);
			pingMonitorSender=new PingMonitorSender(problematicServer,MessageType.ResponseProblematicServer,
					PingMonitorSender.DecisionType.Agree);
		}
		else{
			System.out.println(Server.getMyInfo().getName()+" DisAgree for "+ serverInfo.getName());
			serverInfo.setNumAnswersNotAgreeRemovingServer(serverInfo.getNumAnswersNotAgreeRemovingServer()+1);
			pingMonitorSender=new PingMonitorSender(problematicServer,MessageType.ResponseProblematicServer,
					PingMonitorSender.DecisionType.Disagree);
		}		
		serverInfo.setTotalNumAnswersAggreement(serverInfo.getNumAnswersAgreeRemovingServer() 
													+ serverInfo.getNumAnswersNotAgreeRemovingServer());
		//update problematic server info
		Server.getServerList().replace(problematicServer, serverInfo);
		//broadcast node's decision
		new Thread(pingMonitorSender).start();
		
		System.out.println("SS5 "+System.nanoTime()+" MESSAGE" );

		if(serverInfo.getTotalNumAnswersAggreement() >= (Server.getNumAliveServers() - Server.getNumProblematicServers())){
			//ready to decide
			this.serverOwner.DecideForRemoval(problematicServer);
		}
			
	}
	

	private void onResponseProblematicServerMessageReceived(ServerServerMessage message) {
		System.out.println("SS6 "+System.nanoTime()+" MESSAGE" );
		System.out.println("onResponseProblematicServerMessageReceived");
		Node problematicServer = message.getProblematicServerToCheck();
		ServerInfo serverInfo = Server.getServerList().get(problematicServer);
		if(!serverInfo.isProblematicServer())
			System.err.println("ResponseProblematicServer : ProblematicServer is not true in ServerList");
		
		switch(message.getContent().get("Decision")){
		case "Agree":
			serverInfo.setNumAnswersAgreeRemovingServer(serverInfo.getNumAnswersAgreeRemovingServer()+1);
			break;
		case "Disagree":
			serverInfo.setNumAnswersNotAgreeRemovingServer(serverInfo.getNumAnswersNotAgreeRemovingServer()+1);
			break;
		}
		serverInfo.setTotalNumAnswersAggreement(serverInfo.getTotalNumAnswersAggreement()+1);
		Server.getServerList().replace(problematicServer, serverInfo);
		if(serverInfo.getTotalNumAnswersAggreement() < Server.getNumAliveServers() - Server.getNumProblematicServers())
					return;
		
		//ready to decide
		this.serverOwner.DecideForRemoval(problematicServer);
		
	}
	
	
	private void onCheckPendingMessageReceived(ServerServerMessage message) {
		System.out.println("onCheckPendingMessageReceived");
		boolean hasToReplyPendingInvalid=false;
		
		LogInfo messagePendingMove = message.getActionToBeChecked();
		Map<String, LogInfo> pendingLog = Server.getPendingActions();
		
		for(Iterator<Entry<String, LogInfo>> it= pendingLog.entrySet().iterator();it.hasNext();){
			Map.Entry<String, LogInfo> entry = it.next();
			
			//check contradictions according to game rules
			if((entry.getValue().getTargetUnitID()==messagePendingMove.getSenderUnitID() 
										&& messagePendingMove.getAction().equals(Action.Move)) ||
				(messagePendingMove.getTargetUnitID()==entry.getValue().getSenderUnitID() 
										&& entry.getValue().getAction().equals(Action.Move))){
				
				if(messagePendingMove.getTimestamp()<= entry.getValue().getTimestamp()){
					System.err.println("Action "+ entry.getValue().toString() 
										+ " is Removed due to the action\n : "+ messagePendingMove.toString());
					it.remove();
				}
				else
					hasToReplyPendingInvalid=true;
			}
			
			if(entry.getValue().getTargetX() == messagePendingMove.getTargetX() 	&&
					entry.getValue().getTargetY() == messagePendingMove.getTargetY() &&
					messagePendingMove.getTargetType().equals(UnitType.undefined)){
				
					if(messagePendingMove.getTimestamp()<= entry.getValue().getTimestamp()){
						System.err.println("Action "+ entry.getValue().toString() 
											+ " is Removed due to the action\n : "+ messagePendingMove.toString());
						it.remove();
					}
				else
					hasToReplyPendingInvalid=true;	
			}	
		}
		
		if(!hasToReplyPendingInvalid) return;
		
		//With this message, Server sends the Invalid Pending MOve that the other Server should remove
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
			//e.printStackTrace();
		 }
		
		 System.out.println("SS2 "+System.nanoTime()+" Server: PendingMoveInvalid sent to Server"+ message.getSender()+ "serverIP: "+ message.getSender());
	}

	private void onServerServerPingMessageReceived(ServerServerMessage message) {
		System.out.println("SS1 "+System.nanoTime()+" onServerServerPingMessageReceived");
		System.out.println("onServerServerPingMessageReceived");
		
		Node server = new Node(message.getSender(), message.getSenderIP());
		ServerInfo serverInfo = Server.getServerList().get(server);
		if (serverInfo == null)
			return; // client is not player in my database
		if(!serverInfo.isAlive())
			return;
		//update server's info
		serverInfo.setNumClients(message.getNumClients());
		serverInfo.setRunsGame(message.isSenderRunsGame());
		serverInfo.setRunsDragons(message.isSenderRunsDragons());
		serverInfo.setRemoteNodeTimeLastPingSent(System.nanoTime());
		Server.getServerList().replace(server, serverInfo);
	}

	public Server getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}

}
