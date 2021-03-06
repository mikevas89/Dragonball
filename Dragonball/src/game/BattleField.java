package game;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import Node.Server;



import units.Dragon;
import units.Player;
import units.Unit;

/**
 * The actual battlefield where the fighting takes place.
 * It consists of an array of a certain width and height.
 * 
 * It is a singleton, which can be requested by the 
 * getBattleField() method. A unit can be put onto the
 * battlefield by using the putUnit() method.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class BattleField  implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	/* The array of units */
	private Unit[][] map;

	/* The static singleton */
	private static BattleField battlefield;
	
	
	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private List <Unit> units; 

	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 */
	public BattleField(int width, int height) {
		
		
		synchronized (this) {
			map = new Unit[width][height];
			units = Collections.synchronizedList(new ArrayList<Unit>());
		}
		
	}

	/**
	 * Singleton method which returns the sole 
	 * instance of the battlefield.
	 * 
	 * @return the battlefield.
	 */
	public static BattleField getBattleField() {
		if (battlefield == null){
			System.err.println("A new Battlefield was created");
			battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
		}
		return battlefield;
	}
	
	/**
	 * Puts a new unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * In addition, the unit is also put in the list of known units.
	 * 
	 * @param unit is the actual unit being spawned 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	public boolean spawnUnit(Unit unit, int x, int y)
	{
		synchronized (this) {
			if (map[x][y] != null)
				return false;
	
			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);

		return true;
	}

	/**
	 * Put a unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * 
	 * @param unit is the actual unit being put 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	private synchronized boolean putUnit(Unit unit, int x, int y)
	{
		if (map[x][y] != null)
			return false;

		map[x][y] = unit;
		unit.setPosition(x, y);

		return true;
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x position.
	 * @param y position.
	 * @return the unit at the specified position, or return
	 * null if there is no unit at that specific position.
	 */
	public Unit getUnit(int x, int y)
	{
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;

		return map[x][y];
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit is the unit being moved.
	 * @param deltax is the delta in the x position.
	 * @param deltay is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	public synchronized boolean moveUnit(Unit unit, int newX, int newY)
	{
		int originalX = unit.getX();
		int originalY = unit.getY();

		if (unit.getHitPoints() <= 0)
			return false;

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[originalX][originalY] = null;
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from the server.
	 * 
	 * @param x position.
	 * @param y position.
	 */
	public synchronized void removeUnit(int x, int y)
	{
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		units.remove(unitToRemove);
	}
	
	public synchronized void removeUnit(int x, int y,ListIterator<Unit> it)
	{
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		it.remove();
	}


	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	/**
	 * Close down the battlefield. Unregisters
	 * the serverSocket so the program can 
	 * actually end.
	 */ 
	public synchronized void shutdown() {
		// Remove all units from the battlefield and make them disconnect from the server
		for (Unit unit : units) {
			unit.disconnect();
		}
	}
	
	public void setMap(int x, int y, Unit value)
	{
		System.out.println("dfsfs");
		this.map[x][y]= value;
	}
	
	public void printUnitSize(){
		
		System.out.println("Battlefield - Num Units "+ this.units.size());
		for(int i = 0; i < BattleField.MAP_WIDTH; i++)
			for(int j = 0; j < BattleField.MAP_HEIGHT; j++) {
				if(this.map[i][j] == null) continue;
				System.out.println("Unit x:"+ this.getUnit(i, j).getX() + " y: "+ this.map[i][j].getY() );
			}
	}
	
	public void copyMap(Unit[][] messageUnits){
		this.map = messageUnits.clone();
	}

	public void copyListUnits(List<Unit> list){
		this.units= Collections.synchronizedList(list);
	}
	
	public Unit[][] getMap(){
		return this.map;
	}
	
	public List<Unit> getUnits(){
		return this.units;
	}
	
	public void healDamage(int x, int y, int healPoints)
	{
		BattleField.getBattleField().getUnit(x, y).adjustHitPoints(healPoints);
	}
	
	public void dealDamage(int x, int y, int damage) 
	{
		Unit unit;
		unit = BattleField.getBattleField().getUnit(x, y);
		if (unit != null)
			unit.adjustHitPoints( -(Integer)damage );
	}
	
	public Unit getUnitByUnitID(int UnitID)
	{
		for(Unit temp : this.getUnits())
		{
			if(temp.getUnitID()==UnitID)
			{
				return temp;
			}
		}
		return null;
		
	}
	
	public void copyBattleField(BattleField parameterBattleField){
		
		battlefield.copyListUnits(parameterBattleField.getUnits());
		battlefield.copyMap(parameterBattleField.getMap());
	}
	
	public void clearBattleField(){
		battlefield.units.clear();
		battlefield.copyMap(new Unit[MAP_WIDTH][MAP_HEIGHT]);
	}
	
	
	
}
