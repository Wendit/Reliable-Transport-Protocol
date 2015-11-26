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
    private String remoteSocketAddress;
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
		return new AAPSocket(remoteSocketAddress,remoteSocketPort, port, true); 
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
	    while(true){
	    	try{
	    		socket.receive(recvPacket);
	    		DebugUtils.debugPrint("Recieved SYN from client");
	    		//block until receive
	    		break;
	    	}catch(InterruptedIOException e){
	    		
	    	}
	    }
	    
    	remoteSocketAddress = recvPacket.getAddress().toString().split("/")[1];
    	remoteSocketPort = recvPacket.getPort();
    	
    	try{	
    		DebugUtils.debugPrint("Recieved SYN from client: "+remoteSocketAddress+" "+remoteSocketPort);
    		//Extract SYN
    		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
    		//Send SYN_ACK
    		if(recvAAPPacket.getFlags() == AAPPacket.SYN_FLAG){
    			sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_ACK_FLAG,
    					(short)0, "SYN_ACK".getBytes());
    			DatagramPacket temp = new DatagramPacket(sendAAPacket.getPacketData(),
  					  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort);
    	    	socket.send(temp);
    	    	DebugUtils.debugPrint("Send SYN_ACK to client: "+remoteSocketAddress+" "+remoteSocketPort);
    		}
    		

    		//Receive ACK
    		try{
    			DebugUtils.debugPrint("Try to recieve ACK from client: "+remoteSocketAddress+" "+remoteSocketPort);
    			socket.receive(recvPacket);
    			//Extract ACK
        		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
        		if(recvPacket.getAddress().toString().split("/")[1].equals(remoteSocketAddress)
        				&& recvAAPPacket.getFlags() == AAPPacket.ACK_FLAG){
        			DebugUtils.debugPrint("Recieved ACK from client: "+remoteSocketAddress+" "+remoteSocketPort);
        			socket.close();
        			return true;
        		}
    			
    		}catch(InterruptedIOException e){
    			DebugUtils.debugPrint("Recieve SYN_ACK times out: "+remoteSocketAddress+" "+remoteSocketPort);
    			socket.close();
    			return false;
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
	
	public String getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

}
