package messages;

import game.BattleField;

import java.io.Serializable;

import structInfo.LogInfo;
import units.Unit;

public class ServerServerMessage extends Message implements Serializable{
	

	
	private static final long serialVersionUID = 1L;
	
	private Unit messageUnit;
	private BattleField battlefield;
	private LogInfo actionToBeChecked;
	
	
	public ServerServerMessage(){
		super();
	}
	
	public ServerServerMessage(MessageType messageRequest, String sender,
			String senderIP, String receiver, String receiverIP) {
		
		super(messageRequest, sender, senderIP, receiver, receiverIP);
	}
	
	

	
	public Unit getMessageUnit() {
		return messageUnit;
	}
	public void setMessageUnit(Unit messageUnit) {
		this.messageUnit = messageUnit;
	}
	public BattleField getBattlefield() {
		return battlefield;
	}
	public void setBattlefield(BattleField battlefield) {
		this.battlefield = battlefield;
	}
	public LogInfo getActionToBeChecked() {
		return actionToBeChecked;
	}
	public void setActionToBeChecked(LogInfo actionToBeChecked) {
		this.actionToBeChecked = actionToBeChecked;
	}





}
