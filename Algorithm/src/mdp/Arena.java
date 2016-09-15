package mdp;

public class Arena {
	
	private static final int ARENA_LENGTH = 15;
	private static final int ARENA_HEIGHT = 20;
	
	private static final int UP = 0;
	private static final int RIGHT = 90;
	private static final int DOWN = 180;
	private static final int LEFT = 270;
	
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
	
	public int getRepeatedVisitedGrid() {
		int counter = 0;
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				if (grid[i][j].isVisitedTwice()) {
					counter++;
				}
			}
		}
		return counter;
	}
	
	public boolean isObstacleGrid(int x, int y) {
		Grid grid = getGrid(x, y);
		if (grid.isObstacle()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isVisitedGrid(int x, int y) {
		Grid grid = getGrid(x, y);
		if (grid.isVisitedOnce() || grid.isVisitedTwice()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setGridAsVisited(int x, int y) {
		Grid grid = getGrid(x, y);
		grid.markAsVisited();
	}
	
	public void setGridAsSafe(int x, int y) {
		Grid grid = getGrid(x, y);
		grid.markAsSafe();
	}
	
	public void setGridAsObstacle(int x, int y) {
		Grid grid = getGrid(x, y);
		grid.markAsObstacle();
	}
	
	public int getNumberOfVisit(int x, int y) {
		Grid grid = getGrid(x, y);
		if (grid.isVisitedOnce()) {
			return 1;
		} else if (grid.isVisitedTwice()) {
			return 2;
		} else {
			return 0;
		}
	}
	
	public int distanceToObstacle(int x, int y, int direction) {
		//x, y is the coordinate of the robot
		//direction is the direction of the robot
		if (direction % 90 != 0) { 
			return -1; //invalid direction
		}
		if (isWall(x, y)) {
			return -1; //invalid location
		}
		int counter = 0;
		switch (direction) {
			case 0: //UP
				for (int i = y + 1; i < ARENA_HEIGHT; i++) {
					if (this.getGrid(x, i).isObstacle()) {
						return counter;
					} else {
						counter++;
					}
				}
				return counter;
			case 90: //RIGHT
				for (int i = x + 1; i < ARENA_LENGTH; i++) {
					if (this.getGrid(i, y).isObstacle()) {
						return counter;
					} else {
						counter++;
					}
				}
				return counter;
			case 180: //DOWN
				for (int i = y - 1; i >= 0; i--) {
					if (this.getGrid(x, i).isObstacle()) {
						return counter;
					} else {
						counter++;
					}
				}
				return counter;
			case 270: //LEFT
				for (int i = x - 1; i >= 0; i--) {
					if (this.getGrid(i, y).isObstacle()) {
						return counter;
					} else {
						counter++;
					}
				}
				return counter;
			default:
				return -1;
		}
	}
	
	
	public boolean isWall(int x, int y) {
		//x, y is the coordinated of selected grid
		try {
			this.getGrid(x, y);
		} catch (IndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}
	
	public boolean isWall(int x, int y, int direction, int distance) {
		//x and y is the coordinate of the robot
		//direction is the direction of the robot
		//distance is the distance of the grid from the front side of the robot
		int xTarget = 0;
		int yTarget = 0;
		switch (direction) {
			case UP:
				xTarget = x + 1 + distance;
				yTarget = y;
				break;
			case RIGHT:
				xTarget = x;
				yTarget = yTarget + 1 + distance;
				break;
			case DOWN:
				xTarget = x - 1 - distance;
				yTarget = y;
				break;
			case LEFT:
				xTarget = x;
				yTarget = yTarget - 1 - distance;
				break;
		}
		try {
			this.getGrid(xTarget, yTarget);
		} catch (IndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}
	
	

}
