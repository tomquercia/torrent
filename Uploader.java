package RUBTClient;

import RUBTClient.MessageInterpreter.*;

public class Uploader implements Runnable {

	PeerInterface peer;
	boolean choked=false;
	boolean interest=true;
	boolean shouldHandshake=true;

	public Uploader(PeerInterface pi){
		peer=pi;
	}
	public Uploader(PeerInterface p, boolean handshake) {

		peer = p;

	shouldHandshake = handshake;

	}

	public MessageInterpreter listen(){
		try{
			return MessageInterpreter.handle(PeerInterface.fromPeer);
		}
		catch(Exception e){			
		}
		return null;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (shouldHandshake) {
			if (!PeerInterface.recieveHandshake().equals(RUBTClient.info_hash)) {
			peer.sendHandshake(RUBTClient.peer_id, RUBTClient.info_hash);
			try {
				MessageInterpreter.encode(peer.toPeer,
						new BitfieldMessage(RUBTClient.bitfield()));
			} catch (Exception e) {}
		}
		}
		while (interest) {
			MessageInterpreter temp = listen();
			if(temp == null){
				continue;
			}
			if (choked) {
				if (temp.getId() == MessageInterpreter.unchoke) {
					continue;
				}
			}
			if(temp.getId()==MessageInterpreter.bitfield){
				BitfieldMessage bfm = (BitfieldMessage) temp;
				//set the peer's bitfield
				//Like this??? peer.bitfield=RUBTClient.convertbool(bfm.getData());
				break;
			}
			else if(temp.getId()==MessageInterpreter.keepAlive){
				//nothing to see here
				break;
			}
			else if(temp.getId()==MessageInterpreter.choke){
				//we got choked
				choked = true;
				break;
			}
			else if(temp.getId()==MessageInterpreter.unchoke){
				//we just got unchoked
				break;
			}
			else if(temp.getId()==MessageInterpreter.have){
				//we got a have message
				HaveMessage tempHave = (HaveMessage) temp;
				//update the bitfield
				//Like this??? peer.bitfield[tempHave.getPieceIndex()]=true;
				break;
			}
			else if(temp.getId()==MessageInterpreter.interested){
				//interested message
				try {
					MessageInterpreter.encode(peer.toPeer, MessageInterpreter.unchokeMessage);
					//eclipse is saying toPeer should be static, I'll leave that up to you
				} catch (Exception e) {
				}
				break;
			}
			else if(temp.getId()==MessageInterpreter.notInterested){
				interest = false;
				break;
			}
			else if(temp.getId()==MessageInterpreter.request){
				//got a request for a piece!!!
				RequestMessage tempRequest = (RequestMessage) temp;
				byte[] data = new byte[tempRequest.getBlockLength()];
				byte[]tempbytes = RUBTClient.getPiece(tempRequest.getPieceIndex());
				System.arraycopy(tempbytes, tempRequest.getBegin(), data, 0,
						tempRequest.getBlockLength());
				PieceMessage toSend = new PieceMessage(
						tempRequest.getPieceIndex(), tempRequest.getBegin(),
						data);
				try {
					//how are we keeping track of what we've sent out?
					MessageInterpreter.encode(peer.toPeer, toSend);
					RUBTClient.addUploaded(toSend.getBlockLength());
				} catch (Exception e) {
				}
				break;
			}
			else if(temp.getId()==MessageInterpreter.piece){
				//this should hopefully never happen...
				break;
			}
		}
	}
}
