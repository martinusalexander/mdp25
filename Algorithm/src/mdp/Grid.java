package mdp;

public class Grid implements Comparable<Grid>, Updatable {

	private int xCoordinate;
	private int yCoordinate;
	
	private boolean isVisitedOnce;
	private boolean isVisitedTwice;
	private boolean isObstacle;
	private boolean isSafe;
	
	//FOR A* ALGO
	private int pathCost = Integer.MAX_VALUE;
	private int heuristic;
	private int[] previousGrid;
	private boolean pathCostUpdated = false;
	private boolean isExpanded = false;
	private int direction;
	
	public Grid(int x, int y) {
		this.xCoordinate = x;
		this.yCoordinate = y;
		this.isVisitedOnce = false;
		
		this.isObstacle = false;
		this.isSafe = false;
	}
	
	public Grid() {
		this.xCoordinate = 0;
		this.yCoordinate = 0;
		this.isVisitedOnce = false;
		this.isVisitedTwice = false;
		this.isObstacle = false;
		this.isSafe = false;
	}
	
	public String getCoordinate() {
		String location = xCoordinate + ", " + yCoordinate;
		return location;
	}
	
	public int[] getXY() {
		int[] location = new int[2];
		location[0] = xCoordinate;
		location[1] = yCoordinate;
		return location;
	}
	
	public boolean isVisited() {
		return (isVisitedOnce || isVisitedTwice);
	}
	
	public boolean isVisitedOnce() {
		return isVisitedOnce;
	}
	
	public boolean isVisitedTwice() {
		return isVisitedTwice;
	}
	
	public boolean isSafe() {
		return isSafe;
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean isWalkable() {
		return !isObstacle && (isVisitedOnce || isVisitedTwice);
	}
	
	public void setCoordinate(int x, int y) {
		this.xCoordinate = x;
		this.yCoordinate = y;
	}
	
	public void markAsVisited() {
		if(isVisitedOnce) {
			this.isVisitedTwice = true;
		} else {
			this.isVisitedOnce = true;
		}
	}
	
	public void markAsVisitedOnce() {
		this.isVisitedOnce = true;
	}
	
	public void markAsVisitedTwice() {
		this.isVisitedTwice = true;
	}
	
	public void markAsObstacle() {
		this.isObstacle = true;
	}
	
	public void markAsSafe() {
		this.isSafe = true;
	}
	
	public void markAsNotVisitedOnce() {
		this.isVisitedOnce = false;
	}
	
	public void markAsNotVisitedTwice() {
		this.isVisitedTwice = false;
	}
	
	public void markAsNotObstacle() {
		this.isObstacle = false;
	}
	
	public void markAsNotSafe() {
		this.isSafe = false;
	}
	
	public int getHeuristic() {
		return this.heuristic;
	}
	
	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}
	
	public void setPathCost(int pathCost) {
		this.pathCost = pathCost;
	}
	
	public int getPathCost() {
		return this.pathCost;
	}
	
	public int getTotalCost() {
		return this.pathCost + this.heuristic;
	}

	@Override
	public int compareTo(Grid g) {
		if(this.getTotalCost() > g.getTotalCost())
            return 1;
        else if(this.getTotalCost() < g.getTotalCost())
            return -1;
        else
            return 0;
	}
	
	public int[] getPreviousGrid() {
		return this.previousGrid;
	}
	
	public void setPreviousGrid(int[] previousGrid) {
		this.previousGrid = previousGrid;
	}
	
	@Override
	public boolean needUpdate() {
		return pathCostUpdated;
	}

	public void setPathCostUpdated(boolean update) {
		this.pathCostUpdated = update;
	}
	
	public boolean isExpanded() {
		return this.isExpanded;
	}
	
	public void setExpanded(boolean expanded) {
		this.isExpanded = expanded;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}


}