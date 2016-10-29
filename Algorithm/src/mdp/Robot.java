package mdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;

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
	
	private static final int REACHABLE_OBSTACLE_DISTANCE = 2;
	
	private int xLocation;
	private int yLocation;
	private int direction;
	private int mode;
	private int previousMovement;
	private List<Grid> explorationPath;
	//private List<Integer> explorationCommandList;
	private List<Grid> fastestRunPath;
	
	private Stack<Integer> fastestRunCommand;
	
	private boolean fastestPathFound;
	
	private boolean calibrateAtTopLeftCorner = false;
	private boolean calibrateAtTopRightCorner = false;
	private boolean calibrateAtBottomRightCorner = false;
	
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
		this.mode = IDLE_MODE;
		this.arena = App.arena;
		this.previousMovement = 0;
		this.explorationPath = new LinkedList<>();
		this.fastestRunCommand = new Stack<>();
		this.fastestPathFound = false;
	}
	
	public Robot(int startingXLocation, int startingYLocation, int direction) {
		this.xLocation = startingXLocation;
		this.yLocation = startingYLocation;
		this.direction = direction;
		this.mode = IDLE_MODE;
		this.arena = App.arena;
		this.previousMovement = 0;
		this.explorationPath = new LinkedList<>();
		this.fastestRunCommand = new Stack<>();
		this.fastestPathFound = false;
	}
	
	private void explore() {
		this.mode = EXPLORE_MODE;
		//App.listenToAndroidThread.interrupt();
		//Code here
		int repeatedGrid = 0;
		int counter = 0;
		//Mark initial robot position as visited
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				App.arena.setGridAsVisited(i, j);
			}
		}
		if (!App.isSimulation) {
			//Initial calibration
			performCalibration();
			this.command(TURN_RIGHT);System.out.println("amarc");
			performCalibration();
			this.command(TURN_RIGHT);System.out.println("amard");
		} else {
			this.command(TURN_RIGHT);System.out.println("amare");
			this.command(TURN_RIGHT);System.out.println("amarf");
			processSensorData("");
		}
		
		
		do  {
			counter++;
			System.out.println("Exploring step: " + counter);
				
			//Decide appropriate movement
			int nextCommand = getAppropriateExplorationMovement();
					
			
			if (nextCommand == TURN_RIGHT) {
				//Recover the unexplored grid
				if ((direction == HEADING_UP && !arena.isVisitedGrid(xLocation-2, yLocation+1)) ||
						(direction == HEADING_DOWN && !arena.isVisitedGrid(xLocation+2, yLocation-1)) ||
						(direction == HEADING_LEFT && !arena.isVisitedGrid(xLocation-1, yLocation-2)) ||
						(direction == HEADING_RIGHT && !arena.isVisitedGrid(xLocation+1, yLocation+2)))
				this.command(TURN_LEFT);
				//this.command(TURN_RIGHT);System.out.println("amarg");
			}
			//Execute the appropriate command
			this.command(nextCommand);
			
			if (this.xLocation == 1 && this.yLocation == 18 && !calibrateAtTopLeftCorner) {
				//Calibrate at top left corner
				calibrateAtTopLeftCorner = true;
				performCalibration();
				switch (direction) {
					case HEADING_UP: 
						this.command(TURN_LEFT);
						performCalibration();
						this.command(TURN_RIGHT);System.out.println("amarh");
						System.out.println("HERE");
						break;
					case HEADING_LEFT:
						this.command(TURN_RIGHT);System.out.println("amari");
						performCalibration();
						this.command(TURN_LEFT);
						break;
				}				
			} else if (this.xLocation == 13 && this.yLocation == 18 && !calibrateAtTopRightCorner) {
				//Calibrate at top right corner
				calibrateAtTopRightCorner = true;
				performCalibration();
				switch (direction) {
					case HEADING_UP: 
						this.command(TURN_RIGHT);System.out.println("amarj");
						performCalibration();
						this.command(TURN_LEFT);
						break;
					case HEADING_RIGHT:
						this.command(TURN_LEFT);
						performCalibration();
						this.command(TURN_RIGHT);System.out.println("amark");
						break;
				}
			} else if (this.xLocation == 13 && this.yLocation == 1 && !calibrateAtBottomRightCorner) {
				//Calibrate at bottom right corner
				calibrateAtBottomRightCorner = true;
				performCalibration();
				switch (direction) {
					case HEADING_DOWN: 
						this.command(TURN_LEFT);
						performCalibration();
						this.command(TURN_RIGHT);System.out.println("amarm");
						break;
					case HEADING_RIGHT:
						this.command(TURN_RIGHT);System.out.println("amarn");
						performCalibration();
						this.command(TURN_LEFT);
						break;
				}
			} else if (counter % 5 == 0) {
				calibrateRobotPositionLeftRight();
			} else {
				calibrateRobotPositionFront();
			}
			
			this.previousMovement = nextCommand;
			
			//Update arena data in simulator and Android
			sendArenaData();
			
			//Update path taken
			Grid grid = arena.getGrid(xLocation, yLocation);
			explorationPath.add(grid);
			
			repeatedGrid = arena.getExploredGrid();			
		} while (!(xLocation == 1 && yLocation == 1 && counter > 10));
		
		while (direction != HEADING_UP) {
			this.command(TURN_RIGHT);System.out.println("amarl");
			sendArenaData();
		}
		
		// additional code for exploring unexplored grids, returns to start grid at (1,1) at the end
		unexploredSearcher();
		
		//Inform Android if done
		if (!App.isSimulation) {
			App.connectionManager.sendMessage("done", ConnectionManager.SEND_TO_ANDROID);
		}
		
		//Finishing
		this.mode = IDLE_MODE;
		//App.listenToAndroidThread.start();
		
	}
	
	private void fastestRun() {
		this.mode = FASTEST_MODE;
		//App.listenToAndroidThread.interrupt();
		
		//Code here
		fastestPathFound = getFastestPath();
		if (fastestPathFound) {
			Collections.reverse(fastestRunCommand);
			while (!fastestRunCommand.isEmpty()) {
				int moveForwardCounter = 1;
				Integer command = fastestRunCommand.pop();
				if (command != MOVE_FORWARD ) {
					this.command(command);
				} else {
					while (true) {
						if (!fastestRunCommand.isEmpty()) {
							if (fastestRunCommand.peek() == MOVE_FORWARD) {
								fastestRunCommand.pop();
								moveForwardCounter++;
							} else {
								break;
							}
						} else {
							System.out.println("Break");
							break;
						}
					}
					this.command(MOVE_FORWARD, moveForwardCounter);
				}
				sendArenaData();
			}
			System.out.println("Congratulations!!! Go and eat!");
			
			/*for (Integer command : fastestRunCommand) {
				this.command(command);
				sendArenaData();
			}*/
		} else {
			System.out.println("Fastest path not found.");
		}
		//Inform Android if done
		if (!App.isSimulation) {
			App.connectionManager.sendMessage("done", ConnectionManager.SEND_TO_ANDROID);
		}
		//Finishing
		this.mode = IDLE_MODE;
		//App.listenToAndroidThread.start();
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
			jsonObject = new JSONObject();
			jsonObject.put("sensor", arenaData2ForAndroid);
			App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			jsonObject = new JSONObject();
			jsonObject.put("obstacle", arenaData1ForAndroid);
			App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
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
        	/*ArrayList commandList = getCommandList(fastestRunPath.get(i), fastestRunPath.get(i+1));
        	for (int j = 0; j < commandList.size(); j++) {
        		int command = (int) commandList.get(j);
        		fastestRunCommand.push(command);
        		System.out.println(fastestRunCommand.size());
        	}*/
        	fastestRunCommand.addAll(getCommandList(fastestRunPath.get(i), fastestRunPath.get(i+1)));
        	System.out.println(fastestRunCommand.size());
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
			case MOVE_FORWARD:  moveForward(1); break;
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
	
	private void command(int commandType, int data1) {
		switch (commandType) {
			//data1 is distance
			case MOVE_FORWARD:  moveForward(data1); break;
			//case MOVE_BACKWARD: moveBackward(data1); break;
			default: break;
		}
	}
	/*
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
			App.connectionManager.sendMessage("L;", ConnectionManager.SEND_TO_ROBOT);
			if (mode == EXPLORE_MODE) {
				ACKMessage = App.connectionManager.readMessage();
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
			App.connectionManager.sendMessage("R;", ConnectionManager.SEND_TO_ROBOT);
			if (mode == EXPLORE_MODE) {
				ACKMessage = App.connectionManager.readMessage();
				processSensorData(ACKMessage.substring(4));
			}
		} else {
			processSensorData("");
		}
	}
	
	private void moveForward(int distance) {
		switch (direction) {
			case HEADING_UP: this.yLocation += distance; break;
			case HEADING_DOWN: this.yLocation -= distance; break;
			case HEADING_LEFT: this.xLocation -= distance; break;
			case HEADING_RIGHT: this.xLocation += distance; break;
		}
		if (!App.isSimulation) {
			String ACKMessage;
			/*if (mode == EXPLORE_MODE) {
				App.connectionManager.sendMessage("M;", ConnectionManager.SEND_TO_ROBOT);
				ACKMessage = App.connectionManager.readMessage();
				processSensorData(ACKMessage.substring(1));
			}*/
			System.out.println("Moving forward.");
			//TODO (command Arduino) -> move forward
			while (true) {
				App.connectionManager.sendMessage("F" + distance + ";", ConnectionManager.SEND_TO_ROBOT);
				if (mode == EXPLORE_MODE) {
					ACKMessage = App.connectionManager.readMessage();
					if (ACKMessage.toUpperCase().contains("F0")) {
						App.connectionManager.sendMessage("M;", ConnectionManager.SEND_TO_ROBOT);
						ACKMessage = App.connectionManager.readMessage();
						processSensorData(ACKMessage.substring(1));				
					} else if (ACKMessage.toUpperCase().contains("F1")) {
						processSensorData(ACKMessage.substring(4));
						return;
					}
				} else if (mode == FASTEST_MODE) {
					return;
				}
			}
		} else {
			processSensorData("");
		}
	}
	
	private void moveBackward() {
		if (!App.isSimulation) {
			
			System.out.println("Moving backward.");
			//TODO (command Arduino) -> move forward
			App.connectionManager.sendMessage("B;", ConnectionManager.SEND_TO_ROBOT);
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
		int leftObstacleDistance = leftReading + 1;
		int middleObstacleDistance = middleReading + 1;
		int rightObstacleDistance = rightReading + 1;
		
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
					rightObstacleDistance = App.arena.distanceToObstacle(xLocation-1, yLocation-1, HEADING_DOWN) + 1;
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
		
		int robotFrontPosition;
		switch (direction) {
			case HEADING_UP:
				robotFrontPosition = yLocation + 1;
				for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
					//Front left
					if (i < leftObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation - 1, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition + i);
					} 
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition + i);
					}
					//Front middle	
					if (i < middleObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation, robotFrontPosition + i);
					} 
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation, robotFrontPosition + i);
					}
					//Front right
					if (i < rightObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation + 1, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition + i);
					} 
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition + i);
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition + i);
					}
				}
				break;
			case HEADING_DOWN:
				robotFrontPosition = yLocation - 1;
				for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
					//Front left
					if (i < leftObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation + 1, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition - i);
					} 
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation + 1, robotFrontPosition - i);
					}
					//Middle front
					if (i < middleObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation, robotFrontPosition - i);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(xLocation, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation, robotFrontPosition - i);
					}
					//Front right
					if (i < rightObstacleDistance) {
						arena.setGridAsNotObstacle(xLocation - 1, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition - i);
					}
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1, robotFrontPosition - i);
						arena.setGridAsVisited(xLocation - 1, robotFrontPosition - i);
					}
				}
				break;
			case HEADING_RIGHT:
				robotFrontPosition = xLocation + 1;
				for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
					//Front left
					if (i < leftObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition + i, yLocation + 1);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation + 1);
					}
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation + 1);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation + 1);
					}
					//Middle front
					if (i < middleObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition + i, yLocation);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation);
					}
					//Front right
					
					if (i < rightObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition + i, yLocation - 1);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation - 1);
					} 
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition + i, yLocation - 1);
						arena.setGridAsVisited(robotFrontPosition + i, yLocation - 1);
					}
				}
				break;
			case HEADING_LEFT:
				robotFrontPosition = xLocation - 1;
				for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
					//Front left
					if (i < leftObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition - i, yLocation - 1);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation - 1);
					}
					if (i == leftObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation - 1);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation - 1);
					}
					//Middle front
					if (i < middleObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition - i, yLocation);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation);
					}
					if (i == middleObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation);
					}
					//Front right
					if (i < rightObstacleDistance) {
						arena.setGridAsNotObstacle(robotFrontPosition - i, yLocation + 1);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation + 1);
					}
					if (i == rightObstacleDistance) {
						arena.setGridAsObstacle(robotFrontPosition - i, yLocation + 1);
						arena.setGridAsVisited(robotFrontPosition - i, yLocation + 1);
					}
				}
				break;
		}
	}
	
	private void processDataFromLeftSensor(int reading) {
		System.out.println("Processing data from left sensors.");
		System.out.println("Reading: " + reading + ".");
		int obstacleDistance = reading + 1;
				
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
		
		for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
			switch (direction) {
				case HEADING_UP:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation - 1 - i, yLocation);
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_DOWN:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation + 1 + i, yLocation);
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_LEFT:
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, yLocation - 1 - i);
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					}
					break;
				case HEADING_RIGHT:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, yLocation + 1 + i);
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
					break;
			}
		}
	}
	
	private void processDataFromRightSensor(int reading) {
		System.out.println("Processing data from right sensors.");
		System.out.println("Reading: " + reading + ".");
		int obstacleDistance = reading + 1;
		
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
		
		for (int i = 1; i <= REACHABLE_OBSTACLE_DISTANCE; i++) {
			switch (direction) {
				case HEADING_UP:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation + 1 + i, yLocation);
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation + 1 + i, yLocation);
						arena.setGridAsVisited(xLocation + 1 + i, yLocation);
					}
					break;
				case HEADING_DOWN:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation - 1 - i, yLocation);
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation - 1 - i, yLocation);
						arena.setGridAsVisited(xLocation - 1 - i, yLocation);
					}
					break;
				case HEADING_LEFT:
					
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, yLocation + 1 + i);
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation + 1 + i);
						arena.setGridAsVisited(xLocation, yLocation + 1 + i);
					}
					break;
				case HEADING_RIGHT:
					if (i < obstacleDistance) {
						arena.setGridAsNotObstacle(xLocation, yLocation - 1 - i);
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
					}
					if (i == obstacleDistance) {
						arena.setGridAsObstacle(xLocation, yLocation - 1 - i);
						arena.setGridAsVisited(xLocation, yLocation - 1 - i);
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
		this.direction = Robot.HEADING_DOWN;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return this.mode;
	}
	
	public void calibrateRobotPositionFront() {
		switch(direction) {
		case HEADING_UP:
			if (arena.isObstacleGrid(xLocation-1, yLocation+2) && arena.isObstacleGrid(xLocation+1, yLocation+2)) {
				performCalibration();
			}
			break;
		case HEADING_DOWN:
			if (arena.isObstacleGrid(xLocation-1, yLocation-2) && arena.isObstacleGrid(xLocation+1, yLocation-2)) {
				performCalibration();
			}
			break;
		case HEADING_RIGHT:
			if (arena.isObstacleGrid(xLocation+2, yLocation+1) && arena.isObstacleGrid(xLocation+2, yLocation-1)) {
				System.out.println("B");
				performCalibration();
			}
			break;
		case HEADING_LEFT:
			if (arena.isObstacleGrid(xLocation-2, yLocation+1) && arena.isObstacleGrid(xLocation-2, yLocation-1)) {
				performCalibration();
			}
			break;
	}

	}
	
	public void calibrateRobotPositionLeftRight() {
		switch(direction) {
			case HEADING_UP:
				if (arena.isObstacleGrid(xLocation-2, yLocation+1) && arena.isObstacleGrid(xLocation-2, yLocation-1)) {
					this.command(TURN_LEFT);
					performCalibration();
					this.command(TURN_RIGHT);System.out.println("amarm");
				} else if (arena.isObstacleGrid(xLocation+2, yLocation+1) && arena.isObstacleGrid(xLocation+2, yLocation-1)) {
					this.command(TURN_RIGHT);System.out.println("amarn");
					performCalibration();
					this.command(TURN_LEFT);
				}
				break;
			case HEADING_DOWN:
				if (arena.isObstacleGrid(xLocation+2, yLocation+1) && arena.isObstacleGrid(xLocation+2, yLocation-1)) {
					this.command(TURN_LEFT);
					performCalibration();
					this.command(TURN_RIGHT);System.out.println("amaro");
				} else if (arena.isObstacleGrid(xLocation-2, yLocation+1) && arena.isObstacleGrid(xLocation-2, yLocation-1)) {
					this.command(TURN_RIGHT);System.out.println("amarp");
					performCalibration();
					this.command(TURN_LEFT);
				}
				break;
			case HEADING_RIGHT:
				if (arena.isObstacleGrid(xLocation+1, yLocation+2) && arena.isObstacleGrid(xLocation-1, yLocation+2)) {
					this.command(TURN_LEFT);
					System.out.println("A");
					performCalibration();
					this.command(TURN_RIGHT);System.out.println("amarq");
				} else if (arena.isObstacleGrid(xLocation+1, yLocation-2) && arena.isObstacleGrid(xLocation-1, yLocation-2)) {
					this.command(TURN_RIGHT);System.out.println("amarr");
					System.out.println("B");
					performCalibration();
					this.command(TURN_LEFT);
				}
				break;
			case HEADING_LEFT:
				if (arena.isObstacleGrid(xLocation+1, yLocation-2) && arena.isObstacleGrid(xLocation-1, yLocation-2)) {
					this.command(TURN_LEFT);
					performCalibration();
					this.command(TURN_RIGHT);System.out.println("amara");
				} else if (arena.isObstacleGrid(xLocation+1, yLocation+2) && arena.isObstacleGrid(xLocation-1, yLocation+2)) {
					this.command(TURN_RIGHT);System.out.println("amarb");
					performCalibration();
					this.command(TURN_LEFT);
				}
				break;
				
				
		}
	}
	
	public void performCalibration() {
		
		/*try {
			Scanner sc = new Scanner(System.in);
			String input = sc.nextLine();
			if (input.equals("e")) {
				throw new RuntimeException();
			}
		} catch (RuntimeException e) {
			for (int i = 0; i < 5; i++) {
				StackTraceElement callingFrame = Thread.currentThread().getStackTrace()[i];
				System.out.println(callingFrame.getMethodName() + " line " + callingFrame.getLineNumber());
			}
		}*/
		if (!App.isSimulation) {
			App.connectionManager.sendMessage("C;", ConnectionManager.SEND_TO_ROBOT);
			App.connectionManager.readMessage();
		}
	}

	private void unexploredSearcher() {
		// NOTE: Robot is expected to check its surroundings while attempting to reach destination
		
		List<Grid> unexploredGrids = new LinkedList<Grid>();
		List<Grid> traversableGrids = new ArrayList<Grid>();	// centre point of robot traversable only
		Set<Grid> contactedGrids = new TreeSet<Grid>();
		Grid[][] arenaGrids = arena.getArenaGrids();
		List<Grid> gridRoute, bestGridRoute;
		Grid bestUnexploredGrid;
		boolean flag;
		int i, j;
		
		// Scan to fill lists
		for (i = 1; i < Arena.ARENA_LENGTH-1; i++){
			for (j = 1; j < Arena.ARENA_HEIGHT-1; j++){
				// grid is unexplored, assuming flawless wall hugging
				if (!arenaGrids[i][j].isVisited){
					unexploredGrids.add(arenaGrids[i][j]);
				}
				// buffer from wall, checking using 3x3 padding (buffered)
				else {
					// check clockwise of centre point, accepting unexplored grids as walkable grids
					flag = arenaGrids[i-1][j-1].isWalkable() || !arenaGrids[i-1][j-1].isVisited();
					flag = (arenaGrids[i][j-1].isWalkable() || !arenaGrids[i][j-1].isVisited()) && flag;
					flag = (arenaGrids[i+1][j-1].isWalkable() || !arenaGrids[i+1][j-1].isVisited()) && flag;
					flag = (arenaGrids[i+1][j].isWalkable() || !arenaGrids[i+1][j].isVisited()) && flag;
					flag = (arenaGrids[i+1][j+1].isWalkable() || !arenaGrids[i+1][j+1].isVisited()) && flag;
					flag = (arenaGrids[i][j+1].isWalkable() || !arenaGrids[i][j+1].isVisited()) && flag;
					flag = (arenaGrids[i-1][j+1].isWalkable() || !arenaGrids[i-1][j+1].isVisited()) && flag;
					flag = (arenaGrids[i-1][j].isWalkable() || !arenaGrids[i-1][j].isVisited()) && flag;
					// grid is centre point of robot traversable
					if (flag) traversableGrids.add(arenaGrids[i][j]);
				}
			}
		}
		
		// if unexploredGrids is empty, return to start
		if (unexploredGrids.isEmpty()) {
			returnToStart(traversableGrids);
			return;
		}
		
		// Find traversable grids in contact with unexplored grids
		for (Grid g : (Grid) unexploredGrids.toArray()){
			// Get grid's coordinates
			i = g.getX();
			j = g.getY();
			// Check four directions for traversable grids, add to list if any
			if (traversableGrids.contains(arenaGrids[i-1][j])){
				contactedGrids.add(arenaGrids[i-1][j]);
			}
			if (traversableGrids.contains(arenaGrids[i+1][j])){
				contactedGrids.add(arenaGrids[i+1][j]);
			}
			if (traversableGrids.contains(arenaGrids[i][j-1]){
				contactedGrids.add(arenaGrids[i][j-1]);
			}
			if (traversableGrids.contains(arenaGrids[i][j+1]){
				contactedGrids.add(arenaGrids[i][j+1]);
			}
		}
		
		// Identify best gridRoute to nearest unexplored grid
		for (Grid g : (Grid) contactedGrids.toArray()){
			gridRoute = aStarSearch(arenaGrids[this.xLocation][this.yLocation],g,traversableGrids);
			try{
				if (gridRoute.size() < bestGridRoute.size()){
					bestGridRoute = gridRoute;
					bestUnexploredGrid = contactedGrids.get(contactedGrids.indexOf(g));
				}
			} catch (Exception e){
				bestGridRoute = gridRoute;
				bestUnexploredGrid = contactedGrids.get(contactedGrids.indexOf(g));
			}
		}
		
		// move robot to destination grid
		moveRobot(bestGridRoute);
		
		// clear immediate unexplored grids using pad scanning
		//clearUnexplored();			// skipping this code will result in longer time taken
		
		// check for unexplored grid and attempt to visit grid again
		unexploredSearcher();
	}
	
	private ArrayList<Grid> aStarSearch(Grid start, Grid end, List<Grid> traversableGridList){
		// start and end are elements of traversableGridList
		ArrayList<Grid> queue = new ArrayList<Grid>();
		int i, index;
		Grid gridEndPtr, gridStartPtr, gridPtr;
		int end_x, end_y, start_x, start_y;
		boolean flag;
		
		// Error checking
		if (!traversableGridList.contains(start) || !traversableGridList.contains(end)) {
			System.err.println("Invalid aStarSearch() argument(s)");
			return queue;
		}
		
		// Initialize all grids in traversableGridList to large value (999) and calculate heuristic
		i = 0;
		while (i < traversableGridList.size()){
			gridPtr = traversableGridList.get(i);
			gridPtr.setPathCost(999);
			gridPtr.setHeuristic(calculateHeuristic(start, gridPtr));
		}
		
		// initialize start grid values
		start.setPathCost(0);
		start.setHeuristic(calculateHeuristic(start,end));
		
		// add start grid to queue
		queue.add(start);
		
		// start AStarSearch expansion
		aStarSearch(0, end, traversableGridList, queue);
		
		// return empty list if end is not found in queue
		if (!queue.contains(end)){
			queue.clear();
			return queue;
		}
		
		// end is found in queue, remove trailing grids
		index = queue.indexOf(end);
		while (index > queue.size()){
			queue.remove(index+1);
		}
		
		// remove unneccessary nodes
		gridEndPtr = end;
		gridStartPtr = queue.get(--index);
		while (index > 0){						// while (!gridStartPtr.equals(queue.get(0)))
			end_x = gridEndPtr.getX();
			end_y = gridEndPtr.getY();
			start_x = gridStartPtr.getX();
			start_y = gridStartPtr.getY();
			
			flag = (end_x == start_x) && (end_y == start_y + 1);
			flag = ((end_x == start_x) && (end_y == start_y - 1)) || flag;
			flag = ((end_y == start_y) && (start_x == start_x + 1)) || flag;
			flag = ((end_y == start_y) && (start_x == start_x - 1)) || flag;
			flag = (gridStartPtr.getPathCost() == gridEndPtr.getPathCost() - 1) && flag;
			
			if (flag) {
				gridEndPtr = gridStartPtr;
				gridStartPtr = queue.get(--index);
			} else {
				gridStartPtr = queue.get(index - 1);
				queue.remove(index--);
			}
		}
		
		return queue;
	}
	
	private void aStarSearch(int index, Grid destGrid, List<Grid> traversableGridList, List<Grid> queue){
		Grid currentGrid, checkingGrid;
		int x, y, nextPathCost;
		
		// extract currentGrid's information
		currentGrid = queue.get(index);
		x = currentGrid.getX();
		y = currentGrid.getY();
		nextPathCost = currentGrid.getPathCost() + 1;
		
		// grid[x+1][y]
		if (x+1 < Arena.ARENA_LENGTH){
			checkingGrid = Arena.getGrid(x+1,y);
			if (traversableGridList.contains(checkingGrid)){
				if (checkingGrid.getPathCost() > nextPathCost){
					checkingGrid.setPathCost(nextPathCost);
					if (!queue.contains(checkingGrid)){
						queue.add(checkingGrid);
					}
				}
			}
		}
		
		// grid[x-1][y]
		if (x-1 >= 0){
			checkingGrid = Arena.getGrid(x-1,y);
			if (traversableGridList.contains(checkingGrid)){
				if (checkingGrid.getPathCost() > nextPathCost){
					checkingGrid.setPathCost(nextPathCost);
					if (!queue.contains(checkingGrid)){
						queue.add(checkingGrid);
					}
				}
			}
		}
		
		// grid[x][y+1]
		if (y+1 < Arena.ARENA_HEIGHT){
			checkingGrid = Arena.getGrid(x,y+1);
			if (traversableGridList.contains(checkingGrid)){
				if (checkingGrid.getPathCost() > nextPathCost){
					checkingGrid.setPathCost(nextPathCost);
					if (!queue.contains(checkingGrid)){
						queue.add(checkingGrid);
					}
				}
			}
		}
		
		// grid[x][y-1]
		if (y-1 >= 0){
			checkingGrid = Arena.getGrid(x,y-1);
			if (traversableGridList.contains(checkingGrid)){
				if (checkingGrid.getPathCost() > nextPathCost){
					checkingGrid.setPathCost(nextPathCost);
					if (!queue.contains(checkingGrid)){
						queue.add(checkingGrid);
					}
				}
			}
		}
		
		// sort queue in descending order
		Collections.sort(queue);
		
		// stops if destGrid is added to queue, else continue
		if (queue.contains(destGrid)){
			return;
		} else {
			aStarSearch(index, destGrid, traversableGridList, queue);
		}
	}
	
	private int calculateHeuristic(Grid start, Grid end){
		// simple distance from start grid to end grid
		int x_diff = start.getX() - end.getX();
		int y_diff = start.getY() - end.getY();
		if (x_diff < 0) x_diff = -x_diff;
		if (y_diff < 0) y_diff = -y_diff;
		return x_diff + y_diff;
	}
	
	private void moveRobot(List<Grid> travelRoute){
		for (Grid nextGrid : travelRoute){
			moveTo(nextGrid);
		}
	}
	
	private void moveTo(Grid next){
		// move robot to grid 'end', 'end' has to be an adjacent grid
		
		int next_x, next_y;
		Grid current = arena.getGrid(this.xLocation, this.yLocation);
		
		// check validity of grid 'next', return if premise is false
		if (!((this.xLocation == next_x+1 && this.yLocation == next_y) ||
			 (this.xLocation == next_x-1 && this.yLocation == next_y) ||
			 (this.yLocation == next_y+1 && this.xLocation == next_x) ||
			 (this.yLocation == next_y-1 && this.xLocation == next_x))
			 ){
			return;	
		}
		
		// align facing of robot to next grid
		alignFacing(next);
		
		// move forward to next grid
		moveForward(1);
	}
	
	private void alignFacing(Grid to){
		int finalDirection, turningRequirement;
		
		// determine robot's final facing, stops if invalid
		switch(this.xLocation - to.getX()){
			case 1: finalDirection = HEADING_LEFT;
				break;
			case -1: finalDirection = HEADING_RIGHT;
				break;
			case 0: switch(this.yLocation - to.getY()){
					case 1: finalDirection = HEADING_DOWN;
						break;
					case -1: finalDirection = HEADING_UP;
						break;
				}
			default: return;
		}
		
		// rotate robot
		turningRequirement = ((finalDirection - this.direction)/90)%4;
		// 0: no rotation, 1: turn right, 2: turn right twice, 3: turn left
		switch(turningRequirement){
			case 0: break;
			case 1: turnRight();
				break;
			case 2: turnRight();
				turnRight();
				break;
			case 3: turnLeft();
				break;
			default: break;
		}
	}
	
	private void clearUnexplored(){
		// find unexplored grids in vicinity and move robot to there
		
	}
	
	private void returnToStart(List<Grid> traversableGridList){
		Grid currentGrid = this.arena.getGrid(this.xLocation, this.yLocation);
		Grid startGrid = this.arena.getGrid(1,1);
		Grid facingGrid = this.arena.getGrid(1,2);
		
		List<Grid> travelRoute = aStarSearch(currentGrid, startGrid, traversableGridList);
		moveRobot(travelRoute);
		// add necessary calibration command here if any
		alginFacing(facingGrid);
	}
}