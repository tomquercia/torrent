package RUBTClient;

import java.net.Socket;

import RUBTClient.MessageInterpreter.*;
import RUBTClient.PeerInterface;

public class Downloader implements Runnable {
	public String download_ip = null;
	public int download_port = 0;
	public byte[] bitfieldEntries = {0,0,0,0,0};
	public Downloader(String ip, int port){
		download_ip = ip;
		download_port = port;
	}
	
	public void interpret(MessageInterpreter m) {
		if (m == null) { //no message exists
			try {
				Thread.sleep(500L + (long) (Math.random() * 10)); 
			} catch (Exception e) {
				// e.printStackTrace();
			}
			run(); //restart connection
			return;
		}			
		if (m.getId()==MessageInterpreter.bitfield) {
			BitfieldMessage bfm = (BitfieldMessage) m;
			//here we got a bitfield message, so set the peer's bitfield accordingly.
			return;
		}
		else if (m.getId()==MessageInterpreter.keepAlive){
			//No-op, just let timer handle it
			return;
		}
		else if (m.getId()==MessageInterpreter.choke){
			//We got a choked message, so set is as choked
			return;
		}
		else if (m.getId()==MessageInterpreter.unchoke){
			//got an unchoked message!
			return;	
		}		
		else if (m.getId()==MessageInterpreter.have){
			HaveMessage hvm = (HaveMessage) m;
			//got a have message, update accordingly
			return;
		}
		else if (m.getId()==MessageInterpreter.piece){
			PieceMessage pm = (PieceMessage) m;
			byte[] piece_data = pm.getData();
			//got a piece message, now we can do the saving, 
			//I'm not sure how to handle this, but I've included some handy functions in MessageInterpreter
			return;
		}
		else if (m.getId()==MessageInterpreter.interested){
			//Got an interested message, here we should:
			//check the number of unchoked, and if it's less than 5, add this peer to the number of unchoked
			//call the encode message with the peer we're sending to and the unchoke message specified
			//else we can add it to some kind of array of pending peers
			return;

		}
		else if (m.getId()==MessageInterpreter.notInterested){
			//nothing to do here (I think...)
			return;
		}
		else if (m.getId()==MessageInterpreter.request){
			
			//if the peer isn't choked, do RequestMessage whatever=(RequestMessage)m;
			//byte[] dataToSend = new byte[whatever.getBlockLength()];
			// then we need to generate a new piece message that we have to send out using PieceMessage
			//then do: MessageInterpreter.encode(peer, pieceMessage)
				return;
		}

		return;
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
		down.sendHandshake(RUBTClient.info_hash, RUBTClient.peer_id);
		if (!down.recieveHandshake().equals(RUBTClient.info_hash)){
			System.out.println("Handshake was unsuccessful");
			return;
		}
		else{
		down.sendMessage(5, bitfieldEntries); //send the bitfield
			MessageInterpreter d = null;
			while(true /*peer bitfield == null*/){
				try{
					//d =***** need to listen for an incoming message here, maybe make receiveUnknownMessage return a MessageInterpreter object?
					//I've placed a listen function in Uploader, that may be of great use.
				}
				catch (Exception e){
					run();
					return;
				}
				interpret(d);
			}

			//make an array to store the pieces seperately for use by the SHA-1 hash check
			int pieceAsWholeCounter = 0;
			int copyOfBytesToDownload = bytesToDownload;
			byte[][] pieceAsWhole = new byte[totalPieces][];
			while(pieceAsWholeCounter < totalPieces){
				if(copyOfBytesToDownload - (blocksInPieces*block_length)>-1){
					pieceAsWhole[pieceAsWholeCounter] = new byte[blocksInPieces*block_length];
					copyOfBytesToDownload = copyOfBytesToDownload - (blocksInPieces*block_length);
				}
				else if(copyOfBytesToDownload - (blocksInPieces*block_length) < 0){
					copyOfBytesToDownload = copyOfBytesToDownload - (block_length*(blocksInPieces-1));
					pieceAsWhole[pieceAsWholeCounter] = new byte[copyOfBytesToDownload + (block_length*(blocksInPieces-1))];
				}
				++pieceAsWholeCounter;
			}

			
			//now check if they are interested, listen for something from the peer, set it to 'd', then kick it out to interpret(d)
			int requestPieceCounter = 0;
			int requestBlockCounter = 0;
			int requestsSent = 0;
			int intoPieceArrayCounter = 0;

			while(requestPieceCounter < totalPieces){
				while(requestBlockCounter < blocksInPieces){

					if(bytesToDownload >= block_length){ // if the block to be requested is just the standard block length
						request(requestPieceCounter,(block_length*requestBlockCounter),block_length);
						System.out.println("REQUEST message sent");
						byte[] tempHolding = new byte [block_length];
						tempHolding = recievePiece();
						intoPieceArrayCounter = 0;

						while(intoPieceArrayCounter < tempHolding.length){//put the piece from recieve into the array for use by SHA-1 hash compare
							pieceAsWhole[requestPieceCounter][intoPieceArrayCounter+(block_length*requestBlockCounter)] = tempHolding[intoPieceArrayCounter];
							++intoPieceArrayCounter;
						}

						++requestsSent;
					}

					else if(bytesToDownload<block_length){ // if the data to requested is smaller than the regular block 
						request(requestPieceCounter,(block_length*requestBlockCounter),bytesToDownload);
						System.out.println("REQUEST message sent");
						byte[] tempHolding = new byte [bytesToDownload];
						tempHolding = recievePiece();
						intoPieceArrayCounter = 0;

						while(intoPieceArrayCounter < tempHolding.length){//put the piece from recieve into the array for use by SHA-1 hash compare
							pieceAsWhole[requestPieceCounter][intoPieceArrayCounter+(block_length*requestBlockCounter)] = tempHolding[intoPieceArrayCounter];
							++intoPieceArrayCounter;
						}

						++requestsSent;
					}
					++requestBlockCounter;
				}
				requestBlockCounter = 0;
				checkHash(pieceAsWhole[requestPieceCounter], requestPieceCounter);
				have(requestPieceCounter);
				System.out.println("HAVE message sent");
				++requestPieceCounter;
			}
			System.out.println("Client sent : " + requestsSent + " requests");

			System.out.println("Bytes remaining to be downloaded : "+bytesToDownload);
				
			
		}
	}

}
