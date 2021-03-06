package Node;

import game.BattleField;
import game.BattleFieldViewer;

import interfaces.ClientServer;
import interfaces.ServerServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import structInfo.Constants;
import structInfo.Directions;
import structInfo.ServerInfo;
import structInfo.UnitType;
import units.*;
import communication.ClientRMI;
import messages.ClientServerMessage;
import messages.MessageType;

public class Client extends Node{

	
	private static Node serverConnected; //server whom Client is connected
	private boolean isSubscribed; //if client is subscribed to a server
	
	
	Timer serverTimeoutTimer;
	public volatile boolean running=true;

	private static final long serialVersionUID = 1L;
	private int unitID; //unique ID returned from Server
	private static  BattleField battlefield;
	
	private static ConcurrentHashMap<Node, ServerInfo> serverList;
	



	public Client() throws IOException{
		super();
		serverList = new ConcurrentHashMap<Node, ServerInfo>();
		
		int numClient = this.getUniqueIdForName("Clientid.txt");
		//unique name of Client
		this.setName("Client"+ String.valueOf(numClient));
		this.setIP("192.168.1.14");
		System.out.println("Client Name: "+ this.getName());
		this.isSubscribed=false;
		
		this.readServers("Servers.txt");
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
				
				Directions direction;
				int targetX = 0, targetY = 0;
				UnitType adjacentUnitType;
				
				
				Client client = null;
				try {
					client = new Client();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				
		 		//battlefield = BattleField.getBattleField();
				//new BattleFieldViewer(battlefield);
				//System.out.println("Client: BattleField size after going to Viewer");
				//Client.getBattleField().printUnitSize();

				
				//create Client's RMI
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
				Node server = null;
				server = new Node();

				 //take the first Server Name from the server list
				//Node firstServer= (Node) Client.getServerList().keySet().toArray()[0];
				//server.setName(Client.getServerList().get(firstServer).getName());
				//server.setIP(Client.getServerList().get(firstServer).getIP());
				//put names for testing
				server.setName("Server2");
				server.setIP("192.168.1.14");
				
				System.out.println(server.getIP());
				
				
				//setup connection to server registry
				ClientServer serverComm;
				serverComm = Client.getServerReg(server);

				 //mentions to which server is connected
				Client.setServerConnected(server); 

				Client.printlist();
				
				ClientServerMessage subscribeMessage= new ClientServerMessage(
												MessageType.Subscribe2Server,
												client.getName(),
												client.getIP(),
												server.getName(),
												server.getIP());
				
				try {
					serverComm.onMessageReceived(subscribeMessage);
				} catch (RemoteException | NotBoundException e) {
					//e.printStackTrace();
				}
				int prevX=0;
				int prevY=0;
				System.out.println("Client is waiting");
				while(client.running){
					try {
						
						while(!client.isSubscribed){
							Thread.sleep(Constants.CLIENT_CHECKING_ISSUBSCRIBED);
						}
					
				//		Client.printlist();
						
						Thread.sleep(Constants.PLAYER_PERIOD_ACTION);
						System.out.println("Connected serverName:"+ Client.getServerConnected().getName());
						ServerInfo serverInfo = Client.getServerList().get(Client.getServerConnected());
						if(serverInfo ==null)
							System.err.println("ServerInfo is null");
							//no response from the connected Server
						if((System.nanoTime() - serverInfo.getRemoteNodeTimeLastPingSent()) > 2* Constants.SERVER2CLIENT_TIMEOUT
																									*Constants.NANO){
							System.err.println("Client Removes Server due to Ping");
							client.serverTimeoutTimer.cancel();
							client.running=false;
							continue;
						}
						
						Unit neighbors[] = new Unit[4];
						Unit myUnit= client.getUnitFromBattleFieldList();
						if (myUnit.getY() > 0)
							neighbors[0]= Client.getBattleField().getUnit(myUnit.getX(), myUnit.getY() - 1);
						if (myUnit.getY() < BattleField.MAP_HEIGHT - 1)
							neighbors[1]= Client.getBattleField().getUnit(myUnit.getX(), myUnit.getY() + 1);
						if (myUnit.getX() > 0)
							neighbors[2]= Client.getBattleField().getUnit(myUnit.getX()-1, myUnit.getY() );
						if (myUnit.getX() < BattleField.MAP_WIDTH - 1)
							neighbors[3]= Client.getBattleField().getUnit(myUnit.getX()+1, myUnit.getY() );

						int foundplayer=0;
						for(Unit temp: neighbors)
						{
							if(temp==null)
								continue;
							if(temp.getType(temp.getX(), temp.getY()).equals(UnitType.player)){
									if(temp.getHitPoints() < temp.getMaxHitPoints()/2){
										targetX=temp.getX();
										targetY=temp.getY();
										foundplayer=1;
										break;
									}
							}
							
						}
						int founddragon=0;
						if(foundplayer!=1){
							for(Unit temp: neighbors)
							{
								if(temp==null)
									continue;
								if(temp.getType(temp.getX(), temp.getY()).equals(UnitType.dragon)){
									targetX=temp.getX();
									targetY=temp.getY();
									founddragon=1;
								//	System.err.println("FIGHTING A DRAGON!!!!!!!");
									break;
								}
								
							}
						}
						
						if(founddragon!=1 && foundplayer!=1){
							// Randomly choose one of the four wind directions to move to if there are no units present
							direction = Directions.values()[ (int)(Directions.values().length * Math.random()) ];
							adjacentUnitType = UnitType.undefined;
							targetX=0;
							targetY=0;
							myUnit= client.getUnitFromBattleFieldList();
							if(myUnit == null && client.running==true)
							{
								System.err.println("Client: Cannot find my player");
								client.running=false;
								continue;
							}
							try{
							switch (direction) {
								case up:
									if (myUnit.getY() <= 0)
										// The player was at the edge of the map, so he can't move north and there are no units there
										continue;
									
									targetX = myUnit.getX();
									targetY = myUnit.getY() - 1;
									break;
								case down:
									if (myUnit.getY() >= BattleField.MAP_HEIGHT - 1)
										// The player was at the edge of the map, so he can't move south and there are no units there
										continue;

									targetX = myUnit.getX();
									targetY = myUnit.getY() + 1;
									break;
								case left:
									if (myUnit.getX() <= 0)
										// The player was at the edge of the map, so he can't move west and there are no units there
										continue;

									targetX = myUnit.getX() - 1;
									targetY = myUnit.getY();
									break;
								case right:
									if (myUnit.getX() >= BattleField.MAP_WIDTH - 1)
										// The player was at the edge of the map, so he can't move east and there are no units there
										continue;

									targetX = myUnit.getX() + 1;
									targetY = myUnit.getY();
									break;
							}
							
							}catch(NullPointerException e){
								client.running=false;
								continue;
							}
							
							
						}
						
						Unit targetUnit=Client.getBattleField().getUnit(targetX, targetY);
						
						//player shouldn't heal one player with full health
						if(targetUnit!=null && targetUnit.getHitPoints()==targetUnit.getMaxHitPoints() && (targetUnit instanceof Player))
							continue;
						
						prevX=targetX;
						prevY=targetY;
						ClientServerMessage actionMessage = new ClientServerMessage(
								MessageType.Action, client.getName(),
								client.getIP(), Client.getServerConnected().getName(), Client.getServerConnected()
										.getIP());
						actionMessage.setContent("UnitID", String.valueOf(client.getUnitID()));
						actionMessage.setContent("x", String.valueOf(targetX));
						actionMessage.setContent("y", String.valueOf(targetY));

						serverComm = Client.getServerReg(Client.getServerConnected());
						
						if(serverComm==null)
							continue;

						try {
							serverComm.onMessageReceived(actionMessage);
						} catch (RemoteException | NotBoundException e) {
							//e.printStackTrace();
						}
						
					
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Cliient: UnSubscribedFromServer");
			}

		});
		
		clientThread.start();

	}
	
	public Unit getUnitFromBattleFieldList()
	{
		for(Unit temp: battlefield.getUnits())
		{
			if(temp.getUnitID()==this.getUnitID())
			{
				return temp;
			}
		}
		return null;
		
		
	}
	
	

	
	
	
	
	/*---------------------------------------------------
	 * ESTABLISH CLIENT RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	public static boolean bindInExistingRegistry(Node node, ClientRMI comm)
	{
		System.out.println("bindInExistingRegistry");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(Constants.SERVER_CLIENT_RMI_PORT);
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
			myRegistry = LocateRegistry.createRegistry(Constants.SERVER_CLIENT_RMI_PORT);
			myRegistry.rebind(node.getName(), comm); // server's name
			System.out.println("createRegistryAndBind completed");
			return true;
		} catch (RemoteException e) {
			System.out.println("createRegistryAndBind failed");
			//e.printStackTrace();
			return false;
		}
	}
	
	
	//contact with server
	public static ClientServer getServerReg(Node server)
	{
		ClientServer serverCommunication = null;
		try {
			serverCommunication = (ClientServer) 
			Naming.lookup("rmi://"+server.getIP()+":"+String.valueOf(Constants.SERVER_CLIENT_RMI_PORT)
					+"/"+server.getName());
		} catch (MalformedURLException e) {
			//e.printStackTrace();
		} catch (RemoteException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ server.getName()+" ServerRMI RemoteException error");
			return null;
		} catch (NotBoundException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ server.getName()+" ServerRMI NotBoundException error");
			return null;
		}		//getServerInfo returns from ServerList
																//serverIp and serverName
		//System.out.println("getServerReg to "+ server.getName());
		return serverCommunication;
	}
	
	
	public int getUniqueIdForName(String fname) throws IOException{
		//getting a unique id for every server
				int numClient = -1;
				String line = null;
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(fname));
					try {
						line = reader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (line == null) {
						numClient = 0;
					} else {
						numClient = Integer.parseInt(line);
					}
				} catch (FileNotFoundException e) {
					numClient = 0;
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}

				BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new FileWriter(fname));
					writer.write(String.valueOf(numClient + 1));
				} finally {
					if (writer != null) {
						writer.flush();
						writer.close();
					}
				}
				return numClient;
	}
	//read Servers list
	public void readServers(String fname){
		
 		File file = new File(fname);
		BufferedReader reader = null;
		int j=0;

		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
		    	String[] parts = text.split(" ");
		    	//if(!this.getName().equals(parts[0]))
		    	Client.putToServerList(new ServerInfo(parts[0], parts[1], ++j,false));
		    }
		    
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		            reader.close();
		        }
		    } catch (IOException e) {
		    }
		}
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
											Client.serverConnected.getName(),
											Client.serverConnected.getIP());
		//send the subscription message to the server
		ClientServer serverComm = null;
		serverComm = Client.getServerReg(Client.serverConnected);
		//System.out.println("Client sends Ping");

		try {
			serverComm.onMessageReceived(pingMessage);
		} catch (RemoteException e) {
			//e.printStackTrace();
		} catch (NotBoundException e) {
			//e.printStackTrace();
		}
	}
	
	public void subscribeServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{
		ClientServerMessage subscribeMessage = new ClientServerMessage(
												MessageType.Subscribe2Server,
												this.getName(),
												this.getIP(),
												Client.serverConnected.getName(),
												Client.serverConnected.getIP());
		
		//send the subscription message to the server
		ClientServer serverComm= Client.getServerReg(Client.serverConnected);
		serverComm.onMessageReceived(subscribeMessage);
	}
	
	public void unSubscribeServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{
		
		ClientServerMessage unSubscribeMessage = new ClientServerMessage(
												MessageType.UnSubscribeFromServer,
												this.getName(),
												this.getIP(),
												Client.serverConnected.getName(),
												Client.serverConnected.getIP());
		unSubscribeMessage.setContent("unitID", String.valueOf(this.getUnitID()));
		//client unsubscribes himself
		this.isSubscribed=false;
		//send the subscription message to the server
		ClientServer serverComm= Client.getServerReg(Client.serverConnected);
		serverComm.onMessageReceived(unSubscribeMessage);
		
	}
	
	// MOVE UNIT
	
	public void moveUnit(Unit unit) throws MalformedURLException, RemoteException, NotBoundException
	{
		ClientServerMessage moveUnitMessage = new ClientServerMessage(
												MessageType.Action,
												this.getName(),
												this.getIP(),
												Client.serverConnected.getName(),
												Client.serverConnected.getIP());
		moveUnitMessage.setMessageUnit(unit);
		
		//send the subscription message to the server
		ClientServer serverComm= Client.getServerReg(Client.serverConnected);
		serverComm.onMessageReceived(moveUnitMessage);
	}
	
	
	//REQUEST BATTLEFIELD
	public void getBattlefieldFromServer(Node Server) throws MalformedURLException, RemoteException, NotBoundException{	
		ClientServerMessage getBattlefieldMessage = new ClientServerMessage(
													MessageType.GetBattlefield,
													this.getName(),
													this.getIP(),
													Client.serverConnected.getName(),
													Client.serverConnected.getIP());

		getBattlefieldMessage.setContent("unitID", String.valueOf(this.getUnitID()));
		
		//send the subscription message to the server
		ClientServer serverComm= Client.getServerReg(Client.serverConnected);
		serverComm.onMessageReceived(getBattlefieldMessage);	
	}
	
	
	/*---------------------------------------------------
	 * SERVER TO CLIENT METHODS 
	 ----------------------------------------------------		
	*/
	
	//after the Ack of the subscribe message, the server sends the updates of the battlefield
	public void onSubscribeMessageReceived(ClientServerMessage message){
		
		if(message.getSender().equals(Client.serverConnected.getName())){
			System.out.println("C1 "+System.nanoTime()+" Connection Established from the Server - Subscription Completed");
			//content collection of the message contains the unique clientID
			this.setUnitID(Integer.parseInt(message.getContent().get("unitID")));
			System.out.println("Client: Received unitID "+ this.getUnitID());
			this.isSubscribed=true;
			
			//set timers, sets a timer for sending the Ping
			serverTimeoutTimer = new Timer(true);
			serverTimeoutTimer.scheduleAtFixedRate(new SchedulingTimer(),0,Constants.CLIENT2SERVER_PING_PERIOD); 
		}	
	}
	
	public void onBattleFieldMessageReceived(ClientServerMessage message){
		if(message.getSender().equals(Client.serverConnected.getName())){
			System.out.println("C2 "+System.nanoTime());
			//System.out.println(" BattleField updated from the Server ");
			//update the battlefield of the subscribed client
			this.recomputeBattleField(message.getBattlefield());
			
			//update ping timestamp from server 
			for(ServerInfo serverInfo: Client.getServerList().values()){
				if(!serverInfo.getName().equals(Client.serverConnected.getName()))
					continue;
				//change the timestamp of the last ping from server
				serverInfo.setRemoteNodeTimeLastPingSent(message.getTimeIssuedFromServer());
				Client.getServerList().replace(new Node(Client.serverConnected.getName(), Client.serverConnected.getIP()),serverInfo);
			}	
		}
	}
	

	public void onServerClientPingMessageReceived(ClientServerMessage message) {
		if(message.getSender().equals(Client.serverConnected.getName())){
			System.out.println("C3 "+System.nanoTime());
			System.out.println(" onServerClientPingMessageReceived");
			//send immediately to Server because the connection is at stake
			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						sendClientServerPing();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
			//update ping timestamp from server 
			for(ServerInfo serverInfo: Client.getServerList().values()){
				if(!serverInfo.getName().equals(Client.serverConnected.getName()))
					continue;
				//change the timestamp of the last ping from server
				serverInfo.setRemoteNodeTimeLastPingSent(message.getTimeIssuedFromServer());
				Client.getServerList().replace(new Node(Client.serverConnected.getName(), Client.serverConnected.getIP()),serverInfo);
			}	
		}
		
	}
	

	public void onUnSubscribeFromServerMessageReceived(ClientServerMessage message) {
		if(message.getSender().equals(Client.serverConnected.getName())){
			System.out.println("C1 "+System.nanoTime()+" Client: onUnSubscribeFromServerMessageReceived");
			//this.recomputeBattleField(message.getBattlefield());
			this.running=false;	
			serverTimeoutTimer.cancel();
		}
		
	}
	
	public void onRedirectServerMessageReceived(ClientServerMessage message){
		if(message.getSender().equals(Client.serverConnected.getName())){
			Client.serverConnected.setName(message.getContent().get("Name"));
			Client.serverConnected.setIP(message.getContent().get("IP"));
			
			//setup connection to server registry
			ClientServer serverComm;
			serverComm = Client.getServerReg(Client.serverConnected);

			ClientServerMessage subscribeMessage= new ClientServerMessage(
											MessageType.Subscribe2Server,
											this.getName(),
											this.getIP(),
											serverConnected.getName(),
											serverConnected.getIP());
			if(serverComm == null)
					return;
			try {
				serverComm.onMessageReceived(subscribeMessage);
			} catch (RemoteException | NotBoundException e) {
				//e.printStackTrace();
			}
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
	
	public static void printlist()
	{	System.out.println("Printing server list:");
		for(ServerInfo serverInfo: Client.getServerList().values()){
			System.out.println(serverInfo.getName()+"   "+serverInfo.getIP());
			System.out.println("ID: "+ serverInfo.getServerID()+" Alive:"+ serverInfo.isAlive()+
									" ProblematicServer:"+ serverInfo.isProblematicServer()+
									" TimeStamp:"+ serverInfo.getRemoteNodeTimeLastPingSent()+
									" Total:"+ serverInfo.getTotalNumAnswersAggreement()+
									" Agree:"+ serverInfo.getNumAnswersAgreeRemovingServer()+
									" Disagree:"+ serverInfo.getNumAnswersNotAgreeRemovingServer());
		}
		System.out.println("Leaving from server list:");
	}
	
	
	/*----------------------------------------------------
					GETTERS AND SETTERS
	----------------------------------------------------		
	 */
	
	public static ConcurrentHashMap<Node,ServerInfo> getServerList() {
		return serverList;
	}
	
	public synchronized static void putToServerList(ServerInfo serverinfo) {
		Node node = new Node(serverinfo.getName(),
				serverinfo.getIP());
		Client.getServerList().put(node, serverinfo);
	}

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
	
	public static Node getServerConnected() {
		return serverConnected;
	}

	public static void setServerConnected(Node serverConnected) {
		Client.serverConnected = serverConnected;
	}













}
