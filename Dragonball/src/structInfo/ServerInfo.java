package structInfo;
	
	public class ServerInfo{
		private String Name;
		private String IP;
		private long currentNodeTimeLastPingSent; //timestamp of the last message the serverList owner has sent
		private long remoteNodeTimeLastPingSent; //timestamp of the last message the referenced Node to this list has sent 	
		private int serverID;
		private boolean Alive; //server is able to operate
		private int numClients; //number of clients for the server
		private boolean runsGame; //server is running the game (only if clients are connected)
	

		public ServerInfo(String name, String ip, int serverid, boolean alive) {
			this.Name = name;
			this.IP = ip;
			this.serverID=serverid;
			this.Alive=alive;
			this.setNumClients(0);
			this.setRunsGame(false);
			this.setRemoteNodeTimeLastPingSent(0);
		}
		
		public String getName() {
			return Name;
		}
		public String getIP() {
			return IP;
		}
		public long getRemoteNodeTimeLastPingSent() {
			return remoteNodeTimeLastPingSent;
		}
		public void setRemoteNodeTimeLastPingSent(long timeLastPingSent) {
			remoteNodeTimeLastPingSent = timeLastPingSent;
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
		public int getNumClients() {
			return numClients;
		}
		public void setNumClients(int numClients) {
			this.numClients = numClients;
		}
		public boolean isRunsGame() {
			return runsGame;
		}
		public void setRunsGame(boolean runsGame) {
			this.runsGame = runsGame;
		}

		public long getCurrentNodeTimeLastPingSent() {
			return currentNodeTimeLastPingSent;
		}

		public void setCurrentNodeTimeLastPingSent(
				long currentNodeTimeLastPingSent) {
			this.currentNodeTimeLastPingSent = currentNodeTimeLastPingSent;
		}
	}
	
