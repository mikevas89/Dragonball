package communication;

import interfaces.ServerServer;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import structInfo.ClientPlayerInfo;
import structInfo.ServerInfo;
import messages.Message;
import messages.ServerServerMessage;
import Node.Node;
import Node.Server;

public class Server2ServerRMI extends UnicastRemoteObject implements
		ServerServer { // implements
	// Server2Server

	private static final long serialVersionUID = 1L;

	private Server serverOwner; // server instance of this communication

	public Server2ServerRMI(Server server) throws RemoteException {
		this.setServerOwner(server);
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException,
			NotBoundException {
		// if the message is not a proper ClientServerMessage, it is discarded
		if (!(message instanceof ServerServerMessage))
			return;

		// issued time of Message in the Server
		message.setTimeIssuedFromServer(System.currentTimeMillis());
		ServerServerMessage serverServerMessage = (ServerServerMessage) message;

		System.out.println("Server: Received Message");
		// TODO: spawn a thread to handle the message

		switch (message.getMessageTypeRequest()) {
		case ServerServerPing:
			onServerServerPingMessageReceived(serverServerMessage);
			break;
		case CheckPending:
			onCheckPendingMessageReceived(serverServerMessage);
			break;
		case DeleteFromValid:
			onDeleteFromValidMessageReceived(serverServerMessage);
			break;
		case Subscribe2Server:
			onSubscribe2ServerMessageReceived(serverServerMessage);
			break;
		case GetBattlefield:
			onGetBattlefieldMessageReceived(serverServerMessage);
			break;
		default:
			break;
		}
	}

	private void onGetBattlefieldMessageReceived(
			ServerServerMessage serverServerMessage) {
	}

	private void onSubscribe2ServerMessageReceived(
			ServerServerMessage serverServerMessage) {
	}

	private void onDeleteFromValidMessageReceived(
			ServerServerMessage serverServerMessage) {
	}

	private void onCheckPendingMessageReceived(
			ServerServerMessage serverServerMessage) {
	}

	private void onServerServerPingMessageReceived(ServerServerMessage message) {
		System.out.println("onServerServerPingMessageReceived");
		Node server = new Node(message.getSender(), message.getSenderIP());
		ServerInfo result = Server.getServerList().get(server);
		if (result == null)
			return; // client is not player in my database
		result.setTimeLastPingSent(System.currentTimeMillis());
		Server.getServerList().remove(server);
		Server.getServerList().put(server, result);
	}

	public Server getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}

}
