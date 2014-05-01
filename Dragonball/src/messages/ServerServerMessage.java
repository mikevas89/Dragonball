package messages;

import game.BattleField;


import java.io.Serializable;
import java.util.ArrayList;

import Node.Node;

import structInfo.LogInfo;
import units.Unit;

public class ServerServerMessage extends Message implements Serializable{
	

	
	private static final long serialVersionUID = 1L;
	
	private BattleField battlefield;
	private LogInfo actionToBeChecked;
	private int numClients;
	private boolean senderRunsGame;
	private boolean senderRunsDragons;
	private Node problematicServerToCheck;
	private ArrayList<LogInfo> validActionsLog;
	
	
	public ServerServerMessage(){
		super();
		this.battlefield=BattleField.getBattleField();
		this.validActionsLog = new ArrayList<LogInfo>();
	}
	
	public ServerServerMessage(MessageType messageRequest, String sender,
			String senderIP, String receiver, String receiverIP) {
		
		super(messageRequest, sender, senderIP, receiver, receiverIP);
		this.battlefield=BattleField.getBattleField();
		this.validActionsLog = new ArrayList<LogInfo>();
		System.out.println("NMSS "+ System.nanoTime());
	}
	
	

	

	public BattleField getBattlefield() {
		return battlefield;
	}
	public void setBattlefield(BattleField battlefield) {
		if (this.battlefield==null) System.err.println("MY BattleFiled is NULL");
		if(battlefield==null) System.err.println("BattleFiled is NULL");
		this.battlefield.copyBattleField(battlefield);
	}
	public LogInfo getActionToBeChecked() {
		return actionToBeChecked;
	}
	public void setActionToBeChecked(LogInfo actionToBeChecked) {
		this.actionToBeChecked = actionToBeChecked;
	}

	public int getNumClients() {
		return numClients;
	}

	public void setNumClients(int numClients) {
		this.numClients = numClients;
	}

	public boolean isSenderRunsGame() {
		return senderRunsGame;
	}

	public void setSenderRunsGame(boolean senderRunsGame) {
		this.senderRunsGame = senderRunsGame;
	}

	public Node getProblematicServerToCheck() {
		return problematicServerToCheck;
	}

	public void setProblematicServerToCheck(Node problematicServerToCheck) {
		this.problematicServerToCheck = problematicServerToCheck;
	}

	public boolean isSenderRunsDragons() {
		return senderRunsDragons;
	}

	public void setSenderRunsDragons(boolean senderRunsDragons) {
		this.senderRunsDragons = senderRunsDragons;
	}

	public ArrayList<LogInfo> getValidActionsLog() {
		return validActionsLog;
	}

	public void setValidActionsLog(ArrayList<LogInfo> ActionsLog) {
		this.validActionsLog = new ArrayList<LogInfo>(ActionsLog);
	}





}
