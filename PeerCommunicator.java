package RUBTClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerCommunicator implements Runnable {
	
	public int port;
	public DataInputStream incoming;
	public DataOutputStream outgoing;
	public Socket data;
	public ServerSocket listener;
	
	public PeerCommunicator(int port){
		this.port=port;
		try{
			listener=new ServerSocket(port);
		} catch (Exception e){
			
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

}
