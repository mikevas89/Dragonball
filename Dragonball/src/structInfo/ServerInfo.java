package structInfo;

import Node.Node;
	
	public class ServerInfo{
		private String Name;
		private String IP;
		private long currentNodeTimeLastPingSent; //timestamp of the last message the serverList owner has sent
		private long remoteNodeTimeLastPingSent; //timestamp of the last message the referenced Node to this list has sent 	
		private int serverID;
		private boolean Alive; //server is able to operate
		private int numClients; //number of clients for the server
		private boolean runsGame; //server is running the game (only if clients are connected)
		
		private boolean problematicServer; //server finds out that this server does not respond
		private Node nodeFoundTheProblematicServer; //keeps track of the node that started the agreement procedures
		private int totalNumAnswersAggreement; //broadcast messages to agree about removing server
		private int numAnswersAgreeRemovingServer;
		private int numAnswersNotAgreeRemovingServer;
	

		public ServerInfo(String name, String ip, int serverid, boolean alive) {
			this.Name = name;
			this.IP = ip;
			this.serverID=serverid;
			this.Alive=alive;
			this.setNumClients(0);
			this.setRunsGame(false);
			this.setCurrentNodeTimeLastPingSent(0);
			this.setRemoteNodeTimeLastPingSent(0);
			this.setProblematicServer(false);
			this.setTotalNumAnswersAggreement(0);
			this.setNumAnswersAgreeRemovingServer(0);
			this.setNumAnswersNotAgreeRemovingServer(0);
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

		public int getTotalNumAnswersAggreement() {
			return totalNumAnswersAggreement;
		}

		public void setTotalNumAnswersAggreement(int totalNumAnswersAggreement) {
			this.totalNumAnswersAggreement = totalNumAnswersAggreement;
		}

		public boolean isProblematicServer() {
			return problematicServer;
		}

		public void setProblematicServer(boolean problematicServer) {
			this.problematicServer = problematicServer;
		}

		public int getNumAnswersAgreeRemovingServer() {
			return numAnswersAgreeRemovingServer;
		}

		public void setNumAnswersAgreeRemovingServer(int numAnswersAgreeRemovingServer) {
			this.numAnswersAgreeRemovingServer = numAnswersAgreeRemovingServer;
		}

		public int getNumAnswersNotAgreeRemovingServer() {
			return numAnswersNotAgreeRemovingServer;
		}

		public void setNumAnswersNotAgreeRemovingServer(
				int numAnswersNotAgreeRemovingServer) {
			this.numAnswersNotAgreeRemovingServer = numAnswersNotAgreeRemovingServer;
		}

		public Node getNodeFoundTheProblematicServer() {
			return nodeFoundTheProblematicServer;
		}

		public void setNodeFoundTheProblematicServer(
				Node nodeFoundTheProblematicServer) {
			this.nodeFoundTheProblematicServer = nodeFoundTheProblematicServer;
		}
	}
	
