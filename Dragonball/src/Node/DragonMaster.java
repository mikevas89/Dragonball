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
	private int dragonCount;

	public DragonMaster(BattleField battlefield,int dragonCount) {
		this.battlefield = battlefield;
		this.dragonCount = dragonCount;

		if (Server.isStartDragons()) {
			for (int i = 0; i < this.dragonCount; i++) {
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
				Dragon dragon = new Dragon(finalX, finalY, battlefield,Server.getMyInfo().getServerID());
			}
		}

	}

	@Override
	public void run() {

		while (!Server.isRunDragons()) {
			// sleep if other server sends the moves of Dragons
			try {
				Thread.sleep(Constants.SERVER2SERVER_TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//update info for the Ping Messages
		Server.getMyInfo().setRunsDragons(true);

		// this server makes the moves
		int turn = 0;
		while (Server.isRunDragons()) {
			// turn++;
			if (turn == 5) {
				Server.setRunDragons(false);
				break;
			}

			// Sleep while the dragon is considering its next move
			try {
				Thread.sleep(Constants.DRAGON_PERIOD_ACTION);
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
					Server.getValidBlockQueue().put(actionDragon);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	}



}
