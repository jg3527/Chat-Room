
public interface MyProtocol {
public int HEATBEAT_TIME = 30000;
//Server Side
public final String OFFLINEMESSAGE = "offline_message";
public final String SHUTDOWN = "shutdown";
public final String PEER_TO_PEER_REQUEST = "P2P_REQUEST";
public final int BLOCK_TIME = 60000; // 60s
public final String WELCOME = "Welcome to simple chat server.";
public final String LOGIN = "Login successfully.";
public final String USERNAME = "Please note that you need to login in 1 min. Otherwise you need to restart program to login. Username: ";
public final String PASSWORD = "Password: ";
public final String INVALID_USERNAME = "Invalid Username. Please retry. Username:";
public final String INVAILD_PASSWORD = "Invaild Password. Please retry. Password:";
public final String BLOCK_ACCOUNT = "Invaild Password. Your account has been blocked please try again after "
									+ BLOCK_TIME/1000 +"s.";
public final String BLOCK_ACCOUNT_ANOTHER_IP = "Due to multiple login failures. Your account has been blocked please try again after sometime.";
public final String REAPTED_LOGIN = "Your account login with another IP. Offline!";
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
public final long HEARTBEAT_CLIENT = 30000;
public final String PEER_TO_PEER_ANSWER= "@";
public final String PEER_TO_PEER_ANSWER_ALLOW = "@A";
public final String PEER_TO_PEER_ANSWER_REJECT = "@R";
public final String INVAILD_INPUT = "Invalid input, please try again.";
public final String TIMEOUT_INFO = "Time out.";
}
