package Node;

import game.BattleField;

import game.BattleFieldViewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import structInfo.ClientPlayerInfo;
import structInfo.Constants;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import units.Player;
import units.Unit;
import Node.Node;
import communication.Server2ClientRMI;

public class Server extends Node implements java.io.Serializable{
	
	
	private static final long serialVersionUID = 1L;

	private ArrayList<ServerInfo> serverList;
	private HashMap<Node,ClientPlayerInfo> clientList;
	
	public static final int MIN_PLAYER_COUNT = 30;
	public static final int MAX_PLAYER_COUNT = 60;
	public static final int DRAGON_COUNT = 20;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	
	private static BattleField battlefield; 
	private HashMap<String,LogInfo> PendingActions;
	
	public volatile static boolean killServer = false;
	

	private ArrayList<LogInfo> ValidActions;
	public int test=0;

	public Server(){
		super();
		serverList = new ArrayList<ServerInfo>();
		clientList= new HashMap<Node,ClientPlayerInfo>(); //list of clients connected to that server
		PendingActions= new HashMap<String, LogInfo>();   // list of pending action
		ValidActions= new ArrayList<LogInfo>();   // list of valid actions
	}
	
		

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException, ExecutionException {
		
		
		
		/*----------------------------------------------------
				Creation of Server Thread
		----------------------------------------------------		
		*/
		
		new Thread(new Runnable() {
		    public void run() {
		    	Server server=new Server();
		 		server.setName("dante"); //pc name
		 		
		 		//Server RMI for Client communication
		 		Server2ClientRMI serverComm = null;
				try {
					serverComm = new Server2ClientRMI(server);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				server.createServerReg(server,serverComm);

		 		
		 		
		 		battlefield = BattleField.getBattleField();
				new BattleFieldViewer(battlefield);
		 		
		 		File file = new File("src/Servers.txt");
				BufferedReader reader = null;

				try {
				    reader = new BufferedReader(new FileReader(file));
				    String text = null;

				    while ((text = reader.readLine()) != null) {
				    	String[] parts = text.split(" ");
				        server.getServerList().add(new ServerInfo(parts[0], parts[1]));
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

				//print out the list
				server.printlist();
				
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
				Runnable pendingMonitor = new PendingMonitor(server.getPendingActions(),server.getValidActions());
				new Thread(pendingMonitor).start();
				
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
						server.setPendingActions("1 2", new LogInfo("192.","move","0","0", time));
						server.setPendingActions("1 3", new LogInfo("193.","move","0","0", time));
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
	{
		for(ServerInfo item: this.getServerList()){
			System.out.println(item.getName()+"   "+item.getIP());			
		}
	}

	
	/*---------------------------------------------------
	 * ESTABLISH SERVER RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	public void createServerReg(Node node, Server2ClientRMI comm) {  //server creates its Registry entry
		
		Registry serverRegistry = null;
		try {
			serverRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			serverRegistry.bind(this.getName(), comm );
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		} //server's name
		System.out.println(this.getName()+ " is up and running!");
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
	

	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	 ----------------------------------------------------		
	*/

	public HashMap<Node,ClientPlayerInfo> getClientList() {
		return clientList;
	}

	public synchronized void putToClientList(ClientPlayerInfo clientPlayerInfo) {
		Node node = new Node(clientPlayerInfo.getName(),
											clientPlayerInfo.getIP());
		this.getClientList().put(node, clientPlayerInfo);
		System.out.println("Server: adding new Client: unitID="+ clientPlayerInfo.getUnitID()+
								"clientIP= "+ clientPlayerInfo.getIP());
	}
	
	public synchronized Unit getBattlefieldUnit(int x, int y)
	{
		return Server.getBattlefield().getUnit(x, y);
	}

	public ArrayList<ServerInfo> getServerList() {
		return serverList;
	}
	
	
	public static BattleField getBattlefield() {
		return battlefield;
	}

	public static void setBattlefield(BattleField battlefield) {
		Server.battlefield = battlefield;
	}
	
	
	public HashMap<String, LogInfo> getPendingActions() {
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

	

}
