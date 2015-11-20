import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class AAPOutputStream {

	private DatagramSocket sendSocket;
	private DatagramPacket recvPacket;
	private AAPPacket sendAAPacket;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private Deque<AAPPacket> packetsList; 
	private Deque<AAPPacket> packetWindow;
	private short currentWindowSize = 10;
	private static short MAX_WINDOW_SIZE = 10;
	
	private String recverAddress;
	private int recverPort;
	
	private int currentSeqNum;
	
	public AAPOutputStream(String address, int port,int initSeqNum, String recverAddress, int recverPort)
			throws UnknownHostException, SocketException  {
		sendSocket = new DatagramSocket(port, InetAddress.getByName(address));
		this.currentSeqNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.recverAddress = recverAddress;
		this.recverPort = recverPort;
		this.packetsList = new LinkedList();
		this.packetWindow = new LinkedList();
		this.currentWindowSize = MAX_WINDOW_SIZE;
		this.recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		
	}
	
	public void write(byte b){
		
	}
	
	public void write(byte[] bArray){
		
	}
	
	public void write(byte[] b, int off, int len){
		
	}
	
	public void close(){
		
	}
	
	private void send(byte[] b) throws FlagNotFoundException, IOException, PayLoadSizeTooLargeException{
		putPacketInQueue(b);
		
		
		
	}
	
	private void putPacketInQueue(byte[] b) throws FlagNotFoundException, IOException, PayLoadSizeTooLargeException{
		
		int byteLeft = b.length;
		int currentPos = 0;
		while(byteLeft - AAPPacket.MAX_PAYLOAD_SIZE >0){
			sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG,
					(short)0, Arrays.copyOfRange(b, currentPos, currentPos+AAPPacket.MAX_PAYLOAD_SIZE)); 
			currentPos = currentPos+AAPPacket.MAX_PAYLOAD_SIZE;
			packetsList.add(sendAAPacket);
			currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
		}
		sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG,
				(short)0, Arrays.copyOfRange(b, currentPos, currentPos+AAPPacket.MAX_PAYLOAD_SIZE));
		packetsList.add(sendAAPacket);
		currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
	}
	
	private void putPacketsInWindow() throws IOException{
		while(packetWindow.size() < MAX_WINDOW_SIZE && packetsList.size() != 0){
			moveListHeadToWindow();
		}
	}
	private void moveListHeadToWindow(){
		if(packetWindow.size() < MAX_WINDOW_SIZE){
			packetWindow.add(packetsList.poll());
		}
	}
	
	private void moveWindowTailToWList(int windowSize){
		if(packetWindow.size() < MAX_WINDOW_SIZE){
			packetWindow.add(packetsList.poll());
		}
	}
	
	private void sendPackets() throws IOException{
	  putPacketsInWindow();
	  //First send all packets in window
	  for(int i =0; i < packetWindow.size();i++){
		  sendSocket.send(new DatagramPacket(sendAAPacket.getPacketData(),
				  AAPPacket.PACKET_SIZE,InetAddress.getByName(recverAddress), recverPort));
	  }
	  while(packetsList.size() != 0 && packetWindow.size() != 0){
		  recvAckAndFillWindow();
	  }
	  //Then if a an ack if recieved, discad head and add new packet(if any)
	}
	
	private int recvAckAndFillWindow() throws IOException{
		int lastAcked = -1;
		AAPPacket recvAAPPacket;
		while(true){		
			try {
				sendSocket.receive(recvPacket);
			} catch (InterruptedIOException e) {
				break;//break if times out
			}
			
			try{
				//Drop the packet if it was bad
				 recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
				 
				 if(recvAAPPacket.getFlags() == AAPPacket.ACK_FLAG ||
						 recvAAPPacket.getFlags() == AAPPacket.FIN_ACK_FLAG ||
						 recvAAPPacket.getFlags() == AAPPacket.SYN_ACK_FLAG ){
					 lastAcked = recvAAPPacket.getAckNum();
					 //Adjust window size according to remaining buffe size
					 adjustWindowSize((int)Math.floor(recvAAPPacket.getWindowSize()/AAPPacket.MAX_PAYLOAD_SIZE));
				 }
			 }catch(FlagNotFoundException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }catch(PacketCorruptedException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }
		}
		return lastAcked;
	}
	
	private void adjustWindowSize(int newIndowSize) throws IOException{
		
	}
	
}
