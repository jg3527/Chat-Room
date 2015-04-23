import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class ClientThread extends Thread{
	private Socket connectionSocket;
	private BufferedReader inFromServerOrPeer;
	private PrintStream ps;

	public ClientThread (Socket connectionSocket) throws IOException{
		this.connectionSocket = connectionSocket;
		this.inFromServerOrPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		this.ps = new PrintStream(connectionSocket.getOutputStream());

	}

	public void run(){
		String line;
		Debug.print("ClientThread.");
		try {
			if((line = inFromServerOrPeer.readLine()) != null){
				String command = line.split(" ")[0];
				if(line.equals(MyProtocol.SHUTDOWN)){
					System.out.println("Server shutdown.");
					closeConnection();
					System.exit(0);
				}else if(line.equals(MyProtocol.REAPTED_LOGIN)){
					System.out.println("> " + line);
					closeConnection();
					System.exit(0);
				}else if(command.equals(MyProtocol.PEER_TO_PEER_REQUEST)){
					System.out.println(line.substring(line.indexOf(" ")).trim());
					Debug.print("P2p answer : ");
				}else if(command.equals(MyProtocol.PEER_TO_PEER_ANSWER_ALLOW)){
					String[] info = line.split(" ");
					System.out.println(info[1] + " --- Hostname: " + info[2] + " Port number: " + info[3]);
					String[] t ={info[2], info[3]}; 
					Client.peer.put(info[1], t);
				}else if(command.equals(MyProtocol.PEER_TO_PEER_ANSWER_REJECT)){
					System.out.println("Rejected by " + line.split(" ")[1]);
				}else if(command.equals(MyProtocol.LOGOUT)){
					System.out.println("> Successfully logout. If you want to login please restart this program.");
					closeConnection();
					Client.hbTimer.cancel();
					System.exit(0);					
				}
				else{
					System.out.println("> " + line);
				}
			}
		} catch (IOException e) {
			Debug.print(e.getMessage());
		}
		closeConnection();

	}
	private void closeConnection()
	{
		try{
			inFromServerOrPeer.close();
		}catch(IOException e){
			
		}
		try {
				ps.close();			
			connectionSocket.close();
		} catch (IOException e) {
			Debug.print(e.getMessage());
		}
	}
}

