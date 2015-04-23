import java.util.TimerTask;
import java.util.Timer;

public class ServerHeartBeatThread extends Thread {
	private String userName;
	private Timer timer;
	public ServerHeartBeatThread(String userName){
		this.userName = userName;
		this.timer = new Timer();
	}
public void run(){
	TimerTask heartBeat = new TimerTask(){

		@Override
		public void run() {
			Debug.print("Server heartbeat.");
			if(Server.heartBeat.containsKey(userName) && Server.heartBeat.get(userName)){
				Server.heartBeat.put(userName, false);
			}else{
				Debug.print("Heartbeat kill " + userName);
				Server.clients.remove(userName);
				Server.userHostNamePortNumber.remove(userName);
				Server.heartBeat.remove(userName);
				timer.cancel();

			}
			
		}
		
	};
	timer.schedule(heartBeat, 0, MyProtocol.HEARTBEAT_TIME_SERVER);
}
}
