package Node;

import interfaces.ClientServer;
import interfaces.ServerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Iterator;

import structInfo.ServerInfo;



import messages.ClientServerMessage;
import messages.MessageType;
import messages.ServerServerMessage;

public class PingMonitorSender implements Runnable{
	
	public enum DecisionType {
		Agree,
		Disagree,
		Undefined
	}

	private Node referencedNode; //client if MessageType ==ServerClientPing
								//server to ProblematicServer if MessageType ==CheckIfAlive
	private MessageType messageTypeToSend;
	
	private DecisionType nodeDesicion;
	
	
	public PingMonitorSender(Node referencedNode, MessageType messageType,DecisionType decisionType){
		this.setReferencedNode(referencedNode);
		this.setMessageTypeToSend(messageType);	
		this.setNodeDesicion(decisionType);
	}

	
	@Override
	public void run() {
		switch(this.getMessageTypeToSend()){
		case ServerClientPing:
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
		break;
		case ProblematicServer:
			for(Iterator<ServerInfo> it = Server.getServerList().values().iterator();it.hasNext();){
				ServerInfo entry = it.next();
				Node server = new Node(entry.getName(), entry.getIP());
				if(server.equals(this.referencedNode))
					continue;
				
				if(entry.isProblematicServer() || !entry.isAlive())
					continue;
				
				//With this message, Server sends the ProblematicServer that the other Server should agree to remove
				 ServerServerMessage sendProblematicServer = new ServerServerMessage(
														MessageType.ProblematicServer,
														Server.getMyInfo().getName(),
														Server.getMyInfo().getIP(),
														server.getName(),
														server.getIP());
				 //put problematic server info to message
				 sendProblematicServer.setProblematicServerToCheck(this.getReferencedNode());
				 
				 //sending the ProblematicServer message to subscribed client
				 ServerServer serverRMI=null;
				 serverRMI = Server.getServerReg(server);
				 
				 if(serverRMI == null) return;
				
				 try {
					 serverRMI.onMessageReceived(sendProblematicServer);
				 } catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				 }
				
				 System.out.println("Server: ProblematicServer sent to Server"+ server.getName()+ "serverIP: "+ server.getIP());
			}
			break;
		case ResponseProblematicServer:
			for(Iterator<ServerInfo> it = Server.getServerList().values().iterator();it.hasNext();){
				ServerInfo entry = it.next();
				Node server = new Node(entry.getName(), entry.getIP());
				if(server.equals(this.referencedNode))
					continue;
				if(entry.isProblematicServer() || !entry.isAlive())
					continue;
				
				//With this message, Server sends the ProblematicServer that the other Server should agree to remove
				 ServerServerMessage sendProblematicServer = new ServerServerMessage(
														MessageType.ResponseProblematicServer,
														Server.getMyInfo().getName(),
														Server.getMyInfo().getIP(),
														server.getName(),
														server.getIP());
				 //put problematic server decision info to message
				 sendProblematicServer.setProblematicServerToCheck(this.getReferencedNode());
				 sendProblematicServer.getContent().put("Decision", this.getNodeDesicion());
				 
				 //sending the ProblematicServer message to subscribed client
				 ServerServer serverRMI=null;
				 serverRMI = Server.getServerReg(server);
				 
				 if(serverRMI == null) return;
				
				 try {
					 serverRMI.onMessageReceived(sendProblematicServer);
				 } catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				 }
				
				 System.out.println("Server: ResponseProblematicServer sent to Server"+ server.getName()+ "serverIP: "+ server.getIP());
			}
			break;
		default:
			break;
		
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


	public String getNodeDesicion() {
		return nodeDesicion.toString();
	}


	public void setNodeDesicion(DecisionType nodeDesicion) {
		this.nodeDesicion = nodeDesicion;
	}




	
	
}
