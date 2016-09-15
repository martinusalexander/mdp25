package mdp;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Robot {
	
	//MODE
	private static final int IDLE_MODE = 900;
	private static final int EXPLORE_MODE = 901;
	private static final int FASTEST_MODE = 902;
	
	//DIRECTION
	public static final int HEADING_UP = 0;
	public static final int HEADING_RIGHT = 90;
	public static final int HEADING_DOWN = 180;
	public static final int HEADING_LEFT = 270;
	
	//LIST OF ACTION
	public static final int MOVE_FORWARD = 800;
	public static final int MOVE_BACKWARD = 801;
	public static final int TURN_LEFT = 802;
	public static final int TURN_RIGHT = 803;
	public static final int TURN_180 = 804;
	public static final int CHECK_OBSTACLE_IN_FRONT = 805;
	public static final int CHECK_OBSTACLE_ON_LEFT = 806;
	public static final int CHECK_OBSTACLE_ON_RIGHT = 807;
	public static final int CHECK_OBSTACLE_ON_SIDE = 808;
	public static final int EXPLORE = 809;
	public static final int FASTEST_RUN = 810;
	
	//INITIAL POSITION AND DIRECTION
	private static final int DESIRED_INITIAL_X_LOCATION = 1;
	private static final int DESIRED_INITIAL_Y_LOCATION = 1;
	private static final int DESIRED_INITIAL_DIRECTION = HEADING_LEFT;
	
	private int xLocation;
	private int yLocation;
	private int direction;
	private boolean isCalibrated;
	private int mode;
	private int previousMovement;
	private List<Grid> explorationPath;
	//private List<Integer> explorationCommandList;
	//private List<Grid> fastestRunPath;
	private List<Integer> fastestRunCommand;
	
	private boolean fastestPathFound;
	
	//Helper
	private Arena arena;
	
	public Robot() {
		this.xLocation = 1;
		this.yLocation = 1;
		this.direction = HEADING_LEFT;
		this.isCalibrated = false;
		this.mode = IDLE_MODE;
		this.arena = App.arena;
		this.previousMovement = 0;
		this.explorationPath = new LinkedList<>();
		this.fastestRunCommand = new LinkedList<>();
		this.fastestPathFound = false;
	}
	
	public Robot(int startingXLocation, int startingYLocation, int direction) {
		this.xLocation = startingXLocation;
		this.yLocation = startingYLocation;
		this.direction = direction;
		this.isCalibrated = false;
		this.mode = IDLE_MODE;
		this.arena = App.arena;
		this.previousMovement = 0;
		this.explorationPath = new LinkedList<>();
		this.fastestRunCommand = new LinkedList<>();
		this.fastestPathFound = false;
	}
	
	private void explore() {
		this.mode = EXPLORE_MODE;
		do {
			calibrateInitialPosition();
		} while (!isCalibrated);
		//Code here
		int repeatedGrid = 0;
		//int exploredArea = 0;
		//int exploredPercentage = 0;
		int counter = 0;
		while (repeatedGrid <= 20) {
			counter++;
			System.out.println("Exploring step: " + counter);
			
			//Update arena data in simulator and Android
			String arenaData = encodeArenaData();
			//TODO (send data to Android and simulator)
			System.out.println(arenaData);
			
			//Decide appropriate movement
			int nextCommand = getAppropriateMovement();
			
			//Execute the appropriate command
			this.command(nextCommand);
			this.previousMovement = nextCommand;
			
			//Update path taken
			Grid grid = arena.getGrid(xLocation, yLocation);
			explorationPath.add(grid);
			
			repeatedGrid = arena.getRepeatedVisitedGrid();
			
		}
		
		fastestPathFound = this.getFastestPath();
		
		
		//Finishing
		this.mode = IDLE_MODE;
		
	}
	
	private void fastestRun() {
		this.mode = FASTEST_MODE;
		do {
			calibrateInitialPosition();
		} while (!isCalibrated);
		
		//Code here
		if (fastestPathFound) {
			for (Integer command : fastestRunCommand) {
				this.command(command);
			}
		} else {
			System.out.println("Fastest path not found.");
		}
		
		//Finishing
		this.mode = IDLE_MODE;
	}
	
	private void calibrateInitialPosition() {
		//TODO (command Arduino)
		isCalibrated = true;
	}

//Not useful
/* 
	private void correctRobotPosition() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Re-enter X location: ");
		this.xLocation = scanner.nextInt();
		System.out.print("Re-enter Y location: ");
		this.yLocation = scanner.nextInt();
		System.out.println("Select direction:");
		System.out.println("1. Heading up");
		System.out.println("2. Heading down");
		System.out.println("3. Heading left");
		System.out.println("4. Heading right");
		System.out.print("Re-enter direction: ");
		int userInput = 0;
		do{
			userInput = scanner.nextInt();
			switch (userInput) {
				case 1: direction = HEADING_UP; break;
				case 2: direction = HEADING_DOWN; break;
				case 3: direction = HEADING_LEFT; break;
				case 4: direction = HEADING_RIGHT; break;
				default: System.out.print("Re-enter direction: "); break;
			}
		} while (!(1 <= userInput && userInput <= 4));
	}
*/
	
	private String encodeArenaData() {
		//TODO (coordinate with Android team)
		return "";
	}
	
	private int getAppropriateMovement() {
		int command = 0;
		switch (direction) {
			case HEADING_UP:
				if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.getGrid(xLocation - 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation - 2, yLocation).isWalkable() && arena.getGrid(xLocation - 2, yLocation + 1).isWalkable() && previousMovement != TURN_LEFT) { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.getGrid(xLocation - 1, yLocation + 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation + 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation + 2).isWalkable()) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.getGrid(xLocation + 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation + 2, yLocation).isWalkable() && arena.getGrid(xLocation + 2, yLocation + 1).isWalkable()) {
					command = TURN_RIGHT;
				} else {
					command = TURN_LEFT;
				}
				break;
			case HEADING_DOWN:
				if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.getGrid(xLocation + 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation + 2, yLocation).isWalkable() && arena.getGrid(xLocation + 2, yLocation + 1).isWalkable() && previousMovement != TURN_LEFT) { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.getGrid(xLocation - 1, yLocation - 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation - 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation - 2).isWalkable()) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.getGrid(xLocation - 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation - 2, yLocation).isWalkable() && arena.getGrid(xLocation - 2, yLocation + 1).isWalkable()) {
					command = TURN_RIGHT;
				} else {
					command = TURN_LEFT;
				}
				break;
			case HEADING_LEFT:
				if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.getGrid(xLocation - 1, yLocation - 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation - 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation - 2).isWalkable() && previousMovement != TURN_LEFT)  { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.getGrid(xLocation - 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation - 2, yLocation).isWalkable() && arena.getGrid(xLocation - 2, yLocation + 1).isWalkable()) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.getGrid(xLocation - 1, yLocation + 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation + 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation + 2).isWalkable()) {
					command = TURN_RIGHT;
				} else {
					command = TURN_LEFT;
				}
				break;
			case HEADING_RIGHT:
				if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.getGrid(xLocation - 1, yLocation + 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation + 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation + 2).isWalkable() && previousMovement != TURN_LEFT) {
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.getGrid(xLocation + 2, yLocation - 1).isWalkable() &&
						arena.getGrid(xLocation + 2, yLocation).isWalkable() && arena.getGrid(xLocation + 2, yLocation + 1).isWalkable()) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.getGrid(xLocation - 1, yLocation - 2).isWalkable() &&
						arena.getGrid(xLocation, yLocation - 2).isWalkable() && arena.getGrid(xLocation + 1, yLocation - 2).isWalkable()) {
					command = TURN_RIGHT;
				} else {
					command = TURN_LEFT;
				}
		}
		return command;
	}
	
	private boolean getFastestPath() {
		//TODO: Append each action to fastestPathCommand
		return true;
	}
	
	private void command(int commandType) {
		switch (commandType) {
			case EXPLORE: explore(); break;
			case FASTEST_RUN: fastestRun(); break;
			case TURN_LEFT: turnLeft(); break;
			case TURN_RIGHT: turnRight(); break;
			case MOVE_FORWARD:  moveForward(10); break;
			case MOVE_BACKWARD: moveBackward(10); break;
			case TURN_180: turnLeft(); turnLeft(); break;
			case CHECK_OBSTACLE_IN_FRONT: checkObstacleInFront(); break;
			case CHECK_OBSTACLE_ON_LEFT: checkObstacleOnLeft(); break;
			case CHECK_OBSTACLE_ON_RIGHT: checkObstacleOnRight(); break;
			case CHECK_OBSTACLE_ON_SIDE: checkObstacleOnSide(); break;
			default: break;
		}
	}

	//Later we see if this code is usable
/*	
	private void command(int commandType, int data1) {
		switch (commandType) {
			//data1 is distance
			case MOVE_FORWARD:  moveForward(data1); break;
			case MOVE_BACKWARD: moveBackward(data1); break;
			default: break;
		}
	}
	
	private void command (int commandType, int data1, int data2) {
		switch (commandType) {
		//data1 is x coordinate
		//data2 is y coordinate
		//case MOVE_FORWARD:  moveForward(data1); break;
		//case MOVE_BACKWARD: moveBackward(data1); break;
		default: break;
	}
	}
*/
	private void turnLeft() {
		System.out.println("Turning left.");
		this.direction -= 90;
		this.direction %= 360;
		//TODO (command Arduino)
	}
	
	private void turnRight() {
		System.out.println("Turning right.");
		this.direction += 90;
		this.direction %= 360;
		//TODO (command Arduino)
	}
	
	private void moveForward(int distance) {
		if (mode == EXPLORE_MODE) {
			System.out.println("Moving forward for " + distance + " cm and sense.");
			int frontLeftReading = 0;
			int frontMiddleReading = 0;
			int frontRightReading = 0;
			int leftSensorReading = 0;
			int rightSensorReading = 0;
			//TODO (command Arduino) -> move forward and sense
			processDataFromFrontSensors(frontLeftReading, frontMiddleReading, frontRightReading);
			processDataFromLeftSensor(leftSensorReading);
			processDataFromRightSensor(rightSensorReading);
		} else {
			System.out.println("Moving forward for " + distance + " cm.");
			//TODO (command Arduino) -> move forward only
		}
		switch (direction) {
			case HEADING_UP: this.xLocation += distance; break;
			case HEADING_DOWN: this.xLocation -= distance; break;
			case HEADING_LEFT: this.yLocation -= distance; break;
			case HEADING_RIGHT: this.yLocation += distance; break;
		}
	}
	
	private void moveBackward(int distance) {
		System.out.println("Moving backward for " + distance + " cm.");
		//TODO (command Arduino)
		switch (direction) {
			case HEADING_UP: this.xLocation -= distance; break;
			case HEADING_DOWN: this.xLocation += distance; break;
			case HEADING_LEFT: this.yLocation += distance; break;
			case HEADING_RIGHT: this.yLocation -= distance; break;
		}
	}
	
	private void checkNearbyObstacle() {
		checkObstacleInFront();
		checkObstacleOnSide();
	}
	
	private void checkObstacleInFront() {
		System.out.println("Checking obstacle in front.");
		int leftReading = 0;
		int middleReading = 0;
		int rightReading = 0;
		//TODO (command Arduino) -> sense
		processDataFromFrontSensors(leftReading, middleReading, rightReading);
	}
	
	private void checkObstacleOnSide() {
		System.out.println("Checking obstacle on the side.");
		checkObstacleOnLeft();
		checkObstacleOnRight();
	}
	
	private void checkObstacleOnLeft() {
		System.out.println("Checking obstacle on the left.");
		int reading = 0;
		//TODO (command Arduino) -> sense
		processDataFromLeftSensor(reading);
	}
	
	private void checkObstacleOnRight() {
		System.out.println("Checking obstacle on the right.");
		int reading = 0;
		//TODO (command Arduino) -> sense
		processDataFromRightSensor(reading);
	}
	
	private void processDataFromFrontSensors(int leftReading, int middleReading, int rightReading) {
		System.out.println("Processing data from front sensors.");
		System.out.println("Left reading: " + leftReading + ", middle reading: " + middleReading + ", rightReading: " + rightReading + ".");
		final int FRONT_SENSOR_DISTANCE_FROM_EDGE = 7;
		int leftObstacleDistance = ((leftReading - FRONT_SENSOR_DISTANCE_FROM_EDGE + 5) % 10) + 1;
		int middleObstacleDistance = ((middleReading - FRONT_SENSOR_DISTANCE_FROM_EDGE + 5) % 10) + 1;
		int rightObstacleDistance = ((rightReading - FRONT_SENSOR_DISTANCE_FROM_EDGE + 5) % 10) + 1;
		
		//Cap the distance to be 3 grid (i.e. make sure the sensors are working optimal)
		leftObstacleDistance = Math.min(leftObstacleDistance, 4);
		middleObstacleDistance = Math.min(middleObstacleDistance, 4);
		rightObstacleDistance = Math.min(rightObstacleDistance, 4);
		
		int robotFrontPosition;
		switch (direction) {
			case HEADING_UP:
				robotFrontPosition = yLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(xLocation - 1, robotFrontPosition + i);
					if (i < leftObstacleDistance) {
						arena.setGridAsSafe(xLocation - 1, robotFrontPosition + i);
					} else if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition + i);
					}
					//Front middle
					arena.setGridAsVisited(xLocation, robotFrontPosition + i);
					if (i < middleObstacleDistance) {
						arena.setGridAsSafe(xLocation, robotFrontPosition + i);
					} else if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition + i);
					}
					//Front right
					arena.setGridAsVisited(xLocation + 1, robotFrontPosition + i);
					if (i < rightObstacleDistance) {
						arena.setGridAsSafe(xLocation + 1, robotFrontPosition + i);
					} else if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition + i);
					}
				}
				break;
			case HEADING_DOWN:
				robotFrontPosition = yLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(xLocation + 1, robotFrontPosition - i);
					if (i < leftObstacleDistance) {
						arena.setGridAsSafe(xLocation + 1, robotFrontPosition - i);
					} else if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition - i);
					}
					//Middle front
					arena.setGridAsVisited(xLocation, robotFrontPosition - i);
					if (i < middleObstacleDistance) {
						arena.setGridAsSafe(xLocation, robotFrontPosition - i);
					} else if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition - i);
					}
					//Front right
					arena.setGridAsVisited(xLocation - 1, robotFrontPosition - i);
					if (i < middleObstacleDistance) {
						arena.setGridAsSafe(xLocation - 1, robotFrontPosition - i);
					} else if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition - i);
					}
				}
				break;
			case HEADING_RIGHT:
				robotFrontPosition = xLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(robotFrontPosition + i, yLocation + 1);
					if (i < leftObstacleDistance) {
						arena.setGridAsSafe(robotFrontPosition + i, yLocation + 1);
					} else if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation + 1);
					}
					//Middle front
					arena.setGridAsVisited(robotFrontPosition + i, yLocation);
					if (i < middleObstacleDistance) {
						arena.setGridAsSafe(robotFrontPosition + i, yLocation);
					} else if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation);
					}
					//Front right
					arena.setGridAsSafe(robotFrontPosition + i, yLocation - 1);
					if (i < rightObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition + i, yLocation - 1);
					} else if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation - 1);
					}
				}
				break;
			case HEADING_LEFT:
				robotFrontPosition = xLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(robotFrontPosition - i, yLocation - 1);
					if (i < leftObstacleDistance) {
						arena.setGridAsSafe(robotFrontPosition - i, yLocation - 1);
					} else if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation - 1);
					}
					//Middle front
					arena.setGridAsVisited(robotFrontPosition - i, yLocation);
					if (i < middleObstacleDistance) {
						arena.setGridAsSafe(robotFrontPosition - i, yLocation);
					} else if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation);
					}
					//Front right
					arena.setGridAsVisited(robotFrontPosition - i, yLocation + 1);
					if (i < leftObstacleDistance) {
						arena.setGridAsSafe(robotFrontPosition - i, yLocation + 1);
					} else if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation + 1);
					}
				}
				break;
		}
	}
	
	private void processDataFromLeftSensor(int reading) {
		System.out.println("Processing data from left sensors.");
		System.out.println("Reading: " + reading + ".");
		final int LEFT_SENSOR_DISTANCE_FROM_EDGE = 7;
		int obstacleDistance = ((reading - LEFT_SENSOR_DISTANCE_FROM_EDGE + 5) % 10) + 1;
		
		//Cap the distance to be 3 grid (i.e. make sure the sensors are working optimal)
		obstacleDistance = Math.min(obstacleDistance, 4);
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation - 1 - i, yLocation);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_DOWN:
					arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation + 1 + i, yLocation);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_LEFT:
					arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation, yLocation - 1 - i);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
					}
					break;
				case HEADING_RIGHT:
					arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation, yLocation + 1 + i);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
					}
					break;
			}
		}
	}
	
	private void processDataFromRightSensor(int reading) {
		System.out.println("Processing data from right sensors.");
		System.out.println("Reading: " + reading + ".");
		final int RIGHT_SENSOR_DISTANCE_FROM_EDGE = 7;
		int obstacleDistance = ((reading - RIGHT_SENSOR_DISTANCE_FROM_EDGE + 5) % 10) + 1;
		
		//Cap the distance to be 3 grid (i.e. make sure the sensors are working optimal)
		obstacleDistance = Math.min(obstacleDistance, 4);
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation + 1 + i, yLocation);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_DOWN:
					arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation - 1 - i, yLocation);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_LEFT:
					arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation, yLocation + 1 + i);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
					}
					break;
				case HEADING_RIGHT:
					arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					if (i < obstacleDistance) {
						arena.setGridAsSafe(xLocation, yLocation - 1 - i);
					} else if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
					}
					break;
			}
		}
	}
}