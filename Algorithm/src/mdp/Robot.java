package mdp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.sun.beans.finder.FieldFinder;

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
	private List<Grid> fastestRunPath;
	
	private List<Integer> fastestRunCommand;
	
	private boolean fastestPathFound;
	
	//Helper
	private Arena arena;
	
	//A* Algo
	public static final double TURN_TO_MAKE_DIAGONAL_TURN = 0.8;
	public static final double COST_TO_MOVE_ONE_STEP = 1;
    public static final double COST_TO_MAKE_A_TURN = 1;
    
    private static double fastestRunCost = 0;
	
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
		int counter = 0;
		//Mark initial robot position as visited
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				App.arena.setGridAsVisited(i, j);
			}
		}
		do  {
			counter++;
			System.out.println("Exploring step: " + counter);
			
			//Decide appropriate movement
			int nextCommand = getAppropriateExplorationMovement();
			
			//Execute the appropriate command
			this.command(nextCommand);
			System.out.println(nextCommand);
			System.out.println(this.xLocation + " " + this.yLocation + " " + this.direction);
			this.previousMovement = nextCommand;
			try {
			    Thread.sleep(100);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			//Update arena data in simulator and Android
			String arenaData = sendArenaData();
			//TODO (send data to Android and simulator)
			System.out.println(arenaData);
			
			//Update path taken
			Grid grid = arena.getGrid(xLocation, yLocation);
			explorationPath.add(grid);
			
			repeatedGrid = arena.getExploredGrid();			
		} while (!(xLocation == 1 && yLocation == 1 && counter > 10));
		
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
	
	private String sendArenaData() {
		String arenaData1 = App.arena.getArenaDataPart1();
		String arenaData2 = App.arena.getArenaDataPart2();
		//TODO (coordinate with Android team)
		//Do something
		
		//Update simulator
		App.simulator.updateMap(arenaData1, arenaData2);
		return "";
	}
	
	private int getAppropriateExplorationMovement() {
		int command = 0;
		System.out.println(previousMovement);
		switch (direction) {
			case HEADING_UP:
				if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.isWalkableGrid(xLocation - 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation - 2, yLocation) && arena.isWalkableGrid(xLocation - 2, yLocation + 1) && previousMovement != TURN_LEFT) { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.isWalkableGrid(xLocation - 1, yLocation + 2) &&
						arena.isWalkableGrid(xLocation, yLocation + 2) && arena.isWalkableGrid(xLocation + 1, yLocation + 2)) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.isWalkableGrid(xLocation + 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation + 2, yLocation) && arena.isWalkableGrid(xLocation + 2, yLocation + 1)) {
					command = TURN_RIGHT;
				} else {
					/*System.out.println("Else");*/
					command = TURN_LEFT;
				}
				break;
			case HEADING_DOWN:
				if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.isWalkableGrid(xLocation + 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation + 2, yLocation) && arena.isWalkableGrid(xLocation + 2, yLocation + 1) && previousMovement != TURN_LEFT) { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.isWalkableGrid(xLocation - 1, yLocation - 2) &&
						arena.isWalkableGrid(xLocation, yLocation - 2) && arena.isWalkableGrid(xLocation + 1, yLocation - 2)) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.isWalkableGrid(xLocation - 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation - 2, yLocation) && arena.isWalkableGrid(xLocation - 2, yLocation + 1)) {
					command = TURN_RIGHT;
				} else {
					/*System.out.println(!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1));
					System.out.println(arena.isWalkableGrid(xLocation - 2, yLocation - 1));
					System.out.println(arena.isWalkableGrid(xLocation - 2, yLocation));
					System.out.println(arena.isWalkableGrid(xLocation - 2, yLocation + 1));
					System.out.println("Else");*/
					command = TURN_LEFT;
				}
				break;
			case HEADING_LEFT:
				if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.isWalkableGrid(xLocation - 1, yLocation - 2) &&
						arena.isWalkableGrid(xLocation, yLocation - 2) && arena.isWalkableGrid(xLocation + 1, yLocation - 2) && previousMovement != TURN_LEFT)  { 
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_LEFT, 1) && arena.isWalkableGrid(xLocation - 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation - 2, yLocation) && arena.isWalkableGrid(xLocation - 2, yLocation + 1)) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.isWalkableGrid(xLocation - 1, yLocation + 2) &&
						arena.isWalkableGrid(xLocation, yLocation + 2) && arena.isWalkableGrid(xLocation + 1, yLocation + 2)) {
					command = TURN_RIGHT;
				} else {
					/*System.out.println("Else");*/
					command = TURN_LEFT;
				}
				break;
			case HEADING_RIGHT:
				if (!arena.isWall(xLocation, yLocation, HEADING_UP, 1) && arena.isWalkableGrid(xLocation - 1, yLocation + 2) &&
						arena.isWalkableGrid(xLocation, yLocation + 2) && arena.isWalkableGrid(xLocation + 1, yLocation + 2) && previousMovement != TURN_LEFT) {
					command = TURN_LEFT;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_RIGHT, 1) && arena.isWalkableGrid(xLocation + 2, yLocation - 1) &&
						arena.isWalkableGrid(xLocation + 2, yLocation) && arena.isWalkableGrid(xLocation + 2, yLocation + 1)) {
					command = MOVE_FORWARD;
				} else if (!arena.isWall(xLocation, yLocation, HEADING_DOWN, 1) && arena.isWalkableGrid(xLocation - 1, yLocation - 2) &&
						arena.isWalkableGrid(xLocation, yLocation - 2) && arena.isWalkableGrid(xLocation + 1, yLocation - 2)) {
					command = TURN_RIGHT;
				} else {
					/*System.out.println("Else");*/
					command = TURN_LEFT;
				}
		}
		return command;
	}
	
	private boolean getFastestPath() {
		
		final double TURN_COST = 0.8;
		final int[] GOAL = {14, 19};
		final int[] START = {1, 1};
		final int START_ORIENTATION = Robot.HEADING_UP;
		arena.findSafeGrid();
		//TODO: Append each action to fastestPathCommand

        //Initializing heuristics
        for(int i=0; i<Arena.ARENA_LENGTH; i++) {
            for (int j = 0; j < Arena.ARENA_HEIGHT; j++) {
                arena.getGrid(i, j).setHeuristic(calculateHeuristic(i, j, GOAL));
            }
        }

        Grid startGrid = arena.getGrid(START[0], START[1]);
        
        startGrid.setPathCost(0);
        startGrid.setPreviousGrid(null);

        HeapPriorityQueue<Grid> queue = new HeapPriorityQueue<Grid>();

        expand(arena, START, queue);

        Grid expandingGrid = startGrid;

        while(queue.size()>0){    //repeatedly polling from the queue and expand
            expandingGrid = queue.poll();

            //*********debugging code
            //virtualMap.printExpanded();

            expand(arena, expandingGrid.getXY(), queue);

            if(GlobalUtilities.sameLocation(expandingGrid.getXY(), GOAL)) {
                System.out.println("Reached goal");
                break;
            }
        }

        if(!GlobalUtilities.sameLocation(expandingGrid.getXY(), GOAL)) {
            return false;
        }
        
        fastestRunPath = getFastestRunPath(START, GOAL);
        
        for (int i = 0; i < fastestRunPath.size()-1; i++) {
        	fastestRunCommand.addAll(getCommandList(fastestRunPath.get(i), fastestRunPath.get(i+1)));
        }

        return true;
		
	}
	
	//expand a node and mark its reachables nodes
    public void expand(Arena arena, int[] currentGridIndex, HeapPriorityQueue<Grid> queue) {
        Grid thisGrid = arena.getGrid(currentGridIndex[0], currentGridIndex[1]);
    	thisGrid.setExpanded(true);
        //System.out.println("****Expanded index: " + thisNode.index[0] + ", " + thisNode.index[1]);
        for(int[] reachableNodeIndex : arena.getReachableGridIndices(this.xLocation, this.yLocation)){
            //trying to mark reachable nodes and ignore those that are out of index bound
            try {
            	mark(arena, reachableNodeIndex, currentGridIndex, queue);
            } catch (ArrayIndexOutOfBoundsException e){}
        }
        queue.update();
    }
	
	private int calculateHeuristic(int x, int y, int[] goal){
        return Math.abs(goal[0] - x) + Math.abs(goal[1] - y);
    }
	
	private void mark(Arena arena, int[] currentPosition, int[] previousPosition, HeapPriorityQueue<Grid> queue) {
        if(arena.getGrid(currentPosition[0], currentPosition[1]).isSafe()) {
            int orientation = getRelativeOrientation(currentPosition, previousPosition);
            double stepCost = COST_TO_MOVE_ONE_STEP;
            //if previous orientation is the same as relative orientation, then no need to turn
            if(orientation == this.direction){}
            //if orientation is opposite, turn twice
            else if(orientation == ((this.direction + 90) % 360)) {
                stepCost += 2 * COST_TO_MAKE_A_TURN;
            }
            //otherwise, turn once
            else {
                stepCost += COST_TO_MAKE_A_TURN;
            }
            if (arena.getGrid(previousPosition[0], previousPosition[1]).getPathCost() + stepCost < arena.getGrid(currentPosition[0], currentPosition[1]).getPathCost()) {
                Grid thisGrid = arena.getGrid(currentPosition[0], currentPosition[1]);
            	Grid previousGrid = arena.getGrid(previousPosition[0], previousPosition[1]);
                if(thisGrid.getPathCost() == Integer.MAX_VALUE) {
                    queue.offer(thisGrid);
                }
                thisGrid.setPathCostUpdated(true);
                thisGrid.setPathCost((int) (previousGrid.getPathCost() + stepCost));
                thisGrid.setPreviousGrid(previousGrid.getPreviousGrid());
                thisGrid.setDirection(orientation);
            }
        }
    }
	
	public static int getRelativeOrientation(int[] destination, int[] origin){
		int xDifference = destination[0] - origin[0];
		int yDifference = destination[1] - origin[1];
		return yDifference == 0 ? (xDifference > 0 ? Arena.UP : Arena.DOWN) : (yDifference > 0 ? Arena.RIGHT : Arena.LEFT);
	}
	
	public ArrayList getFastestRunPath(int[] start, int[] end) {
		ArrayList<Grid> gridList = new ArrayList<Grid>();
		Grid grid = arena.getGrid(end[0], end[1]);
		this.fastestRunCost = grid.getPathCost();
		gridList.add(grid);
		while (arena.getGrid(grid.getPreviousGrid()[0], grid.getPreviousGrid()[1]) != null) {
			grid = arena.getGrid(grid.getPreviousGrid()[0], grid.getPreviousGrid()[1]);
			gridList.add(grid);
		}
		Collections.reverse(gridList);
		return gridList;
		
	}
	
	public void command(int commandType) {
		switch (commandType) {
			case EXPLORE: explore(); break;
			case FASTEST_RUN: fastestRun(); break;
			case TURN_LEFT: turnLeft(); break;
			case TURN_RIGHT: turnRight(); break;
			case MOVE_FORWARD:  moveForward(); break;
			case MOVE_BACKWARD: moveBackward(); break;
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
		this.direction += 360;
		this.direction %= 360;
		//TODO (command Arduino)
	}
	
	private void turnRight() {
		System.out.println("Turning right.");
		this.direction += 90;
		this.direction += 360;
		this.direction %= 360;
		//TODO (command Arduino)
	}
	
	private void moveForward() {
		if (mode == EXPLORE_MODE) {
			System.out.println("Moving forward and sense.");
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
			System.out.println("Moving forward.");
			//TODO (command Arduino) -> move forward only
		}
		switch (direction) {
			case HEADING_UP: this.yLocation += 1; break;
			case HEADING_DOWN: this.yLocation -= 1; break;
			case HEADING_LEFT: this.xLocation -= 1; break;
			case HEADING_RIGHT: this.xLocation += 1; break;
		}
	}
	
	private void moveBackward() {
		System.out.println("Moving backward.");
		//TODO (command Arduino)
		switch (direction) {
			case HEADING_UP: this.yLocation -= 1; break;
			case HEADING_DOWN: this.yLocation += 1; break;
			case HEADING_LEFT: this.xLocation += 1; break;
			case HEADING_RIGHT: this.xLocation -= 1; break;
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
		
		//Temp
		leftObstacleDistance = 4;
		middleObstacleDistance = 4;
		rightObstacleDistance = 4;
		
		int robotFrontPosition;
		switch (direction) {
			case HEADING_UP:
				robotFrontPosition = yLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					System.out.println("D" + (xLocation - 1) + " " + (robotFrontPosition + i));
					arena.setGridAsVisited(xLocation - 1, robotFrontPosition + i);
					if (i == leftObstacleDistance) {
						
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition + i);
					}
					//Front middle
					arena.setGridAsVisited(xLocation, robotFrontPosition + i);
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition + i);
					}
					//Front right
					arena.setGridAsVisited(xLocation + 1, robotFrontPosition + i);
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition + i);
					}
				}
				break;
			case HEADING_DOWN:
				robotFrontPosition = yLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(xLocation + 1, robotFrontPosition - i);
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition - i);
					}
					//Middle front
					arena.setGridAsVisited(xLocation, robotFrontPosition - i);
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition - i);
					}
					//Front right
					arena.setGridAsVisited(xLocation - 1, robotFrontPosition - i);
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition - i);
					}
				}
				break;
			case HEADING_RIGHT:
				robotFrontPosition = xLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(robotFrontPosition + i, yLocation + 1);
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation + 1);
					}
					//Middle front
					arena.setGridAsVisited(robotFrontPosition + i, yLocation);
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation);
					}
					//Front right
					arena.setGridAsVisited(robotFrontPosition + i, yLocation - 1);
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation - 1);
					}
				}
				break;
			case HEADING_LEFT:
				robotFrontPosition = xLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					arena.setGridAsVisited(robotFrontPosition - i, yLocation - 1);
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation - 1);
					}
					//Middle front
					arena.setGridAsVisited(robotFrontPosition - i, yLocation);
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation);
					}
					//Front right
					arena.setGridAsVisited(robotFrontPosition - i, yLocation + 1);
					if (i == leftObstacleDistance) {
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
		
		//Temp
		obstacleDistance = 4;
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_DOWN:
					arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_LEFT:
					arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
					}
					break;
				case HEADING_RIGHT:
					arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					if (i == obstacleDistance) {
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
		
		//Temp
		obstacleDistance = 4;
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_DOWN:
					arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_LEFT:
					arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
					}
					break;
				case HEADING_RIGHT:
					arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
					}
					break;
			}
		}
	}
	
	public int getX() {
		return this.xLocation;
	}
	
	public int getY() {
		return this.yLocation;
	}
	
	private ArrayList getCommandList(Grid origin, Grid destination) {
		int[] originIndex = origin.getXY();
		int[] destinationIndex = destination.getXY();
		int xOrigin = originIndex[0];
		int yOrigin = originIndex[1];
		int xDestination = destinationIndex[0];
		int yDestination = destinationIndex[1];
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		int destinationRelativeLocation;
		
		if (xOrigin > xDestination) {
			destinationRelativeLocation = Arena.LEFT;
		} else if (xOrigin < xDestination) {
			destinationRelativeLocation = Arena.RIGHT;
		} else if (yOrigin > yDestination) {
			destinationRelativeLocation = Arena.DOWN;
		} else {
			destinationRelativeLocation = Arena.UP;
		}
		
		//Figure out how many turn needed
		if (Math.abs(destinationRelativeLocation - origin.getDirection()) == 180) {
			result.add(Robot.TURN_LEFT);
			result.add(Robot.TURN_LEFT);
		} else if (destinationRelativeLocation - origin.getDirection() == 90) {
			result.add(Robot.TURN_RIGHT);
		} else if (destinationRelativeLocation - origin.getDirection() == -90) {
			result.add(Robot.TURN_LEFT);
		}
		//Finally, move forward
		result.add(Robot.MOVE_FORWARD);
		return result;
	}
}