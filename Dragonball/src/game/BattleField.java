package game;

import java.util.ArrayList;



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
	private ArrayList <Unit> units; 

	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 */
	private BattleField(int width, int height) {
		
		
		synchronized (this) {
			map = new Unit[width][height];
			units = new ArrayList<Unit>();
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
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	/*public void onMessageReceived(Message msg) {
		Message reply = null;
		String origin = (String)msg.get("origin");
		MessageRequest request = (MessageRequest)msg.get("request");
		Unit unit;
		switch(request)
		{
			case spawnUnit:
				this.spawnUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
			case putUnit:
				this.putUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
			case getUnit:
			{
				reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				 Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));
				break;
			}
			case getType:
			{
				reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				 Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 
				reply.put("id", msg.get("id"));
				if (getUnit(x, y) instanceof Player)
					reply.put("type", UnitType.player);
				else if (getUnit(x, y) instanceof Dragon)
					reply.put("type", UnitType.dragon);
				else reply.put("type", UnitType.undefined);
				break;
			}
			case dealDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					unit.adjustHitPoints( -(Integer)msg.get("damage") );
				 Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 
				break;
			}
			case healDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					unit.adjustHitPoints( (Integer)msg.get("healed") );
				 Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 
				break;
			}
			case moveUnit:
				reply = new Message();
				this.moveUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				 Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 
				reply.put("id", msg.get("id"));
				break;
			case removeUnit:
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				return;
		}

		try {
			if (reply != null)
				serverSocket.sendMessage(reply, origin);
		}
		catch(IDNotAssignedException idnae)  {
			// Could happen if the target already logged out
		}
	}*/

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
	public void copyListUnits(ArrayList<Unit> listUnits){
		this.units= (ArrayList<Unit>) listUnits.clone();
	}
	
	public Unit[][] getMap(){
		return this.map;
	}
	
	public ArrayList<Unit> getUnits(){
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
	
}
