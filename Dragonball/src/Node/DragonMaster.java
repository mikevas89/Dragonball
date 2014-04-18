package Node;

import game.BattleField;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import units.Dragon;

public class DragonMaster implements Runnable {
	
	BattleField battlefield;
	int dragonCount;
	
	public DragonMaster(BattleField battlefield, int dragonCount) {
		this.battlefield=battlefield;
		this.dragonCount=dragonCount;
	}

	@Override
	public void run() {
		Executor ex= Executors.newFixedThreadPool(dragonCount);
		CompletionService<String> cs = new ExecutorCompletionService<String>(ex);
		for(int i = 0; i < dragonCount; i++) {
			int x, y, attempt = 0;
			do {
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new dragon
			if (battlefield.getUnit(x, y) != null) break;
			
			final int finalX = x;
			final int finalY = y;
			Dragon dragon =new Dragon(finalX, finalY,battlefield);
			cs.submit(dragon);
			
		}
		String terminationResult=null;
		for(int i=0;i<dragonCount;i++){
			
			try {
				terminationResult = cs.take().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			//utilize the result 
			//System.out.println(terminationResult);
			String[] tokens = terminationResult.split(" ");
			battlefield.removeUnit(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
		}
		
	}
	

}
