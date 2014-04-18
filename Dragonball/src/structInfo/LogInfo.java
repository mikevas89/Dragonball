package structInfo;

public class LogInfo {
	
	
	private String action;
	private String x;
	private String y;
	private Long timestamp;
	
	public LogInfo(String senderIP, String action, String x, String y, Long time)
	{
		this.senderIP=senderIP;
		this.action=action;
		this.x=x;
		this.y=y;
		this.timestamp=time;
	}
	
	
	private String senderIP;
	public String getSenderIP() {
		return senderIP;
	}

	public void setSenderIP(String senderIP) {
		this.senderIP = senderIP;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	
	
	
}
