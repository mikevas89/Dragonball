package structInfo;

import Node.Node;



public class ClientPlayerInfo extends Node{

	private static final long serialVersionUID = 1L;

	private int unitID; //unit ID of the client

	private long TimeLastPingSent; //time that the client sent his last ClientServerPing	
	
	private long lastPingFromServer; //last timestamp of Ping from Server
	
	private boolean serverHasSentPingForCheckingClient;
	private boolean regularCommunicationFromClient;
	
	
	
	
	public ClientPlayerInfo(String Name, String IP,int unitID){
		super(Name,IP);
		this.setTimeLastPingSent(0); //default
		this.setUnitID(unitID);//default
		
	}

    public boolean equals(ClientPlayerInfo info) {

            return (this.getName().equals(info.getName()) && this.getIP().equals(info.getIP())
            		&& this.getUnitID()==info.getUnitID());
    }

    
    
    
	public int getUnitID() {
		return this.unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

    
	public long getTimeLastPingSent() {
		return TimeLastPingSent;
	}

	public void setTimeLastPingSent(long timeLastPingSent) {
		TimeLastPingSent = timeLastPingSent;
	}

	public long getLastPingFromServer() {
		return lastPingFromServer;
	}

	public void setLastPingFromServer(long lastPingFromServer) {
		this.lastPingFromServer = lastPingFromServer;
	}

	public boolean isServerHasSentPingForCheckingClient() {
		return serverHasSentPingForCheckingClient;
	}

	public void setServerHasSentPingForCheckingClient(
			boolean serverHasSentPingForCheckingClient) {
		this.serverHasSentPingForCheckingClient = serverHasSentPingForCheckingClient;
	}

	public boolean isRegularCommunicationFromClient() {
		return regularCommunicationFromClient;
	}

	public void setRegularCommunicationFromClient(boolean regularCommunicationFromClient) {
		this.regularCommunicationFromClient = regularCommunicationFromClient;
	}
}

