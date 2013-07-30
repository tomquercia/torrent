package RUBTClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MessageInterpreter {
	byte id;
	static int len;
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
	public static final class InterestedMessage extends MessageInterpreter {
		public InterestedMessage() {
			super(interested, 1);
		}
	}
	public static final class UninterestedMessage extends MessageInterpreter {
		public UninterestedMessage() {
			super(notInterested, 1);
		}
	}
	public static final class PieceMessage extends MessageInterpreter {
		private final byte[] data;
		private final int pieceIndex;
		private final int begin;
		public PieceMessage(final int pieceIndex, final int begin, final byte[] data) {
			super(piece, data.length + 9);
			this.begin = begin;
			this.pieceIndex = pieceIndex;
			this.data = data;
		}
		public int getPieceIndex() {
			return pieceIndex;
		}
		public int getBegin() {
			return begin;
		}
		public byte[] getData() {
			return data;
		}
		public int getBlockLength() {
			return data.length;
		}
	}
	public static final class HaveMessage extends MessageInterpreter {

		private final int pieceIndex;
		public int getPieceIndex() {
			return pieceIndex;
		}
		public HaveMessage(final int pieceIndex) {
			super(have, 5);
			this.pieceIndex = pieceIndex;
		}
	}
	public static final class BitfieldMessage extends MessageInterpreter {
		private final byte[] data;
		public BitfieldMessage(final byte[] data) {
			super(bitfield, 1 + data.length);
			this.data = data;
		}
		public byte[] getData() {
			return data;
		}
	}

	public static final class RequestMessage extends MessageInterpreter {
		private final int pieceIndex;
		private final int begin;
		private final int blockLength;

		public RequestMessage(final int pieceIndex, final int begin, final int blockLength) {
			super(request, 13);
			this.pieceIndex = pieceIndex;
			this.begin = begin;
			this.blockLength = blockLength;
		}
		public int getBegin() {
			return begin;
		}
		public int getPieceIndex() {
			return pieceIndex;
		}
		public int getBlockLength() {
			return blockLength;
		}
	}
	public int getLength() {
		return len;
	}

	public byte getId() {
		return id;
	}

	public static void encode(final OutputStream out, final MessageInterpreter message)
			throws IOException {
		DataOutputStream sending = new DataOutputStream(out);
		if (message.getId() == keepAlive) {
			sending.writeInt(message.getLength());

		} else {

			if (message.getId()==notInterested) {
				sending.writeInt(notInterestedMessage.getLength());
				sending.writeByte(notInterestedMessage.getId());				
			}

			else if(message.getId()== interested){
				sending.writeInt(interestedMessage.getLength());
				sending.writeByte(interestedMessage.getId());
				}
			else if(message.getId()== choke){
				sending.writeInt(chokeMessage.getLength());
				sending.writeByte(chokeMessage.getId());				
			}
			else if(message.getId()== unchoke){
				sending.writeInt(unchokeMessage.getLength());
				sending.writeByte(unchokeMessage.getId());				
			}
			else if(message.getId()== piece){
				PieceMessage msg = (PieceMessage) message;
				sending.writeInt(message.getLength());
				sending.writeByte(message.getId());
				sending.writeInt(msg.getPieceIndex());
				sending.writeInt(msg.getBegin());
				sending.write(msg.getData());				
			}			
			else if(message.getId()== request){
				RequestMessage req = (RequestMessage) message;
				sending.writeInt(req.getLength());
				sending.writeByte(req.getId());
				sending.writeInt(req.getPieceIndex());
				sending.writeInt(req.getBegin());
				sending.writeInt(req.getBlockLength());
				
			}			
			else if(message.getId()== bitfield){
				BitfieldMessage bit = (BitfieldMessage) message;
				sending.writeInt(bit.getLength());
				sending.writeByte(bit.getId());
				sending.write(bit.getData(), 0, bit.getData().length);
				
			}
			else if(message.getId()== have){
				HaveMessage hvm = (HaveMessage) message;
				sending.writeInt(hvm.getLength());
				sending.writeByte(hvm.getId());
				sending.writeInt(hvm.getPieceIndex());
				
			}
		}
	sending.flush();
}


public static MessageInterpreter handle(InputStream input) throws IOException{
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
		byte[] dataContained;
		int bitLength = RUBTClient.totalPieces;
		if (bitLength % 8 == 0) {
			dataContained = new byte[bitLength / 8];
		} else {
			dataContained = new byte[bitLength / 8 + 1];
		}
		data.readFully(dataContained);
		return new BitfieldMessage(dataContained);		
	}
	else if(ident==have){
		int index = data.readInt();
		return new HaveMessage(index);
	}
	else if(ident==request){
		int index = data.readInt();
		int start = data.readInt();
		int blockLength = data.readInt();
		return new RequestMessage(index, start, blockLength);
	}
	else if(ident==piece){
		int index = data.readInt();
		int start = data.readInt();
		byte[] dataContained = new byte[len - 9];
		data.readFully(dataContained);
		return new PieceMessage(index, start, dataContained);
	}
	else{
		System.out.println("Did not receive an ID with the message");
	}
	return null;

}
}
