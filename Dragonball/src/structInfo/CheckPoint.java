package structInfo;

import game.BattleField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import units.Unit;

public class CheckPoint {

	/* The array of units */
	private Unit[][] map;
	private List <Unit> units;
	private ArrayList<LogInfo> checkPointValidActions; //checkpoint ValidActions

	public CheckPoint(int width, int height){
		map = new Unit[width][height];
		units = Collections.synchronizedList(new ArrayList<Unit>());
		checkPointValidActions = new ArrayList<LogInfo>();   // list of valid actions for checkPoint
	}
	
	
	public void captureCheckPoint(BattleField battlefield,ArrayList<LogInfo> validActions){
		this.map = battlefield.getMap().clone();
		this.units = Collections.synchronizedList(battlefield.getUnits());
		checkPointValidActions = new ArrayList<LogInfo>(validActions);
	}
	
	
	public List <Unit> getUnits() {
		return units;
	}

	public void setUnits(List <Unit> units) {
		this.units = units;
	}

	public Unit[][] getMap() {
		return map;
	}

	public void setMap(Unit[][] map) {
		this.map = map;
	}

	public ArrayList<LogInfo> getCheckPointValidActions() {
		return checkPointValidActions;
	}


	public void setCheckPointValidActions(ArrayList<LogInfo> checkPointValidActions) {
		this.checkPointValidActions = checkPointValidActions;
	} 
	
	
}
