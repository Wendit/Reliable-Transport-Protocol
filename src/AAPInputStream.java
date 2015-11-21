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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class AAPInputStream {
	private static final short MAX_WINDOW_SIZE = Short.MAX_VALUE;
	private String remoteSocketAddress;
	private int remoteSocketPort;
	
	private DatagramSocket recvSocket;
	private DatagramPacket  recvPacket;
	private DatagramPacket ackPacket;
	private AAPPacket recvAAPPacket;
	private AAPPacket ackAAPPacket;
	
	private short remainWindowSize;
	
	private ByteBufferQueue streamBuffer;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];

	private int currentSeqNum;
	private int lastAckNum;
	
	public AAPInputStream(int port,int initSeqNum, String remoteSocketAddress, int remoteSocketPort)
			throws UnknownHostException, SocketException  {
		recvSocket = new DatagramSocket(port);
		this.currentSeqNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.lastAckNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.streamBuffer = new ByteBufferQueue();
	}
	
	public Byte read() throws IOException{
		receive();
		return streamBuffer.getByte();
	}
	
	public int read(byte[] recvBuffer) throws IOException{
		receive();
		Byte temp;
		for(int i = 0; i < recvBuffer.length; i++){
			if((temp =  streamBuffer.getByte()) != null){
				recvBuffer[i] = temp;
			}
		}
		return Math.min(recvBuffer.length, streamBuffer.getLength());
	}
	
	public int read(byte[] recvBuffer, int off, int len) throws IOException{
		receive();
		Byte temp;
		for(int i = 0; i < len; i++){
			if((temp =  streamBuffer.getByte()) != null){
				recvBuffer[off+i] = temp;
			}
		}
		return Math.min(len, streamBuffer.getLength());
	}
	
	public void close(){
		recvSocket.close();
	}
	
	private int receive() throws IOException{
		recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		int bytesRead = 0;
		
		while(true){
			 try {
				 recvSocket.receive(recvPacket);
				 }catch(InterruptedIOException e){
					 break;
				 }		 
			 sendAckBack();
		}
		return bytesRead;
	}
	
	private boolean checkError(DatagramPacket recvPacket) throws IOException{
		boolean corrupted = false;
		boolean errorOccurs = false;
		try{
			 recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
		 }catch(FlagNotFoundException e){
			 DebugUtils.debugPrint(e.getMessage());
		 }catch(PacketCorruptedException e){
			 corrupted = true;
			 DebugUtils.debugPrint(e.getMessage());
		 }
		 if(corrupted)
			 errorOccurs = true;
		 else if(recvAAPPacket.getSeqNum() != lastAckNum){
					 errorOccurs = true;
				 }
		//If no errors, put payload into our buffer and change the remaining window size
		 else{
			 streamBuffer.put(recvAAPPacket.getPayload());
			 remainWindowSize = (short) (MAX_WINDOW_SIZE - (short)streamBuffer.getLength());
		 }
		 return errorOccurs;	 
	}
	
	private void sendAckBack() throws UnknownHostException, IOException{
		//error checking
		//Drop everything except the packet which is not corrupted and is expected
		//Always Ack last recieved packet upon recieving new packets
		 if(!checkError(recvPacket) || ackAAPPacket == null){
			 //If ackAAPPacket = null, construct new ackPacket
			 //otherwise increment lastAckNum and update 
			 //ackPacket
			 if(ackAAPPacket != null){
				 lastAckNum = AAPUtils.incrementSeqNum(lastAckNum);
			 }
			 try {
				ackAAPPacket = new AAPPacket(
						 currentSeqNum++,lastAckNum,AAPPacket.ACK_FLAG,
						 remainWindowSize, "ack".getBytes());
			} catch (FlagNotFoundException | PayLoadSizeTooLargeException e) {
				e.printStackTrace();
			}
			 ackPacket = new DatagramPacket(ackAAPPacket.getPacketData(), 
					  ackAAPPacket.getPacketData().length, 
					  InetAddress.getByName(remoteSocketAddress), remoteSocketPort);
		 }		 
		 // Sending ack back
		 recvSocket.send(ackPacket);
	}
}
