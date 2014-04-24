package units;

import game.BattleField;

import game.GameState;

import java.io.Serializable;

/**
 * A Player is, as the name implies, a playing 
 * character. It can move in the four wind directions,
 * has a hitpoint range between 10 and 20 
 * and an attack range between 1 and 10.
 * 
 * Every player runs in its own thread, simulating
 * individual behaviour, not unlike a distributed
 * server setup.
 *   
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class Player extends Unit implements Serializable {
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	/**
	 * Create a player, initialize both 
	 * the hit and the attackpoints. 
	 */
	
	
	public Player(int x, int y,BattleField battlefield,int serverID) {
		/* Initialize the hitpoints and attackpoints */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), 
				(int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS),
				battlefield);
		//serverID+unitID
		this.setUnitID(Integer.parseInt(String.valueOf(serverID)+String.valueOf(this.getUnitID())));
		System.out.println("New Player : UnitID: " + this.getUnitID() );

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		if (!spawnOnBattlefield(x, y))
			return; // We could not spawn on the battlefield
	}

}
