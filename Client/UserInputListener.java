import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class UserInputListener extends Thread{
	private Socket connectionSocket;
	private BufferedReader inFromUser;
	private PrintStream ps;
	public UserInputListener(){
		inFromUser = Client.inFromUser;
	}
	public void run(){
		String line;
		Boolean legal = true;

		Debug.print("User input listener");
		try {
			while((line = inFromUser.readLine().trim()) != null){
				Debug.print("user's input: ");
				String[] tmp = line.split(" ");
				//check whether the command is legal:
				legal = isCommandLegal(tmp);

				if(!legal){
					if(tmp[0].equals("^C")){
						inFromUser.close();
						break;
					}
					System.out.println(MyProtocol.INVAILD_INPUT);
				}else if(tmp[0].equals(MyProtocol.PRIVATE)){
					if(Client.peer.containsKey(tmp[1])){
						String[] info = Client.peer.get(tmp[1]);
						privateSendMessage(info, line.substring(line.indexOf(tmp[1])).trim(), tmp[1]);

					}else{
						Debug.print(tmp[1] + "???");
						System.out.println("You can not privately talk to this user. Please use command getaddress <user name> first");
					}
				}else{
					sendMessageToServer(line);	
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Boolean isCommandLegal(String tmp[]){
		Boolean legal = true;
		if(tmp[0].equals(MyProtocol.MESSAGE)){
			if(tmp.length < 3 || tmp[1].equals(Client.userName) ){
				System.out.println(MyProtocol.INVAILD_INPUT);
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.BROADCAST)){
			if(tmp.length < 2){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.BLOCK)){
			if(tmp.length != 2 || tmp[1].equals(Client.userName)){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.UNBLOCK)){
			if(tmp.length != 2 || tmp[1].equals(Client.userName)){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.GET_IP_ADDRESS)){
			if(tmp.length != 2 || tmp[1].equals(Client.userName)){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.ONLINE)){
			if(tmp.length != 1){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.PRIVATE)){
			if(tmp.length < 3 || tmp[1].equals(Client.userName)){
				legal = false;
			}
		}else if(tmp[0].equals(MyProtocol.PEER_TO_PEER_ANSWER)){
			Debug.print("islegal? PEER_TO_PEER_ANSWER");
			if(!tmp[1].toLowerCase().equals("y")  && !tmp[1].toLowerCase().equals("n") || tmp.length != 3){
				legal = false;
			}
		}else if(!tmp[0].equals(MyProtocol.LOGOUT)){
			legal = false;
		}
		return legal;
	}
	private void privateSendMessage(String[] info, String message, String sendToUser){
		message = Client.userName + " (private): " + message.substring(message.indexOf(" "));
		Debug.print("private send message user name: "  + Client.userName);
		try {
			connectionSocket = new Socket(info[0], Integer.parseInt(info[1]));
			ps = new PrintStream(connectionSocket.getOutputStream());
			ps.println(message);

		} catch (IOException e) {
			if(Client.peer.containsKey(sendToUser)){
				Client.peer.remove(sendToUser);
			}
			System.out.println("This user is offline or changed address. Message send to the server instead");			
			sendMessageToServer(MyProtocol.PRIVATE + " " + sendToUser +" " + message);
			Debug.print(e.getMessage());
		}


		try {

				ps.close();
			connectionSocket.close();

		} catch (IOException e) {
			Debug.print(e.getMessage());
		}

	}
	public void sendMessageToServer(String message){
		try {
			connectionSocket = new Socket(Client.serverName, Client.serverPortNumber);
			ps = new PrintStream(connectionSocket.getOutputStream());
			ps.println(message);

		} catch (UnknownHostException e) {
			try {
				inFromUser.close();
			} catch (IOException e1) {
			}
			System.out.println("Can not find Server");
			Debug.print(e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			try {
				inFromUser.close();
			} catch (IOException e1) {
			}
			System.out.println("Cannot connect to Server.");
			Debug.print(e.getMessage());
			System.exit(0);
		}


		try { 
			
				ps.close();
			connectionSocket.close();

		} catch (IOException e) {
			Debug.print(e.getMessage());
		}
	}
}
