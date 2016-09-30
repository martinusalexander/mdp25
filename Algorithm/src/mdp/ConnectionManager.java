package mdp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ConnectionManager {
	
    public static final String RPI_IP_ADDRESS = "192.168.25.1";
    public static final int RPI_PORT = 5182;
    
    public static final int SEND_TO_ROBOT = 1001;
    public static final int SEND_TO_ANDROID = 1002;

    private static ConnectionManager instance;
    private Socket clientSocket;
    private PrintWriter toRPi;
    private Scanner fromRPi;
    private final int DELAY_IN_SENDING_MESSAGE = 2;

    private final int TIME_TO_RETRY = 1000;       //wait for 1 second to retry

    private ConnectionManager(){}

    public static ConnectionManager getInstance(){
        if (instance == null) {
            instance = new ConnectionManager();
            instance.connectToHost();
        }
        return instance;
    }

    public void connectToHost(){
        try {
            clientSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
            toRPi = new PrintWriter(clientSocket.getOutputStream());
            fromRPi = new Scanner(clientSocket.getInputStream());
        }catch (IOException ioe){
            ioe.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            connectToHost();
        }
        System.out.println("RPi successfully connected");
    }

    public void closeConnection(){
        try {
            if (!clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            closeConnection();
        }
        System.out.println("Connection closed");
    }

    public void sendMessage(String message, int destination){
        if (destination == SEND_TO_ANDROID) {
        	message = "A" + message;
        } else if (destination == SEND_TO_ROBOT) {
        	message = "R" + message;
        } else {
        	return;
        }
        message += "\n";
    	try {
            Thread.sleep(DELAY_IN_SENDING_MESSAGE);
            toRPi.print(message);
            toRPi.flush();
        }catch (InterruptedException ite){
            ite.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException ite){
                ite.printStackTrace();
            }
            connectToHost();
            sendMessage(message, destination);
        }

        System.out.println("Message sent: ****" + message + "****");
    }

    public String readMessage() {
        String messageReceived = "";
        try {
        	System.out.println("Called");
            messageReceived = fromRPi.nextLine();
            System.out.println("Here AB");
            System.out.println("Message received: ****" + messageReceived + "****");
            
        }catch (Exception e){
            e.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException ite){
                ite.printStackTrace();
            }
            connectToHost();
            readMessage();
        }

        return messageReceived;
    }

}
