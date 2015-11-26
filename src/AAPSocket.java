
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAPSocket {
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private static final int TIMEOUT = 3000;
	private static final int MAX_TRY = 10;
	private String remoteSocketAddress;
	DatagramSocket socket;
	private int remoteSocketPort;
	private int localBindPort;
	private AAPInputStream inputStream;
	private AAPOutputStream outputStream;
	
	
	/**
	 * Default constructor
	 * @throws ServerNotRespondingException 
	 * @throws PayLoadSizeTooLargeException 
	 * @throws IOException 
	 * @throws FlagNotFoundException 
	 */
	public AAPSocket(String remoteSocketAddress,int remoteSocketPort, int localBindPort) throws IOException, ServerNotRespondingException{
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.localBindPort = localBindPort;
		this.socket = new DatagramSocket(localBindPort);
		this.socket.setSoTimeout(TIMEOUT);
		//Three way handshake here
		threeWayHandShake(remoteSocketAddress,remoteSocketPort);
	}
	
	/**
	 * Server accept constructor
	 * @param remoteSocketAddress
	 * @param remoteSocketPort
	 * @param localBindPort
	 * @throws IOException
	 * @throws PayLoadSizeTooLargeException
	 * @throws ServerNotRespondingException
	 */
	public AAPSocket(String remoteSocketAddress,int remoteSocketPort, int localBindPort, boolean fromServerAccept) throws IOException,ServerNotRespondingException{
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.localBindPort = localBindPort;
		this.socket = new DatagramSocket(localBindPort);
		this.socket.setSoTimeout(TIMEOUT);
		if(fromServerAccept){
			this.inputStream = new AAPInputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
			this.outputStream = new AAPOutputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
		}else{
			threeWayHandShake(remoteSocketAddress,remoteSocketPort);
		}
	}
	
	public AAPInputStream getInputStream(){
		return inputStream;
	}
	
	public AAPOutputStream getOutputStream(){
		return outputStream;
	}
	
	public void close() throws UnknownHostException, IOException{
		socket.close();
		return;
	}
	
	public InetAddress getRemoteSocketAddress() throws UnknownHostException{
		return InetAddress.getByName(remoteSocketAddress);
	}
	
	private void threeWayHandShake(String remoteSocketAddress,int remoteSocketPort) throws IOException, ServerNotRespondingException{

		DatagramPacket recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		AAPPacket recvAAPPacket;
	    AAPPacket sendAAPacket;
	    
	    int tries = MAX_TRY;

	    while(true){
		    	try{
		    		//Send SYN
					sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_FLAG,
							(short)0, "SYN".getBytes());
					DatagramPacket temp = new DatagramPacket(sendAAPacket.getPacketData(),
							  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort);
			    	socket.send(temp);
			    	DebugUtils.debugPrint("Send SYN to server: "+remoteSocketAddress+" "+remoteSocketPort);
		    	
					//Receive SYN_ACK
					sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_FLAG,
							(short)0, "SYN".getBytes());
					socket.receive(recvPacket);
					DebugUtils.debugPrint("Recieved SYN_ACK from server: "+remoteSocketAddress+" "+remoteSocketPort);
									    	
		    		//Extract SYN_ACK
		    		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
		    		if(recvAAPPacket.getFlags() == AAPPacket.SYN_ACK_FLAG){
		    			sendAAPacket = new AAPPacket(0, 0, AAPPacket.ACK_FLAG,
		    					(short)0, "ACK".getBytes());
		    	    	socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
		    					  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
		    	    	
		    	    	this.inputStream = new AAPInputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
		    			this.outputStream = new AAPOutputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
		    			DebugUtils.debugPrint("Send ACK back to server: "+remoteSocketAddress+" "+remoteSocketPort);
		    			break;
		    		}
		    		
		    	}catch(InterruptedIOException e){
		    		tries--;
		    		if(tries == 0){
		    			DebugUtils.debugPrint("Server is not responding. Please reset the sockt.");
		    			throw new ServerNotRespondingException("Server is not responding. Please reset sockt.");
		    		}
		    	}
		    	catch(FlagNotFoundException | PayLoadSizeTooLargeException | PacketCorruptedException e){
		    		tries--;	
		    		e.getMessage();
		    	}
	    	}
	   }
}
