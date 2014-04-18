package communication;

import interfaces.ClientServer;




import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Node.Node;
import communication.ClientPlayerInfo;

import Node.Node;
import Node.Server;

import units.Unit;




public class Server2ClientCommunication extends UnicastRemoteObject implements ClientServer { //implements 
																						//Server2Server

	private static final long serialVersionUID = 1L;

	private Server serverOwner; //server instance of this communication
	

	public Server2ClientCommunication(Server server) throws RemoteException {
		this.serverOwner=server;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException, NotBoundException {
		//if the message is not a proper ClientServerMessage, it is discarded
		if(!(message instanceof ClientServerMessage)) return;
		ClientServerMessage clientServerMessage= (ClientServerMessage) message;
		
		switch(message.getMessageTypeRequest()){
		case ClientServerPing : 
			onClientServerPingMessageReceived(clientServerMessage);
			break;
		case Subscribe2Server :
			onSubscribe2ServerMessageReceived(clientServerMessage);
			break;
		case UnSubscribeFromServer :
			onUnSubscribeFromServerMessageReceived(clientServerMessage);
			break;
		case MoveUnit : 
			onMoveUnitMessageReceived(clientServerMessage);
			break;
		case GetBattlefield :
			try {
				onGetBattlefieldMessageReceived(clientServerMessage);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	
	
	/*---------------------------------------------------
	 * ESTABLISH SERVER RMI REGISTRY
	 ----------------------------------------------------		
	*/
	
	public void createServerReg() throws AccessException, RemoteException, AlreadyBoundException{  //server creates its Registry entry
		
		Registry serverRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
		serverRegistry.bind(serverOwner.getName(), this ); //server's name
		System.out.println(serverOwner.getName()+ " is up and running!");
	}
	
	
	
	public ClientServer getClientReg(Node client) throws MalformedURLException, RemoteException, NotBoundException
	{
		ClientServer clientCommunication = (ClientServer) 
		Naming.lookup("rmi://"+client.getIP()
				+"/"+client.getName());		//getClientInfo returns from ClientList
																//clientIp and clientName
		System.out.println(client.getName());
		return clientCommunication;
	}
	
	/*---------------------------------------------------
	 * SERVER TO CLIENT METHODS 
	 ----------------------------------------------------		
	*/
	
	//All these messages are being sent from the backend implementation
	//1. OK message for the subscription of the client as player, MasterPlayer will send this
	//2. send Battlefield after a request of the client- it is implemented in onGetBattlefieldMessageReceived
	//3. send Battlefield after an update of the game , server will send this
	//4. the message for the redirection of the client to this server ,server will send this
	
	
	/*---------------------------------------------------
	 * CLIENT TO SERVER METHODS 
	 ----------------------------------------------------		
	*/
	
	private void onClientServerPingMessageReceived(Message message) {
		Node client=new Node(message.getSender(),message.getSenderIP());
		ClientPlayerInfo result = this.serverOwner.getClientList().get(client);
		if(result==null) return; //client is not player in my database
		result.setTimeLastPingSent(System.currentTimeMillis());
		this.serverOwner.getClientList().remove(client);
		this.serverOwner.getClientList().put(client, result);
	}
	
	public void onSubscribe2ServerMessageReceived(Message message){
		Node client=new Node(message.getSender(),message.getSenderIP());
		if(this.serverOwner.getClientList().containsKey(client)){
			System.err.println("Client: "+ message.getSender() +" tries to resubscribe");
			return;
		}
		//TODO put "Make new Player" to the Queue sharing with MasterPlayer so the Master Player can see it
		//TODO the OK for the subscription should be sent from the MasterPlayer
		/*	With this message, MasterPlayer sends the OK subscription to the client
		 * ClientServerMessage Subscribe2Server = new ClientServerMessage(
												MessageType.Subscribe2Server,
												this.serverOwner.getName(),
												this.serverOwner.getIP(),
												client.getName());
		//put it to the clientList
		ClientPlayerInfo result = this.serverOwner.getClientList().get(client);
		
		*/
	}
	
	private void onUnSubscribeFromServerMessageReceived(ClientServerMessage message) {
		Node client=new Node(message.getSender(),message.getSenderIP());
		if(!this.serverOwner.getClientList().containsKey(client)){
			System.err.println("Unknown Client: "+ message.getSender() +" tries to unsubscribe");
			return;
		}
		//TODO put "Remove new Player" to the Queue sharing with MP so the Master Player can see it
	}
	
	private void onMoveUnitMessageReceived(ClientServerMessage message) {
		Node client=new Node(message.getSender(),message.getSenderIP());
		ClientPlayerInfo player= this.serverOwner.getClientList().get((client));
		if(player==null){
			System.err.println("Unknown Client: "+ message.getSender() +" tries to moveUnit");
			return;
		}	
		//TODO put "Move Unit Player" with id <- player.getUnitID() to the Queue sharing with Server
	}
	

	private void onGetBattlefieldMessageReceived(ClientServerMessage message) throws RemoteException, 
																NotBoundException, MalformedURLException {
		Node client=new Node(message.getSender(),message.getSenderIP());
		ClientPlayerInfo player= this.serverOwner.getClientList().get((client));
		if(player==null){
			System.err.println("Unknown Client: "+ message.getSender() +" tries to get Battlefield");
			return;
		}
		ClientServerMessage sendBattleFieldMessage = new ClientServerMessage(
					MessageType.GetBattlefield,
					this.serverOwner.getName(),
					this.serverOwner.getIP(),
					client.getName());
		sendBattleFieldMessage.setBattlefield(Server.getBattlefield());
		//send the BattleField to the client
		ClientServer clientComm= this.getClientReg(new Node(player.getName(),player.getIP()));
		clientComm.onMessageReceived(sendBattleFieldMessage);	
	}

	
	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	----------------------------------------------------		
	 */
	
	private ClientPlayerInfo getClientInfo(Node node){ 

		
		return null;	
	}
	
	private void setClientInfo(ClientPlayerInfo clienInfo){
		
	}
	
	
}
