package Node;

import interfaces.ClientServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import messages.ClientServerMessage;
import messages.MessageType;

import structInfo.Constants;

public class BattlefieldSender implements Runnable, java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Server serverOwner;

	public BattlefieldSender(Server server){
		this.setServerOwner(server);
	}
	
	
	@Override
	public void run() {
		
		//broadcasts the new battlefield to ALL clients
		while(!Server.killServer){
			try {
				Thread.sleep(Constants.BROADCAST_PERIOD_TO_CLIENTS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//send to EACH client
			for(Node client : Server.getClientList().keySet()){
				//get client's RMI instance
				ClientServer clientComm = null;
				clientComm = Server.getClientReg(client); 
				//create Message
				ClientServerMessage sendBattlefieldMessage = new ClientServerMessage(
							MessageType.GetBattlefield,
							this.serverOwner.getName(),
							this.serverOwner.getIP(),
							client.getName(),
							client.getIP());
				sendBattlefieldMessage.setBattlefield(Server.getBattlefield());
				
				if(clientComm==null){
					System.out.println("clientComm is null");
					continue;
				}
				
				System.out.println("Server: Battlefield sent");

				try {
					clientComm.onMessageReceived(sendBattlefieldMessage);
				} catch (RemoteException e) {
					//e.printStackTrace();
				} catch (NotBoundException e) {
					//e.printStackTrace();
				}


			}
			
		}
	}
	

	public Server getServerOwner() {
		return serverOwner;
	}


	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}
	
	

}
