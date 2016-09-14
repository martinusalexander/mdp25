package mdp;

public class Grid {

	private int xCoordinate;
	private int yCoordinate;
	
	private boolean isVisitedOnce;
	private boolean isVisitedTwice;
	private boolean isObstacle;
	private boolean isSafe;
	
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
	
	public void setCoordinate(int x, int y) {
		this.xCoordinate = x;
		this.yCoordinate = y;
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

}