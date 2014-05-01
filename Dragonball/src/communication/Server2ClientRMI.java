package communication;

import interfaces.ClientServer;
import interfaces.ServerServer;







import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import messages.ClientServerMessage;
import messages.Message;
import messages.MessageType;
import messages.ServerServerMessage;
import Node.Node;
import Node.Server;
import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import structInfo.UnitType;
import structInfo.LogInfo.Action;
import units.Unit;





public class Server2ClientRMI extends UnicastRemoteObject implements ClientServer { //implements 
																						//Server2Server

	private static final long serialVersionUID = 1L;

	private Server serverOwner; //server instance of this communication
	

	public Server2ClientRMI(Server server) throws RemoteException {
		this.serverOwner=server;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException, NotBoundException {
		//if the message is not a proper ClientServerMessage, it is discarded
		if(!(message instanceof ClientServerMessage)) return;
		
		//issued time of Message in the Server
		message.setTimeIssuedFromServer(System.nanoTime());
		ClientServerMessage clientServerMessage= (ClientServerMessage) message;
		
		System.out.println("Server: Received Message");
		
		switch(message.getMessageTypeRequest()){
		case ClientServerPing : 
			onClientServerPingMessageReceived(clientServerMessage);
			break;
		case Subscribe2Server :
			onSubscribe2ServerMessageReceived(clientServerMessage);
			break;
		case UnSubscribeFromServer :
			onUnSubscribeFromServerMessageReceived(clientServerMessage);
			break;
		case Action : 
			onActionMessageReceived(clientServerMessage);
			break;
		case GetBattlefield :
			try {
				onGetBattlefieldMessageReceived(clientServerMessage);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	


	
	/*---------------------------------------------------
	 * SERVER TO CLIENT METHODS 
	 ----------------------------------------------------		
	*/
	
	//All these messages are being sent from the backend implementation
	//1. OK message for the subscription of the client as player, MasterPlayer will send this
	//2. send Battlefield after a request of the client- it is implemented in onGetBattlefieldMessageReceived
	//3. send Battlefield after an update of the game , server will send this
	//4. the message for the redirection of the client to this server ,server will send this
	
	
	/*---------------------------------------------------
	 * CLIENT TO SERVER METHODS 
	 ----------------------------------------------------		
	*/
	
	private void onClientServerPingMessageReceived(Message message) {
		System.out.println("SC1 "+System.nanoTime()+" onClientServerPingMessageReceived");
		Node client=new Node(message.getSender(),message.getSenderIP());
		ClientPlayerInfo result = Server.getClientList().get(client);
		if(result==null) return; //client is not player in my database
		result.setTimeLastPingSent(System.nanoTime());
		Server.getClientList().replace(client, result);
	}
	
	public void onSubscribe2ServerMessageReceived(Message message){
		
		System.out.println("SC2 "+System.nanoTime()+" Server: onSubscribe2ServerMessageReceived");
		
		Node client=new Node(message.getSender(),message.getSenderIP());
		//checking if client is already subscribed
		if(Server.getClientList().containsKey(client)){
			System.err.println("Client: "+ message.getSender() +" tries to resubscribe");
			return;
		}
		
		
		//if i am not runGame
		//get battlefield from anyone RunGame server
		//copy the battlefield form the other RunGame server
		//sleep until myInfo.runGame is true
		if(!Server.getMyInfo().isRunsGame()){
			System.out.println("Server: NO RunGame() so look for battlefield");
			
			for (ServerInfo serverInfo : Server.getServerList().values()) {
				if (!serverInfo.isRunsGame())
					continue;
				System.out.println("Server "+ serverInfo.getName()+" has the BattleField");
				
				ServerServerMessage requestBattleFieldMessage = new ServerServerMessage(
										MessageType.RequestBattlefield, 
										Server.getMyInfo().getName(), Server.getMyInfo().getIP(),
										serverInfo.getName(), serverInfo.getIP());
				
				// send the subscription message to the server
				ServerServer serverComm = null;
				serverComm = Server.getServerReg(new Node(serverInfo.getName(),
						serverInfo.getIP()));
				
				if(serverComm == null) continue;
				
				System.out.println("Server: "+ Server.getMyInfo().getName() + " sends RequestBattleField to "+ serverInfo.getName());

				try {
					serverComm.onMessageReceived(requestBattleFieldMessage);
				} catch (RemoteException e) {
					//e.printStackTrace();
				} catch (NotBoundException e) {
					//e.printStackTrace();
				}
				
				System.out.println("Server is going to sleep for Server2ServerTimeout");
				try {
					Thread.sleep(2* Constants.SERVER2SERVER_TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(Server.getMyInfo().isRunsGame()) break;
			}

		}
		else{  //current server runs the game and serves the client
			////first makes a load balance to the clients that can be connected to the server  
			if(Server.getMyInfo().getNumClients() >=Constants.MAX_CLIENTS_PER_SERVER){
				Node serverToConnect = null;
				ServerInfo serverInfoMinClients = new ServerInfo("","",-1,true);
				serverInfoMinClients.setNumClients(Constants.MAX_CLIENTS_PER_SERVER);
				for(ServerInfo serverInfo: Server.getServerList().values()){
					if(serverInfo.isRunsGame() && serverInfo.getNumClients() < serverInfoMinClients.getNumClients()){
						serverInfoMinClients = serverInfo;
						serverToConnect = new Node(serverInfoMinClients.getName(),serverInfoMinClients.getIP());
					}
				}
				//only one server and full of clients
				if(serverToConnect==null)
					return;
				
				//server will send a redirection message to client
				
				//With this message, Server sends the Redirection to the client
				 ClientServerMessage sendRedirection = new ClientServerMessage(
														MessageType.RedirectConnection,
														Server.getMyInfo().getName(),
														Server.getMyInfo().getIP(),
														client.getName(),
														client.getIP());
				 
				 sendRedirection.setContent("Name",serverToConnect.getName());
				 sendRedirection.setContent("IP",serverToConnect.getIP());
				 //sending the ACK message to subscribed client
				 ClientServer clientRMI=null;
				 clientRMI = Server.getClientReg(client);
				 
				 if(clientRMI==null) 
					 return;
				
				try {
					clientRMI.onMessageReceived(sendRedirection);
				} catch (RemoteException | NotBoundException e) {
					//e.printStackTrace();
				}
				
				System.out.println("Server: Redirection sent to Client"+ client.getName()+ " to Server "+ serverToConnect.getName());	
				return;
			}	
		}
		
		if(!Server.getMyInfo().isRunsGame()){
			System.out.println("NO-ONE is alive");
			return;
		}
		System.out.println("Someone was alive");
		
		//create new client as Player to Battlefield
		int newUnitID= this.serverOwner.createPlayer();
		if(newUnitID==-1) {
			System.out.println("Server: Cannot create new Player");
			return;
		}
		
		
		
		//broadcast to Servers the New Player Subscription
		for(ServerInfo serverInfo: Server.getServerList().values()){
			if(!serverInfo.isRunsGame()) continue;
			System.err.println(Server.getMyInfo().getName()+": sends New Player");
			
			 ServerServerMessage sendCreatePlayer = new ServerServerMessage(
									MessageType.NewPlayer,
									Server.getMyInfo().getName(),
									Server.getMyInfo().getIP(),
									serverInfo.getName(),
									serverInfo.getIP());
			 
			 Unit newPlayer = Server.getBattlefield().getUnitByUnitID(newUnitID);
			 sendCreatePlayer.setContent("x", String.valueOf(newPlayer.getX()));
			 sendCreatePlayer.setContent("y", String.valueOf(newPlayer.getY()));
			 sendCreatePlayer.setContent("unitID", String.valueOf(newPlayer.getUnitID()));
			 sendCreatePlayer.setContent("serverOwnerID", String.valueOf(newPlayer.getServerOwnerID()));
			 
			//sending the PendingInvalid message to subscribed client
			 ServerServer serverRMI=null;
			 serverRMI = Server.getServerReg(new Node(serverInfo.getName(),serverInfo.getIP()));
			 
			 if(serverRMI == null) return;
			
			 try {
				 serverRMI.onMessageReceived(sendCreatePlayer);
			 } catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			 }
			
			 System.out.println("Server: sendCreatePlayer sent to Server"+ serverInfo.getName()+ "serverIP: "+ serverInfo.getIP());
		}
		
		
		//put new Client to the clientList
		ClientPlayerInfo newClient = new ClientPlayerInfo(client.getName(), client.getIP(),newUnitID);
		//update timestamp info
		newClient.setLastPingFromServer(System.nanoTime());
		this.serverOwner.putToClientList(newClient);
		//update number of clients
		Server.getMyInfo().setNumClients(Server.getClientList().size());

		
			//With this message, Server sends the OK subscription to the client
		 ClientServerMessage sendSubscribed = new ClientServerMessage(
												MessageType.Subscribe2Server,
												this.serverOwner.getName(),
												this.serverOwner.getIP(),
												client.getName(),
												client.getIP());
		 
		 sendSubscribed.setContent("unitID", String.valueOf(newClient.getUnitID()));
		 //sending the ACK message to subscribed client
		 ClientServer clientRMI=null;
		 clientRMI = Server.getClientReg(client);
		 
		 if(clientRMI==null) 
			 return;
		
		try {
			clientRMI.onMessageReceived(sendSubscribed);
		} catch (RemoteException | NotBoundException e) {
			//e.printStackTrace();
		}
		
		System.out.println("Server: ACK sent to Client"+ client.getName()+ "unitID: "+ newClient.getUnitID());
		 
	}
	
	private void onUnSubscribeFromServerMessageReceived(ClientServerMessage message) {
		Node client=new Node(message.getSender(),message.getSenderIP());
		System.out.println("SC3 "+System.nanoTime()+" MESSAGE");
		if(!Server.getClientList().containsKey(client)){
			System.err.println("Unknown Client: "+ message.getSender() +" tries to unsubscribe");
			return;
		}
	}
	
	private void onActionMessageReceived(ClientServerMessage message) {
		System.out.println("SC4 "+System.nanoTime()+" Server: onActionMessageReceived");
		//Node client=new Node(message.getSender(),message.getSenderIP());

		int senderUnitID=Integer.parseInt(message.getContent().get("UnitID"));
		int targetX=Integer.parseInt(message.getContent().get("x"));
		int targetY=Integer.parseInt(message.getContent().get("y"));
		//find sender unit among the units of BattleField
		
		Unit senderUnit = Server.getBattlefield().getUnitByUnitID(senderUnitID);
		if(senderUnit==null){
			System.err.println("Server: Action from not Client, invalid unitID="+ senderUnitID);
			return;
		}
		
		System.out.println("Server: onActionMessageReceived - Checking the targetType");
		// Get what unit lies in the target square
		Unit targetUnit = Server.getBattlefield().getUnit(targetX, targetY);
		UnitType targetType;
		int targetUnitID;
		Action action = null;
		if(targetUnit==null){
			targetType = UnitType.undefined;
			targetUnitID=-1;
		}
		else{
			targetType = targetUnit.getType(targetX, targetY);
			targetUnitID = targetUnit.getUnitID();
		}
		
		switch (targetType) {
			case undefined:
				action = Action.Move;
				break;
			case player:
				action= Action.Heal;
				break;
			case dragon:
				action=Action.Damage;
				break;
		}
		System.out.println("Server: onActionMessageReceived - targetType is "+ action);
		//new pending move
		LogInfo newPendingAction = new LogInfo(action, senderUnitID, senderUnit.getX(),senderUnit.getY(),
												senderUnit.getType(senderUnit.getX(),senderUnit.getY()),
												targetUnitID, 
												targetX, targetY, targetType,
												message.getTimeIssuedFromServer(), Server.getMyInfo().getName());
		System.out.println("New Action : "+ newPendingAction.toString());
		
		//add action as pending move 
		Server.getPendingActions().put(String.valueOf(targetX)+" "+String.valueOf(targetY), newPendingAction);
		
		//broadcast to all servers running the game the new Pending Move
		for(ServerInfo serverInfo: Server.getServerList().values()){
			if(!serverInfo.isRunsGame())
				continue;
			
			ServerServerMessage checkPendingMessage = new ServerServerMessage(
											MessageType.CheckPending, 
											Server.getMyInfo().getName(), Server.getMyInfo().getIP(),
											serverInfo.getName(), serverInfo.getIP());
			
			checkPendingMessage.setActionToBeChecked(newPendingAction);

			// send the subscription message to the server
			ServerServer serverComm = null;
			serverComm = Server.getServerReg(new Node(serverInfo.getName(),
					serverInfo.getIP()));
			
			if(serverComm == null) continue;
			
			System.out.println("Server: "+ Server.getMyInfo().getName() + " sends CheckPending to "+ serverInfo.getName());

			try {
				serverComm.onMessageReceived(checkPendingMessage);
			} catch (RemoteException e) {
				//e.printStackTrace();
			} catch (NotBoundException e) {
				//e.printStackTrace();
			}
		}
		
	}
	

	private void onGetBattlefieldMessageReceived(ClientServerMessage message) throws RemoteException, 
																NotBoundException, MalformedURLException {
		Node client=new Node(message.getSender(),message.getSenderIP());
		ClientPlayerInfo player= Server.getClientList().get((client));
		System.out.println("SC5 "+System.nanoTime()+" MESSAGE");
		if(player==null){
			System.err.println("Unknown Client: "+ message.getSender() +" tries to get Battlefield");
			return;
		}
		ClientServerMessage sendBattleFieldMessage = new ClientServerMessage(
					MessageType.GetBattlefield,
					this.serverOwner.getName(),
					this.serverOwner.getIP(),
					client.getName(),
					client.getIP());
		sendBattleFieldMessage.setBattlefield(Server.getBattlefield());
		//send the BattleField to the client
		ClientServer clientComm= Server.getClientReg(new Node(player.getName(),player.getIP()));
		clientComm.onMessageReceived(sendBattleFieldMessage);	
	}
	
}
