package RUBTClient;

import java.util.*;

public class TrackerTalker extends TimerTask {
	int type = 0;
	
	public TrackerTalker(int i){
		type = i;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(type == 0){
			//use RUBT.querygen(port, uploaded, downloaded, tor.filelength - downloaded, "");
		}
		else if(type==1){
			//use RUBT.querygen(port, uploaded, downloaded, 0, "completed");
		}
		else{
			//use RUBT.querygen(port, uploaded, downloaded, tor.filelength - downloaded, "");
		}
	}

}
