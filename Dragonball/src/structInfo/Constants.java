package structInfo;


public class Constants {

	public static final String[] RMI_CLIENT_HOSTNAME={"dante","mike"};
	public static final String[] RMI_CLIENT_IP={ "127.0.1.1","145.94.181.223"} ; //Server IP
	public static final int NUM_IPS=2;
	public static final int NODES_PER_IP=3;
	public static final int NUM_NODES=NUM_IPS*NODES_PER_IP;
	
	public static final int SERVER_CLIENT_RMI_PORT=1099;
	public static final int SERVER_SERVER_RMI_PORT=1100;
	
	
	public static final long CLIENT_CHECKING_ISSUBSCRIBED = 500;
	public static final long CLIENT_PERIOD_ACTION= 3000;//make it 5000
	
	//Server BattleFieldSender
	public static final long BROADCAST_PERIOD_TO_CLIENTS = CLIENT_PERIOD_ACTION/2;
	
	//Server PendingMonitor
	public static final long PENDING_TIMEOUT=1000;
	public static final long CHECK_PENDING_LIST_PERIOD=1000;
	
	//Server PingMonitor/SchedulerTimer for Ping ,  Client Actions/SchedulerTimer for Ping
	public static final long CLIENT2SERVER_PING_PERIOD=5000;
	public static final long SERVER2SERVER_PING_PERIOD=20000;
	public static final long SERVER2CLIENT_TIMEOUT = 2* CLIENT2SERVER_PING_PERIOD;
	public static final long SERVER2SERVER_TIMEOUT = 5000;
	
	//Server PingMonitor , it is calculated according to the smallest Timeout PingMonitor checks for Pings -> SERVER2SERVER_TIMEOUT
	public static final long PING_MONITOR_CHECKING_PERIOD =  SERVER2SERVER_TIMEOUT / 5; 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
