package RUBTClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageInterpreter {
	byte id;
	int len;
	public static byte keepAlive = -1;
	public static byte choke = 0;
	public static byte unchoke = 1;
	public static byte interested = 2;
	public static byte notInterested = 3;
	public static byte have = 4;
	public static byte bitfield = 5;
	public static byte request = 6;
	public static byte piece = 7;
	public static MessageInterpreter notInterestedMessage = new MessageInterpreter(notInterested, 1);
	public static MessageInterpreter keepAliveMessage = new MessageInterpreter(keepAlive, 0);
	public static MessageInterpreter chokeMessage = new MessageInterpreter(choke, 1);
	public static MessageInterpreter interestedMessage = new MessageInterpreter(interested, 1);
	public static MessageInterpreter unchokeMessage = new MessageInterpreter(unchoke, 1);
	
	protected MessageInterpreter(byte id, int length) {
		this.id = id;
		this.len = length;
	}
	
	public MessageInterpreter handle(InputStream input) throws IOException{
		DataInputStream data = new DataInputStream(input);
		int messageLength = data.readInt();
		if(messageLength == 0){
			return keepAliveMessage;
		}
		byte ident=data.readByte();
		if(ident==choke){
			return chokeMessage;
		}
		else if(ident==unchoke){
			return unchokeMessage;
		}
		else if(ident==interested){
			return interestedMessage;
		}
		else if(ident==notInterested){
			return notInterestedMessage;
		}
		else if(ident==bitfield){
			//bitfield message
		}
		else if(ident==have){
			//have message
		}
		else if(ident==request){
			//request message
		}
		else if(ident==piece){
			//piecemessage
		}
		else{
			System.out.println("Did not receive an ID with the message");
		}
		return null;
		
	}
}
