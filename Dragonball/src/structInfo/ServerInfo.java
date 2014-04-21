package structInfo;
	
	public class ServerInfo{
		private String Name;
		private String IP;
		private long TimeLastPingSent; //time that the client sent his last ClientServerPing	
		private int serverID;
		private boolean Alive;

		public ServerInfo(String name, String ip, int serverid, boolean alive) {
			this.Name = name;
			this.IP = ip;
			this.serverID=serverid;
			this.Alive=alive;
		}
		public String getName() {
			return Name;
		}
		public String getIP() {
			return IP;
		}
		public long getTimeLastPingSent() {
			return TimeLastPingSent;
		}
		public void setTimeLastPingSent(long timeLastPingSent) {
			TimeLastPingSent = timeLastPingSent;
		}
		public int getServerID() {
			return serverID;
		}
		public void setServerID(int serverID) {
			this.serverID = serverID;
		}
		public boolean isAlive() {
			return Alive;
		}
		public void setAlive(boolean alive) {
			Alive = alive;
		}
	}
	
