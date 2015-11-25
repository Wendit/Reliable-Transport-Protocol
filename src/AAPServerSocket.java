import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AAPServerSocket {
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private int port;
	private static final int TIMEOUT = 3000;
	private static final int MAX_TRY = 10;
    private InetAddress remoteSocketAddress;
    private int remoteSocketPort;
	
	/**
	 * Default constructor
	 */
	
	public AAPServerSocket(int port) {
		this.port = port;
		
	}
	
	public AAPSocket accept() throws IOException, ServerNotRespondingException{
		//three way handshake here
		while(!threeWayHandShake());
		return new AAPSocket(remoteSocketAddress.getHostAddress(),remoteSocketPort, port, true); 
	}
	
	public void bind(){
		
	}
	
	public void close() throws SocketException{

	}
	
	private boolean threeWayHandShake() throws IOException{
		DatagramSocket socket = new DatagramSocket(port);
		DatagramPacket recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		AAPPacket recvAAPPacket;
	    AAPPacket sendAAPacket;
	    socket.setSoTimeout(TIMEOUT);
	    int tries = MAX_TRY;
	    
    	//Receive SYN
    	socket.receive(recvPacket);
    	remoteSocketAddress = recvPacket.getAddress();
    	remoteSocketPort = recvPacket.getPort();
    	
    	try{		
    		//Extract SYN
    		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
    		//Send SYN_ACK
    		if(recvAAPPacket.getFlags() == AAPPacket.SYN_FLAG){
    			sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_ACK_FLAG,
    					(short)0, "SYN_ACK".getBytes());
    	    	socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
    					  AAPPacket.PACKET_SIZE,remoteSocketAddress, remoteSocketPort));
    		}
    		
    		while(tries != 0){
	    		//Receive ACK
	    		try{
	    			socket.receive(recvPacket);
	    			//Extract ACK
	        		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
	        		if(recvPacket.getAddress().equals(remoteSocketAddress)
	        				&& recvAAPPacket.getFlags() == AAPPacket.ACK_FLAG){
	        			socket.close();
	        			return true;
	        		}else{
	        			tries --;
	        		}
	    			
	    		}catch(InterruptedIOException e){
	    			tries --;
	    		}
    		}
    	}catch(FlagNotFoundException e){
			 DebugUtils.debugPrint(e.getMessage());
		 }catch(PacketCorruptedException e){
			 DebugUtils.debugPrint(e.getMessage());
		 }catch(PayLoadSizeTooLargeException e){
			 DebugUtils.debugPrint(e.getMessage());
		 }
    	socket.close();
    	return false;
	}

}
