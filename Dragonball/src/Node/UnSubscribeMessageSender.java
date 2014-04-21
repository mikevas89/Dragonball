package Node;

import interfaces.ClientServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import messages.ClientServerMessage;
import messages.MessageType;

import structInfo.ClientPlayerInfo;
import structInfo.LogInfo;
import units.Unit;

public class UnSubscribeMessageSender implements Runnable{
	
	private Unit unit;
	private Server serverSender;
	
	public UnSubscribeMessageSender(Server server,Unit unit) {
		this.setServerSender(server);
		this.setUnit(unit);
	}

	@Override
	public void run() {
		Node client=null;
		//remove pending Actions concerning this unit
		for(Iterator<Entry<String, LogInfo>> it= this.getServerSender().getPendingActions().entrySet().iterator();it.hasNext();){
			Map.Entry<String, LogInfo> entry = it.next();
			if(entry.getValue().getSenderUnitID()==unit.getUnitID() ||
					entry.getValue().getTargetUnitID()==unit.getUnitID()){
				System.err.println("UnSubscribe: Remove pending Actions(Good thing):"+ entry.getValue().toString());
				it.remove();
			}
		}
		
		//remove player from battlefield
		//remove player from clientList
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
		
		ClientServer clientCommunication=null;
		try {
			 clientCommunication = (ClientServer) 
					Naming.lookup("rmi://"+client.getIP()
							+"/"+client.getName());
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
																		//clientIp and clientName
		System.out.println("Getting Registry from "+ client.getName()+" for Unsubscribe");
		
		
		ClientServerMessage sendUnSubscribed = new ClientServerMessage(
				MessageType.UnSubscribeFromServer,
				this.getServerSender().getName(),
				this.getServerSender().getIP(),
				client.getName(),
				client.getIP());
		sendUnSubscribed.setBattlefield(Server.getBattlefield());
		
		try {
			clientCommunication.onMessageReceived(sendUnSubscribed);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
		
	}

	public Server getServerSender() {
		return serverSender;
	}

	public void setServerSender(Server serverSender) {
		this.serverSender = serverSender;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	
	

}
