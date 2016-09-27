package mdp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

public class App {
	
	protected static Arena arena;
	protected static Robot robot;
	protected static ArenaSimulator simulator;
	protected static WebSocketClient webSocketClient;
	protected static ConnectionManager connectionManager;


	public static void main(String[] args) {
		//System.out.println("Running...");
		//generate robot
		generateArena();
		//Initialize robot
		generateRobot();
		//System.out.println("Terminating...");
		try {
			openConnectionToRobot();
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
		robot = new Robot(1, 1, Robot.HEADING_UP);
	}
	
	public static void displayArena() {
		simulator = new ArenaSimulator();
		simulator.setVisible(true);
	}
	
	public static void openConnectionToRobot() throws Exception {
		/*webSocketClient = new WebSocketClient(new URI("ws://192.168.25.1:5182/"), new Draft_10()) {
			
			@Override
		    public void onMessage(String message) {
				System.out.println("Received message: " + message);
		    }
		
		    @Override
		    public void onOpen(ServerHandshake handshake) {
		        System.out.println("opened connection");
		    }
		
		    @Override
		    public void onClose(int code, String reason, boolean remote) {
		        System.out.println( "closed connection" );
		    }
		
		    @Override
		    public void onError(Exception ex) {
		        ex.printStackTrace();
		    }
		};
		webSocketClient.connect();
		while (!webSocketClient.isOpen()) {
			try {
			    Thread.sleep(500);                 //1000 milliseconds is one second.
			    System.out.println(webSocketClient.isOpen());
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			} 
		}
		//System.out.println("Connection established");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		do {
			//System.out.println(webSocketClient.isOpen());
			webSocketClient.send(input);
			
			input = scanner.nextLine();
		} while (!input.equals("close"));		
	}*/
		
		connectionManager = new ConnectionManager();
		connectionManager.setConnection(5000);
		if (connectionManager.isConnected()) {
			System.out.println("Connection established");
		}
		//connectionManager.sendMsg("Jackson", "1", true);
		connectionManager.sendMsg("F;;", "A", true);	
		Thread t1 = new Thread(new Runnable() {
		     public void run() {
		    	 try {
		    		 while (connectionManager.isConnected()) {
		    			 connectionManager.recvMsg();
		    			 //Thread.sleep(100); 
		    		 }
				} /*catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}*/
		    	 catch (Exception e) {
		    		 
		    	 }
		     }
		});  
		t1.start();
		/*Scanner sc = new Scanner(System.in);
		String input = sc.nextLine();
		while (!input.equals("close")) {
			connectionManager.sendMsg(input, "A", true);
			input = sc.nextLine();
		}*/
		
		
		
		
	}
}
