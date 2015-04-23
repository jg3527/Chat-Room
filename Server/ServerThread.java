import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;


public class ServerThread extends Thread {

	private BufferedReader inFromClient; // Use to read message from user. 
	private Socket connectionSocket; // The sockt it use to communicate with this user.
	private PrintStream ps; // Use to send message to this user.
	private String userName; // This user's name.
	private String password; // This user's password.
	private Boolean success;// Denote whether the user login successfully or not. false : failed; true : success.
	// If socket is no longer avaliable this thread should be terminated.
	public ServerThread(Socket socket) throws IOException{
		connectionSocket = socket;
		ps = new PrintStream(socket.getOutputStream());
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		success = false;
	}
	/**
	 * There are 3 reasons that this thread is started.
	 * 1. User try to login.
	 * 2. User try to send a message to someone. (Broadcast, private, getipaddress, logout)
	 * 3. HeartBeat.
	 * */

	public void run(){
		Debug.print("Start ServerThread");
		try{
			if(isLoginAlready()){
				Debug.print("Same user with same IP.");
				userName = getUsernameByInetAddress();
				communicate();
			}else{
				Debug.print("This user is trying to login.");
				login();
			}
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	private String getUsernameByInetAddress(){
		for(String un : Server.clients.keySet()){
			if(Server.clients.get(un).equals(connectionSocket.getInetAddress())){
				return un;
			}	
		}
		return null;
	}

	private void closeThisConnectionInAMin(){
		TimerTask ft = new TimerTask(){
			public void run(){
				if (!success){
					ps.println(MyProtocol.TIMEOUT_INFO);
					closeConnection();
				}
			}
		};

		(new Timer()).schedule(ft, MyProtocol.TIMEOUT);
	}

	private void login() throws IOException, InterruptedException{

		// Close this connection if the user has not finished login in 1 min.
		closeThisConnectionInAMin();

		try{
			ps.println(MyProtocol.USERNAME);
			userName = inFromClient.readLine().trim();
			Debug.print("Username: " + userName);
			// Judge whether it is a valid user name.
			while (!Server.userPass.keySet().contains(userName)){
				Debug.print("This username: " + userName + " is not valid.");
				ps.println(MyProtocol.INVALID_USERNAME);
				userName = inFromClient.readLine().trim();
			}

			// Judge whether the user name has been blocked by th

			if(Server.blockedUsername.contains(userName)){
				Debug.print("User has been blocked by server.");
				ps.println(MyProtocol.BLOCK_ACCOUNT_ANOTHER_IP);
				closeConnection();
			}else{
				ps.println(MyProtocol.PASSWORD);
				for(int i = 0; i < 3; i++){		
					password = "" + inFromClient.readLine().trim();
					Debug.print("Password typed in by user: " + password);
					if(Server.userPass.get(userName).equals(password)){
						success = true;
						Debug.print("Valid Password.");
						break;
					}else if(i < 2){
						ps.println(MyProtocol.INVAILD_PASSWORD);
						Debug.print("Invaid password: " + password + " The right password is: " + Server.userPass.get(userName));
					}else{
						ps.println(MyProtocol.BLOCK_ACCOUNT);
						Debug.print("User blocked.");
					}
				}
				if(!success){
					closeConnection();
					Server.blockedUsername.add(userName);
					unblockUser();
				}else{ 										//Successfully login.

					ps.println(MyProtocol.WELCOME);
					isUserAlreadyOnline();
					displayOfflineMessage();
					String t = inFromClient.readLine();
					Debug.print("t: " + t);
					recordUserInformation(t);
					broadCastOnline();
					new ServerHeartBeatThread(userName).start();
					closeConnection();
				}
			}
		}catch(IOException  e ){
			e.printStackTrace();	
		}finally{
			closeConnection();
		}
	}

	private void broadCastOnline(){
		for(String user : Server.clients.keySet()){
			if(!userName.equals(user) && !isInBlackList(user, userName)){
				Debug.print("broadcast online");
				sendMessageToUser(user,userName + " is online.", true);
			}
		}
	}

	private void isUserAlreadyOnline(){
		if(Server.clients.containsKey(userName)){
			sendMessageToUser(userName, MyProtocol.REAPTED_LOGIN, true);
		}
	}
	private void recordUserInformation(String t){
		Server.clients.put(userName, connectionSocket.getInetAddress());
		Server.heartBeat.put(userName, true);
		String[] tmp = t.trim().split(" ");
		Debug.print(tmp.toString());
		Debug.print(userName);
		Server.userHostNamePortNumber.put(userName, tmp);
		ps.println(MyProtocol.LOGIN);
		if(!Server.blackLists.keySet().contains(userName)){
			HashSet<String> hs = new HashSet<String>();
			Server.blackLists.put(userName, hs);
		}
	}
	// Judge whether user has logined in or not
	private Boolean isLoginAlready(){
		if(Server.clients.values().contains(connectionSocket.getInetAddress())){
			return true;	
		}
		return false;
	}


	//Server unblock user.
	private void unblockUser(){
		TimerTask unblockUser = new TimerTask(){
			public void run() {
				Server.blockedUsername.remove(userName);
			}
		};
		(new Timer()).schedule(unblockUser, MyProtocol.BLOCK_TIME);
	}
	private void closeConnection(){
		Debug.print("Trying to close the connection.");
		try {
			if(ps != null)
				ps.close();
			Debug.print("ps closed.");			
			inFromClient.close();	
			Debug.print("inFromClient closed.");
		}catch (IOException e1) {

			e1.printStackTrace();
		}
		try {
			connectionSocket.close();
			Debug.print("connectionSocket closed.");

		}catch (IOException e1) {

			e1.printStackTrace();
		}

	}
	private void printAllOfflineMessage(HashMap<String, String> tmp){
		Debug.print("print all offline message.");
		Iterator<Entry<String, String>> iter = tmp.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			String from = (String) entry.getKey();
			String message = (String)entry.getValue();
			Debug.print("Print offline message: " + message);
			ps.println(MyProtocol.OFFLINEMESSAGE + " " + from + ": " + message);
		}
	}
	private void displayOfflineMessage(){
		Debug.print("displayofflineMessage.");
		if(Server.offlineMessage.containsKey(userName) && Server.offlineMessage.get(userName).values().size() != 0){
			Debug.print("displayofflineMessage. have offline message.");
			HashMap<String, String> tmp = Server.offlineMessage.get(userName);
			printAllOfflineMessage(tmp);
		}
		Server.offlineMessage.remove(userName);
	}
	private void communicate() throws IOException{
		String line = null;	
		if((line = inFromClient.readLine().trim()) != null){
			Debug.print(line);
			parseUserRequest(line);
			closeConnection();
		}

	}
	private void parseUserRequest(String line){
		String command = line.split(" ")[0].toLowerCase();
		Debug.print("ServerThread, command, line247 " + command);
		String message;
		if(command.equals(MyProtocol.SHUTDOWN)){
			logout();
		}else if(command.equals(MyProtocol.HEARTBEAT)){
			heartBeat();
		}else if(command.equals(MyProtocol.MESSAGE)){
			message = line.substring(line.indexOf(" ")).trim();
			Debug.print("Message : " + message);
			message(message);
		}else if(command.equals(MyProtocol.ONLINE)){
			Debug.print("online success triggered.");
			online();
		}else if(command.equals(MyProtocol.BROADCAST)){
			message = line.substring(line.indexOf(" ")).trim();
			broadcast(message);
		}else if(command.equals(MyProtocol.GET_IP_ADDRESS)){
			Debug.print("Get ip address.");
			message = line.substring(line.indexOf(" ")).trim();
			getIPAddress(message);
		}else if(command.equals(MyProtocol.BLOCK)){
			block(line.substring(line.indexOf(" ")).trim());
		}else if(command.equals(MyProtocol.UNBLOCK)){
			unblock(line.substring(line.indexOf(" ")).trim());
		}else if(command.equals(MyProtocol.PRIVATE)){
			Debug.print("Server Thread private!!!!!");
			message = line.substring(line.indexOf(" ")).trim();
			privateSend(message);
		}else if(command.equals(MyProtocol.LOGOUT)){
			logout();
		}else if(command.equals(MyProtocol.PEER_TO_PEER_ANSWER)){
			String[] info = line.split(" ");
			p2pAnswer(info);
		}else{

		}
	}
	private void p2pAnswer(String[] info){
		String user = info[2];
		String message = "";
		if(Server.ipRequest.containsKey(userName) && Server.ipRequest.get(userName) != null && Server.ipRequest.get(userName).contains(user)){
			String[] tmp = Server.userHostNamePortNumber.get(userName); 
			if(info[1].equals("y")){
				message = MyProtocol.PEER_TO_PEER_ANSWER_ALLOW;
				message = message + " " + userName +" " + tmp[0] + " " + tmp[1];
			}else if(info[1].equals("n")){
				message = MyProtocol.PEER_TO_PEER_ANSWER_REJECT;
				message += " " + userName;
			}

			sendMessageToUser(user, message, true);	
			HashSet<String> hs = Server.ipRequest.get(userName);
			hs.remove(user);
			Server.ipRequest.put(userName, hs);
		}else{
			message = user + " has not request your ip address.";
			sendMessageToUser(userName, message, true);
		}
	}
	private void heartBeat(){
		Debug.print("HeartBeat " + userName);
		Server.heartBeat.put(userName, true);
	}
	private void message(String line){
		String sendToUser = line.split(" ")[0];
		Debug.print("message(): "+ sendToUser);
		String message = line.substring(line.indexOf(" ")).trim();	
		if(isValidUserName(sendToUser)){

			if(!isInBlackList(sendToUser, userName)){
				if(isUserOnline(sendToUser)){
					sendMessageToUser(sendToUser, message, false);
				}else{
					sendMessageToUser(sendToUser, message, false);
					sendMessageToUser(userName, MyProtocol.USER_OFFLINE, true);			
				}
			}else{
				sendMessageToUser(userName, MyProtocol.BLOCKED_BY_ANOTHER_USER_BROADCAST, true);
			}
		}else{
			sendMessageToUser(userName, MyProtocol.INVALID_USERNAME, true);
		}

	}
	private Boolean isValidUserName(String username){
		Debug.print("isValidUserName" + username);
		if(Server.userPass.keySet().contains(username)){
			return true;
		}else
			return false;
	}
	private Boolean isUserOnline(String user){
		if(Server.clients.keySet().contains(userName)){
			return true;
		}else
			return false;
	}
	//Judge whether user1 has blocked user2.
	private Boolean isInBlackList(String user1, String user2){
		if(Server.blackLists.keySet().contains(user1) &&Server.blackLists.get(user1).contains(user2)){
			return true;
		}else
			return false;
	}
	private void broadcast(String message){
		Debug.print("broadcast");
		Boolean isBlocked = false;
		for(String user : Server.clients.keySet()){
			if(!userName.equals(user)){
				if(!isInBlackList(user, userName)){
					sendMessageToUser(user,message, false);
				}else{
					isBlocked = true;
				}
			}
		}
		if(isBlocked){
			sendMessageToUser(userName, MyProtocol.BLOCKED_BY_ANOTHER_USER_BROADCAST, true);
		}
	}
	private void sendMessageToUser(String user, String message, Boolean fromServer){
		Debug.print("sendMessageToUser");
		if(Server.clients.containsKey(user)){
			Debug.print(user + "is online");
			String[] tmp = Server.userHostNamePortNumber.get(user); 
			String hostName = tmp[0];
			int port = Integer.parseInt(tmp[1]);
			if(!fromServer){
				message = userName + ": " + message;
			}
			try {
				Socket s = new Socket(hostName, port);
				PrintStream tps = new PrintStream(s.getOutputStream());
				tps.println(message);
				tps.close();
				s.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}else if(Server.userPass.keySet().contains(user)){
			Debug.print(user + " is offline. Saved offline message.");
			HashMap<String, String> tmp;
			String string;
			if(Server.offlineMessage.get(user) == null){
				tmp = new HashMap<String, String>();
			}else{
				tmp = Server.offlineMessage.get(user);				
			}
			if(tmp.containsKey(userName)){
				string = tmp.get(userName) + " ** " + message;
				tmp.put(userName, string);
			}else{
				tmp.put(userName, message);
			}
			Server.offlineMessage.put(user, tmp);
		}
	}
	private void getIPAddress(String user){
		if(isInBlackList(user, userName)){
			sendMessageToUser(userName, MyProtocol.BLOCKED_BY_ANDTHER_USER_GETIPADDRESS, true);
		}else{
			if(Server.clients.containsKey(user)){
				HashSet<String> tmp = new HashSet<String>();
				if(Server.ipRequest.containsKey(user)){
					if(Server.ipRequest.get(user) != null){
						tmp = Server.ipRequest.get(user);
					}
				}
				tmp.add(userName);
				Server.ipRequest.put(user, tmp);
				getIPApproval(user);
			}else{
				sendMessageToUser(userName, user + " is offline.", true);
			}
		}
	}
	private void block(String user){
		HashSet<String> tmp = Server.blackLists.get(userName);
		tmp.add(user);
		Server.blackLists.put(userName, tmp);
		sendMessageToUser(userName, user + MyProtocol.BLOCKE_OTHER_USER_SUCCESS, true);
	}
	private void unblock(String user){
		HashSet<String> tmp = Server.blackLists.get(userName);
		if(tmp.contains(user)){
			tmp.remove(user);
			sendMessageToUser(userName, user + MyProtocol.UNBLOCKE_OTHER_USER_SUCCESS, true);
			Server.blackLists.put(userName, tmp);
		}else{
			sendMessageToUser(userName, MyProtocol.UNBLOCKE_OTHER_USER_FAIL + user, true);
		}

	}
	private void getIPApproval(String user){

		String requestMessage = MyProtocol.PEER_TO_PEER_REQUEST + " " + userName +" want to have your address."
				+ " Do you agree? If yes please reply '@ y <user name>', otherwise reply @ n <user name>.";
		sendMessageToUser(user, requestMessage, true);

	}
	private void privateSend(String line){
		Debug.print("private send.");
		String sendToUser = line.split(" ")[0];

		String message = line.substring(line.indexOf(" ")).trim();
		message = line.substring(line.indexOf(" ")).trim();
		HashMap<String, String> tmp;
		if(Server.offlineMessage.get(sendToUser) == null){
			tmp = new HashMap<String, String>();	
		}else{
			tmp = Server.offlineMessage.get(sendToUser);
			if(Server.offlineMessage.get(sendToUser).containsKey(userName)){
				message = Server.offlineMessage.get(sendToUser).get(userName) + " ** " + message; 
			}
		}

		tmp.put(userName, message);
		Server.offlineMessage.put(sendToUser, tmp);
		Debug.print("private send: add offline message for " + sendToUser + " from " + userName + " message " + message);

	}
	private void logout(){
		sendMessageToUser(userName, MyProtocol.LOGOUT, true);
		Server.clients.remove(userName);
		Server.userHostNamePortNumber.remove(userName);
		Server.heartBeat.remove(userName);
		closeConnection();
	}
	private void online(){
		Debug.print("In online");
		Set<String> tmp = new HashSet<String>(Server.clients.keySet());
		tmp.remove(userName);
		String string = "Online users:";
		if(tmp.size() == 0){
			string = "You are the only online person.";
		}else{
			for(String name : tmp){
				Debug.print("Online user: " + name);
				string += " " + name;
			}
		}
		sendMessageToUser(userName, string, true);
	}
}
