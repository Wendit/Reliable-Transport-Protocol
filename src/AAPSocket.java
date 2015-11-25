
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class AAPSocket {
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private static final int TIMEOUT = 3000;
	private static final int MAX_TRY = 10;
	private String remoteSocketAddress;
	private int remoteSocketPort;
	private int localBindPort;
	private AAPInputStream inputStream;
	private AAPOutputStream outputStream;
	
	
	/**
	 * Default constructor
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	public AAPSocket(String remoteSocketAddress,int remoteSocketPort, int localBindPort) throws UnknownHostException, SocketException{
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.localBindPort = localBindPort;
		this.inputStream = new AAPInputStream(localBindPort,0, remoteSocketAddress, remoteSocketPort);
		this.outputStream = new AAPOutputStream(localBindPort,0, remoteSocketAddress, remoteSocketPort);
		
		//Three way handshake here
	}
	
	public AAPInputStream getInputStream(){
		return inputStream;
	}
	
	public AAPOutputStream getOutputStream(){
		return outputStream;
	}
	
	public void close(){
		inputStream.close();
		outputStream.close();
	}
	
	public InetAddress getRemoteSocketAddress() throws UnknownHostException{
		return InetAddress.getByName(remoteSocketAddress);
	}
	
	public void threeWayHandShake(String remoteSocketAddress,int remoteSocketPort) throws FlagNotFoundException, IOException, PayLoadSizeTooLargeException{
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		AAPPacket recvAAPPacket;
	    AAPPacket sendAAPacket;
	    socket.setSoTimeout(TIMEOUT);
	    int tries = MAX_TRY;

	    while(tries >0){
	    	sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_FLAG,
					(short)0, "SYN".getBytes());
	    	socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
					  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
	    	socket.receive(recvPacket);
	    	try{
	    		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
	    		if(recvAAPPacket.getFlags() == AAPPacket.SYN_ACK_FLAG){
	    			sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_FLAG,
	    					(short)0, "SYN".getBytes());
	    	    	socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
	    					  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
	    		}
	    	}catch(FlagNotFoundException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }catch(PacketCorruptedException e){
				 DebugUtils.debugPrint(e.getMessage());
			 }
	    }
	    
	}
}
