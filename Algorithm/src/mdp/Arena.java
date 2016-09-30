package mdp;

import java.math.BigInteger;
import java.util.Scanner;

public class Arena {
	
	public static final int ARENA_LENGTH = 15;
	public static final int ARENA_HEIGHT = 20;
	
	public static final int UP = 0;
	public static final int RIGHT = 90;
	public static final int DOWN = 180;
	public static final int LEFT = 270;
	
	private Grid[][] grid;
	
	public Arena() {
		this.grid = new Grid[ARENA_LENGTH][ARENA_HEIGHT];
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				grid[i][j] = new Grid(i, j);
			}
		}
		//By default, each grid is not visited, not safe, and not obstacle
	}
	
	public Grid getGrid(int x, int y) {
		try {
			Grid selectedGrid = grid[x][y];
			return selectedGrid;
		} catch (IndexOutOfBoundsException e) {
			//System.out.println(x + " " + y);
			//System.out.println("Index");
			return null;
		} catch (NullPointerException e) {
			//System.out.println("Null");
			return null;
		}
	}
	
	public String getArenaDataPart1() {
		//Initial padding
		String dataBinary = "11";
		for (int j = 0; j < ARENA_HEIGHT; j++) {
			for (int i = 0; i < ARENA_LENGTH; i++) {
				if (this.getGrid(i, j).isVisited()) {
					dataBinary += "1";
				} else {
					dataBinary += "0";
				}
			}
		}
		//Final padding
		dataBinary += "11";
		//Reference: http://stackoverflow.com/a/16918459
		BigInteger bigInt = new BigInteger(dataBinary, 2);
		String encodedData = bigInt.toString(16);
		return encodedData;
	}
	
	public String getArenaDataPart2() {
		String dataBinary = "";
		for (int j = 0; j < ARENA_HEIGHT; j++) {
			for (int i = 0; i < ARENA_LENGTH; i++) {
				if (this.getGrid(i, j).isVisited()) {
					if (this.getGrid(i, j).isObstacle()) {
						dataBinary += "1";
					} else {
						dataBinary += "0";
					}
				} 
				
			}
		}
		while (dataBinary.length() % 8 != 0)  {
			dataBinary += "0";
		}
		for (int i = 0; i < 8; i++) {
			dataBinary = "1" + dataBinary;
		}
		//Reference: http://stackoverflow.com/a/16918459
		BigInteger bigInt = new BigInteger(dataBinary, 2);
		String encodedData = bigInt.toString(16);
		encodedData = encodedData.substring(2);
		return encodedData;
		
	}
	
	public String getArenaData1ForAndroid() {
		String dataBinary = "";
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				if (this.getGrid(i, j).isObstacle()) {
					dataBinary += "1";
				} else {
					dataBinary += "0";
				}
			}
		}
		dataBinary = "1000" + dataBinary;
		//Reference: http://stackoverflow.com/a/16918459
		BigInteger bigInt = new BigInteger(dataBinary, 2);
		String encodedData = bigInt.toString(16);
		encodedData = encodedData.substring(1);
		return encodedData;
	}
	
	public String getArenaData2ForAndroid() {
		String dataBinary = "";
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				if (this.getGrid(i, j).isVisited()) {
					dataBinary += "1";
				} else {
					dataBinary += "0";
				}
			}
		}
		dataBinary = "1000" + dataBinary;
		//Reference: http://stackoverflow.com/a/16918459
		BigInteger bigInt = new BigInteger(dataBinary, 2);
		String encodedData = bigInt.toString(16);
		encodedData = encodedData.substring(1);
		return encodedData;
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
		if (isWall(x, y)) {
			return true;
		}
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
	
	public boolean isWalkableGrid(int x, int y) {
		if (isWall(x, y)) {
			//System.out.println(x + " " + y);
			//System.out.println("Wall");
			return false;
		} else if (isObstacleGrid(x, y)) {
			//System.out.println("Obstacle");
			return false;
		}
		return true;
	}
	
	public void setGridAsVisited(int x, int y) {
		Grid grid = getGrid(x, y);
		if (grid != null) {
			grid.markAsVisited();
		}
	}
	
	public void setGridAsObstacle(int x, int y) {
		Grid grid = getGrid(x, y);
		if (grid != null) {
			grid.markAsObstacle();
		}
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
	
	public void findSafeGrid() {
		for (int i = 0; i < ARENA_LENGTH; i++) {
			for (int j = 0; j < ARENA_HEIGHT; j++) {
				Grid grid = getGrid(i, j);
				//Grids near wall are not safe
				if (i == 0 || i == ARENA_LENGTH - 1 || j == 0 || j == ARENA_HEIGHT - 1) {
					grid.markAsNotSafe();
				} else 
					if (this.isWalkableGrid(i - 1, j - 1) &&
							this.isWalkableGrid(i - 1, j) &&
							this.isWalkableGrid(i - 1, j + 1) &&
							this.isWalkableGrid(i, j - 1) &&
							this.isWalkableGrid(i, j) &&
							this.isWalkableGrid(i , j + 1) &&
							this.isWalkableGrid(i + 1, j - 1) &&
							this.isWalkableGrid(i + 1, j) &&
							this.isWalkableGrid(i + 1, j + 1))	{
						grid.markAsSafe();
				}
			}
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
		Grid grid = this.getGrid(x, y);
		if (grid == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isWall(int x, int y, int direction, int distance) {
		//x and y is the coordinate of the robot
		//direction is the direction of the robot
		//distance is the distance of the grid from the front side of the robot
		int xTarget = 0;
		int yTarget = 0;
		switch (direction) {
			case UP:
				xTarget = x;
				yTarget = y + 1 + distance;
				break;
			case RIGHT:
				xTarget = x + 1 + distance;
				yTarget = y;
				break;
			case DOWN:
				xTarget = x;
				yTarget = y - 1 - distance;
				break;
			case LEFT:
				xTarget = x - 1 - distance;
				yTarget = y;
				break;
		}
		Grid grid = this.getGrid(xTarget, yTarget);
		if (grid == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public int[][] getSurrondedGrid(int x, int y) {
		int n = 0;
		int[][] result = new int[9][2];
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				result[n][0] = x + i;
				result[n][1] = y + j;
				n++;
			}
		}
		return result;
	}
	
	public int[][] getReachableGridIndices(int xLocation, int yLocation) throws ArrayIndexOutOfBoundsException{
        int[][] reachableNodes = new int[4][2];
        reachableNodes[0] = new int[]{xLocation-1, yLocation};
        reachableNodes[1] = new int[]{xLocation, yLocation+1};
        reachableNodes[2] = new int[]{xLocation+1, yLocation};
        reachableNodes[3] = new int[]{xLocation, yLocation-1};
        return reachableNodes;
    }
	
	

}
