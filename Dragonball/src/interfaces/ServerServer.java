package interfaces;

import java.rmi.NotBoundException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import messages.Message;

public interface ServerServer extends Remote{
	
	public void onMessageReceived(Message message) throws RemoteException, NotBoundException; //different implementation for Server/Server
	
}

