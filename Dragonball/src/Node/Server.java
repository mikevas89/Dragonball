package Node;

import game.BattleField;
import game.BattleFieldViewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import units.Player;
import units.Unit;
import Node.Node;
import communication.ClientRMI;
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
		 		Server2ClientRMI serverComm = null;
				try {
					serverComm = new Server2ClientRMI(server);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				server.createServerClientReg(server,serverComm);
				
				//read server List
				server.readServers("Servers.txt");

				//print out the list
				server.printlist();
				
				boolean startDragons,runDragons;
				
				if (server.startGame()) {
					// create Game
					battlefield = BattleField.getBattleField();
					//create Dragons
					runDragons=true;
					startDragons=true;
				}
				else{
					runDragons=false;
					startDragons=false;
				}
					
				
		 		

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
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Long time = System.currentTimeMillis();
					//System.out.println(time);
					if(i==0)
					{
					//	server.setPendingActions("1 2", new LogInfo("192.","move","0","0", time));
					//	server.setPendingActions("1 3", new LogInfo("193.","move","0","0", time));
						i++;
					}
					
					
					
					for(LogInfo temp: server.getValidActions())
					{
						System.out.println("Valid are: "+temp.getSenderIP());
					}
				 }
				
				
				    }
		}).start();
			
	}
	
	public void printlist()
	{	System.out.println("Printing server list:");
		for(Node item: Server.getServerList().keySet()){
			System.out.println(item.getName()+"   "+item.getIP());			
		}
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
		Player player = new Player(x,y,this.battlefield);
		//return Unit's unitID
		return player.getUnitID();

	}
	

	public boolean startGame(){
		//TODO: send broadcast Ping to Servers
		boolean startGame=false;
		while(!startGame){
			try {
				Thread.sleep(Constants.SERVER2SERVER_TIMEOUT/10);
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(Server.otherServerExists)
				return false;
		}
		//timeout reached
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
						numServer = 0;
					} else {
						numServer = Integer.parseInt(line);
					}
				} catch (FileNotFoundException e) {
					numServer = 0;
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
								"clientIP= "+ clientPlayerInfo.getIP());
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


}
