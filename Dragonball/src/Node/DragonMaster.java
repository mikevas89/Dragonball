package Node;

import game.BattleField;
import game.GameState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import structInfo.Constants;
import structInfo.Directions;
import structInfo.LogInfo;
import structInfo.UnitType;
import structInfo.LogInfo.Action;
import units.Dragon;
import units.Player;
import units.Unit;

public class DragonMaster implements Runnable {

	private BattleField battlefield;
	private Server serverOwner;
	private int dragonCount;
	private volatile boolean runDragons; // server that moves the dragons
	private volatile boolean createDragons; // server creates dragons

	public DragonMaster(Server serverOwner, BattleField battlefield,
			int dragonCount, boolean runDragons, boolean createDragons) {
		this.setServerOwner(serverOwner);
		this.battlefield = battlefield;
		this.dragonCount = dragonCount;
		this.setRunDragons(runDragons);
		this.setCreateDragons(createDragons);

		if (this.createDragons) {
			for (int i = 0; i < dragonCount; i++) {
				int x, y, attempt = 0;
				do {
					x = (int) (Math.random() * BattleField.MAP_WIDTH);
					y = (int) (Math.random() * BattleField.MAP_HEIGHT);
					attempt++;
				} while (battlefield.getUnit(x, y) != null && attempt < 10);

				// If we didn't find an empty spot, we won't add a new dragon
				if (battlefield.getUnit(x, y) != null)
					break;

				final int finalX = x;
				final int finalY = y;
				Dragon dragon = new Dragon(finalX, finalY, battlefield);
			}
		}

	}

	@Override
	public void run() {

		while (!runDragons) {
			// sleep if other server sends the moves of Dragons
			try {
				Thread.sleep(Constants.SERVER2SERVER_TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// this server makes the moves
		int turn = 0;
		while (runDragons) {
			// turn++;
			if (turn == 5) {
				runDragons = false;
				break;
			}

			// Sleep while the dragon is considering its next move
			try {
				Thread.sleep(Constants.CLIENT_PERIOD_ACTION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// TODO: make moves for Dragons, remove dead
			for (Unit unit : this.battlefield.getUnits()) {
				if (unit instanceof Player)
					continue;
				ArrayList<Directions> adjacentPlayers = new ArrayList<Directions>();

				// Stop if the dragon runs out of hitpoints
				if (unit.getHitPoints() <= 0) {
					this.battlefield.removeUnit(unit.getX(), unit.getY());
					continue;
				}

				// Decide what players are near
				if (unit.getY() > 0)
					if (unit.getType(unit.getX(), unit.getY() - 1) == UnitType.player)
						adjacentPlayers.add(Directions.up);
				if (unit.getY() < BattleField.MAP_WIDTH - 1)
					if (unit.getType(unit.getX(), unit.getY() + 1) == UnitType.player)
						adjacentPlayers.add(Directions.down);
				if (unit.getX() > 0)
					if (unit.getType(unit.getX() - 1, unit.getY()) == UnitType.player)
						adjacentPlayers.add(Directions.left);
				if (unit.getX() < BattleField.MAP_WIDTH - 1)
					if (unit.getType(unit.getX() + 1, unit.getY()) == UnitType.player)
						adjacentPlayers.add(Directions.right);

				// Pick a random player to attack
				if (adjacentPlayers.size() == 0)
					continue; // There are no players to attack
				Directions playerToAttack = adjacentPlayers.get((int) (Math
						.random() * adjacentPlayers.size()));

				Unit targetUnit = null;

				// Attack the player
				switch (playerToAttack) {
				case up:
					targetUnit = this.battlefield.getUnit(unit.getX(),
							unit.getY() - 1);
					this.battlefield.dealDamage(unit.getX(), unit.getY() - 1,
							unit.getAttackPoints());
					break;
				case right:
					targetUnit = this.battlefield.getUnit(unit.getX() + 1,
							unit.getY());
					this.battlefield.dealDamage(unit.getX() + 1, unit.getY(),
							unit.getAttackPoints());
					break;
				case down:
					targetUnit = this.battlefield.getUnit(unit.getX(),
							unit.getY() + 1);
					this.battlefield.dealDamage(unit.getX(), unit.getY() + 1,
							unit.getAttackPoints());
					break;
				case left:
					targetUnit = this.battlefield.getUnit(unit.getX() - 1,
							unit.getY());
					this.battlefield.dealDamage(unit.getX() - 1, unit.getY(),
							unit.getAttackPoints());
					break;
				}
				// new Move for the Dragon - going to ValidMonitor
				LogInfo actionDragon = new LogInfo(
						Action.Damage,
						unit.getUnitID(),
						unit.getX(),
						unit.getY(),
						UnitType.dragon,
						targetUnit.getUnitID(),
						targetUnit.getX(),
						targetUnit.getY(),
						targetUnit.getType(targetUnit.getX(), targetUnit.getY()),
						System.currentTimeMillis(), Server.getMyInfo()
								.getName());
				// put action to Queue
				try {
					this.serverOwner.getValidBlockQueue().put(actionDragon);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		/*
		 * if(!this.runDragons){ ListIterator<Unit> it =
		 * this.battlefield.getUnits().listIterator(); while(it.hasNext()){ Unit
		 * unit= it.next(); if (unit instanceof Player) continue;
		 * this.battlefield.removeUnit(unit.getX(), unit.getY(), it); }
		 */
	}

	// TODO:stopGame();

	public boolean isRunDragons() {
		return runDragons;
	}

	public void setRunDragons(boolean runDragons) {
		this.runDragons = runDragons;
	}

	public boolean isCreateDragons() {
		return createDragons;
	}

	public void setCreateDragons(boolean createDragons) {
		this.createDragons = createDragons;
	}

	public Server getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(Server serverOwner) {
		this.serverOwner = serverOwner;
	}

}
