package mdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class Robot {
	
	//MODE
	public static final int IDLE_MODE = 900;
	public static final int EXPLORE_MODE = 901;
	public static final int FASTEST_MODE = 902;
	
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
	public static final double TURN_TO_MAKE_DIAGONAL_TURN = 1.2;
	public static final double COST_TO_MOVE_ONE_STEP = 1;
    public static final double COST_TO_MAKE_A_TURN = 1.5;
    
    private static double fastestRunCost = 0;
	
	public Robot() {
		this.xLocation = 1;
		this.yLocation = 1;
		this.direction = HEADING_UP;
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
		//App.listenToAndroidThread.interrupt();
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
		String ACKMessage;
		if (!App.isSimulation) {
			App.connectionManager.sendMessage("M;;", ConnectionManager.SEND_TO_ROBOT);
			ACKMessage = App.connectionManager.readMessage();
			processSensorData(ACKMessage.substring(1));
		} else {
			processSensorData("");
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
			/*try {
			    Thread.sleep(100);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/
			//Update arena data in simulator and Android
			sendArenaData();
			//TODO (send data to Android and simulator)
			
			//Update path taken
			Grid grid = arena.getGrid(xLocation, yLocation);
			explorationPath.add(grid);
			
			repeatedGrid = arena.getExploredGrid();			
		} while (!(xLocation == 1 && yLocation == 1 && counter > 10));
		
		while (direction != HEADING_UP) {
			this.command(TURN_RIGHT);
		}
		
		//Finishing
		this.mode = IDLE_MODE;
		//App.listenToAndroidThread.start();
		
	}
	
	private void fastestRun() {
		this.mode = FASTEST_MODE;
		//App.listenToAndroidThread.interrupt();
		do {
			calibrateInitialPosition();
		} while (!isCalibrated);
		
		//Code here
		fastestPathFound = getFastestPath();
		if (fastestPathFound) {
			for (Integer command : fastestRunCommand) {
				
				this.command(command);
				/*try {
				    Thread.sleep(100);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}*/
				sendArenaData();
				
			}
		} else {
			System.out.println("Fastest path not found.");
		}
		
		//Finishing
		this.mode = IDLE_MODE;
		//App.listenToAndroidThread.start();
	}
	
	private void calibrateInitialPosition() {
		//TODO (command Arduino)
		isCalibrated = true;
	}
	
	private void sendArenaData() {
		String arenaData1 = App.arena.getArenaDataPart1();
		String arenaData2 = App.arena.getArenaDataPart2();
		String arenaData1ForAndroid = App.arena.getArenaData1ForAndroid();
		String arenaData2ForAndroid = App.arena.getArenaData2ForAndroid();
		//System.out.println(arenaDataForAndroid);
		//TODO (coordinate with Android team)
		//Update simulator
		App.simulator.updateMap(arenaData1, arenaData2);
		if (!App.isSimulation) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("robotPosition", "[" + (yLocation-1) + "," + (xLocation-1) + "," + (direction) + "]");
			App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			/*try {
			    Thread.sleep(25);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/
			jsonObject = new JSONObject();
			jsonObject.put("sensor", arenaData2ForAndroid);
			App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			/*try {
			    Thread.sleep(25);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/
			jsonObject = new JSONObject();
			jsonObject.put("obstacle", arenaData1ForAndroid);
			App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			/*try {
			    Thread.sleep(25);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/
		}
	}
	private int getAppropriateExplorationMovement() {
		int command = 0;
		//System.out.println(previousMovement);
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
					command = TURN_LEFT;
				}
		}
		return command;
	}
	
	private boolean getFastestPath() {
		final double TURN_COST = 0.8;
		final int[] GOAL = {13, 18};
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
        startGrid.setDirection(Robot.HEADING_UP);
        
        HeapPriorityQueue<Grid> queue = new HeapPriorityQueue<Grid>();
        queue.offer(startGrid);

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
        //System.out.println("****Expanded index: " + thisGrid.getX() + ", " + thisGrid.getY());
        for(int[] reachableNodeIndex : arena.getReachableGridIndices(thisGrid.getX(), thisGrid.getY())){
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
            Grid previousGrid = arena.getGrid(previousPosition[0], previousPosition[1]);
        	int orientation = getRelativeOrientation(currentPosition, previousPosition);
            double stepCost = COST_TO_MOVE_ONE_STEP;
            //if previous orientation is the same as relative orientation, then no need to turn
            if(orientation == previousGrid.getDirection()){}
            //if orientation is opposite, turn twice
            else if(orientation == ((previousGrid.getDirection() + 90) % 360)) {
                stepCost += 2 * COST_TO_MAKE_A_TURN;
            }
            //otherwise, turn once
            else {
                stepCost += COST_TO_MAKE_A_TURN;
            }
            if (arena.getGrid(previousPosition[0], previousPosition[1]).getPathCost() + stepCost < arena.getGrid(currentPosition[0], currentPosition[1]).getPathCost()) {
                Grid thisGrid = arena.getGrid(currentPosition[0], currentPosition[1]);
                if(thisGrid.getPathCost() == Integer.MAX_VALUE) {
                    queue.offer(thisGrid);
                }
                thisGrid.setPathCostUpdated(true);
                thisGrid.setPathCost((int) (previousGrid.getPathCost() + stepCost));
                thisGrid.setPreviousGrid(previousGrid.getXY());
                thisGrid.setDirection(orientation);
            }
        }
    }
	
	public static int getRelativeOrientation(int[] destination, int[] origin){
		int xDifference = destination[0] - origin[0];
		int yDifference = destination[1] - origin[1];
		return yDifference == 0 ? (xDifference > 0 ? Arena.RIGHT : Arena.LEFT) : (yDifference > 0 ? Arena.UP : Arena.DOWN);
	}
	
	public ArrayList getFastestRunPath(int[] start, int[] end) {
		ArrayList<Grid> gridList = new ArrayList<Grid>();
		Grid grid = arena.getGrid(end[0], end[1]);
		this.fastestRunCost = grid.getPathCost();
		gridList.add(grid);
		while (true) {
			//System.out.println(grid.getPreviousGrid()[0] + " " + grid.getPreviousGrid()[1]);
			grid = arena.getGrid(grid.getPreviousGrid()[0], grid.getPreviousGrid()[1]);
			
			gridList.add(grid);
			if (grid.getPreviousGrid()[0] == 1 && grid.getPreviousGrid()[1] == 1) {
				gridList.add(arena.getGrid(1, 1));
				break;
			}
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
			/*case CHECK_OBSTACLE_IN_FRONT: checkObstacleInFront(); break;
			case CHECK_OBSTACLE_ON_LEFT: checkObstacleOnLeft(); break;
			case CHECK_OBSTACLE_ON_RIGHT: checkObstacleOnRight(); break;
			case CHECK_OBSTACLE_ON_SIDE: checkObstacleOnSide(); break;*/
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
		String ACKMessage;
		System.out.println("Turning left.");
		this.direction -= 90;
		this.direction += 360;
		this.direction %= 360;
		
		if (!App.isSimulation) {
			//TODO (command Arduino)
			App.connectionManager.sendMessage("L;;", ConnectionManager.SEND_TO_ROBOT);
			ACKMessage = App.connectionManager.readMessage();
			if (mode == EXPLORE_MODE) {
				processSensorData(ACKMessage.substring(4));
			}
		} else {
			processSensorData("");
		}
	}
	
	private void turnRight() {
		String ACKMessage;
		System.out.println("Turning right.");
		this.direction += 90;
		this.direction += 360;
		this.direction %= 360;
		if (!App.isSimulation) {
			//TODO (command Arduino)
			App.connectionManager.sendMessage("R;;", ConnectionManager.SEND_TO_ROBOT);
			ACKMessage = App.connectionManager.readMessage();
			if (mode == EXPLORE_MODE) {
				processSensorData(ACKMessage.substring(4));
			}
		} else {
			processSensorData("");
		}
	}
	
	private void moveForward() {
		if (!App.isSimulation) {
			String ACKMessage;
			/*if (mode == EXPLORE_MODE) {
				App.connectionManager.sendMessage("M;;", ConnectionManager.SEND_TO_ROBOT);
				ACKMessage = App.connectionManager.readMessage();
				processSensorData(ACKMessage.substring(1));
			}*/
			System.out.println("Moving forward.");
			//TODO (command Arduino) -> move forward
			App.connectionManager.sendMessage("F;;", ConnectionManager.SEND_TO_ROBOT);
			ACKMessage = App.connectionManager.readMessage();
			if (mode == EXPLORE_MODE) {
				processSensorData(ACKMessage.substring(4));
			}
		} else {
			processSensorData("");
		}
		switch (direction) {
			case HEADING_UP: this.yLocation += 1; break;
			case HEADING_DOWN: this.yLocation -= 1; break;
			case HEADING_LEFT: this.xLocation -= 1; break;
			case HEADING_RIGHT: this.xLocation += 1; break;
		}
	}
	
	private void moveBackward() {
		if (!App.isSimulation) {
			
			System.out.println("Moving backward.");
			//TODO (command Arduino) -> move forward
			App.connectionManager.sendMessage("B;;", ConnectionManager.SEND_TO_ROBOT);
			//TODO (wait for ACK then ask for sensor reading)
			String ACKMessage = App.connectionManager.readMessage();
		}
		switch (direction) {
			case HEADING_UP: this.yLocation -= 1; break;
			case HEADING_DOWN: this.yLocation += 1; break;
			case HEADING_LEFT: this.xLocation += 1; break;
			case HEADING_RIGHT: this.xLocation -= 1; break;
		}
	}
	
	private void processSensorData(String ACKMessage) {
		//TODO (decode ACKMessage)
		int frontLeftReading;
		int frontMiddleReading;
		int frontRightReading;
		int leftSensorReading;
		int rightSensorReading;
		//Remove last semicolon
		if (!App.isSimulation) {
			ACKMessage = ACKMessage.substring(0, ACKMessage.length()-1);
			String[] reading = ACKMessage.split(";");
			for (String s : reading) {
				System.out.print(s + ", ");
			}
			System.out.println();
			frontLeftReading = Integer.parseInt(reading[0]);
			frontMiddleReading = Integer.parseInt(reading[1]);
			frontRightReading = Integer.parseInt(reading[2]);
			leftSensorReading = Integer.parseInt(reading[3]);
			rightSensorReading = Integer.parseInt(reading[4]);
		} else {
			//Hardcode
			frontLeftReading = 4;
			frontMiddleReading = 4;
			frontRightReading = 4;
			leftSensorReading = 4;
			rightSensorReading = 4;
		}
		
		processDataFromFrontSensors(frontLeftReading, frontMiddleReading, frontRightReading);
		processDataFromLeftSensor(leftSensorReading);
		processDataFromRightSensor(rightSensorReading);
	}
	
	private void processDataFromFrontSensors(int leftReading, int middleReading, int rightReading) {
		System.out.println("Processing data from front sensors.");
		System.out.println("Left reading: " + leftReading + ", middle reading: " + middleReading + ", rightReading: " + rightReading + ".");
		
		//Cap the distance to be 3 grid (i.e. make sure the sensors are working optimal)
		int leftObstacleDistance = leftReading + 2;
		int middleObstacleDistance = middleReading + 2;
		int rightObstacleDistance = rightReading + 2;
		
		if (App.isSimulation) {
			switch (direction) {
				case HEADING_UP:
					leftObstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation+1, HEADING_UP) + 1;
					middleObstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation+1, HEADING_UP) + 1;
					rightObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation+1, HEADING_UP) + 1;
					break;
				case HEADING_DOWN:
					leftObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation-1, HEADING_DOWN) + 1;
					middleObstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation-1, HEADING_DOWN) + 1;
					rightObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation-1, HEADING_DOWN) + 1;
					break;
				case HEADING_RIGHT:
					leftObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation+1, HEADING_RIGHT) + 1;
					middleObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation, HEADING_RIGHT) + 1;
					rightObstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation-1, HEADING_RIGHT) + 1;
					break;
				case HEADING_LEFT:
					leftObstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation-1, HEADING_LEFT) + 1;
					middleObstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation, HEADING_LEFT) + 1;
					rightObstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation+1, HEADING_LEFT) + 1;
					break;
			}
		}
		//System.out.println(leftObstacleDistance);
		//System.out.println(middleObstacleDistance);
		//System.out.println(rightObstacleDistance);
		
		int robotFrontPosition;
		switch (direction) {
			case HEADING_UP:
				robotFrontPosition = yLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					System.out.println(i);
					if (i <= leftObstacleDistance) {
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition + i);
					} 
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition + i);
						System.out.println(xLocation-1 + " " + robotFrontPosition + i);
					}
					//Front middle
					if (i <= middleObstacleDistance) {
						arena.setGridAsVisited(xLocation, robotFrontPosition + i);
					} 
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition + i);
					}
					//Front right
					if (i <= rightObstacleDistance) {
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition + i);
					} 
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition + i);
						}
					}
				break;
			case HEADING_DOWN:
				robotFrontPosition = yLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					if (i <= leftObstacleDistance) {
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition - i);
					} 
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition - i);
					}
					//Middle front
					if (i <= middleObstacleDistance) {
						arena.setGridAsVisited(xLocation, robotFrontPosition - i);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition - i);
					}
					//Front right
					if (i <= rightObstacleDistance) {
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition - i);
					}
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition - i);
					}
				}
				break;
			case HEADING_RIGHT:
				robotFrontPosition = xLocation + 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					if (i <= leftObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition + i, yLocation + 1);
					}
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation + 1);
					}
					//Middle front
					if (i <= middleObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition + i, yLocation);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation);
					}
					//Front right
					if (i <= rightObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition + i, yLocation - 1);
					} 
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation - 1);
					}
				}
				break;
			case HEADING_LEFT:
				robotFrontPosition = xLocation - 1;
				for (int i = 1; i <= 3; i++) {
					//Front left
					if (i <= leftObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition - i, yLocation - 1);
					}
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation - 1);
					}
					//Middle front
					if (i <= middleObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition - i, yLocation);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation);
					}
					//Front right
					if (i <= rightObstacleDistance) {
						arena.setGridAsVisited(robotFrontPosition - i, yLocation + 1);
					}
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation + 1);
					}
				}
				break;
		}
	}
	
	private void processDataFromLeftSensor(int reading) {
		System.out.println("Processing data from left sensors.");
		System.out.println("Reading: " + reading + ".");
		int obstacleDistance = reading + 2;
				
		if (App.isSimulation) {
			switch (direction) {
				case HEADING_UP:
					obstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation, HEADING_LEFT) + 1;
					break;
				case HEADING_DOWN:
					obstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation, HEADING_RIGHT) + 1;
					break;
				case HEADING_RIGHT:
					obstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation+1, HEADING_UP) + 1;
					break;
				case HEADING_LEFT:
					obstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation-1, HEADING_DOWN) + 1;
					break;
			}
			
		}
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_DOWN:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_LEFT:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
					}
					break;
				case HEADING_RIGHT:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
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
		int obstacleDistance = reading + 2;
		
		if (App.isSimulation) {
			switch (direction) {
				case HEADING_UP:
					obstacleDistance = App.arena.distanceToObstacle(xLocation+1, yLocation, HEADING_RIGHT) + 1;
					break;
				case HEADING_DOWN:
					obstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation, HEADING_LEFT) + 1;
					break;
				case HEADING_RIGHT:
					obstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation-1, HEADING_DOWN) + 1;
					break;
				case HEADING_LEFT:
					obstacleDistance = App.arena.distanceToObstacle(xLocation, yLocation+1, HEADING_UP) + 1;
					break;
			}
			
		}
		
		for (int i = 1; i <= 3; i++) {
			switch (direction) {
				case HEADING_UP:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_DOWN:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_LEFT:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
					}
					break;
				case HEADING_RIGHT:
					if (i <= obstacleDistance) {
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					}
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
	
	public int getDirection() {
		return this.direction;
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
	
	public void reset() {
		this.xLocation = 1;
		this.yLocation = 1;
		this.direction = Robot.HEADING_UP;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return this.mode;
	}
}