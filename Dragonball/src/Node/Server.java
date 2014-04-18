package Node;

import game.BattleField;
import game.BattleFieldViewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import structInfo.ClientPlayerInfo;
import structInfo.LogInfo;
import structInfo.ServerInfo;
import units.Unit;
import Node.Node;
import communication.Server2ClientCommunication;

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
		 		
		 	/*	Server2ClientCommunication serverComm = null;
				try {
					serverComm = new Server2ClientCommunication(server);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}

		 		try {
					serverComm.createServerReg();
				} catch (RemoteException
						| AlreadyBoundException e1) {
					e1.printStackTrace();
				}
		 		*/
		 		
		 		battlefield = BattleField.getBattleField();
		 		
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
						Main loop of server
				----------------------------------------------------		
				*/
				int i=0;
				while(true)
				{
					System.out.println("Server is running...");
					try {
						Thread.sleep(5000);
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
			
	
		/* Spawn a new battlefield viewer */
		new Thread(new Runnable() {
			public void run() {
				new BattleFieldViewer();
			}
		}).start();
		
		
	}
	
	public void printlist()
	{
		for(ServerInfo item: this.getServerList()){
			System.out.println(item.getName()+"   "+item.getIP());			
		}
	}

	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	 ----------------------------------------------------		
	*/

	public HashMap<Node,ClientPlayerInfo> getClientList() {
		return clientList;
	}

	public synchronized void setClientList(ClientPlayerInfo clientPlayerInfo) {
		Node node = new Node(clientPlayerInfo.getName(),
											clientPlayerInfo.getIP());
		this.getClientList().put(node, clientPlayerInfo);
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
	
	public synchronized void setPendingActions(String key,LogInfo value) {
		PendingActions.put(key,value);;
	}
	
	public synchronized void removePendingActions(String key) {
		PendingActions.remove(key);;
	}



	public ArrayList<LogInfo> getValidActions() {
		return ValidActions;
	}

	

}
