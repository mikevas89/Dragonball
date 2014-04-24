package Node;

import game.BattleField;
import game.BattleFieldViewer;

import interfaces.ServerServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import messages.MessageType;
import messages.ServerServerMessage;

import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import units.Player;
import units.Unit;
import Node.Node;

import communication.Server2ClientRMI;
import communication.Server2ServerRMI;

public class Server extends Node implements java.io.Serializable{
	
	
	private static final long serialVersionUID = 1L;

	private static HashMap<Node, ServerInfo> serverList;
	

	private static HashMap<Node,ClientPlayerInfo> clientList;
	
	public static final int MIN_PLAYER_COUNT = 30;
	public static final int MAX_PLAYER_COUNT = 60;
	public static final int DRAGON_COUNT = 20;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	
	private static BattleField battlefield; 
	private Map<String, LogInfo> PendingActions; //pending moves
	private ArrayList<LogInfo> ValidActions; //log of the valid actions
	private final BlockingQueue<LogInfo> validBlockQueue;//intermediate between valid and pending
	
	public volatile static boolean killServer = false;
	
	public volatile static boolean otherServerExists=false;
	
	private Timer serverServerTimeoutTimer;
	private static ServerInfo myInfo;
	private static int myServerID;


	public int test=0;

	public Server() throws IOException{
		super();
		serverList = new HashMap<Node, ServerInfo>();
		clientList= new HashMap<Node,ClientPlayerInfo>(); //list of clients connected to that server
		PendingActions= Collections.synchronizedMap(new HashMap<String, LogInfo>());   // list of pending action
		ValidActions= new ArrayList<LogInfo>();   // list of valid actions
		validBlockQueue = new LinkedBlockingQueue<>();
		
		int numServer= this.getUniqueIdForName("ServerID.txt");
		
		//unique name of Client
		this.setName("Server"+ String.valueOf(numServer));
		this.setIP("127.0.0.1");
		
		
		//set timers, sets a timer for sending the Ping to other Alive Servers
		this.setServerServerTimeoutTimer(new Timer(true));
		this.getServerServerTimeoutTimer().scheduleAtFixedRate(new SchedulingTimer(),0,Constants.SERVER2SERVER_PING_PERIOD); 

		System.out.println("Server Name: "+ this.getName());

}

	
		

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, ExecutionException {
		
		
		
		/*----------------------------------------------------
				Creation of Server Thread
		----------------------------------------------------		
		*/
		
		new Thread(new Runnable() {
		    public void run() {
		    	Server server = null;
				try {
					server = new Server();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				
				//Server RMI for Server Communication to port 1100
				
				Server2ServerRMI serverServerComm =null;
				try {
					serverServerComm = new Server2ServerRMI(server);
				} catch (RemoteException e2) {
					e2.printStackTrace();
				}
				
				if(!createServerServerRegAndBind(server,serverServerComm))
					bindInExistingServerServerRegistry(server, serverServerComm);
				
				
		 		
		 		//Server RMI for Client communication to port 1099
		 		Server2ClientRMI serverClientComm = null;
				try {
					serverClientComm = new Server2ClientRMI(server);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				if(!Server.createServerClientRegistryAndBind(server,serverClientComm))
					bindInExistingServerClientRegistry(server, serverClientComm);
				
				
				//read server List
				server.readServers("Servers.txt");

				//print out the list
				//server.printlist();
				
				//create my Info 
				myInfo = new ServerInfo(server.getName(),server.getIP(),Server.getMyServerID(),true);
				if(Server.getClientList().size()>0){
					myInfo.setNumClients(Server.getClientList().size());
					myInfo.setRunsGame(true);
				}

				
				boolean startDragons,runDragons;
				
				if (server.startGame()) {
					// create Game
					//battlefield = BattleField.getBattleField();
					//create Dragons
					runDragons=true;
					startDragons=true;
					Server.getMyInfo().setRunsGame(true);
				}
				else{
					runDragons=false;
					startDragons=false;
				}
				battlefield = BattleField.getBattleField();

				new BattleFieldViewer(battlefield);
				
				/*----------------------------------------------------
				DragonMaster creation (which creates all dragons)
				----------------------------------------------------		
				 */
				Runnable dragonmaster = new DragonMaster(server,battlefield,DRAGON_COUNT,runDragons,startDragons);
				new Thread(dragonmaster).start();
				

				/*----------------------------------------------------
						Create thread for monitoring the pending action list
				----------------------------------------------------		
				*/
				Runnable pendingMonitor = new PendingMonitor(server.getPendingActions(),server.getValidBlockQueue());
				new Thread(pendingMonitor).start();
				
				/*----------------------------------------------------
						Create thread for validating the pending action list
				----------------------------------------------------		
				*/
				Runnable validMonitor = new ValidMonitor(server,server.getValidActions(),server.getValidBlockQueue());
				new Thread(validMonitor).start();
				
				/*----------------------------------------------------
				  		Thread to update clients about Battlefield periodically
				----------------------------------------------------		
				*/
				Runnable battlefieldSender = new BattlefieldSender(server);
				new Thread(battlefieldSender).start();
				
				
				
				/*----------------------------------------------------
						Main loop of server
				----------------------------------------------------		
				*/
				int i=0;
				while(!Server.killServer)
				{
					System.out.println("Server is running...");
					Server.printlist();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//Long time = System.currentTimeMillis();
					//System.out.println(time);
					if(i==0)
					{
					//	server.setPendingActions("1 2", new LogInfo("192.","move","0","0", time));
					//	server.setPendingActions("1 3", new LogInfo("193.","move","0","0", time));
						i++;
					}
					
					
					
					for(LogInfo temp: server.getValidActions())
					{
						System.out.println("Valid are: "+temp.getSenderName());
					}
				 }
				
				
				    }
		}).start();
			
	}
	
	public static void printlist()
	{	System.out.println("Printing server list:");
		for(Node item: Server.getServerList().keySet()){
			System.out.println(item.getName()+"   "+item.getIP());
			System.out.println(Server.getServerList().get(item).getServerID()+" Alive:"+ Server.getServerList().get(item).isAlive()
								+" TimeStamp:"+ Server.getServerList().get(item).getTimeLastPingSent());
		}
		System.out.println("Leaving from server list:");
	}

	
	
	
	/*---------------------------------------------------
	 * ESTABLISH SERVER SERVER RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	public static boolean bindInExistingServerServerRegistry(Node node, Server2ServerRMI comm)
	{
		System.out.println("bindInExistingServerServerRegistry");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(Constants.SERVER_SERVER_RMI_PORT);
			myRegistry.bind(node.getName(), comm); // bind with their names
			System.out.println("bindInExistingServerServerRegistry completed");
			return true;
		} catch (RemoteException e) {
			return false;
		} catch (AlreadyBoundException e) {
			return false;
		}
	}
	
	public static boolean createServerServerRegAndBind(Node node, Server2ServerRMI comm) {  //server creates its Registry entry
		
		System.out.println("createServerServerRegistryAndBind");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.createRegistry(Constants.SERVER_SERVER_RMI_PORT);
			myRegistry.rebind(node.getName(), comm); // server's name
			System.out.println("createServerServerRegistryAndBind completed");
			return true;
		} catch (RemoteException e) {
			System.out.println("createServerServerRegistryAndBind failed");
			//e.printStackTrace();
			return false;
		}
		//System.out.println(this.getName()+ " is up and running for Server/Server Com!");
	}
	
	
	
	
	
	/*---------------------------------------------------
	 * ESTABLISH SERVER CLIENT RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	
	public static boolean bindInExistingServerClientRegistry(Node node, Server2ClientRMI comm)
	{
		System.out.println("bindInExistingServerClientRegistry");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(Constants.SERVER_CLIENT_RMI_PORT);
			myRegistry.bind(node.getName(), comm); // bind with their names
			System.out.println("bindInExistingServerClientRegistry completed");
			return true;
		} catch (RemoteException e) {
			return false;
		} catch (AlreadyBoundException e) {
			return false;
		}
	}
	
	public static boolean createServerClientRegistryAndBind(Node node, Server2ClientRMI comm)
	{
		System.out.println("createServerClientRegistryAndBind");
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.createRegistry(Constants.SERVER_CLIENT_RMI_PORT);
			myRegistry.rebind(node.getName(), comm); // server's name
			System.out.println("createServerClientRegistryAndBind completed");
			return true;
		} catch (RemoteException e) {
			System.out.println("createServerClientRegistryAndBind failed");
			//e.printStackTrace();
			return false;
		}
	}
	
/*	
	public void createServerClientReg(Node node, Server2ClientRMI comm) {  //server creates its Registry entry
		
		Registry serverRegistry = null;
		try {
			serverRegistry = LocateRegistry.createRegistry(Constants.SERVER_CLIENT_RMI_PORT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			serverRegistry.bind(this.getName(), comm );
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		} //server's name
		System.out.println(this.getName()+ " is up and running for Server/Client Com!");
	}
	
	*/
	
	
	public int createPlayer(){
		int x,y;
		int attempt = 0;
		do {
			x = (int)(Math.random() * BattleField.MAP_WIDTH);
			y = (int)(Math.random() * BattleField.MAP_HEIGHT);
			attempt++;
		} while (battlefield.getUnit(x, y) != null && attempt < 10);
		// If we didn't find an empty spot, we won't add a new dragon
		if (battlefield.getUnit(x, y) != null) return -1;
		
		//create new Player
		Player player = new Player(x,y,Server.battlefield,myInfo.getServerID());
		//return unique Unit's serverID+unitID
		return player.getUnitID();

	}
	

	public boolean startGame(){
		
		System.out.println("Server: "+ Server.getMyInfo().getName()+" entered startGame");
		//TODO: send broadcast Subscribe2Server to Servers
		for (ServerInfo serverInfo : Server.getServerList().values()) {

			ServerServerMessage subscribeServerMessage = new ServerServerMessage(
									MessageType.Subscribe2Server,
									Server.getMyInfo().getName(), Server.getMyInfo().getIP(),
									serverInfo.getName(), serverInfo.getIP());
			
			// send the subscription message to the server
			ServerServer serverComm = null;
			serverComm = Server.getServerReg(new Node(serverInfo.getName(),
					serverInfo.getIP()));
			
			if(serverComm == null) continue;
			
			System.out.println("Server: "+ myInfo.getName() + " sends Subscribe2Server to "+ serverInfo.getName());

			try {
				serverComm.onMessageReceived(subscribeServerMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

		}

		try {
			Thread.sleep(Constants.SERVER2SERVER_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//timeout reached
		//System.out.println("startGame : PrintList");
		//this.printlist();
		
		for (ServerInfo serverInfo : Server.getServerList().values()) {
			if(serverInfo.isAlive())
				return false;
		}
		
		//server is the first Running - should build the game
		System.out.println("Server: "+ Server.getMyInfo().getName() + " STARTING THE GAME");
		return true;

	}
	
	public int getUniqueIdForName(String fname) throws IOException{
		//getting a unique id for every server
				int numServer = -1;
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
						numServer = 1;
					} else {
						numServer = Integer.parseInt(line);
					}
				} catch (FileNotFoundException e) {
					numServer = 1;
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
					writer.write(String.valueOf(numServer + 1));
				} finally {
					if (writer != null) {
						writer.flush();
						writer.close();
					}
				}
				return numServer;
	}
	
	public void readServers(String fname){
		
 		File file = new File(fname);
		BufferedReader reader = null;
		int j=0;

		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
		    	String[] parts = text.split(" ");
		    	if(!this.getName().equals(parts[0]))
		    			Server.putToServerList(new ServerInfo(parts[0], parts[1], ++j,false));
		    	else
		    		Server.setMyServerID(++j);
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
	

	
	
	/*----------------------------------------------------
				PRIVATE SERVER COMMUNICATION METHODS
	----------------------------------------------------		
	 	*/
	
	public class SchedulingTimer extends TimerTask{
		@Override
		public void run() {
			try {
				sendServerServerPing();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	//refresh the subscription time
	public void sendServerServerPing() throws MalformedURLException {

		for (ServerInfo serverInfo : Server.getServerList().values()) {
			if (!serverInfo.isAlive())
				continue;
			ServerServerMessage pingMessage = new ServerServerMessage(
					MessageType.ServerServerPing, this.getName(), this.getIP(),
					serverInfo.getName(), serverInfo.getIP());

			pingMessage.setNumClients(myInfo.getNumClients());
			// send the subscription message to the server
			ServerServer serverComm = null;
			serverComm = Server.getServerReg(new Node(serverInfo.getName(),
					serverInfo.getIP()));
			
			if(serverComm == null) continue;
			
			System.out.println("Server: "+ myInfo.getName() + " sends Ping to "+ serverInfo.getName());

			try {
				serverComm.onMessageReceived(pingMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

		}
	}
	
		
	public static ServerServer getServerReg(Node server)
	{
		ServerServer serverCommunication = null;
		try {
			serverCommunication = (ServerServer) 
			Naming.lookup("rmi://"+server.getIP()+":"+String.valueOf(Constants.SERVER_SERVER_RMI_PORT)
					+"/"+server.getName());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ myInfo.getName()+" ServerRMI RemoteException error with server: "+ server.getName());
			return null;
		} catch (NotBoundException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ myInfo.getName()+" ServerRMI NotBoundException error with server: "+ server.getName());
			return null;
		}		//getServerInfo returns from ServerList
																//serverIp and serverName
		System.out.println("getServerReg to "+ server.getName());
		return serverCommunication;
	}
		
		
	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	 ----------------------------------------------------		
	*/

	public static HashMap<Node,ClientPlayerInfo> getClientList() {
		return clientList;
	}
	
	public synchronized static void putToServerList(ServerInfo serverinfo) {
		Node node = new Node(serverinfo.getName(),
				serverinfo.getIP());
		Server.getServerList().put(node, serverinfo);
	}

	public synchronized void putToClientList(ClientPlayerInfo clientPlayerInfo) {
		Node node = new Node(clientPlayerInfo.getName(),
											clientPlayerInfo.getIP());
		Server.getClientList().put(node, clientPlayerInfo);
		System.out.println("Server: adding new Client: unitID="+ clientPlayerInfo.getUnitID()+
								" clientIP= "+ clientPlayerInfo.getIP());
	}
	
	public synchronized Unit getBattlefieldUnit(int x, int y)
	{
		return Server.getBattlefield().getUnit(x, y);
	}

	public static HashMap<Node, ServerInfo> getServerList() {
		return serverList;
	}
	
	
	public static BattleField getBattlefield() {
		return battlefield;
	}

	public static void setBattlefield(BattleField battlefield) {
		Server.battlefield = battlefield;
	}
	
	
	public Map<String, LogInfo> getPendingActions() {
		return PendingActions;
	}
	//	public synchronized void setPendingActions(int key,LogInfo value) {
	public synchronized void setPendingActions(String key,LogInfo value) {
		PendingActions.put(key,value);
	}
	
	public synchronized void removePendingActions(String key) {
		PendingActions.remove(key);;
	}

	public ArrayList<LogInfo> getValidActions() {
		return ValidActions;
	}

	public BlockingQueue<LogInfo> getValidBlockQueue() {
		return validBlockQueue;
	}




	public Timer getServerServerTimeoutTimer() {
		return serverServerTimeoutTimer;
	}




	public void setServerServerTimeoutTimer(Timer serverServerTimeoutTimer) {
		this.serverServerTimeoutTimer = serverServerTimeoutTimer;
	}




	public static int getMyServerID() {
		return myServerID;
	}




	public static void setMyServerID(int myServerID) {
		Server.myServerID = myServerID;
	}




	public static ServerInfo getMyInfo() {
		return myInfo;
	}




	public static void setMyInfo(ServerInfo myInfo) {
		Server.myInfo = myInfo;
	}


}
