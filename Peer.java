package RUBTClient;

public class Peer {
	String host;
	int port;
	byte[] bitfield;
	boolean choked;

	public Peer(String hostIn, int portIn, byte[] bitfieldIn, boolean chokedStatus){
		host = hostIn;
		port = portIn;
		bitfield = bitfieldIn;
		choked = chokedStatus;
	}
	
	public String getHost(){
		return host;
	}
	
	public int getPort(){
		return port;
	}
	
	public byte[] getBitfield(){
		return bitfield;
	}
	
	public boolean[] getBooleanBitfield(){ // convert the byte[] bitfield to a boolean one
		boolean[] boolBitfield = new boolean[bitfield.length];
		int tempCountingInt = 0;
		while(tempCountingInt < bitfield.length){
			if(bitfield[tempCountingInt] == 0x0){
				boolBitfield[tempCountingInt] = false;
			}
			else if(bitfield[tempCountingInt] == 0x1){
				boolBitfield[tempCountingInt] = true;
			}
			++tempCountingInt;
		}
		return boolBitfield;
	}

	public boolean getChokedStatus(){
		return choked;
	}
	
	public void setChoked(){
		choked = true;
	}
	
	public void setUnchoked(){
		choked = false;
	}
}
