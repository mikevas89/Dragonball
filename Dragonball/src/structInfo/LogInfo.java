package structInfo;

public class LogInfo {

	public enum Action {
		Move, Heal, Damage
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
	private String senderIP;
	
	
	public LogInfo(Action action, int senderUnitID, int senderX, int senderY,
			UnitType senderType, int targetUnitID, int targetX, int targetY,
			UnitType targetType, Long timestamp, String senderIP) {
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
		this.senderIP = senderIP;
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


	public String getSenderIP() {
		return senderIP;
	}

	public void setSenderIP(String senderIP) {
		this.senderIP = senderIP;
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
