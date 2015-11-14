import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class AAPInputStream {
	private static final int MAX_WINDOW_SIZE = 3000;
	
	private DatagramSocket recvSocket;
	private DatagramPacket  recvPacket;
	
	private short remainWindowSize;
	
	private ByteBuffer streamBuffer;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	
	private int expectedSeqNum;
	private int currentSeqNum;
	private int lastAckNum;
	
	public AAPInputStream(String address, int port,int initSeqNum) throws UnknownHostException, SocketException  {
		streamBuffer = ByteBuffer.allocate(MAX_WINDOW_SIZE);
		recvSocket = new DatagramSocket(port, InetAddress.getByName(address));
		this.expectedSeqNum = initSeqNum + 1;
		this.currentSeqNum = initSeqNum + 1;
		this.lastAckNum = -1;//Haven't acked anything yet
	}
	
	public Byte read(){
		return null;
	}
	
	public int read(byte[] recvBuffer){
		return 0;
	}
	
	public int read(byte[] recvBuffer, int off, int len){
		return 0;
	}
	
	public void close(){
		
	}
	
	private byte[] receive() throws IOException{
		boolean corrupted;
		
		recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		while(true){
			corrupted = false;
			 try {
				 recvSocket.receive(recvPacket);
				 }catch(InterruptedIOException e){
					 break;
				 }
			 
			 try{
				 getRecvAAPPacket(getAAPPacketData(recvPacket));
			 }catch(FlagNotFoundException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }catch(PacketCorruptedException e){
				 corrupted = true;
				 DebugUtils.debugPrint(e.getMessage());
			 }
		}
		return null;
	}
	
	private byte[] getAAPPacketData(DatagramPacket recvPacket){
		return null;		
	}
	
	private AAPPacket getRecvAAPPacket(byte[] packetData) throws FlagNotFoundException, IOException, PacketCorruptedException{
		return new AAPPacket(packetData);
	}


}
