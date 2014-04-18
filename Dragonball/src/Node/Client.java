package Node;

import game.BattleField;

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

import com.node.Communication;
import com.talkinterf.Node;

import structInfo.Constants;
import units.Player;
import units.Unit;
import communication.ClientCommunication;
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
	private int clientID; //unique ID returned from Server
	private BattleField battlefield;


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
		
		
		Thread clientThread = new Thread(new Runnable(){
			Client client = new Client();

			@Override
			public void run() {
				client.setName("dante");
				ClientCommunication commClient = null;
				try {
					commClient = new ClientCommunication(client);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				client.createClientReg(client, commClient);
				
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
				
				ClientServer serverComm;
				serverComm = client.getServerReg(server);

				client.setServerConnected(server);  //mentions to which server is connected
				
				//TODO: constants server names
				//TODO: build message

				
				//TODO: while loop for making moves
				
				ClientServerMessage message= new messages.ClientServerMessage();
				
				Player unitTest= new Player(1,1);
				message.setMessageUnit(unitTest);
				
				try {
					serverComm.onMessageReceived(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
				
				
			}

		});
		
		clientThread.start();

	}
	
	
	
	public static boolean bindInExistingRegistry(Node node, Communication comm)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(Constants.RMI_PORT);
			myRegistry.bind(node.getName(), comm); // bind with their names
			return true;
		} catch (RemoteException e) {
			return false;
		} catch (AlreadyBoundException e) {
			return false;
		}
	}
	
	public static boolean createRegistryAndBind(Node node, Communication comm)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
			myRegistry.rebind(node.getName(), comm); // server's name
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	/*---------------------------------------------------
	 * ESTABLISH CLIENT RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	//create client Registry
	public void createClientReg(Node node, ClientCommunication comm){  //server creates its Registry entry
		
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
		System.out.println("Connecting to "+server.getName());
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
											this.serverConnected.getName());
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
												this.serverConnected.getName());
		
		//send the subscription message to the server
		ClientServer serverComm= this.getServerReg(this.serverConnected);
		serverComm.onMessageReceived(subscribeMessage);
	}
	
	public void unSubscribeServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{
		
		ClientServerMessage unSubscribeMessage = new ClientServerMessage(
												MessageType.UnSubscribeFromServer,
												this.getName(),
												this.getIP(),
												this.serverConnected.getName());
		unSubscribeMessage.setContent("clientID", String.valueOf(this.getClientID()));
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
												this.serverConnected.getName());
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
													this.serverConnected.getName());

		getBattlefieldMessage.setContent("clientID", String.valueOf(this.getClientID()));
		
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
			this.setClientID(Integer.parseInt(message.getContent().get("clientID")));
			this.isSubscribed=true;
			//set timers, sets a timer for sending the Ping
			serverTimeoutTimer = new Timer(true);
			serverTimeoutTimer.scheduleAtFixedRate(new SchedulingTimer(),0,1000); 
		}	
	}
	
	public void onBattleFieldMessageReceived(ClientServerMessage message){
		if(message.getSender().equals(this.serverConnected.getName())){
			System.out.println("BattleFiled updated from the Server");
			//update the battlefield of the subscribed client
			//TODO: observable??
			this.setBattleField(message.getBattlefield());
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
	
	/*----------------------------------------------------
					GETTERS AND SETTERS
	----------------------------------------------------		
	 */

	public int getClientID() {
		return this.clientID;
		
	}
	
	public void setClientID(int clientID) {
		this.clientID=clientID;
		
	}
	
	public BattleField getBattleField() {
		return this.battlefield;
	}
	

	public void setBattleField(BattleField battlefield) {
		this.battlefield=battlefield;
	}
	
	public Node getServerConnected() {
		return serverConnected;
	}

	public void setServerConnected(Node serverConnected) {
		this.serverConnected = serverConnected;
	}







}
