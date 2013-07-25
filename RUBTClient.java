/*RUBT CLIENT by Naqvi,Syed and Quercia,Tom
 * 
 * 
 *   Expectations about input:
 * 1.argv[0] should be the path of the torrent file in the format C:\\DirectoryParent\\DirectoryChild\\cs352.png.torrent
 * 
 * 2.argv[1] should be what the user wants the name of the PNG file to be, IE : printerpic.png 
 * 			the resulting file will be stored in the parent directory of this file, unless other file path is provided as argv[1] 
 *  		if the desired file location is not the default, please enter the path in the format C:\\DirectoryParent\\DirectoryChild\\printerpic.png
 *  
 *  
 */

package RUBTClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RUBTClient {

	public static byte[] unKnownMessage;
	public static byte[] info_hash = new byte[20];
	public static byte[] peer_id = new byte[20];
	public static byte[] download_bytes;
	public static ByteBuffer[] piece_hashes;
	static int bytesDownloaded = 0;
	static File download;
	static String peerToUse = null;
	static String peerToUse2 = null;
	static int portToUse = 0;
	static int portToUse2 = 0;
	static DataOutputStream toPeer = null;
	static DataInputStream fromPeer = null;
	static TorrentInfo tor = null;
	static int block_length = 16384;
	static boolean[] bitField;
	static int bytesToDownload = 0;
	static Socket socket = null;
	static int totalPieces;
	static int port = 0;
	static int blocksInPieces;
	static int totalBlocks;
	public static int[] cur_piece;
	public static byte[] peer_info_hash = new byte[20];

	public static void main(String argv[]) throws UnknownHostException, IOException, BencodingException{
		if(argv.length != 2){
			System.out.println("Error: Invalid number of arguments");
			return;	
		}
		String torrentFile = argv[0]; 
		System.out.println("Using torrent file " + torrentFile); //print name of file to make sure we are operating on the right one
		String fileToDownload = argv[1];

		/*
		 * 
		 * Decoding and processing the metadata of the torrent file
		 * 
		 */

		try { //try to open and decode the torrent file
			download = new File(fileToDownload);
			tor = new TorrentInfo(readTorrent(torrentFile));
			tor.info_hash.get(info_hash,0,info_hash.length);
		} catch (BencodingException e) {
			System.out.println("Error opening Torrent File");
			System.exit(1);
		}
		System.out.println("Used TorrentInfo to decode torrent file");

		//generate a 20 digit random string for our peer id
		Random ran = new Random();
		int randomNum = ran.nextInt(7777777 - 3333333 + 1) + 3333333;
		String peerIdentification = "LegendofZelda" + randomNum; //string must be of exactly length 13, plus a random 7 digit number
		peer_id = peerIdentification.getBytes(); //change the peer id into bytes
		System.out.println("Made a peer ID" + peerIdentification + " and converted it to bytes");

		blocksInPieces = tor.piece_length/block_length; //calculate the number of blocks in a given piece
		bytesToDownload = tor.file_length % block_length; //calculate bytes we still need to download
		totalPieces = bytesToDownload == 0 ? tor.file_length/tor.piece_length:tor.file_length/tor.piece_length + 1; //calculates total pieces to be downloaded
		totalBlocks = (int) Math.ceil(tor.file_length/block_length); //total blocks that need to be downloaded
		bytesToDownload = tor.file_length;
		cur_piece = new int[totalPieces]; //array to be used to keep track of our current piece
		download_bytes = new byte[tor.file_length];
		piece_hashes = tor.piece_hashes;

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



		/*
		 * 
		 * Start the connection with the peer 
		 * 
		 */



		talkToTracker(); //contact the tracker to retrieve information
		System.out.println("Sent STARTING message to tracker");

		try {
			socket = new Socket(peerToUse, portToUse); //open the socket with the peer
		} catch (Exception e) {}

		try {
			toPeer = new DataOutputStream(socket.getOutputStream()); //open data stream to write to peer
			fromPeer = new DataInputStream(new BufferedInputStream(socket.getInputStream())); //open data stream to accept from peer
		} catch (Exception e) {}

		//do a handshake with the peer.
		handshake();

		//get the peer's handshake
		byte[] peerHandshake = new byte[68];
		fromPeer.readFully(peerHandshake);

		int tempCounter = 0;
		while(tempCounter<20) { //put peer's infohash into peer_info_hash 
			peer_info_hash[tempCounter] = peerHandshake[28+tempCounter];
			++tempCounter;
		}

		//check to see if the info hashes are equal
		if(Arrays.equals(peer_info_hash, info_hash)==false){
			System.out.println("info hash of peer was not equal to info hash of the torrent metadata");
			toPeer.close();
			fromPeer.close();
		}

		else{
			System.out.println("HANDSHAKE response from peer was recieved");
			choke();
			System.out.println("CHOKE message sent");
			interested();
			System.out.println("INTERESTED message sent");

			recieveUnchoke();


			/*
			 * Start requesting, recieving, and processing the pieces
			 */



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


			/*
			 * Tell the tracker that we finished the download 
			 */


			int ir = 0;
			byte[] responseToEnd;
			for (ir = 6881; ir <= 6889;) {
				try {
					responseToEnd = getURL(queryGen(ir, 0, bytesDownloaded,bytesToDownload, "completed")); //try to get the response from the tracker
					port=ir; //bind the port
					break;
				} catch (Exception e) {
					ir++;
					continue;
				}
			}
			System.out.println("Sent COMPLETED message to tracker");

			//write the actual image
			System.out.println("Writing data to image");
			bytesToImage();
			System.out.println("Image created - client now closing connections");

			//close all the connections
			fromPeer.close();
			toPeer.close();
			socket.close();
			System.out.println("Connections closed, exiting");
		}
	}



	public static int toInt(String s){ //convenience method to convert strings to ints
		int ret = Integer.parseInt(s);
		return ret;
	}

	//given function to decode the binary into strings
	public static String[] decodeCompressedPeers(Map map)
	{
		ByteBuffer peers = (ByteBuffer)map.get(ByteBuffer.wrap("peers".getBytes()));
		ArrayList<String> peerURLs = new ArrayList<String>();
		try {
			while (true) {
				String ip = String.format("%d.%d.%d.%d",
						peers.get() & 0xff,
						peers.get() & 0xff,
						peers.get() & 0xff,
						peers.get() & 0xff);
				int port = peers.get() * 256 + peers.get();
				peerURLs.add(ip + ":" + port);
			}
		} catch (BufferUnderflowException e) {
			// done
		}
		return peerURLs.toArray(new String[peerURLs.size()]);
	}

	public static void talkToTracker() throws BencodingException {
		int i = 0;
		byte[] response = null; //response from tracker
		for (i = 6881; i <= 6889;) {
			try {
				response = getURL(queryGen(i, 0, 0,tor.file_length, "started")); //try to get the response from the tracker
				System.out.println(i); //for testing, lets us know which port we're operating on
				port=i; //bind the port
				break;
			} catch (Exception e) {
				i++;
				continue;
			}
		}
		String[] test = decodeCompressedPeers((Map) Bencoder2.decode(response)); //may need to parameterize the Map cast...
		for (int v = 0; v<test.length;v++){
			if(test[v].substring(0,11).equals("128.6.171.3")){
				peerToUse = test[v].substring(0,11);
				portToUse = toInt(test[v].substring(12));
				System.out.println("IP to connect to : "+peerToUse + "\n"+ "Port to connect to : "+portToUse);
			}
			if(test[v].substring(0,11).equals("128.6.171.3")){
				peerToUse2 = test[v].substring(0,11);
				portToUse2 = toInt(test[v].substring(12));
				System.out.println("Secondary IP to connect to : "+peerToUse2+"\n"+"Port to connect to : "+portToUse2);
			}
			System.out.println("List of Peers : " + test[v]); //printing out the found peers.
		}

		/*
		 * here we can populate an array of peers or something, but for this project, we just need to check for 128.6.171.3 and use that one.
		 * 
		 */
	}

	public static byte[] getURL(String string_url) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			int n;
			byte[] byteChunk = new byte[4096];
			URL url = new URL(string_url);
			in = url.openStream(); //read 

			while ((n = in.read(byteChunk)) > 0) {
				out.write(byteChunk, 0, n); //keep writing byte chunks as long as they exist.
			}

			in.close();
		} catch (IOException e) {
		}

		return out.toByteArray();
	}

	public static String queryGen(int port, int uploaded, int downloaded,int left, String event) { //generates and concatenates a query given the inputs
		String queryURL = "";
		try {
			String escaped_id = urlHexConverter(peer_id); //convert the peer id into an escaped hex hash
			String escaped_hash = urlHexConverter(info_hash); //convert the infohash into an escaped hex hash
			InetAddress myIP = InetAddress.getLocalHost(); 
			queryURL = tor.announce_url.toString() + "?port=" + port + "&peer_id=" + escaped_id + "&info_hash=" + escaped_hash + "&uploaded=" + uploaded + "&downloaded=" + downloaded + "&left=" + left;
			if (event.length() != 0) {
				queryURL += "&event=" + event;
			}

		} catch (Exception e) {
		}

		return queryURL;
	}
	public static final char[] Hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; //define hexadecimal characters

	public static String urlHexConverter(byte[] bytes) { //convert the hex for the escaped values
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < bytes.length; ++i) {
			buff.append('%').append(Hex[(bytes[i] >> 4 & 0x0F)]).append(Hex[(bytes[i] & 0x0F)]);
		}
		return buff.toString();
	}

	public static byte[] readTorrent(String torrentFile) { //read in the torrent file into a byte array
		try {
			RandomAccessFile torrent = new RandomAccessFile(torrentFile, "rw");
			byte[] ret = new byte[(int) torrent.length()];
			torrent.read(ret);
			torrent.close();
			return ret;
		} catch (IOException e) {
			return null;
		}
	}

	public static void handshake(){ //send the HANDSHAKE message
		int size = 0;
		byte handshakeOut[] = new byte[68];
		byte[] empty = new byte[8];
		handshakeOut[0] = 0x13;
		size++;
		byte temp[] = new String("BitTorrent protocol").getBytes();
		System.arraycopy(temp, 0, handshakeOut, size, temp.length);
		size += temp.length;
		System.arraycopy(empty, 0, handshakeOut, size, empty.length);
		size += empty.length;
		System.arraycopy(info_hash, 0, handshakeOut, size, info_hash.length);
		size += info_hash.length;
		System.arraycopy(peer_id, 0, handshakeOut, size, peer_id.length);
		size += peer_id.length;
		try {
			toPeer.write(handshakeOut);
		} catch (Exception e) {}
	}

	public static void choke(){ //send the CHOKE message
		byte[] chokeOut = {0x0,0x0,0x0,0x1,0x0};
		try{
			toPeer.write(chokeOut);
		} catch(Exception e) {System.out.println("Error occured with choke, "+ e);}
	}

	public static void keepAlive(){
		byte[] keepAliveOut = {0x0,0x0,0x0,0x0};
		try{
			toPeer.write(keepAliveOut);
		} catch(Exception e){}
	}

	public static void interested(){//send the INTERESTED message
		byte[] interestedOut = {0x0,0x0,0x0,0x1,0x2};
		try{
			toPeer.write(interestedOut);
		} catch(Exception e) {}		
	}

	public static void have(int pieceFinished){
		byte[] haveMessage = new byte[9];
		haveMessage[0] = 0x0;
		haveMessage[1] = 0x0;
		haveMessage[2] = 0x0;
		haveMessage[3] = 0x5;
		haveMessage[4] = 0x4;
		byte[] pieceFinishedByteArray = intToByteArray(pieceFinished);

		int haveMessageCounter = 0;
		while(haveMessageCounter < 4){
			haveMessage[5 + haveMessageCounter] = pieceFinishedByteArray[haveMessageCounter];
			++haveMessageCounter;
		}
		try{
			toPeer.write(haveMessage);
		} catch(Exception e) {}
	}

	public static void request(int pieceToRequest, int offsetToRequest, int lengthToRequest){
		byte[] requestMessage = new byte[17];
		try{
			requestMessage[0]=0x0;
			requestMessage[1]=0x0;
			requestMessage[2]=0x0;
			requestMessage[3]=0xd;
			requestMessage[4]=0x6;

			//add the piece request into the message to send
			byte[] pieceToRequestByteArray = intToByteArray(pieceToRequest);
			int requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[5+requestTempCounter] = pieceToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//add the offset to request to the message
			byte[] offsetToRequestByteArray = intToByteArray(offsetToRequest);
			requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[9+requestTempCounter] = offsetToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//add the requested length
			byte[] lengthToRequestByteArray = intToByteArray(lengthToRequest);
			requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[13+requestTempCounter] = lengthToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//write the actual message
			toPeer.write(requestMessage);

		} catch(Exception e){System.out.println("Error occured at request" + e);}
	}

	/*public static void arrayValuePrinter(byte[] toPrint){ // a convenience method to print out all the values in a byte array
		int tempInt2 = 0;
		while(tempInt2<toPrint.length){
			System.out.print(toPrint[tempInt2]);
			++tempInt2;
		}
		System.out.println();
	}*/

	public static void recieveUnchoke(){
		boolean switchBool = false;
		byte[] recievingMessage = new byte[5];

		try{
			while(switchBool == false){
				recievingMessage[1]= fromPeer.readByte();
				recievingMessage[1]= fromPeer.readByte();
				recievingMessage[2]= fromPeer.readByte();
				recievingMessage[3]= fromPeer.readByte();
				recievingMessage[4]= fromPeer.readByte();

				if(recievingMessage[3] == 0x1 && recievingMessage[4] == 0x1){
					System.out.println("UNCHOKE message recieved");
					switchBool = true;
				}

				else{
					recievingMessage[0]=recievingMessage[1];
					recievingMessage[1]=recievingMessage[2];
					recievingMessage[2]=recievingMessage[3];
					recievingMessage[3]=recievingMessage[4];
					recievingMessage[4] = fromPeer.readByte();
				}
			}
		} catch(Exception e){}
	}

	public static byte[] recievePiece(){
		boolean switchBool = false;
		byte first = 0; 
		byte second = 0; 
		byte third = 0; 
		byte fourth = 0; 
		byte fifth = 0;
		byte[] pieceData = null;

		while(switchBool == false){ //keep looping until the ID byte of the message is found to be 7, indicating an incoming piece
			try{
				first = fromPeer.readByte();
				second = fromPeer.readByte();
				third = fromPeer.readByte();
				fourth = fromPeer.readByte();
				fifth = fromPeer.readByte();
			}catch(Exception e){System.out.println("error occurred at recieve initial 5 bytes " +e);}

			if(fifth == 0x7){//when a piece is detected
				System.out.println("PIECE message recieved");
				switchBool = true;

				//seperate out the different parts of the message, offset, index, actual data
				byte[] messageLength = {first, second, third, fourth};
				int pieceLength = ByteBuffer.wrap(messageLength).getInt() - 9;
				pieceData = new byte[pieceLength];
				byte[] pieceIndex = new byte[4];
				byte[] pieceOffset = new byte[4];
				int aTempInt= 0;

				while(aTempInt<4){//fill in the piece Index
					try{pieceIndex[aTempInt] = fromPeer.readByte();}catch(Exception e){System.out.println("error occurred at recieve - index " +e);}
					++aTempInt;
				}
				int pieceIndexInt = byteArrayToInt(pieceIndex);

				aTempInt= 0;
				while(aTempInt<4){//fill in the piece offset
					try{pieceOffset[aTempInt] = fromPeer.readByte();}catch(Exception e){System.out.println("error occurred at recieve -offset " +e);}
					++aTempInt;
				}
				int pieceOffsetInt = byteArrayToInt(pieceOffset);

				aTempInt = 0;
				while(aTempInt<pieceLength){//get the actual data of the piece
					try{pieceData[aTempInt] = fromPeer.readByte();}catch(Exception e){System.out.println("error occurred at recieve - data " +e);}
					++aTempInt;
				}

				int transferCounter = 0;
				while(transferCounter<pieceLength){//transfer the bytes into the download_bytes class array and update the information about bytes in possession

					download_bytes[(pieceIndexInt*block_length*2)+pieceOffsetInt+transferCounter] = pieceData[transferCounter];
					++transferCounter;
					++bytesDownloaded;
					--bytesToDownload;
				}

			}
			else{
				int tempComputeInt;
				System.out.println("did not get a piece message");
				first=second;
				tempComputeInt = 0;
				second=third;
				tempComputeInt = 2;
				third=fourth;
				tempComputeInt = 3;
				fourth=fifth;
				tempComputeInt = 5;
				try{fifth = fromPeer.readByte();}catch(Exception e){System.out.println("error occurred at recieve - getting a new byte " +e);}
				System.out.println("got a new byte");
				System.out.println("values are now "+first+second+third+fourth+fifth);
			}
		}
		return pieceData;
	} 

	public static void checkHash(byte[] pieceDownloaded, int pieceID){//make the hash of the given piece and compare it to its counterpart from the metadata
		MessageDigest SHA1Hasher;
		byte[] hashedPiece = new byte[20];
		boolean result;
		try{
			SHA1Hasher = MessageDigest.getInstance("SHA1");
			hashedPiece = SHA1Hasher.digest(pieceDownloaded);
			result = Arrays.equals(hashedPiece, piece_hashes[pieceID].array());
			if(result == true){
				System.out.println("PIECE "+pieceID+" hash is equal to info hash");
			}
			else{
				System.out.println("PIECE hash is NOT equal to info hash");
			}
		}catch(Exception e){System.out.println("an error ocurred when checking the hashes "+e);}
	}

	public static byte[] intToByteArray(int toByteArray){ //convenience method to make ints into byte arrays
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{dos.writeInt(toByteArray);}catch(Exception e){System.out.println("error occured with the in to byte array converter "+e);}
		byte[] returnMe = baos.toByteArray();
		return returnMe;
	}

	public static int byteArrayToInt(byte[] toInt){//convenience method to make byte arrays into ints
		ByteBuffer convertToInt = ByteBuffer.wrap(toInt);
		int retAsInt = convertToInt.getInt();
		return retAsInt;
	}

	public static void bytesToImage(){
		try{
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(download_bytes));
			ImageIO.write(image, "png", download);
		}catch(Exception e){System.out.println("an error occured writing the bytes to a file "+e);}
	}

}


