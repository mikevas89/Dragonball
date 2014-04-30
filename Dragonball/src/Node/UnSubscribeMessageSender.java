package Node;

import interfaces.ClientServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import messages.ClientServerMessage;
import messages.MessageType;

import structInfo.ClientPlayerInfo;
import structInfo.LogInfo;
import structInfo.LogInfo.Action;
import units.Dragon;
import units.Player;
import units.Unit;

public class UnSubscribeMessageSender implements Runnable{
	
	private Unit unit;
	private Map<String,LogInfo> pendingActions;//it is needed to delete pending actions of the Unsubscribed
	
	public UnSubscribeMessageSender(Map<String,LogInfo> pendingActions,Unit unit) {
		this.setPendingActions(pendingActions);
		this.setUnit(unit);
	}

	@Override
	public void run() {
		Node client=null;
		//remove pending Actions concerning this unit
		for(Iterator<Entry<String, LogInfo>> it= this.getPendingActions().entrySet().iterator();it.hasNext();){
			Map.Entry<String, LogInfo> entry = it.next();
			if(entry.getValue().getSenderUnitID()==unit.getUnitID() ||
					entry.getValue().getTargetUnitID()==unit.getUnitID()){
				System.err.println("UnSubscribe: Remove pending Actions(Good thing):"+ entry.getValue().toString());
				it.remove();
			}
		}
		//remove Pending to Valid Actions from Queue
		for(Iterator<LogInfo> it= Server.getValidActions().iterator();it.hasNext();){
			LogInfo entry = it.next();
			if((entry.getSenderUnitID()==unit.getUnitID() ||
					entry.getTargetUnitID()==unit.getUnitID()) && !entry.getAction().equals(Action.Removed)){
				System.err.println("UnSubscribe: Remove ToValid Actions(Good thing):"+ entry.toString());
				it.remove();
			}
		}
		
		//Unsubscribe Dragon if he is dead
		if(this.getUnit() instanceof Dragon){
			 ListIterator<Unit> it =Server.getBattlefield().getUnits().listIterator();
			 		while(it.hasNext()){ 
			 			Unit unit= it.next();
			 			if (unit instanceof Player) continue;
			 			Server.getBattlefield().removeUnit(unit.getX(), unit.getY(), it);
			 		}
			 		return;
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
		
		
		ClientServerMessage sendUnSubscribed = new ClientServerMessage(
				MessageType.UnSubscribeFromServer,
				Server.getMyInfo().getName(),
				Server.getMyInfo().getIP(),
				client.getName(),
				client.getIP());
		sendUnSubscribed.setBattlefield(Server.getBattlefield());
		
		ClientServer clientCommunication=null;
		
		clientCommunication= Server.getClientReg(client);
	
	
	//clientIp and clientName
	System.out.println("Getting Registry from "+ client.getName()+" for Unsubscribe");
		

		if(clientCommunication==null)
			return;
		
		try {
			clientCommunication.onMessageReceived(sendUnSubscribed);
		} catch (RemoteException e) {
			//e.printStackTrace();
		} catch (NotBoundException e) {
			//e.printStackTrace();
		}
		
		//TODO check if that client was the last one
		Server.getMyInfo().setNumClients(Server.getMyInfo().getNumClients()-1);
		if(Server.getMyInfo().getNumClients()>0)
			return;
		
		//if clients of the server are equal to zero
		if(!Server.getMyInfo().isRunsDragons()){
			Server.getMyInfo().setRunsGame(false);
			Server.getPendingActions().clear();
			Server.getValidBlockQueue().clear();
		}
		else{ //server runs dragons and so still plays the game
			Server.getPendingActions().clear();
		}
		
		
	}

	
	
	/*----------------------------------------------------
		GETTERS AND SETTERS
	----------------------------------------------------		
	 */

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Map<String, LogInfo> getPendingActions() {
		return pendingActions;
	}

	public void setPendingActions(Map<String, LogInfo> pendingActions) {
		this.pendingActions = pendingActions;
	}
	
	

}
