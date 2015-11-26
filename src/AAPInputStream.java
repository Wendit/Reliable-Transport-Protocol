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
	private static final int TIMEOUT = 3000;
	private static final short MAX_WINDOW_SIZE = Short.MAX_VALUE;
	private static final int MAX_TRY = 10;
	private int currentTries;
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
	private short endOfPacket; 
	

	private int currentSeqNum;
	private int lastAckNum;
	private AAPSocket container;
	public AAPInputStream(DatagramSocket socket,int initSeqNum, String remoteSocketAddress, int remoteSocketPort,AAPSocket container)
			throws UnknownHostException, SocketException  {
		recvSocket = socket;
		recvSocket.setSoTimeout(TIMEOUT);
		this.currentSeqNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.lastAckNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.streamBuffer = new ByteBufferQueue();
		this.container = container;
		this.currentTries = MAX_TRY;
		try {
			this.ackAAPPacket = new AAPPacket(
					 currentSeqNum,lastAckNum,AAPPacket.ACK_FLAG,
					 remainWindowSize, "ack".getBytes());
		} catch (FlagNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PayLoadSizeTooLargeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Byte read() throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException{
		try{
			receive();
			return streamBuffer.getByte();
		}catch(SocketException e ){
			e.printStackTrace();
			return -1;
		}catch(ServerNotRespondingException e){
			e.printStackTrace();
			return -1;
		}	
		
	}
		public int read(byte[] recvBuffer) throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException{
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
			}catch(ServerNotRespondingException e){
				e.printStackTrace();
				return -1;
			}
	}
	
	public int read(byte[] recvBuffer, int off, int len) throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException{
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
	
	public void close(){
		recvSocket.close();
	}
	
	private void receive() throws ServerNotRespondingException, ConnectionAbortEarlyException, SocketException,IOException{
		recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		endOfPacket = 0;
		currentTries = MAX_TRY;
		while(endOfPacket != AAPPacket.END_OF_PACKET_FLAG){
			 try {
				 recvSocket.receive(recvPacket);	
				 DebugUtils.debugPrint("Recieved packet from "+remoteSocketAddress +" "+remoteSocketPort);
				 sendAckBack();
				 }catch(InterruptedIOException e){
					 currentTries --;
					 if(currentTries == 0){
						 container.close();
						 throw new ServerNotRespondingException("Remote not responding. Connection abort. Please reset connection.");
					 }
				 }	
		}
	}
	
	private void sendAckBack() throws UnknownHostException, IOException, ConnectionAbortEarlyException{
		//error checking
		//Drop everything except the packet which is not corrupted and is expected
		//Always Ack last recieved packet upon recieving new packets
		 if(!checkError(recvPacket)){
			 //If ackAAPPacket = null, construct new ackPacket
			 //otherwise increment lastAckNum and update 
			 //ackPacket
			lastAckNum = AAPUtils.incrementSeqNum(lastAckNum);
			 try {
				ackAAPPacket = new AAPPacket(
						 currentSeqNum++,lastAckNum,AAPPacket.ACK_FLAG,
						 remainWindowSize, "ack".getBytes());
			} catch (FlagNotFoundException | PayLoadSizeTooLargeException e) {
				e.printStackTrace();
			}
		 }	
		 
		 ackPacket = new DatagramPacket(ackAAPPacket.getPacketData(), 
				  ackAAPPacket.getPacketData().length, 
				  InetAddress.getByName(remoteSocketAddress), remoteSocketPort);
		 // Sending ack back
		 DebugUtils.debugPrint("Send ACK back to "+remoteSocketAddress +" "+remoteSocketPort);
		 recvSocket.send(ackPacket);
	}
	
	private boolean checkError(DatagramPacket recvPacket) throws IOException, ConnectionAbortEarlyException{
		boolean corrupted = false;
		boolean errorOccurs = false;
		DebugUtils.debugPrint("Check error for the packet from "+remoteSocketAddress +" "+remoteSocketPort);
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
		 else if(!recvPacket.getAddress().equals(InetAddress.getByName(remoteSocketAddress))){
			 //Not from the expected user
			 errorOccurs = true;
		 }
		 else{
			 if(recvAAPPacket.getFlags() == AAPPacket.NULL_FLAG || recvAAPPacket.getFlags() == AAPPacket.END_OF_PACKET_FLAG){
				 endOfPacket = recvAAPPacket.getFlags();
				 streamBuffer.put(recvAAPPacket.getPayload());
				 remainWindowSize = (short) (MAX_WINDOW_SIZE - (short)streamBuffer.getLength());
			 }else if(recvAAPPacket.getFlags() == AAPPacket.FIN_FLAG){
				 container.close();
				 throw new ConnectionAbortEarlyException("Coneection abort unexpected.Socket closing.");
			 }
		 }
		 return errorOccurs;	 
	}
}
