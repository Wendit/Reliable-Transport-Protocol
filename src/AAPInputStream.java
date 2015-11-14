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

public class AAPInputStream {
	private static final short MAX_WINDOW_SIZE = 3000;
	private String senderAddress;
	private int senderPort;
	
	private DatagramSocket recvSocket;
	private DatagramPacket  recvPacket;
	private DatagramPacket ackPacket;
	private AAPPacket recvAAPPacket;
	private AAPPacket ackAAPPacket;
	
	private short remainWindowSize;
	
	private ByteBuffer streamBuffer;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];

	private int currentSeqNum;
	private int lastAckNum;
	
	public AAPInputStream(String address, int port,int initSeqNum, String senderAddress, int senderPort)
			throws UnknownHostException, SocketException  {
		streamBuffer = ByteBuffer.allocate(MAX_WINDOW_SIZE);
		recvSocket = new DatagramSocket(port, InetAddress.getByName(address));
		this.currentSeqNum = incrementSeqNum(initSeqNum);
		this.lastAckNum = incrementSeqNum(initSeqNum);
		this.senderAddress = senderAddress;
		this.senderPort = senderPort;

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
	
	private int receive() throws IOException, FlagNotFoundException, PayLoadSizeTooLargeException{
		recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		int bytesRead = 0;
		
		while(true){
			 try {
				 recvSocket.receive(recvPacket);
				 }catch(InterruptedIOException e){
					 break;
				 }
			 
			//error checking
			//Drop everything except the packet which is not corrupted and is expected
			//Always Ack last recieved packet upon recieving new packets
			 if(!checkError(recvPacket) || ackAAPPacket == null){
				 //If ackAAPPacket = null, construct new ackPacket
				 //otherwise increment lastAckNum and update 
				 //ackPacket
				 if(ackAAPPacket != null){
					 lastAckNum = incrementSeqNum(lastAckNum);
				 }
				 ackAAPPacket = new AAPPacket(
						 currentSeqNum++,lastAckNum,AAPPacket.ACK_FLAG,
						 remainWindowSize, "ack".getBytes());
				 ackPacket = new DatagramPacket(ackAAPPacket.getPacketData(), 
						  ackAAPPacket.getPacketData().length, 
						  InetAddress.getByName(senderAddress), senderPort);
			 }		 
			 // Sending ack back
			 recvSocket.send(ackPacket);

		}
		return bytesRead;
	}
	
	private byte[] getAAPPacketData(DatagramPacket recvPacket){
		return Arrays.copyOfRange(recvPacket.getData(), 0, recvPacket.getLength());		
	}
	
	private AAPPacket getRecvAAPPacket(byte[] packetData) throws FlagNotFoundException, IOException, PacketCorruptedException{
		return new AAPPacket(packetData);
	}

	private int incrementSeqNum(int seqNum){
		if(seqNum == Integer.MAX_VALUE){
			seqNum = 0;
		}else{
			seqNum++;
		}
		return seqNum;
	}
	
	private boolean checkError(DatagramPacket recvPacket) throws IOException{
		boolean corrupted = false;
		boolean errorOccurs = false;
		try{
			 recvAAPPacket = getRecvAAPPacket(getAAPPacketData(recvPacket));
		 }catch(FlagNotFoundException e){
			 DebugUtils.debugPrint(e.getMessage());
		 }catch(PacketCorruptedException e){
			 corrupted = true;
			 DebugUtils.debugPrint(e.getMessage());
		 }
		 if(corrupted)
			 errorOccurs = true;
		 else if(recvAAPPacket.getSeqNum() != expectedSeqNum){
					 errorOccurs = true;
				 }
		 return errorOccurs;	 
	}
}
