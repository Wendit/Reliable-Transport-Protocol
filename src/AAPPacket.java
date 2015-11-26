import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.CRC32;

public class AAPPacket implements Serializable{

	private static Random rand = new Random(); 
	private byte[] payload; 
	private byte payloadSize;
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
	
	public static final int MAX_PAYLOAD_SIZE = 255;
	public static final int PACKET_SIZE = 4 + 4 + 2 + 2 + 8 + 1 + MAX_PAYLOAD_SIZE;
	
	public static final short NULL_FLAG = 0;
	public static final short SYN_FLAG = 1;
	public static final short ACK_FLAG = 1 << 1;
	public static final short FIN_FLAG = 1 << 2;
	public static final short END_OF_PACKET_FLAG = 1 << 3;
	public static final short SYN_ACK_FLAG = SYN_FLAG|ACK_FLAG;
	public static final short FIN_ACK_FLAG = FIN_FLAG|ACK_FLAG;
	
	/**
	 * Construct a packet to be send, use getPacketData() to get the packet in bytes
	 * @param seqNum
	 * @param ackNum
	 * @param flags
	 * @param windowSize
	 * @param payload
	 * @throws FlagNotFoundException
	 * @throws IOException
	 * @throws PayLoadSizeTooLargeException
	 */
	public AAPPacket(int seqNum, int ackNum, short flags, short windowSize, byte[] payload) throws FlagNotFoundException, IOException, PayLoadSizeTooLargeException {
		if(flags != SYN_ACK_FLAG 
				&& flags != FIN_ACK_FLAG
				&& flags != SYN_FLAG 
				&& flags != ACK_FLAG 
				&& flags != FIN_FLAG 
				&& flags != NULL_FLAG
				&& flags != END_OF_PACKET_FLAG
			){
			throw new FlagNotFoundException("Flag not found: "+flags);
		}
		if(payload.length > MAX_PAYLOAD_SIZE){
			throw new PayLoadSizeTooLargeException("The payload should be less than 256 bytes");
		}
		else{		
			this.seqNum = seqNum;
			this.ackNum = ackNum;
			this.flags = flags;
			this.windowSize = windowSize;
			this.payloadSize = (byte) payload.length;
			this.payload = payload; 
			int x = payload.length;
			this.checksum = getChecksum();
		}
	}
	/**
	 * Construct a recieved packet from byte[] array
	 * @param bArray
	 * @throws FlagNotFoundException
	 * @throws IOException
	 * @throws PacketCorruptedException
	 */
	public AAPPacket(byte[] bArray) throws FlagNotFoundException, IOException, PacketCorruptedException {
		ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
		buffer.put(bArray);
		buffer.position(0);
		
		this.seqNum = buffer.getInt();
		this.ackNum = buffer.getInt();
		this.flags = buffer.getShort();
		this.windowSize = buffer.getShort();
		this.checksum = buffer.getLong();
		this.payloadSize = buffer.get();
		this.payload = new byte[payloadSize];
		buffer.get(this.payload, 0, payloadSize); 
		
		if(this.checksum != getChecksum()){
			throw new PacketCorruptedException("Checksum does not match. The packet is corrupted.");
		}
	} 
	
	public static Random getRand() {
		return rand;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public String getPayloadToString() {
		return new String(payload);
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
		ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE-8);
		buffer.putInt(seqNum);
		buffer.putInt(ackNum);	
		buffer.putShort(flags);
		buffer.putShort(windowSize);
		buffer.put(payloadSize);
		buffer.put(payload);

		CRC32 checksum = new CRC32();
		checksum.update(buffer.array());
		return checksum.getValue();
	}
	
	public byte[] getPacketData(){
		
		/*Concatenate byte arrays*/
		ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
		buffer.putInt(seqNum);
		buffer.putInt(ackNum);	
		buffer.putShort(flags);
		buffer.putShort(windowSize);
		buffer.putLong(checksum);
		buffer.put(payloadSize);
		buffer.put(payload);
		
		return buffer.array();
	}
}
