package structInfo;

import Node.Node;



public class ClientPlayerInfo extends Node{

	private int unitID; //unit ID of the client

	private long TimeLastPingSent; //time that the client sent his last ClientServerPing	
	
	
	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	
	
	public ClientPlayerInfo(String Name, String IP){
		super(Name,IP);
		this.unitID=-1;//default
		this.setTimeLastPingSent(0); //default
		
	}

    public boolean equals(ClientPlayerInfo info) {

            return (this.getName().equals(info.getName()) && this.getIP().equals(info.getIP())
            		&& this.getUnitID()==info.getUnitID());
        }

	public long getTimeLastPingSent() {
		return TimeLastPingSent;
	}

	public void setTimeLastPingSent(long timeLastPingSent) {
		TimeLastPingSent = timeLastPingSent;
	}
}

