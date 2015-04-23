
public interface MyProtocol {
	public final  int BLOCK_TIME = 60000; // 60s
	public final String WELCOME = "Welcome to simple chat server.";
	public final String LOGIN = "Login successfully."; // When the Server successfully record the IP address and port number of that user.
	public final String USERNAME = "Please note that you need to login in 1 min. Otherwise you need to restart program to login. Username: ";
	public final String PASSWORD = "Password: ";
	public final String INVALID_USERNAME = "Invalid Username. Please retry. Username:";
	public final String INVAILD_PASSWORD = "Invaild Password. Please retry. Password:";
	public final String BLOCK_ACCOUNT = "Invaild Password. Your account has been blocked please try again after "
										+ BLOCK_TIME/1000 +"s.";
	public final String BLOCK_ACCOUNT_ANOTHER_IP = "Due to multiple login failures. Your account has been blocked please try again after sometime.\n";
	public final String REAPTED_LOGIN = "Your account login with another IP. Offline!";
	public final String BLOCKED_BY_ANOTHER_USER_MESSAGE = "Your message could not be delivered as the recipent has blocked you.";
	public final String BLOCKED_BY_ANOTHER_USER_BROADCAST = "Your message could not be delivered to some recipents.";
	public final String BLOCKED_BY_ANDTHER_USER_GETIPADDRESS = "You are blocked by this recipents and cannot have his/her IP address and port number.";
	public final String BLOCKED_PRIVATE = "You are blocked by that user and can not send message to that user.";
	public final long HEARTBEAT_TIME_SERVER = 40000; 
	public final String PRIVATE_ONLINE = "$$";// If the user the current user wants to privately talk to is online.
	public final String BLOCKE_OTHER_USER_SUCCESS = " has been blocked successfully.";
	public final String UNBLOCKE_OTHER_USER_FAIL = "You hava not blocked ";
	public final String UNBLOCKE_OTHER_USER_SUCCESS = " has been unblocked successfully";
	public final String PEER_TO_PEER_REQUEST = "P2P_REQUEST";
	public final String PEER_TO_PEER_ANSWER_ALLOW = "@A";
	public final String PEER_TO_PEER_ANSWER_REJECT = "@R";
	public final String PEER_TO_PEER_ANSWER = "@";
	public final String USER_OFFLINE = " is offline.";
	public final String OFFLINEMESSAGE = "offline_message";
	//Commands
	public final String MESSAGE = "message";
	public final String BROADCAST = "broadcast";
	public final String ONLINE = "online";
	public final String BLOCK = "block";
	public final String UNBLOCK = "unblock";
	public final String LOGOUT = "logout";
	public final String GET_IP_ADDRESS = "getaddress";
	public final String PRIVATE = "private";
	public final long TIMEOUT = 60000; // User has to login in 1min, otherwise the server will close this connection.
	public final String HEARTBEAT = "heartbeat";
	public final String SHUTDOWN = "shutdown";
	public final String TIMEOUT_INFO = "Time out.";
}
