package structInfo;

import Node.Node;



public class ClientPlayerInfo extends Node{

	private int unitID; //unit ID of the client

	private long TimeLastPingSent; //time that the client sent his last ClientServerPing	
	
	
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
}

