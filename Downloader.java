package RUBTClient;

import java.net.Socket;
import RUBTClient.PeerInterface;

public class Downloader implements Runnable {
	public String download_ip = null;
	public int download_port = 0;

	public Downloader(String ip, int port){
		download_ip = ip;
		download_port = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket connection=null;
		try{
			Thread.sleep(30);
		}catch (InterruptedException e){

		}
		try {
		connection = new Socket(download_ip, download_port);
		} catch (Exception e) {
		}
		PeerInterface down = new PeerInterface(connection);
		PeerInterface.sendHandshake(RUBTClient.info_hash, RUBTClient.peer_id);
		//PeerInterface.sendBitField();

	}

}
