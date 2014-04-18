package Node;

import game.BattleField;
import game.BattleFieldViewer;

import interfaces.ClientServer;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

import structInfo.Constants;
import units.Dragon;
import units.Player;
import units.Unit;
import communication.ClientRMI;
import messages.ClientServerMessage;
import messages.MessageType;

public class Client extends Node{
	
	/*TODO:  //Client and Server have different implementation of the ClientServerInterface
	 * 
	 * 		Messages that client sends
	 * 		1. Client sends Ping Message before ClientTimeout - Y
			2. Un/ Subsribe to a server  - Y
			3. moveMyUnit message - Y
			4. get BattlefieldViewer - Y
			
			Messages received from client/ sent from Server
			1. get BattlefieldViewer - Y
			1.5 get uniqueId created by server - Y
			3. a message saying "send a Ping to Server because ClientTimeout is about to expire - N (client has timer for this)
			4. redirect client to this server(sender of the message is a second server) - Y
	*/
	
	
	private Node serverConnected; //server whom Client is connected
	private boolean isSubscribed; //if client is subscribed to a server
	
	
	Timer serverTimeoutTimer;

	private static final long serialVersionUID = 1L;
	private int unitID; //unique ID returned from Server
	private static  BattleField battlefield;


	public Client(){
		super();
	}
	
	

	/**
	 * @param args
	 * @throws AlreadyBoundException 
	 * @throws RemoteException 
	 * @throws AccessException 
	 * @throws NotBoundException 
	 * @throws MalformedURLException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) {
		
		battlefield = BattleField.getBattleField();
		
		Thread clientThread = new Thread(new Runnable(){

			@Override
			public void run() {
				Client client = new Client();
				client.setName("danteClient");
				
		 		//battlefield = BattleField.getBattleField();
				new BattleFieldViewer(battlefield);
				System.out.println("Client: BattleField size after going to Viewer");
				Client.getBattleField().printUnitSize();
				
				InetAddress clientIP = null;
				try {
					clientIP = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				client.setIP(clientIP.getHostAddress());
				ClientRMI commClient = null;
				try {
					commClient = new ClientRMI(client);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				//client.createClientReg(client, commClient);
				
				if(!Client.createRegistryAndBind(client, commClient))
						bindInExistingRegistry(client, commClient);
				
				//server instance for logging the server for connection
				Node server=new Server();
				server.setName("dante");
				
				InetAddress IP = null;
				try {
					IP = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				server.setIP(IP.getHostAddress());
				System.out.println(server.getIP());
				
				//setup connection to server registry
				ClientServer serverComm;
				serverComm = client.getServerReg(server);

				 //mentions to which server is connected
				client.setServerConnected(server); 
				
				//TODO: constants server names
				//TODO: build message

				
				//TODO: while loop for making moves
				
				ClientServerMessage subscribeMessage= new ClientServerMessage(
												MessageType.Subscribe2Server,
												client.getName(),
												client.getIP(),
												server.getName(),
												server.getIP());
				
				try {
					serverComm.onMessageReceived(subscribeMessage);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				/*
				Player unitTest= new Player(1,1);
				message.setMessageUnit(unitTest);
				
				try {
					serverComm.onMessageReceived(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
				*/
				

				
				System.out.println("Client is waiting");
				while(true){
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		});
		
		clientThread.start();

	}
	
	
	
	public static boolean bindInExistingRegistry(Node node, ClientRMI comm)
	{
		System.out.println("bindInExistingRegistry");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(Constants.RMI_PORT);
			myRegistry.bind(node.getName(), comm); // bind with their names
			System.out.println("bindInExistingRegistry completed");
			return true;
		} catch (RemoteException e) {
			return false;
		} catch (AlreadyBoundException e) {
			return false;
		}
	}
	
	public static boolean createRegistryAndBind(Node node, ClientRMI comm)
	{
		System.out.println("createRegistryAndBind");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
			myRegistry.rebind(node.getName(), comm); // server's name
			System.out.println("createRegistryAndBind completed");
			return true;
		} catch (RemoteException e) {
			System.out.println("createRegistryAndBind failed");
			//e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	/*---------------------------------------------------
	 * ESTABLISH CLIENT RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	//create client Registry
	public void createClientReg(Node node, ClientRMI comm){  //server creates its Registry entry
		
		Registry clientRegistry = null;
		try {
			clientRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			clientRegistry.bind(node.getName(), comm );
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} //server's name
		
		System.out.println(node.getName()+ " is up and running!");
	}
	
	
	//contact with server
	public ClientServer getServerReg(Node server)
	{
		ClientServer serverCommunication = null;
		try {
			serverCommunication = (ClientServer) 
			Naming.lookup("rmi://"+server.getIP()
					+"/"+server.getName());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}		//getClientInfo returns from ClientList
																//clientIp and clientName
		System.out.println("Getting Registry from  "+server.getName());
		//TODO: if connection error appears (throws exception), then connect to other server 
		
		return serverCommunication;
	}
	

	/* ----------------------------------------------------		
	 * CLIENT TO SERVER METHODS
	 ----------------------------------------------------		
	*/
	
	//refresh the subscription time
	public void sendClientServerPing() throws MalformedURLException{
		ClientServerMessage pingMessage = new ClientServerMessage(
											MessageType.ClientServerPing,
											this.getName(),
											this.getIP(),
											this.serverConnected.getName(),
											this.serverConnected.getIP());
		//send the subscription message to the server
		ClientServer serverComm = null;
		serverComm = this.getServerReg(this.serverConnected);

		try {
			serverComm.onMessageReceived(pingMessage);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{
		ClientServerMessage subscribeMessage = new ClientServerMessage(
												MessageType.Subscribe2Server,
												this.getName(),
												this.getIP(),
												this.serverConnected.getName(),
												this.serverConnected.getIP());
		
		//send the subscription message to the server
		ClientServer serverComm= this.getServerReg(this.serverConnected);
		serverComm.onMessageReceived(subscribeMessage);
	}
	
	public void unSubscribeServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{
		
		ClientServerMessage unSubscribeMessage = new ClientServerMessage(
												MessageType.UnSubscribeFromServer,
												this.getName(),
												this.getIP(),
												this.serverConnected.getName(),
												this.serverConnected.getIP());
		unSubscribeMessage.setContent("unitID", String.valueOf(this.getUnitID()));
		//client unsubscribes himself
		this.isSubscribed=false;
		//send the subscription message to the server
		ClientServer serverComm= this.getServerReg(this.serverConnected);
		serverComm.onMessageReceived(unSubscribeMessage);
		
	}
	
	// MOVE UNIT
	
	public void moveUnit(Unit unit) throws MalformedURLException, RemoteException, NotBoundException
	{
		ClientServerMessage moveUnitMessage = new ClientServerMessage(
												MessageType.Action,
												this.getName(),
												this.getIP(),
												this.serverConnected.getName(),
												this.serverConnected.getIP());
		moveUnitMessage.setMessageUnit(unit);
		
		//send the subscription message to the server
		ClientServer serverComm= this.getServerReg(this.serverConnected);
		serverComm.onMessageReceived(moveUnitMessage);
	}
	
	
	//REQUEST BATTLEFIELD
	public void getBattlefieldFromServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{	
		ClientServerMessage getBattlefieldMessage = new ClientServerMessage(
													MessageType.GetBattlefield,
													this.getName(),
													this.getIP(),
													this.serverConnected.getName(),
													this.serverConnected.getIP());

		getBattlefieldMessage.setContent("unitID", String.valueOf(this.getUnitID()));
		
		//send the subscription message to the server
		ClientServer serverComm= this.getServerReg(this.serverConnected);
		serverComm.onMessageReceived(getBattlefieldMessage);	
	}
	
	
	/*---------------------------------------------------
	 * SERVER TO CLIENT METHODS 
	 ----------------------------------------------------		
	*/
	
	//after the Ack of the subscribe message, the server sends the updates of the battlefield
	public void onSubscribeMessageReceived(ClientServerMessage message){
		
		if(message.getSender().equals(this.serverConnected.getName())){
			System.out.println("Connection Established from the Server - Subscription Completed");
			//content collection of the message contains the unique clientID
			this.setUnitID(Integer.parseInt(message.getContent().get("unitID")));
			System.out.println("Client: Received unitID "+ this.getUnitID());
			this.isSubscribed=true;
			
			//set timers, sets a timer for sending the Ping
			serverTimeoutTimer = new Timer(true);
			serverTimeoutTimer.scheduleAtFixedRate(new SchedulingTimer(),0,30000); 
		}	
	}
	
	public void onBattleFieldMessageReceived(ClientServerMessage message){
		if(message.getSender().equals(this.serverConnected.getName())){
			System.out.println("BattleFiled updated from the Server");
			//update the battlefield of the subscribed client
			this.recomputeBattleField(message.getBattlefield());
		}
	}
	
	public void onRedirectServerMessageReceived(ClientServerMessage message) throws MalformedURLException, 
																			RemoteException, NotBoundException{
		//TODO: checks a server list if the sender is on that list ?
		//server info are from the list of the client
		if(!this.isSubscribed){
			//get the serverInfo from the message 
			//TODO: retrieve info from the server list, NOT from message
			this.serverConnected.setName(message.getSender());
			this.serverConnected.setIP(message.getContent().get("IP"));
			//send subscribe message
			subscribeServer(this.serverConnected);
		}
	}
	
	
	/*----------------------------------------------------
				PRIVATE CLIENT COMMUNICATION METHODS
	----------------------------------------------------		
	 	*/
	
	private class SchedulingTimer extends TimerTask{
		@Override
		public void run() {
			try {
				sendClientServerPing();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	private void recomputeBattleField(BattleField messageBattleField){
		battlefield.copyListUnits(messageBattleField.getUnits());
		battlefield.copyMap(messageBattleField.getMap());

	}
	
	/*----------------------------------------------------
					GETTERS AND SETTERS
	----------------------------------------------------		
	 */

	public int getUnitID() {
		return this.unitID;
		
	}
	
	public void setUnitID(int unitID) {
		this.unitID=unitID;
		
	}
	
	public static BattleField getBattleField() {
		return Client.battlefield;
	}
	

	public static void setBattleField(BattleField battlefield) {
		Client.battlefield=battlefield;
	}
	
	public Node getServerConnected() {
		return serverConnected;
	}

	public void setServerConnected(Node serverConnected) {
		this.serverConnected = serverConnected;
	}







}
