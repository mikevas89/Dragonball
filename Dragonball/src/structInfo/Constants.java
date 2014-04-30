package structInfo;


public class Constants {

	public static final String[] RMI_CLIENT_HOSTNAME={"dante","mike"};
	public static final String[] RMI_CLIENT_IP={ "127.0.1.1","145.94.181.223"} ; //Server IP
	public static final int NUM_IPS=2;
	public static final int NODES_PER_IP=3;
	public static final int NUM_NODES=NUM_IPS*NODES_PER_IP;
	
	public static final int MAX_CLIENTS_PER_SERVER=5;
	
	public static final int SERVER_CLIENT_RMI_PORT=1099;
	public static final int SERVER_SERVER_RMI_PORT=1100;
	
	
	//action periods for the players
	public static final long PLAYER_PERIOD_ACTION= 12000;
	//Server BattleFieldSender
	public static final long BROADCAST_BATTLEFIELD_PERIOD_TO_CLIENTS = PLAYER_PERIOD_ACTION/2;
	
	
	//Server PendingMonitor
	public static final long PENDING_TIMEOUT=BROADCAST_BATTLEFIELD_PERIOD_TO_CLIENTS /2;
	public static final long CHECK_PENDING_LIST_PERIOD=PENDING_TIMEOUT / 5;
	
	//client subscription
	public static final long CLIENT_CHECKING_ISSUBSCRIBED = 500;
	//action periods for the dragons
	public static final long DRAGON_PERIOD_ACTION= PLAYER_PERIOD_ACTION + PENDING_TIMEOUT;
	

	
	

	
	//Server PingMonitor/SchedulerTimer for Ping ,  Client Actions/SchedulerTimer for Ping
	public static final long CLIENT2SERVER_PING_PERIOD=5000;
	public static final long SERVER2SERVER_PING_PERIOD=2000;
	public static final long SERVER2CLIENT_TIMEOUT = 4* CLIENT2SERVER_PING_PERIOD;
	public static final long SERVER2SERVER_TIMEOUT = 3* SERVER2SERVER_PING_PERIOD;
	
	public static final long SERVER_CHECKPOINT_PERIOD = 3000;
	
	//Server PingMonitor , it is calculated according to the smallest Timeout PingMonitor checks for Pings -> SERVER2SERVER_TIMEOUT
	public static final long PING_MONITOR_CHECKING_PERIOD =  SERVER2SERVER_PING_PERIOD / 3; 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
