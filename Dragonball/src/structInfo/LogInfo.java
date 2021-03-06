package structInfo;

import java.io.Serializable;

public class LogInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	public enum Action {
		Move, Heal, Damage, Removed
	}
	

	private Action action;
	private int senderUnitID;
	private int senderX;
	private int senderY;
	private UnitType senderType;
	private int targetUnitID;
	private int targetX;
	private int targetY;
	private UnitType targetType;
	private Long timestamp;
	private String senderName;
	
	
	public LogInfo(Action action, int senderUnitID, int senderX, int senderY,
			UnitType senderType, int targetUnitID, int targetX, int targetY,
			UnitType targetType, Long timestamp, String senderName) {
		super();
		this.action = action;
		this.senderUnitID = senderUnitID;
		this.senderX = senderX;
		this.senderY = senderY;
		this.senderType = senderType;
		this.targetUnitID = targetUnitID;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetType = targetType;
		this.timestamp = timestamp;
		this.senderName = senderName;
	}
	@Override
	public boolean equals(Object info)
	{
		System.err.println("onEqualsLogInfo");
		if(this==info)
			return true;
		if(info==null)
			return false;
		if(getClass()!=info.getClass())
			return false;
		
		LogInfo logInfo = (LogInfo)info;
		if(this.senderUnitID != logInfo.senderUnitID || this.targetUnitID!= logInfo.targetUnitID ||
				this.timestamp != logInfo.timestamp)
					return false;
		
		return true;
		
	}


	@Override
	public String toString() {
		return "LogInfo [action=" + action + ", senderUunitID=" + senderUnitID
				+ ", senderX=" + senderX + ", senderY=" + senderY
				+ ", senderType=" + senderType + ", targetUnitID="
				+ targetUnitID + ", targetX=" + targetX + ", targetY="
				+ targetY + ", targetType=" + targetType + ", timestamp="
				+ timestamp + "]";
	}


	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
	public int getSenderUnitID() {
		return senderUnitID;
	}

	public void setSenderUnitID(int senderUnitID) {
		this.senderUnitID = senderUnitID;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}


	public int getSenderX() {
		return senderX;
	}

	public void setSenderX(int senderX) {
		this.senderX = senderX;
	}

	public int getSenderY() {
		return senderY;
	}

	public void setSenderY(int senderY) {
		this.senderY = senderY;
	}

	public int getTargetX() {
		return targetX;
	}

	public void setTargetX(int targetX) {
		this.targetX = targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public void setTargetY(int targetY) {
		this.targetY = targetY;
	}

	public UnitType getTargetType() {
		return targetType;
	}

	public void setTargetType(UnitType targetType) {
		this.targetType = targetType;
	}
	
	public int getTargetUnitID() {
		return targetUnitID;
	}

	public void setTargetUnitID(int targetUnitID) {
		this.targetUnitID = targetUnitID;
	}

	public UnitType getSenderType() {
		return senderType;
	}

	public void setSenderType(UnitType senderType) {
		this.senderType = senderType;
	}

	
	
	
}
