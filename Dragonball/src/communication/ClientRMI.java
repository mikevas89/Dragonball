package communication;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.ClientServer;

import messages.Message;
import messages.ClientServerMessage;

import Node.Client;




//this is the implementation of RMI for the client side
public class ClientRMI extends UnicastRemoteObject implements ClientServer { 
	
	
	private static final long serialVersionUID = 1L;


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
		
		//issued time of Message in the Client
		message.setTimeIssuedFromServer(System.nanoTime());

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
				case ServerClientPing:
					clientOwner.onServerClientPingMessageReceived(csMessage);
				case UnSubscribeFromServer:
					clientOwner.onUnSubscribeFromServerMessageReceived(csMessage);
					break;
				case RedirectConnection:
					clientOwner.onRedirectServerMessageReceived(csMessage);
					break;
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
