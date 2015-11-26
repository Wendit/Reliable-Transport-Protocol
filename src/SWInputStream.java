import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SWInputStream {
	private static final int MAX_CONNECTION_TRY = 5;
	private static final int TIMEOUT = 3000;
	private String remoteSocketAddress;
	private int remoteSocketPort;
	
	private DatagramSocket recvSocket;
	private DatagramPacket  recvPacket;
	private DatagramPacket ackPacket;
	private AAPPacket recvAAPPacket;
	private AAPPacket ackAAPPacket;
	
	private ByteBufferQueue streamBuffer;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private short endOfPacket; 
	
	private int currentSeqNum;
	
	public SWInputStream(DatagramSocket socket,int initSeqNum, String remoteSocketAddress, int remoteSocketPort)
			throws UnknownHostException, SocketException  {
		recvSocket = socket;
		recvSocket.setSoTimeout(TIMEOUT);
		this.currentSeqNum = AAPUtils.alterBit(initSeqNum);
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.streamBuffer = new ByteBufferQueue();
	}
	
	public Byte read() throws IOException{
		try{
			receive();
			return streamBuffer.getByte();
		}catch(SocketException e ){
			e.printStackTrace();
			return -1;
		}	
		
	}
		public int read(byte[] recvBuffer) throws IOException {
		try{
				receive();
				DebugUtils.debugPrint("Trying to read an input ");
				Byte temp;
				int i;
				for( i = 0; i < recvBuffer.length && streamBuffer.getLength() !=0 ; i++){
					if((temp =  streamBuffer.getByte()) != null){
						recvBuffer[i] = temp;
					}
				}
					return i;
			}catch(SocketException e ){
				e.printStackTrace();
				return -1;
			}
	}
	
	public int read(byte[] recvBuffer, int off, int len) throws IOException{
		try{
			receive();
			Byte temp;
			int i;
			for( i = 0; i < len && streamBuffer.getLength() !=0; i++){
				if((temp =  streamBuffer.getByte()) != null){
					recvBuffer[off+i] = temp;
				}
			}
			return i;
		}catch(SocketException e){
			e.printStackTrace();
			return -1;
		}
	}
	
	private void receive() throws IOException{
		recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		endOfPacket = 0;
		while(endOfPacket != AAPPacket.END_OF_PACKET_FLAG){
			receiveSinglePacket();
		}
	}
	
	private void receiveSinglePacket() throws IOException{
		recvSocket.receive(recvPacket);		
		try {
			recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
			endOfPacket = recvAAPPacket.getFlags();
			streamBuffer.put(recvAAPPacket.getPayload());
			int ackNum = AAPUtils.alterBit(recvAAPPacket.getSeqNum());
			recvAAPPacket = new AAPPacket(currentSeqNum, ackNum, AAPPacket.ACK_FLAG, (short)0, "ack".getBytes());
			recvSocket.send(new DatagramPacket(ackAAPPacket.getPacketData(), AAPPacket.PACKET_SIZE,
					InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
		} catch (FlagNotFoundException e) {
			e.printStackTrace();
		} catch (PacketCorruptedException e) {
			e.printStackTrace();
		} catch (PayLoadSizeTooLargeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
