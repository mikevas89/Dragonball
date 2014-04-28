package units;


import game.BattleField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import structInfo.UnitType;


/**
 * Base class for all players whom can 
 * participate in the DAS game. All properties
 * of the units (hitpoints, attackpoints) are
 * initialized in this class.
 *  
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class Unit implements Serializable {


	private static final long serialVersionUID = 1L;
	
	public BattleField battlefield;

	private static final Unit NULL = null;

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;
	
	private int serverOwnerID;

	
	// Map messages from their ids
//	private Map<Integer, Message> messageList;
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;
	
	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */

	
	/**
	 * Create a new unit and specify the 
	 * number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints. 
	 * 
	 * @param maxHealth is the maximum health of 
	 * this specific unit.
	 */
	public Unit(int maxHealth, int attackPoints) {

	//	messageList = new HashMap<Integer, Message>();

		// Initialize the max health and health
		hitPoints = setMaxHitPoints(maxHealth);

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		setUnitID(BattleField.getBattleField().getNewUnitID());

	}
	
	public Unit(int maxHealth, int attackPoints, BattleField battlefield) {

		//	messageList = new HashMap<Integer, Message>();

			// Initialize the max health and health
			hitPoints = setMaxHitPoints(maxHealth);

			// Initialize the attack points
			this.attackPoints = attackPoints;
			
			this.battlefield=battlefield;

			// Get a new unit id
			setUnitID(BattleField.getBattleField().getNewUnitID());
		
		}
	
  
	public boolean spawnOnBattlefield(int x, int y)
	{
		
		if(battlefield.spawnUnit(this, x, y))
			return true;
		else
			return false;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 * @param x x-coordinate of the spawn location
	 * @param y y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
		
	
	protected Unit getUnit(int x, int y)
	{

		int id = localMessageCounter++;
		
		return this.battlefield.getUnit(x,y);
		
	}

		
	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect() {
		running = false;
	}

	


	public int getMaxHitPoints() {
		return maxHitPoints;
	}



	public int setMaxHitPoints(int maxHitPoints) {
		this.maxHitPoints = maxHitPoints;
		return maxHitPoints;
	}
	
	
	public int getHitPoints() {
		return hitPoints;
	}



	public int getUnitID() {
		return unitID;
	}



	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}
	
	public int getX() {
		return x;
	}

	
	public int getY() {
		return y;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getAttackPoints() {
		return attackPoints;
	}
	
	public synchronized void adjustHitPoints(int modifier) {
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

	}
	
	public UnitType getType(int x, int y) {
		
		if (getUnit(x, y) instanceof Player)
			return UnitType.player;
		else if (getUnit(x, y) instanceof Dragon)
			return UnitType.dragon;
		else 
			return UnitType.undefined;
	}

	public int getServerOwnerID() {
		return serverOwnerID;
	}

	public void setServerOwnerID(int serverOwnerID) {
		this.serverOwnerID = serverOwnerID;
	}
	
	
	
	

}
