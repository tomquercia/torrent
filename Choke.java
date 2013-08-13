package RUBTClient;

import java.util.TimerTask;

public class Choke extends TimerTask {
	//Function to make a thread to decide when to choke/unchoke and whatnot

	public void run() {
		double rand = Math.random() * 3;
		Math.round(rand);
		//need to keep track of who we have choked and who isn't choked, as well as how long they've been choked for.
		
		/*
		 * for(i=0; all of our unchoked peers; i++){
		 * total = lastUploaded+lastDownloaded;
		 * set lastUploaded and lastDownloaded to zero;
		 * if(total<Integer.MIN_VALUE){
		 *  minIndex = i; 
		 * }
		 * }
		 * for(i=0; all of our peers that want to be unchoked; i++){
		 * total = lastUploaded+lastDownloaded;
		 * set lastUploaded and lastDownloaded to zero;
		 * if(total<Integer.MAX_VALUE){
		 *  maxIndex = i; 
		 * }
		 * }
		 * 
		 * peer addingPeer = null; peer deletingPeer=null;
		 * if(maxIndex != -1){
		 * addingPeer = our choked peer at location (maxindex) in the arraylist of choked peers (we will be unchoking this peer)
		 * }
		 * if(minIndex != -1){
		 * deletingPeer = our unchoked peer at location (minIndex) in the arraylist of unchoked peers ( we will be choking this peer)
		 * }
		 * 
		 * if(addingPeer==null || deletingPeer==null){ <------we only want to remove/replace peers if we have two candidates, i.e. don't delete until we can replace it with something else
		 * return;
		 * }
		 * else{
		 * Messageinterpreter.encode(addingPeer, MessageInterpreter.unchoke);
		 * addingPeer.amChoked = false;
		 * add addingPeer to our list of unchoked peers
		 * remove addingPeer from our list of choked peers waiting to be unchoked
		 * 
		 * MessageInterpreter.encode(deletingPeer, MessageInterpreter.choke);
		 * deletingPeer.amChoked = true;
		 * add deletingPeer to our list of choked peers
		 * remove deletingPeer from our list of unchoked.
		 * 
		 * }
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
	}

}
