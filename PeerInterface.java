/* PeerInterface
 *
 * TODO : make a method recieveUnknownMessage that identifies a message and runs the relevant function (ie recieveHandshake, recievePiece)
 * make method recieveHandshake() to recieve handshakes from a potential peer
 * make a method that runs a different message method based on input
 * make methods for recieving and sending bittorrent protocol messages that haven't been coded yet
 * create a test client to check compatibility with this class
 *
 *
 *
 *
 *
 *
 */


package RUBTClient;

import java.nio.ByteBuffer;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.net.ServerSocket;

public class PeerInterface {

	public static int keepAlive = -1;
	public static int choke = 0;
	public static int unchoke = 1;
	public static int interested = 2;
	public static int notInterested = 3;
	public static int have = 4;
	public static int bitfield = 5;
	public static int request = 6;
	public static int piece = 7;
	public static int handshake = 13;
	static DataOutputStream toPeer = null;
	static DataInputStream fromPeer = null;
	
	PeerInterface(Socket connection){
		try{
			toPeer = new DataOutputStream(connection.getOutputStream());
			fromPeer = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
		}
		catch (Exception e) {System.out.println("ERROR occured at the PeerInterface PEER constructor when assiging toPeer and fromPeer : " + e);}
	}
	
	PeerInterface(ServerSocket serverSocket){
		try{
			Socket connection = serverSocket.accept();
			toPeer = new DataOutputStream(connection.getOutputStream());
			fromPeer = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
		}
		catch (Exception e) {System.out.println("ERROR occured at the PeerInterface SERVER constructor when assiging toPeer and fromPeer : " + e);}
	}

	public static void sendMessage(int messageType){//Overloaded method to send different messages based on messageType
		if(messageType == keepAlive){
			keepAlive();
		}
		else if(messageType == choke){
			choke();
		}
		else if(messageType == unchoke){
			unchoke();
		}
		else if(messageType == interested){
			interested();
		}
		else if(messageType == notInterested){
			uninterested();
		}
		else{
			System.out.println("Not sure what message type that was, try again with some parameters perhaps?");
		}
	}

	public static void sendMessage(int messageType, int pieceToRequestParam, int offsetToRequestParam, int lengthToRequestParam){//should be for request messages
		if(messageType == request){
			request(pieceToRequestParam, offsetToRequestParam, lengthToRequestParam);
		}
		else{
			System.out.println("something odd happened at SENDMESSAGE() - FOR REQUEST");
		}
	}

	public static void sendMessage(int messageType, byte[] info_hashParam, byte[] peer_idParam){//should be for handshakes
		if(messageType == handshake){
			sendHandshake(info_hashParam, peer_idParam);
		}
		else{
			System.out.println("something odd happened at SENDMESSAGE() - FOR HANDSHAKE");
		}
	}

	public static void sendMessage(int messageType, int pieceHaveParam){//should be for have
		if(messageType == have){
			have(pieceHaveParam);
		}
		else{
			System.out.println("something odd happened at SENDMESSAGE() - FOR HAVE");
		}
	}

	public static void sendMessage(int messageType, byte[] bitfieldToSendParam){//for bitfield
		if(messageType == bitfield){
			sendBitfield(bitfieldToSendParam);
		}
		else{
			System.out.println("something odd happened at SENDMESSAGE() - FOR BITFIELD");
		}
	}
	
	public static void sendMessage(int messageType, byte[] indexPieceParam, byte[] beginPieceParam, byte[] pieceToSendDataParam){//for piece message
		if(messageType == piece){
			piece(indexPieceParam, beginPieceParam, pieceToSendDataParam);
		}
		else{
			System.out.println("something odd happened at SENDMESSAGE() - FOR PIECE");
		}
	}	
	
	public static void sendHandshake(byte[] info_hash, byte[] peer_id){ //send the HANDSHAKE message
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

	public static void unchoke(){ //send the UNCHOKE message
		byte[] unchokeOut = {0x0,0x0,0x0,0x1,0x1};
		try{
			toPeer.write(unchokeOut);
		} catch(Exception e) {System.out.println("Error occured with unchoke, "+ e);}
	}
	
	public static void keepAlive(){//send the KEEPALIVE message
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

	public static void uninterested(){ //send the UNINTERESTED message
		byte[] uninterestedOut = {0x0,0x0,0x0,0x1,0x3};
		try{
			toPeer.write(uninterestedOut);
		} catch(Exception e) {System.out.println("Error occured with uninterested, "+ e);}
	}
	
	public static void bitfield(byte[] piecesFinished){//send BITFIELD message
		byte[] bitfieldOut = new byte[piecesFinished.length+5];
		byte[] bitfieldLenArray = RUBTUtilities.intToByteArray(piecesFinished.length + 1);
		bitfieldOut[0] = bitfieldLenArray[0];
		bitfieldOut[1] = bitfieldLenArray[1];
		bitfieldOut[2] = bitfieldLenArray[2];
		bitfieldOut[3] = bitfieldLenArray[3];
		bitfieldOut[4] = 0x5;
		int bitfieldCounter = 0;
		while(bitfieldCounter<piecesFinished.length){
			bitfieldOut[5+bitfieldCounter]=piecesFinished[bitfieldCounter];
			++bitfieldCounter;
		}
		try{
			toPeer.write(bitfieldOut);
		} catch(Exception e) {System.out.println("Error occured with bitfield, "+ e);}
	}
	
	public static void have(int pieceFinished){//send the HAVE message
		byte[] haveMessage = new byte[9];
		haveMessage[0] = 0x0;
		haveMessage[1] = 0x0;
		haveMessage[2] = 0x0;
		haveMessage[3] = 0x5;
		haveMessage[4] = 0x4;
		byte[] pieceFinishedByteArray = RUBTUtilities.intToByteArray(pieceFinished);

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
			byte[] pieceToRequestByteArray = RUBTUtilities.intToByteArray(pieceToRequest);
			int requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[5+requestTempCounter] = pieceToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//add the offset to request to the message
			byte[] offsetToRequestByteArray = RUBTUtilities.intToByteArray(offsetToRequest);
			requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[9+requestTempCounter] = offsetToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//add the requested length
			byte[] lengthToRequestByteArray = RUBTUtilities.intToByteArray(lengthToRequest);
			requestTempCounter = 0;
			while(requestTempCounter < 4){
				requestMessage[13+requestTempCounter] = lengthToRequestByteArray[requestTempCounter];
				++requestTempCounter;
			}

			//write the actual message
			toPeer.write(requestMessage);

		} catch(Exception e){System.out.println("Error occured at request" + e);}
	}

	public static void piece(byte[] index, byte[] begin, byte[] pieceToSend){
		byte[] pieceOut = new byte[pieceToSend.length + 13];
		byte[] pieceOutLength = RUBTUtilities.intToByteArray(pieceToSend.length + 13);
		pieceOut[0] = pieceOutLength[0];
		pieceOut[1] = pieceOutLength[1];
		pieceOut[2] = pieceOutLength[2];
		pieceOut[3] = pieceOutLength[3];
		pieceOut[4] = 0x7;
		pieceOut[5] = index[0];
		pieceOut[6] = index[1];
		pieceOut[7] = index[2];
		pieceOut[8] = index[3];
		pieceOut[9] = begin[0];
		pieceOut[10] = begin[1];
		pieceOut[11] = begin[2];
		pieceOut[12] = begin[3];
		int pieceCounter = 0;
		while(pieceCounter < pieceToSend.length){
			pieceOut[13+pieceCounter] = pieceToSend[pieceCounter];
			++pieceCounter;
		}
		try{
			toPeer.write(pieceOut);
		} catch(Exception e) {System.out.println("Error occured at piece message " + e);}
	}
	
	public static void recieveUnknownMessage(){//NEEDS WORK, CREATE A MESSAGE IDENTIFIER WHICH RUNS A DIFFERENT FUNCTION BASED ON RECIEVED MESSAGE

	}

	public static void recieveUnchoke(){// MOVE THE MESSAGE IDENTIFIER INTO recieveUnknownMessage()
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

	public static byte[] recieveHandshake(){ //NEEDS WORK, CREATE A HANDSHAKE RECIEVER
		byte[] response=null;
		try {
			byte[] responseHash = new byte[20];
			response = new byte[68];
			fromPeer.read(response);
			System.arraycopy(response, 28, responseHash, 0, 20);
			for (int i = 0; i < 20; i++) {
				if (responseHash[i] != RUBTClient.info_hash[i]) {
					return null;
				}
			}
		} catch (Exception e) {
		}
		return response;
	}

	public static byte[][] recievePiece(){// MOVE THE MESSAGE TYPE IDENTIFIER INTO recieveUnknownMessage()
		boolean switchBool = false;
		byte first = 0;
		byte second = 0;
		byte third = 0;
		byte fourth = 0;
		byte fifth = 0;
		byte[][] pieceData = null;
		int bytesDownloaded = 0;

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
				pieceData = new byte[2][pieceLength];
				byte[] pieceIndex = new byte[4];
				byte[] pieceOffset = new byte[4];
				int aTempInt= 0;

				while(aTempInt<4){//fill in the piece Index
					try{
						pieceIndex[aTempInt] = fromPeer.readByte();
					}catch(Exception e){System.out.println("error occurred at recieve - index " +e);}
					++aTempInt;
				}
				int pieceIndexInt = RUBTUtilities.byteArrayToInt(pieceIndex);

				aTempInt= 0;
				while(aTempInt<4){//fill in the piece offset
					try{
						pieceOffset[aTempInt] = fromPeer.readByte();
					}catch(Exception e){System.out.println("error occurred at recieve -offset " +e);}
					++aTempInt;
				}
				int pieceOffsetInt = RUBTUtilities.byteArrayToInt(pieceOffset);

				aTempInt = 0;
				while(aTempInt<pieceLength){//get the actual data of the piece
					try{
						pieceData[0][aTempInt] = fromPeer.readByte();
					}catch(Exception e){System.out.println("error occurred at recieve - data " +e);}
					++aTempInt;
				}

				int transferCounter = 0;
				while(transferCounter<pieceLength){//transfer the bytes into the download_bytes class array and update the information about bytes in possession
					++transferCounter;
					++bytesDownloaded;
				}

				byte[] bytesDownloadedArray = RUBTUtilities.intToByteArray(bytesDownloaded);

				aTempInt = 0;
				while(aTempInt < bytesDownloadedArray.length){
					pieceData[1][aTempInt] = bytesDownloadedArray[aTempInt];
					++aTempInt;
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
				try{
					fifth = fromPeer.readByte();
				}catch(Exception e){System.out.println("error occurred at recieve - getting a new byte " +e);}
				System.out.println("got a new byte");
				System.out.println("values are now "+first+second+third+fourth+fifth);
			}
		}
		return pieceData;
	}

}
