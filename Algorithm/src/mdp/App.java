package mdp;

public class App {
	
	protected static Arena arena;
	protected static Robot robot;

	public static void main(String[] args) {
		
		System.out.println("Running...");
		//generate robot
		generateArena();
		//Initialize robot
		generateRobot();
		System.out.println("Terminating...");
	}
	
	public static void generateArena() {
		//Initialize arena
		arena = new Arena();
	}
	
	public static void generateRobot() {
		//Initialize robot
		robot = new Robot(0, 0, Robot.HEADING_LEFT);
	}
}
