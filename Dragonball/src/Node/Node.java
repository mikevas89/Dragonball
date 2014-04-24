package Node;

public class Node implements java.io.Serializable{
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String Name;
	private String IP;
	
	public Node(){
		
	}
	public Node(String Name, String IP){
		this.setName(Name);
		this.setIP(IP);
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}
	@Override
    public boolean equals(Object info) {
		if(this==info)
			return true;
		if(info==null)
			return false;
		if(getClass()!=info.getClass())
			return false;
		
		Node nodeInfo = (Node)info;
		if(!this.Name.equals(nodeInfo.getName()) || !this.IP.equals(nodeInfo.IP))
			return false;
		return true;
    }
	
	@Override
    public int hashCode(){
		return this.Name.hashCode() + this.IP.hashCode();
				 
	}

}
