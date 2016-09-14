package mdp;

public class Arena {
	
	private static final int ARENA_LENGTH = 15;
	private static final int ARENA_HEIGHT = 20;
	
	private Grid[][] grid;
	
	public Arena() {
		grid = new Grid[ARENA_LENGTH][ARENA_HEIGHT];
		//By default, each grid is not visited, not safe, and not obstacle
	}
	
	public Grid getGrid(int x, int y) {
		try {
			Grid selectedGrid = grid[x][y];
			return selectedGrid;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public int getExploredGrid() {
		int counter = 0;
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				if (grid[i][j].isVisitedOnce() || grid[i][j].isVisitedTwice()) {
					counter++;
				}
			}
		}
		return counter;
	}
	

}
