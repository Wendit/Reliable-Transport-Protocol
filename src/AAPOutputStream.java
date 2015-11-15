import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class AAPOutputStream {

	private DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	private AAPPacket sendAAPacket;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private ArrayList<DatagramPacket> packetsList; 
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
		this.packetsList = new ArrayList();
		this.currentWindowSize = MAX_WINDOW_SIZE;
		
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
			packetsList.add(new DatagramPacket(sendAAPacket.getPacketData(),
					sendAAPacket.getPacketData().length,InetAddress.getByName(recverAddress), recverPort));
			currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
		}
		sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG,
				(short)0, Arrays.copyOfRange(b, currentPos, currentPos+AAPPacket.MAX_PAYLOAD_SIZE));
		packetsList.add(new DatagramPacket(sendAAPacket.getPacketData(),
				sendAAPacket.getPacketData().length,InetAddress.getByName(recverAddress), recverPort));
		currentSeqNum = AAPUtils.incrementSeqNum(currentSeqNum);
	}
	
	private void sendPacketsInWindow() throws IOException{
		for(int i = 0; i < Math.min(packetsList.size(),currentWindowSize); i++){
			sendSocket.send(packetsList.get(i));
		}
	}
	
}
