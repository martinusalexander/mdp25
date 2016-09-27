package mdp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ConnectionManager {
	
	protected static ConnectionManager _commMgr = null;
	
	// For communication with the Raspberry-Pi
	private static final String HOST = "192.168.25.1";
	//private static final String HOST = "localhost";
	private static final int PORT = 5182;
	
	public static final String MSG_TYPE_ANDROID = "1,";
	public static final String MSG_TYPE_ARDUINO = "3,";

	private static Socket _conn = null;

	private static BufferedOutputStream _bos = null;
	private static OutputStreamWriter _osw = null;
	private static BufferedReader _br = null;
	
	/**
	 * Private constructor used to support the Singleton design pattern
	 * <p>
	 */
	public ConnectionManager()
	{
		
	}
	
	/**
	 * Public static function used to get hold of the CommMgr
	 * 
	 * @return The static instance of the CommMgr
	 */
	public static ConnectionManager getCommMgr()
	{
		if(_commMgr == null)
		{
			_commMgr = new ConnectionManager();
		}
		
		return _commMgr;
	}
	
	public boolean setConnection(int timeoutInMs) {
		
		try {

			_conn = new Socket();
			_conn.connect(new InetSocketAddress(HOST, PORT), timeoutInMs);
			_conn.setSoTimeout(timeoutInMs);

			_bos = new BufferedOutputStream(_conn.getOutputStream());
			_osw = new OutputStreamWriter(_bos, "US-ASCII");
			_br = new BufferedReader(new InputStreamReader(
					_conn.getInputStream()));
            
			/*test communication
			
			Scanner sc = new Scanner(System.in);
            System.out.println("send message:");
			String message = sc.nextLine();
            BufferedWriter bw = new BufferedWriter(_osw);
            bw.write(message);
            bw.flush();
            
            String rmessage = _br.readLine();
            System.out.println("Message received from the server : " +rmessage);*/
            
			// Successful connection, return true
			System.out.println("setConnection() ->" +
					" Connection established successfully!");
			
			return true;
			
		} catch(UnknownHostException e) {
			System.out.println("setConnection() -> Unknown Host Exception");
		} catch(IOException e) {
			System.out.println("setConnection() -> IO Exception");
		} catch(Exception e) {
			System.out.println("setConnection() -> Exception");
		}
		
		System.out.println("Failed to establish connection!");
		return false;
	}
	
	public void closeConnection() {
		try {
			if(_bos != null)
				_bos.close();
			if(_osw != null)
				_osw.close();
			if(_br != null)
				_br.close();
			
			if(_conn != null) {
				_conn.close();
				_conn = null;
			}
			
		} catch (IOException e) {
			System.out.println("closeConnection() -> IO Exception");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("closeConnection() -> Null Pointer Exception");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("closeConnection() -> Exception");
			e.printStackTrace();
		}
	}
	
	public boolean sendMsg(String msg, String msgType, boolean ack) {
		try {
			msg = msg.replace(";;", ";");
			String outputMsg = String.valueOf(msg);
			outputMsg = String.format("%-128s", outputMsg);
			System.out.println("Sending out msg: " + outputMsg);
			
			_osw.write(outputMsg);
			_osw.flush();
			
			return true;
		} catch (IOException e) {
			System.out.println("sendMsg() -> IOException");
		} catch (Exception e) {
			System.out.println("sendMsg() -> Exception");
		}
		
		return false;
	}

	public String recvMsg() {
		System.out.println("Called");
		try {
			String input = _br.readLine();
			if(input != null && input.length() > 0) {
				System.out.println(input.length());
				System.out.println(input);
				return input;
			}
			
		} catch(IOException e) {
			//System.out.println("recvMsg() -> IO exception");
		} catch (Exception e) {
			//System.out.println("recvMsg() -> Exception");
		}
		
		return null;
	}
	
	public boolean isConnected() {
		return _conn.isConnected();
	}

}
