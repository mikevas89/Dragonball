package communication;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.ClientServer;

import messages.Message;
import messages.ClientServerMessage;

import Node.Client;




//this is the implementation of RMI for the client side
public class ClientRMI extends UnicastRemoteObject implements ClientServer { 
	
	
	private static final long serialVersionUID = 1L;

	
	//TODO: List of all connected clients to serverNode: private clientList
	
	//private static List<List<String>>clientList;
	//TODO: serverId	

	private static Client clientOwner;


	public ClientRMI(Client client) throws RemoteException {
		super();
		clientOwner=client;
	}

	@Override
	public void onMessageReceived(final Message message) {
		// if the message is not a proper ClientServerMessage, it is discarded
		if (!(message instanceof ClientServerMessage))
			return;

		Thread handlerMessage = new Thread(new Runnable() {

			ClientServerMessage csMessage = (ClientServerMessage) message;

			@Override
			public void run() {
				switch (message.getMessageTypeRequest()) {
				case Subscribe2Server:
					clientOwner.onSubscribeMessageReceived(csMessage);
					break;
				case GetBattlefield:
					clientOwner.onBattleFieldMessageReceived(csMessage);
					break;
				case RedirectConnection:
					try {
						clientOwner.onRedirectServerMessageReceived(csMessage);
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						e.printStackTrace();
					}
				default:
					break;
				}
			}
		});
		handlerMessage.start();

	}

	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	 ----------------------------------------------------		
	*/

	
	
/*	public List<String> getClientInfo(int clientIndex) {
		return Arrays.asList(clientList.get(clientIndex).get(0),clientList.get(clientIndex).get(1));
	}

	public void addToClientList(List<String> newclientInfo) {
		clientList.add(newclientInfo);
	}
	*/
	
	
	
	
}
