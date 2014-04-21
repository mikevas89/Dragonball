package Node;

import java.util.Iterator;

import structInfo.ClientPlayerInfo;
import structInfo.LogInfo;
import units.Unit;

public class MessageSender implements Runnable{
	
	private Unit unit;
	
	public MessageSender(Unit unit) {
		this.unit = unit;
	}

	@Override
	public void run() {
		Node client=null;
		//for(ClientPlayerInfo info: Server.getClientList().values()){
		Iterator<Node> iter = Server.getClientList().keySet().iterator();
		while(iter.hasNext()) {
			
			Node key = (Node)iter.next();
		    ClientPlayerInfo info = (ClientPlayerInfo)Server.getClientList().get(key);
			if(info.getUnitID()==unit.getUnitID()){
				client= new Node(info.getName(),info.getIP());
				Server.getBattlefield().removeUnit(unit.getX(), unit.getY());
				iter.remove();
				break;
			}
		}
		//TODO: send Unsubscribe
		//Node client = new 
		
	}
	
	

}
