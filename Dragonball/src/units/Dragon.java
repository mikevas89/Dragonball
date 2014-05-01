package units;

import game.BattleField;
import game.GameState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;



import structInfo.Constants;
import structInfo.Directions;
import structInfo.UnitType;

/**
 * A dragon is a non-playing character, which can't
 * move, has a hitpoint range between 50 and 100
 * and an attack range between 5 and 20.
 * 
 * Every dragon runs in its own thread, simulating
 * individual behaviour, not unlike a distributed
 * server setup.
 *   
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class Dragon extends Unit implements Serializable {
	/* Reaction speed of the dragon
	 * This is the time needed for the dragon to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns; 
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	// The minimum and maximum amount of hitpoints that a particular dragon starts with
	public static final int MIN_HITPOINTS = 200;
	public static final int MAX_HITPOINTS = 400;
	// The minimum and maximum amount of hitpoints that a particular dragon has
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 1;
	
	/**
	 * Spawn a new dragon, initialize the 
	 * reaction speed 
	 *
	 */
	public Dragon(int x, int y,BattleField battlefield,int serverID) {
		/* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS),battlefield);

		//serverID+unitID
		this.setUnitID(Integer.parseInt(String.valueOf(serverID)+String.valueOf(this.getUnitID())));
		this.setServerOwnerID(serverID);
		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		if (!spawnOnBattlefield(x,y))
			return;
		/* Awaken the dragon */
		//new Thread(this).start();
	}

		
	
	

}
