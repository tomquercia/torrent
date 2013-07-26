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

public class PeerInterface {

	static DataOutputStream toPeer = null;
	static DataInputStream fromPeer = null;

	PeerInterface(Socket connection){
		try{
			toPeer = new DataOutputStream(connection.getOutputStream());
			fromPeer = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
		}
		catch (Exception e) {System.out.println("ERROR occured at the PeerInterface constructor when assiging toPeer and fromPeer : " + e);}
	}

	public static void sendMessage(int messageType){//Overloaded method to send different messages based on messageType

	}

	public static void sendMessage(int messageType, int pieceToRequestParam, int offsetToRequestParam, int lengthToRequestParam){//should be for request messages

	}

	public static void sendMessage(int messageType, byte[] info_hashParam, byte[] peer_idParam){//should be for handshakes

	}

	public static void sendMessage(int messageType, int pieceHaveParam){//should be for have

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

	public static void have(int pieceFinished){//send the HAVE message
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
				int pieceIndexInt = byteArrayToInt(pieceIndex);

				aTempInt= 0;
				while(aTempInt<4){//fill in the piece offset
					try{
						pieceOffset[aTempInt] = fromPeer.readByte();
					}catch(Exception e){System.out.println("error occurred at recieve -offset " +e);}
					++aTempInt;
				}
				int pieceOffsetInt = byteArrayToInt(pieceOffset);

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

				byte[] bytesDownloadedArray = intToByteArray(bytesDownloaded);

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

	/*public static void arrayValuePrinter(byte[] toPrint){ // a convenience method to print out all the values in a byte array
    int tempInt2 = 0;
    while(tempInt2<toPrint.length){
      System.out.print(toPrint[tempInt2]);
      ++tempInt2;
    }
    System.out.println();
  }*/

	public static byte[] intToByteArray(int toByteArray){ //convenience method to make ints into byte arrays
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeInt(toByteArray);
		}catch(Exception e){System.out.println("error occured with the in to byte array converter "+e);}
		byte[] returnMe = baos.toByteArray();
		return returnMe;
	}

	public static int byteArrayToInt(byte[] toInt){//convenience method to make byte arrays into ints
		ByteBuffer convertToInt = ByteBuffer.wrap(toInt);
		int retAsInt = convertToInt.getInt();
		return retAsInt;
	}
}
