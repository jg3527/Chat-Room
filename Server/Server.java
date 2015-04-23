import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class Server {
	private final static String filePath = "/home/jg3527/Downloads/credentials.txt"; //The path of the credentials.txt 
	
	protected static Map<String, String> userPass; // Username, Password
	protected static Map<String, InetAddress> clients; // Online users
	protected static Map<String, HashSet<String>> blackLists; // Username, blacklist
	protected static Set<String> blockedUsername; // Users who have been frozen due to multiple login failure
	protected static Map<String, String[]> userHostNamePortNumber; //The hostname and port number of this user.
	private static ServerSocket welcomeSocket;
	protected static Map<String, HashMap<String, String>> offlineMessage; // The offline message of that user.
	protected static Map<String, Boolean> heartBeat;
	private static int serverPortnumber;
	protected static Map<String, HashSet<String>> ipRequest; //<user1, users who requested ipaddress of user1>
	public static void main(String[] args) throws IOException{
		serverPortnumber =  Integer.parseInt(args[0]); //7099;//TODO
		Runtime.getRuntime().addShutdownHook(new ShutDownThread());
		try{
		initialize();
		}catch(InterruptedException e){
			System.out.println("Interrupted");
			e.printStackTrace();
		}
		}

	private static void initialize() throws IOException, InterruptedException{ 
		ipRequest = Collections.synchronizedMap(new  HashMap<String, HashSet<String>>());
		offlineMessage = Collections.synchronizedMap(new HashMap<String, HashMap<String, String>>());
		heartBeat = Collections.synchronizedMap(new HashMap<String, Boolean>());
		userPass = Collections.synchronizedMap(new HashMap<String, String>());
		clients = Collections.synchronizedMap(new HashMap<String, InetAddress>());
		blackLists = Collections.synchronizedMap(new HashMap<String, HashSet<String>>());
		blockedUsername = Collections.synchronizedSet(new HashSet<String>());
		userHostNamePortNumber =  Collections.synchronizedMap(new HashMap<String, String[]>()); 
		
		// Load credential.txt file.
		initializeCredentialFile();
		
		// Create the welcome socket.
		welcomeSocket = new ServerSocket(serverPortnumber);
		
		while(true){
			System.out.println("Waiting for new users...");
			Socket connectionSocket = welcomeSocket.accept();
			new ServerThread(connectionSocket).start();
		}

	}
	private static void initializeCredentialFile() throws IOException{
		File file = new File(filePath);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = null;
		String[] tmp;
		while((lineTxt = bufferedReader.readLine()) != null){
			tmp = lineTxt.split(" ");
			userPass.put(tmp[0], tmp[1]);
		}
		bufferedReader.close();
		read.close();		
	}
	private static void sendShutDownMessageToUser(String user){
		String[] tmp = Server.userHostNamePortNumber.get(user); 
		String hostName = tmp[0];
		int port = Integer.parseInt(tmp[1]);
		
		try {
			Socket s = new Socket(hostName, port);
			PrintStream tps = new PrintStream(s.getOutputStream());
			tps.println(MyProtocol.SHUTDOWN);
			tps.close();
			s.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	private static class ShutDownThread extends Thread{
		public void run(){
			Debug.print("Send shutdowm message to users");
			for(String user : Server.clients.keySet()){
				sendShutDownMessageToUser(user);
			}
			try {
				Server.welcomeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
