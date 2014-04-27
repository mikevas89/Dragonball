package Node;

import interfaces.ClientServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Iterator;

import structInfo.ClientPlayerInfo;
import messages.ClientServerMessage;
import messages.MessageType;

public class PingMonitorSender implements Runnable{

	private Node referencedNode; //client if MessageType ==ServerClientPing
								//server to CheckIfAlive if MessageType ==CheckIfAlive
	private MessageType messageTypeToSend;
	
	
	public PingMonitorSender(Node referencedNode, MessageType messageType){
		this.setReferencedNode(referencedNode);
		this.setMessageTypeToSend(messageType);		
	}

	
	@Override
	public void run() {
		if(this.getMessageTypeToSend().equals(MessageType.ServerClientPing)){
			for(Enumeration<Node> it=Server.getClientList().keys();it.hasMoreElements();){
				Node clientToSend = it.nextElement();
				if(!clientToSend.equals(this.getReferencedNode()))
						continue;
				
				ClientServer clientCommunication=null;
				clientCommunication = Server.getClientReg(clientToSend);
				if(clientCommunication==null)
					return;
				
				System.out.println("Getting Registry from "+ clientToSend.getName()+" for ServerClientPing");
				
				
				ClientServerMessage sendServerClientPing = new ClientServerMessage(
						MessageType.ServerClientPing,
						Server.getMyInfo().getName(),
						Server.getMyInfo().getIP(),
						clientToSend.getName(),
						clientToSend.getIP());
				
				System.out.println("PingMonitorSender sends a message to "+ clientToSend.getName());
				
				try {
					clientCommunication.onMessageReceived(sendServerClientPing);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
				
			}	
		}
		else if(this.getMessageTypeToSend().equals(MessageType.CheckIfAlive)){
			//TODO: server broadcasts to remaining servers the checkiIfAlive Message
		}
		
	}
	
	
	
	
	
	/*----------------------------------------------------
		GETTERS AND SETTERS
	----------------------------------------------------		
	 */
	
	public Node getReferencedNode() {
		return referencedNode;
	}


	public void setReferencedNode(Node referencedNode) {
		this.referencedNode = referencedNode;
	}

	
	
	public MessageType getMessageTypeToSend() {
		return messageTypeToSend;
	}


	public void setMessageTypeToSend(MessageType messageTypeToSend) {
		this.messageTypeToSend = messageTypeToSend;
	}




	
	
}
