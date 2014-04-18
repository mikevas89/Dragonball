package messages;

import java.util.HashMap;

public class Message implements java.io.Serializable{
	

	private static final long serialVersionUID = 1L;

	private MessageType messageTypeRequest; 
	private String sender;
	private String senderIP;
	private String receiver;
	private String receiverIP;
	private static int messageId=0; //increments at every instantiation
	
	private HashMap<String,String> content;

	public Message() {
		messageId++;
		content= new HashMap<String,String>();
	}
	
	public Message (MessageType messageRequest, String sender,String senderIP, String receiver,String receiverIP) {
		messageId++;
		content= new HashMap<String,String>();
		this.setMessageTypeRequest(messageRequest);
		this.setSender(sender);
		this.setSenderIP(senderIP);
		this.setReceiver(receiver);
		this.setReceiverIP(receiverIP);
		
	}
	
	/*----------------------------------------------------
				GETTERS AND SETTERS
	----------------------------------------------------		
	 */	
	
	public MessageType getMessageTypeRequest() {
		return messageTypeRequest;
	}

	public void setMessageTypeRequest(MessageType messageRequest) {
		this.messageTypeRequest = messageRequest;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getSenderIP() {
		return senderIP;
	}

	public void setSenderIP(String senderIP) {
		this.senderIP = senderIP;
	}
	

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getReceiverIP() {
		return receiverIP;
	}

	public void setReceiverIP(String receiverIP) {
		this.receiverIP = receiverIP;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		Message.messageId = messageId;
	}

	public HashMap<String,String> getContent() {
		return content;
	}

	public void setContent(String key,String value) {
		content.put(key,value);
	}


	
	

}
