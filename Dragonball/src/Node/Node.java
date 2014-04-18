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
	
    public boolean equals(Node info) {

        return (this.Name.equals(info.Name) && this.IP.equals(info.IP));
    }

}
