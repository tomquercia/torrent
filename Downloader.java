package RUBTClient;

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
		
		
	}

}
