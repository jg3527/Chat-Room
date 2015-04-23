import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class Client {
	protected static String userName;
	protected static String serverName;
	protected static int serverPortNumber;
	protected static BufferedReader inFromUser;
	private static Socket clientSocket;
	protected static BufferedReader inFromServer;
	private static PrintStream ps;
	private static String line;
	private static int portNumber;
	private static String clientHostname;
	protected static ServerSocket welcomeSocket;
	private static Boolean online;
	protected static HashMap<String, String[]> peer;// users can be privately talk to. user name, user ip, port number
	protected static Timer hbTimer;
	private static Boolean on;
	private static class ShutDownThread extends Thread{
		public void run(){
			Debug.print("Client shutdown.");
			Client.sendMessageToServer(MyProtocol.SHUTDOWN);
			Client.closeConnection();
			Client.closeApp();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException{
		Runtime.getRuntime().addShutdownHook(new ShutDownThread());

		serverName = args[0];//"127.0.0.1";////TODO
		serverPortNumber = Integer.parseInt(args[1]); //7099;////7099; //TODO
		Debug.print("Start");
		initialize();
	}
	private static void initialize() {
		peer = new HashMap<String, String[]>();
		hbTimer = new Timer();
		online = false;
		portNumber = 9997;
		Boolean is = true;
		line = null;
		on = false;
		ps = null;
		try {
			clientSocket = new Socket(serverName, serverPortNumber);
		} catch (UnknownHostException e1) {
			System.out.println("This is not a valid ip address. Please restart the program.");
			Debug.print(e1.getMessage());
			is = false;
		} catch (IOException e1) {
			System.out.println("Server is down.");
			Debug.print(e1.getMessage());
			is = false;
			closeConnection();
			closeApp();
			Runtime.getRuntime().exit(-1);
		}


		try {
			clientHostname = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			System.out.println("Please check connect to the internet.");
			Debug.print(e.getMessage());
			is = false;
			closeConnection();
			closeApp();
			System.exit(0);
		}

		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		try {
			ps = new PrintStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Server is down.");
			Debug.print(e.getMessage());
			is = false;
			closeConnection();
			closeApp();
			System.exit(0);
		}

		if(is){
			TimerTask timeout = new TimerTask(){
				public void run(){
					if(!on){
						System.out.println("Time out. Please restart this program.");
						closeConnection();
						closeApp();
						System.exit(0);
					}
				}
			};
			hbTimer = new Timer();
			hbTimer.schedule(timeout, 60000);
			login();
		}

	}
	private static void login(){
		try {
			while((line = inFromServer.readLine()) != null){
				System.out.println("> " + line);
				if(line.equals(MyProtocol.BLOCK_ACCOUNT)){
					Debug.print("Blocked..");
					closeConnection();
					initialize();
				}else if(line.equals(MyProtocol.BLOCK_ACCOUNT_ANOTHER_IP)){
					Debug.print("multiple ip for a blocked account");
					closeConnection();
					initialize();
				}else if(line.equals(MyProtocol.WELCOME)){
					Debug.print("Login successfully..");			
					//Client's IP address and portNumber
					on = true;
					ps.println(clientHostname + " " + portNumber);	
					Debug.print("Sent out hostname port number");
				}else if(line.equals(MyProtocol.LOGIN)){
					online = true;
				}else if(line.equals(MyProtocol.SHUTDOWN)){
					System.out.println("Server shutdown.");
					closeConnection();
					closeApp();
					System.exit(0);
				}else if(line.equals(MyProtocol.USERNAME)){
					userName = inFromUser.readLine().trim();
					Debug.print("user name: " + userName);
					ps.println(userName);
				}else if(line.equals(MyProtocol.TIMEOUT_INFO)){
					System.out.println("You have to restart this program.");
				}else if(line.split(" ")[0].equals(MyProtocol.OFFLINEMESSAGE)){

				}else{
					ps.println(inFromUser.readLine());
				}

				if(online){
					Debug.print("Break while loop.");
					// HeartBeat

					TimerTask heartBeat = new TimerTask(){
						@Override
						public void run() {
							Debug.print("Send heartbeat to server");
							sendMessageToServer(MyProtocol.HEARTBEAT);
						}	
					};
					(new Timer()).schedule(heartBeat, 0, MyProtocol.HEARTBEAT_CLIENT);

					closeConnection();
					communicate();
					break;
				}
			}
			Debug.print("inFromServer = null..");

		}catch (IOException e) {
			online = false;
			System.out.println("Server is down. Or time out. Please restart this program and try again.");
			Debug.print(e.getMessage());
			closeConnection();
			closeApp();
			System.exit(0);

		}
	}
	public static void sendMessageToServer(String message){
		try {
			clientSocket = new Socket(Client.serverName, Client.serverPortNumber);
			ps = new PrintStream(clientSocket.getOutputStream());
			ps.println(message);

		} catch (UnknownHostException e) {
			System.out.println("Can not find Server");
			Debug.print(e.getMessage());
			closeConnection();
			closeApp();
		} catch (IOException e) {
			System.out.println("Cannot connect to Server. Server is down.");
			if(hbTimer != null){
			hbTimer.cancel();
			Debug.print("hbtimer canceled");
			}
			closeConnection();
			closeApp();
			System.exit(-1);

		}

			ps.close();
		
		try {
			
			clientSocket.close();
		} catch (IOException e) {
			Debug.print(e.getMessage());
		}
	}
	private static void communicate(){
		try {
			Debug.print("Communicate");
			// Create the welcome socket
			welcomeSocket = new ServerSocket(portNumber);
			new UserInputListener().start();

			while(true){
				Socket connectionSocket = welcomeSocket.accept();
				new ClientThread(connectionSocket).start();
			}

		} catch (IOException e) {

			Debug.print(e.getMessage());
		}

	}


	private static void closeConnection(){
		Debug.print("Trying to close this connection.");
		if(ps != null)
		ps.close();
		try {
			inFromServer.close();
		}catch(Exception e){

		}
		try {

			clientSocket.close();

		}catch (Exception e1) {
			Debug.print(e1.getMessage());
		}
	}
	private static void closeApp(){
		Debug.print("Trying to close this application.");
		try {
			inFromUser.close();
		}catch(Exception e){

		}
		try{
			welcomeSocket.close();
		} catch (Exception e) {
			Debug.print(e.getMessage());
		}
		System.exit(-1);
	}

}
