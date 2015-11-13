import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.CRC32;

public class AAPPacket implements Serializable{

	private static Random rand = new Random(); 
	private byte[] payload; 
	private char payloadSize;
	private int seqNum;
	private int ackNum;
	private short flags;
	private short windowSize;
	private long checksum;
	
	/**
	 * Header: 
	 * |Sequence Number
	 * |ACK Number
	 * |Flags
	 * |Window Size
	 * |CheckSum
	 * |Payload Size
	 * |Payload
	 */
	
	private static final int MAX_PAYLOAD_SIZE = 32;
	private static final int MAX_PACKET_SIZE = 4 + 4 + 2 + 2 + 8 + 1 + MAX_PAYLOAD_SIZE;
	
	public static final short SYN_FLAG = 1;
	public static final short ACK_FLAG = 1 << 1;
	public static final short FIN_FLAG = 1 << 2;
	public static final short SYN_ACK_FLAG = SYN_FLAG|ACK_FLAG;
	public static final short FIN_ACK_FLAG = FIN_FLAG|ACK_FLAG;
	
	public AAPPacket(int seqNum, int ackNum, short flags, short windowSize, byte[] payload) throws FlagNotFoundException, IOException, PayLoadSizeTooLargeException {
		if(flags != SYN_ACK_FLAG 
				&& flags != FIN_ACK_FLAG
				&& flags != SYN_FLAG 
				&& flags != ACK_FLAG 
				&& flags != FIN_FLAG 
			){
			throw new FlagNotFoundException("Flag not found: "+flags);
		}
		if(payload.length >= MAX_PAYLOAD_SIZE){
			throw new PayLoadSizeTooLargeException("The payload should be less than 32 bytes");
		}
		else{		
			this.seqNum = seqNum;
			this.ackNum = ackNum;
			this.flags = flags;
			this.windowSize = windowSize;
			this.payloadSize = (char)payload.length;
			this.payload = payload; 
			this.checksum = getChecksum();
		}
	}
	
	public AAPPacket(byte[] bArray) throws FlagNotFoundException, IOException {
		ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
		buffer.put(bArray);
		
		this.seqNum = buffer.getInt();
		this.ackNum = buffer.getInt();
		this.flags = buffer.getShort();
		this.windowSize = buffer.getShort();
		this.checksum = buffer.getLong();
		this.payloadSize = buffer.getChar();
		this.payload = new byte[payloadSize];
		buffer.get(this.payload, 0, payloadSize); 
		
		if(this.checksum != getChecksum()){
			
		}
	}
	
	public byte[] getData(){
		return payload;
	}
	
	public static Random getRand() {
		return rand;
	}

	public byte[] getPayload() {
		return payload;
	}

	public int getPayloasSize() {
		return payloadSize;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public int getAckNum() {
		return ackNum;
	}

	public short getFlags() {
		return flags;
	}

	public short getWindowSize() {
		return windowSize;
	}

	public static int generateInitSeqNum(){
		return rand.nextInt();
	}
	
	public long getChecksum() throws IOException{
		
		/*Concatenate byte arrays*/
		ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE-8);
		buffer.putInt(seqNum);
		buffer.putInt(ackNum);	
		buffer.putShort(flags);
		buffer.putShort(windowSize);
		buffer.putChar(payloadSize);
		buffer.put(payload);

		CRC32 checksum = new CRC32();
		checksum.update(buffer.array());
		return checksum.getValue();
	}
	
	public byte[] getPacketData(){
		ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
		buffer.putInt(seqNum);
		buffer.putInt(ackNum);	
		buffer.putShort(flags);
		buffer.putShort(windowSize);
		buffer.putLong(checksum);
		buffer.putChar(payloadSize);
		buffer.put(payload);
		return buffer.array();
	}
}
