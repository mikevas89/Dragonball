package structInfo;


public class Constants {

	public static final String[] RMI_CLIENT_HOSTNAME={"dante","mike"};
	public static final String[] RMI_CLIENT_IP={ "127.0.1.1","145.94.181.223"} ; //Server IP
	public static final int NUM_IPS=2;
	public static final int NODES_PER_IP=3;
	public static final int NUM_NODES=NUM_IPS*NODES_PER_IP;
	
	public static final int SERVER_CLIENT_RMI_PORT=1099;
	public static final int SERVER_SERVER_RMI_PORT=1100;
	
	public static final String RMI_CLIENT2_IP="145.94.181.223"; //Server IP
	
	public static final long CLIENT_CHECKING_ISSUBSCRIBED = 500;
	public static final long CLIENT_PERIOD_ACTION= 3000;//make it 5000
	public static final long BROADCAST_PERIOD_TO_CLIENTS = CLIENT_PERIOD_ACTION/2;
	public static final long PENDING_TIMEOUT=1000;
	public static final long CHECK_PENDING_LIST_PERIOD=1000;
	
	public static final long SERVER2SERVER_TIMEOUT = 2000;
}
