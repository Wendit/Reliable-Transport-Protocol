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
import java.util.Iterator;
import java.util.LinkedList;

public class AAPOutputStream {

	private DatagramSocket sendSocket;
	private DatagramPacket recvPacket;
	private AAPPacket sendAAPacket;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private static final int TIMEOUT = 3000;
	private static final int MAX_TRY = 10;
	private int currentTries;
	private LinkedList<AAPPacket> packetsList; 
	private LinkedList<AAPPacket> packetWindow;
	private short currentWindowSize = 10;
	private static short MAX_WINDOW_SIZE = 10;
	
	private String recverAddress;
	private int recverPort;
	private int lastAcked;
	
	private int currentSeqNum;
	
	public AAPOutputStream(int port,int initSeqNum, String recverAddress, int recverPort)
			throws UnknownHostException, SocketException  {
		sendSocket = new DatagramSocket(port);
		sendSocket.setSoTimeout(TIMEOUT);
		this.currentSeqNum = AAPUtils.incrementSeqNum(initSeqNum);
		this.recverAddress = recverAddress;
		this.recverPort = recverPort;
		this.packetsList = new LinkedList();
		this.packetWindow = new LinkedList();
		this.currentWindowSize = MAX_WINDOW_SIZE;
		this.recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		this.lastAcked = -1;
		this.currentTries = MAX_TRY;
		
	}
	
	public void write(byte b) throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException{
		byte[] temp = {b};
		send(temp);
	}
	
	public void write(byte[] bArray) throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException{
		send(bArray);
	}
	
	public void write(byte[] b, int off, int len) throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException{
		byte[] temp = Arrays.copyOfRange(b, off, off+len);
		send(temp);
	}
	
	public void close(){
		sendSocket.close();
	}
	
	private void send(byte[] b) throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException{
		putPacketInQueue(b);
		sendPackets();	
	}
	
	private void sendPackets() throws IOException, ServerNotRespondingException{	  
	  fillWindow();//Automatically sends the packets added
	  lastAcked = packetWindow.getFirst().getSeqNum();
	  while(packetsList.size() != 0 || packetWindow.size() != 0){
		  recvAckAndFillWindow();
		  //if ackRecieve times out send all packets again if the window is not empty 
		  if(packetWindow.size() !=0 && lastAcked == packetWindow.getFirst().getSeqNum())
			  sendPacketInWindow(packetWindow.size());
	  }
	  //Then if a an ack if recieved, discad head and add new packet(if any)
	}
	
	private void putPacketInQueue(byte[] b) throws  IOException, PayLoadSizeTooLargeException{
		
		int byteLeft = b.length;
		int currentPos = 0;
		while(byteLeft - AAPPacket.MAX_PAYLOAD_SIZE >0){
			try{
				sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG,
					(short)0, Arrays.copyOfRange(b, currentPos, currentPos+AAPPacket.MAX_PAYLOAD_SIZE));
			}catch(FlagNotFoundException e){
				e.printStackTrace();
				break;
			}
			currentPos = currentPos+AAPPacket.MAX_PAYLOAD_SIZE;
			packetsList.add(sendAAPacket);
			currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
		}
		try{
			sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG,
				(short)0, Arrays.copyOfRange(b, currentPos, currentPos+AAPPacket.MAX_PAYLOAD_SIZE));
		}catch (FlagNotFoundException e){
					e.printStackTrace();
				}
		packetsList.add(sendAAPacket);
		currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
	}
	
	private void recvAckAndFillWindow() throws IOException, ServerNotRespondingException{	
		AAPPacket recvAAPPacket;
		while(true){		
			try {
				sendSocket.receive(recvPacket);
			} catch (InterruptedIOException e) {
				currentTries--;
				//Reset the connection if the server is not responding
				if(currentTries == 0){
					throw new ServerNotRespondingException("Server not responding. Connection abort. Please reset connection.");
				}
				break;//break if times out
			}
			currentTries = MAX_TRY;
			
			try{
				//Drop the packet if it was bad
				 recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
				 
				 if(recvAAPPacket.getFlags() == AAPPacket.ACK_FLAG ||
						 recvAAPPacket.getFlags() == AAPPacket.FIN_ACK_FLAG ||
						 recvAAPPacket.getFlags() == AAPPacket.SYN_ACK_FLAG ){
					 lastAcked = Math.max(lastAcked,recvAAPPacket.getAckNum());
					 //discard all acked packets
					 discardAckedPackets(lastAcked);
					 //Adjust window size according to remaining buffe size
					 currentWindowSize = (short) Math.min(MAX_WINDOW_SIZE,(short)Math.floor
							 (recvAAPPacket.getWindowSize()/AAPPacket.MAX_PAYLOAD_SIZE));
					 //Will only actually shirink if the number of packets in window is greater than
					 //the window size
					 shirinkWindowSize(currentWindowSize);
					 //Fill the window, until list is empty or packets in window reach the maxium window size
					 fillWindow();
				 }
			 }catch(FlagNotFoundException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }catch(PacketCorruptedException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }
		}
	}
	
	private void shirinkWindowSize(int newWindowSize) throws IOException{
		while(packetWindow.size()>newWindowSize){
			moveWindowTailToList();
		}
	}
	
	private AAPPacket moveListHeadToWindow(){
		AAPPacket polled;
		if(packetWindow.size() < currentWindowSize){
			polled = packetsList.poll();
			packetWindow.add(polled);
			return polled;
		}
		return null;
	}
	
	private void moveWindowTailToList(){
			packetsList.push(packetWindow.pollLast());
	}
	
	private void discardAckedPackets(int ackedNumber){
		while(packetWindow.size()!=0 && packetWindow.peek().getSeqNum() < ackedNumber){
			packetWindow.poll();
		}
	}
	
	private void fillWindow() throws UnknownHostException, IOException{
		while(packetWindow.size() < currentWindowSize && packetsList.size() != 0){
			sendSocket.send(new DatagramPacket(moveListHeadToWindow().getPacketData(),
					  AAPPacket.PACKET_SIZE,InetAddress.getByName(recverAddress), recverPort));
		}
	}
	
	/**
	 * 
	 * @param num The number of packets in window to send
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	
	private void sendPacketInWindow(int num) throws UnknownHostException, IOException{
		 for(int i = 0; i < num; i++){
			 sendSocket.send(new DatagramPacket(packetWindow.get(i).getPacketData(),
					  AAPPacket.PACKET_SIZE,InetAddress.getByName(recverAddress), recverPort));
		 }
		  
	}
	
}
