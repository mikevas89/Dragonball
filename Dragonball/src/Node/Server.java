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
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import messages.MessageType;
import messages.ServerServerMessage;

import structInfo.CheckPoint;
import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import structInfo.LogInfo.Action;
import units.Dragon;
import units.Player;
import units.Unit;
import Node.Node;

import communication.Server2ClientRMI;
import communication.Server2ServerRMI;

public class Server extends Node implements java.io.Serializable{
	
	
	private static final long serialVersionUID = 1L;

	private static ConcurrentHashMap<Node, ServerInfo> serverList;
	//private static Map<Node,ClientPlayerInfo> clientList;

	private static ConcurrentHashMap<Node,ClientPlayerInfo> clientList;
	
	public static final int MIN_PLAYER_COUNT = 30;
	public static final int MAX_PLAYER_COUNT = 60;
	public static final int DRAGON_COUNT = 30;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	
	private static BattleField battlefield; 
	private static Map<String, LogInfo> PendingActions; //pending moves
	private static ArrayList<LogInfo> ValidActions; //log of the valid actions
	private static LinkedBlockingQueue<LogInfo> validBlockQueue;//intermediate between valid and pending
	private static CheckPoint checkPoint;
	private volatile static boolean startDragons;
	private volatile static boolean runDragons;
	
	public volatile static boolean killServer = false;
	
	public volatile static boolean otherServerExists=false;
	
	private Timer serverServerTimeoutTimer;
	private static ServerInfo myInfo;
	private static int myServerID;
	
	public static Object lock;


	public int test=0;

	public Server(String serverName, String serverIP) throws IOException{
		super();
		lock = new Object();
		serverList = new ConcurrentHashMap<Node, ServerInfo>(16,0.9f,20);
		clientList= new ConcurrentHashMap<Node, ClientPlayerInfo>(16,0.9f,20); //list of clients connected to that server
		PendingActions= new ConcurrentHashMap<String, LogInfo>(16,0.9f,20);   // list of pending action
		ValidActions= new ArrayList<LogInfo>();   // list of valid actions
		validBlockQueue = new LinkedBlockingQueue<>();
		checkPoint = new CheckPoint(25,25);
		//int numServer= this.getUniqueIdForName("ServerID.txt");
		
		//unique name of Client
		this.setName(serverName);
		this.setIP(serverIP);
		
		//set timers, sets a timer for sending the Ping to other Alive Servers
		this.setServerServerTimeoutTimer(new Timer(true));
		this.getServerServerTimeoutTimer().scheduleAtFixedRate(new SchedulingTimer(),0,Constants.SERVER2SERVER_PING_PERIOD); 

		System.out.println("Server Name: "+ this.getName() + " ServerIP:"+ this.getIP());

}

	
		

	public static void main(final String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, ExecutionException {
		
		try {
			System.out.println(java.net.InetAddress.getLocalHost());
		} catch (UnknownHostException e3) {
			e3.printStackTrace();
		}

		
		/*----------------------------------------------------
				Creation of Server Thread
		----------------------------------------------------		
		*/
		
		
		new Thread(new Runnable() {
		    public void run() {
		    	Server server = null;
				try {
					server = new Server(args[1],args[2]);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				
				//Server RMI for Server Communication to port 1099
				
				Server2ServerRMI serverServerComm =null;
				try {
					serverServerComm = new Server2ServerRMI(server);
				} catch (RemoteException e2) {
					e2.printStackTrace();
				}
				
				if(!createServerServerRegAndBind(server,serverServerComm))
					bindInExistingServerServerRegistry(server, serverServerComm);
				
				
		 		
		 		//Server RMI for Client communication to port 1100
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

				
				if (server.startGame()) {
					// create Game
					//battlefield = BattleField.getBattleField();
					//create Dragons
					Server.setRunDragons(true);
					Server.setStartDragons(true);
					Server.getMyInfo().setRunsDragons(true);
					Server.getMyInfo().setRunsGame(true);
				}
				else{
					Server.setRunDragons(false);
					Server.setStartDragons(false);
					Server.getMyInfo().setRunsDragons(false);
				}
				
				System.out.println("Server my Info: "+Server.getMyInfo().getName()+ " "+ Server.getMyInfo().getIP() );
				battlefield = BattleField.getBattleField();

				new BattleFieldViewer(battlefield);
				
				/*----------------------------------------------------
				DragonMaster creation (which creates all dragons)
				----------------------------------------------------		
				 */
				Runnable dragonmaster = new DragonMaster(battlefield,DRAGON_COUNT);
				new Thread(dragonmaster).start();
				

				/*----------------------------------------------------
						Create thread for monitoring the pending action list
				----------------------------------------------------		
				*/
				Runnable pendingMonitor = new PendingMonitor(Server.getPendingActions(),Server.getValidBlockQueue());
				new Thread(pendingMonitor).start();
				
				/*----------------------------------------------------
						Create thread for validating the pending action list
				----------------------------------------------------		
				*/
				Runnable validMonitor = new ValidMonitor(Server.getPendingActions(),Server.getValidActions(),Server.getValidBlockQueue());
				new Thread(validMonitor).start();
				
				/*----------------------------------------------------
				  		Thread to update clients about Battlefield periodically
				----------------------------------------------------		
				*/
				Runnable battlefieldSender = new BattlefieldSender(server);
				new Thread(battlefieldSender).start();
				
				/*----------------------------------------------------
		  			Thread to monitor Ping Messages for Server/Server and Server/Client Alive Connections
				----------------------------------------------------		
				 */
				Runnable pingMonitor = new PingMonitor();
				new Thread(pingMonitor).start();
				
				
				
				/*----------------------------------------------------
						Main loop of server
				----------------------------------------------------		
				*/
				while(!Server.killServer)
				{
					//Server.printlist();
					//System.out.println("Server is getting a Checkpoint of the BattleField...");
					Server.getCheckPoint().captureCheckPoint(Server.getBattlefield(), Server.getValidActions());
					try {
						Thread.sleep(Constants.SERVER_CHECKPOINT_PERIOD);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				 }
				
				
				    }
		}).start();
			
	}
	
	public static void printlist()
	{	System.out.println("Printing server list:");
		for(ServerInfo serverInfo: Server.getServerList().values()){
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
			//e.printStackTrace();
			return false;
		} catch (AlreadyBoundException e) {
			//e.printStackTrace();
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
			//e.printStackTrace();
			return false;
		} catch (AlreadyBoundException e) {
			//e.printStackTrace();
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
	
	public static void checkIfUnitIsDead(LogInfo action){
		//only if the unit belongs to this server
		Unit targetUnit= Server.getBattlefield().getUnitByUnitID(action.getTargetUnitID());
		//only if the unit belongs to this server
		if(targetUnit==null || targetUnit.getServerOwnerID()!=Server.getMyInfo().getServerID())
			return;
		if(((targetUnit instanceof Player) || (targetUnit instanceof Dragon)) && (targetUnit.getHitPoints()<=0)){
			System.err.println("Checker went to unsubscribed");
			Runnable messageSender = new UnSubscribeMessageSender(Server.getPendingActions(),targetUnit);
			new Thread(messageSender).start();
			LogInfo playerDown = new LogInfo(Action.Removed,targetUnit.getUnitID(), targetUnit.getX(),targetUnit.getY(),
												targetUnit.getType(targetUnit.getX(),targetUnit.getY()),
												targetUnit.getUnitID(), 
												targetUnit.getX(),targetUnit.getY(),
												targetUnit.getType(targetUnit.getX(),targetUnit.getY()),
												System.nanoTime(), Server.getMyInfo().getName());
			Server.getValidActions().add(playerDown);
			
			Runnable validActionPlayerDownSender = new ValidActionSender(playerDown);
			new Thread(validActionPlayerDownSender).start();
		}
	}
	

	public boolean startGame(){
		
		System.out.println("Server: "+ Server.getMyInfo().getName()+" entered startGame");
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
				//e.printStackTrace();
			} catch (NotBoundException e) {
				//e.printStackTrace();
			}

		}

		try {
			Thread.sleep(Constants.SERVER2SERVER_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//timeout reached
		//System.out.println("startGame : PrintList");
		Server.printlist();
		
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
		    	else{
		    		Server.setMyServerID(++j);
		    	}
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
			sendServerServerPing();			
		}		
	}
	

	//refresh the subscription time
	public void sendServerServerPing() {

		for (ServerInfo serverInfo : Server.getServerList().values()) {
			if (!serverInfo.isAlive())
				continue;
			ServerServerMessage pingMessage = new ServerServerMessage(
					MessageType.ServerServerPing, this.getName(), this.getIP(),
					serverInfo.getName(), serverInfo.getIP());

			pingMessage.setNumClients(myInfo.getNumClients());
			pingMessage.setSenderRunsGame(Server.getMyInfo().isRunsGame());
			pingMessage.setSenderRunsDragons(Server.getMyInfo().isRunsDragons());
			// send the subscription message to the server
			ServerServer serverComm = null;
			serverComm = Server.getServerReg(new Node(serverInfo.getName(),
					serverInfo.getIP()));
			
			if(serverComm == null) continue;
			
			//System.out.println("Server: "+ myInfo.getName() + " sends Ping to "+ serverInfo.getName());

			try {
				serverComm.onMessageReceived(pingMessage);
			} catch (RemoteException e) {
				//e.printStackTrace();
			} catch (NotBoundException e) {
				//e.printStackTrace();
			}

		}
	}
	

	
	
	public static ClientServer getClientReg(Node client)
	{
		ClientServer clientCommunication = null;
		try {
			clientCommunication = (ClientServer) 
			Naming.lookup("rmi://"+client.getIP()+":"+String.valueOf(Constants.SERVER_CLIENT_RMI_PORT)
					+"/"+client.getName());
		} catch (MalformedURLException e) {
			//e.printStackTrace();
		} catch (RemoteException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ Server.getMyInfo().getName()+" ServerClientRMI RemoteException error with client: "+ client.getName());
			return null;
		} catch (NotBoundException e) {
			//e.printStackTrace();
			System.err.println("Server: "+ Server.getMyInfo().getName()+" ServerClientRMI NotBoundException error with client: "+ client.getName());		
			return null;
		}catch (Exception e){
			System.err.println("Server: "+ Server.getMyInfo().getName()+" ServerClientRMI Exception error with client: "+ client.getName());		
			return null;
		}
																//clientIp and clientName
		//System.out.println("Getting Registry from "+ client.getName());
		return clientCommunication;
	}
	
	
			
	public static ServerServer getServerReg(Node server)
	{
		ServerServer serverCommunication = null;
		try {
			serverCommunication = (ServerServer) 
			Naming.lookup("rmi://"+server.getIP()+":"+String.valueOf(Constants.SERVER_SERVER_RMI_PORT)
					+"/"+server.getName());
		} catch (MalformedURLException e) {
			//e.printStackTrace();
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
		//System.out.println("getServerReg to "+ server.getName());
		return serverCommunication;
	}
	
	
	
	public void DecideForRemoval(Node problematicServer){
		System.err.println(Server.getMyInfo().getName()+ " Ready to Decide");
		ServerInfo serverInfo = Server.getServerList().get(problematicServer);
		
		//at the last message, a removal will be decided
				Node serverToRemove=null;
				if(serverInfo.getNumAnswersAgreeRemovingServer() >= serverInfo.getTotalNumAnswersAggreement()/2)
					//remove problematic server
					serverToRemove=problematicServer;
				else
					//remove the server started the Agreement procedure
					serverToRemove=serverInfo.getNodeFoundTheProblematicServer(); 
				
				System.err.println("Server:"+Server.getMyInfo().getName()+ " Decision Reached: Server Down: "+ serverToRemove.getName());
			
				if((serverToRemove.getName().equals(Server.getMyInfo().getName()) && 
						serverToRemove.getIP().equals(Server.getMyInfo().getIP()))){
					System.out.println("Problematic Server is the current");
					Server.getMyInfo().setAlive(false);
					Server.getMyInfo().setRunsGame(false);
					Server.getPendingActions().clear();
					Server.getValidBlockQueue().clear();
					System.out.println("SSD "+ System.nanoTime());
					Server.killServer=true;
					return;
				}
				
				// remove the current info for the serverList decided to remove
				ServerInfo serverInfoForRemovedServer = Server.getServerList().get(serverToRemove);
				// remove players of the server
				synchronized (Server.lock) {
					Iterator<Unit> it = Server.getBattlefield().getUnits()
							.listIterator();
					while (it.hasNext()) {
						Unit unit = it.next();
						if(unit instanceof Dragon) continue;
					if (unit.getServerOwnerID() == serverInfoForRemovedServer.getServerID()) {
						LogInfo playerDown = new LogInfo(Action.Removed,
								unit.getUnitID(), unit.getX(), unit.getY(),
								unit.getType(unit.getX(), unit.getY()),
								unit.getUnitID(), unit.getX(), unit.getY(),
								unit.getType(unit.getX(), unit.getY()),
								System.nanoTime(), serverInfoForRemovedServer.getName());
						try {
							Server.getValidBlockQueue().put(playerDown);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				  }
				}
				
				//current Server decides if he is going to handle Dragons (in case the removed server was the handler)
				if(serverInfoForRemovedServer.isRunsDragons() && 
						serverInfoForRemovedServer.getServerID() == Server.getMyInfo().getServerID()-1){
					Server.setRunDragons(true);
					Server.getMyInfo().setRunsDragons(true);
					System.err.println(Server.getMyInfo()+" HANDLES DRAGONS");
					//change serverOwnerID of dragons
					synchronized (Server.lock) {
						Iterator<Unit> it = Server.getBattlefield().getUnits().listIterator();
						while (it.hasNext()) {
							Unit unit = it.next();
							if(unit instanceof Dragon) {
								unit.setServerOwnerID(Server.getMyInfo().getServerID());
								unit.setUnitID( Integer.parseInt(String.valueOf(Server.getMyInfo().getServerID())+
															String.valueOf(unit.getUnitID())));
							}
						}
					}
				}

				Server.getServerList().replace(serverToRemove,new ServerInfo(serverInfoForRemovedServer
										.getName(), serverInfoForRemovedServer
										.getIP(), serverInfoForRemovedServer
										.getServerID(), false));

				System.err.println(Server.getMyInfo().getName()+ " : REMOVE SERVER : " + serverToRemove.getName());
				System.out.println("SSD "+ System.nanoTime());
				Server.printlist();
	}
		
		
	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	 ----------------------------------------------------		
	*/
	
	public static int getNumAliveServers(){
		int numAliveServers=1;
		for(Iterator<ServerInfo> it=Server.getServerList().values().iterator();it.hasNext();){
			ServerInfo serverInfo = it.next();
			if(serverInfo.isAlive())
				numAliveServers++;
		}
		return numAliveServers;
	}
	
	public static int getNumProblematicServers(){
		int numProblematicServers=0;
		for(Iterator<ServerInfo> it=Server.getServerList().values().iterator();it.hasNext();){
			ServerInfo serverInfo = it.next();
			if(serverInfo.isProblematicServer())
				numProblematicServers++;
		}
		return numProblematicServers;
	}
	
	public static int getNumRunningGameServers(){
		int numRunningGameServers=0;
		for(Iterator<ServerInfo> it=Server.getServerList().values().iterator();it.hasNext();){
			ServerInfo serverInfo = it.next();
			if(serverInfo.isRunsGame())
				numRunningGameServers++;
		}
		return numRunningGameServers;
	}
	

	public static ConcurrentHashMap<Node,ClientPlayerInfo> getClientList() {
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

	public static ConcurrentHashMap<Node, ServerInfo> getServerList() {
		return serverList;
	}
	
	
	public static BattleField getBattlefield() {
		return battlefield;
	}

	public static void setBattlefield(BattleField battlefield) {
		Server.battlefield = battlefield;
	}
	
	
	public static Map<String, LogInfo> getPendingActions() {
		return PendingActions;
	}
	//	public synchronized void setPendingActions(int key,LogInfo value) {
	public synchronized void setPendingActions(String key,LogInfo value) {
		PendingActions.put(key,value);
	}
	
	public synchronized void removePendingActions(String key) {
		PendingActions.remove(key);;
	}

	public static ArrayList<LogInfo> getValidActions() {
		return ValidActions;
	}
	
	public static void copyValidActions(ArrayList<LogInfo> actions){
		Server.ValidActions = new ArrayList<LogInfo>(actions);
	}

	public static CheckPoint getCheckPoint() {
		return checkPoint;
	}

	public static void setCheckPoint(CheckPoint checkPoint) {
		Server.checkPoint = checkPoint;
	}

	public static LinkedBlockingQueue<LogInfo> getValidBlockQueue() {
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




	public static boolean isStartDragons() {
		return startDragons;
	}




	public static void setStartDragons(boolean startDragons) {
		Server.startDragons = startDragons;
	}




	public static boolean isRunDragons() {
		return runDragons;
	}




	public static void setRunDragons(boolean runDragons) {
		Server.runDragons = runDragons;
	}


}
