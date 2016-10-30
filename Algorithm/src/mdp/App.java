package mdp;

import org.json.simple.JSONObject;

public class App {
	
	protected static Arena arena;
	protected static Robot robot;
	protected static ArenaSimulator simulator;
	protected static ConnectionManager connectionManager;
	protected static Thread listenToAndroidThread;
	protected static boolean isSimulation = true;


	public static void main(String[] args) {
		//System.out.println("Running...");
		//generate robot
		generateArena();
		//Initialize robot
		generateRobot();
		//System.out.println("Terminating...");
		try {
			if (!isSimulation) {
				openConnectionToRobot();
			}
		} catch (Exception e) {
			System.out.println("Cannot establish connection with message: " + e.getMessage());
		}
	}
	
	public static void generateArena() {
		//Initialize arena
		arena = new Arena();
		displayArena();
	}
	
	public static void generateRobot() {
		//Initialize robot
		robot = new Robot(1, 1, Robot.HEADING_DOWN);
	}
	
	public static void displayArena() {
		simulator = new ArenaSimulator();
		simulator.setVisible(true);
	}
	
	public static void openConnectionToRobot() throws Exception {		
		connectionManager = ConnectionManager.getInstance();
		connectionManager.sendMessage("{\"robotPosition\":\"[1,1,180]\"}", ConnectionManager.SEND_TO_ANDROID);
		connectionManager.sendMessage("{\"sensor\":\"fc000fe000fc000f80007800038000100000000000000000000000000000000000000000000\"}", ConnectionManager.SEND_TO_ANDROID);
		connectionManager.sendMessage("{\"obstacle\":\"040000400004000000004000000000000000000000000000000000000000000000000000000\"}",ConnectionManager.SEND_TO_ANDROID);
		//connectionManager.readMessage();
		listenToAndroidThread = new Thread(new Runnable() {
		     public void run() {
		    	 //Listening to Android if idle
		    	 while (true) {
			    	 if (robot.getMode() == Robot.IDLE_MODE) {
			    		System.out.println("Listen to Android thread running");
			 			String message = connectionManager.readMessage();
			 			if (message.toUpperCase().contains("E;")) {
			 				robot.command(Robot.EXPLORE);
			 			} else if (message.toUpperCase().contains("S;")) {
			 				robot.command(Robot.FASTEST_RUN);
			 			} else if (message.toUpperCase().contains("RESET;")) {
			 				//Temporary, need to fix
			 				simulator.reset();
			 			} else if (message.toUpperCase().contains("R;")) {
			 				robot.command(Robot.TURN_RIGHT);
			 			} else if (message.toUpperCase().contains("L;")) {
			 				robot.command(Robot.TURN_LEFT);
			 			} else if (message.toUpperCase().contains("F;")) {
			 				robot.command(Robot.MOVE_FORWARD);
			 			} else if (message.toUpperCase().contains("B;")) {
			 				robot.command(Robot.MOVE_BACKWARD);
			 			} else if (message.toUpperCase().contains("D1;")) {
			 				String arenaData1 = App.arena.getArenaDataPart1();
			 				JSONObject jsonObject = new JSONObject();
			 				jsonObject.put("MDF1", arenaData1);
			 				App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			 			} else if (message.toUpperCase().contains("D2;")) {
			 				String arenaData2 = App.arena.getArenaDataPart2();
			 				JSONObject jsonObject = new JSONObject();
			 				jsonObject.put("MDF2", arenaData2);
			 				App.connectionManager.sendMessage(jsonObject.toString(), ConnectionManager.SEND_TO_ANDROID);
			 			}
			 			/*else if (message.toUpperCase().substring(0,1).equals("A")) {
			 				connectionManager.sendMessage(message.substring(1), ConnectionManager.SEND_TO_ROBOT);
			 			}*/
			 			//Prevent inconsistent message receiving process
			 			try {
						    Thread.sleep(500);                 
						} catch(InterruptedException ex) {
						    Thread.currentThread().interrupt();
						}
			    	}
		 		}
		     }
		});  
		listenToAndroidThread.start();
		
	}
}
