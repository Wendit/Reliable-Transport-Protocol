
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
		AAPPacket sendAAPacket;
		AAPPacket recvAAPPacket;
		DatagramPacket recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		int currentTry = MAX_TRY;
		while(currentTry != 0){
			try {
				sendAAPacket = new AAPPacket(0, 0, AAPPacket.FIN_FLAG,
						(short)0, "FIN".getBytes());
				socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
						  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
				try{
					socket.receive(recvPacket);
					recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
					if(recvAAPPacket.getFlags() == AAPPacket.FIN_ACK_FLAG || recvAAPPacket.getFlags() == AAPPacket.FIN_FLAG){
						if(recvAAPPacket.getFlags() == AAPPacket.FIN_FLAG){
							sendAAPacket = new AAPPacket(0, 0, AAPPacket.FIN_ACK_FLAG,
									(short)0, "FIN_ACK_FLAG".getBytes());
							socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
									  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
						}else{
							sendAAPacket = new AAPPacket(0, 0, AAPPacket.ACK_FLAG,
									(short)0, "ACK".getBytes());
							socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
									  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
							socket.close();
							return;
						}
					}else{
						currentTry--;
					}
				}catch(InterruptedIOException e){
					currentTry--;
				} catch (PacketCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FlagNotFoundException | PayLoadSizeTooLargeException e) {
				e.printStackTrace();
			}		
		}
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
		    	
					//Receive ACK
					sendAAPacket = new AAPPacket(0, 0, AAPPacket.SYN_FLAG,
							(short)0, "SYN".getBytes());
					socket.receive(recvPacket);
									    	
		    		//Extract ACK
		    		recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
		    		if(recvAAPPacket.getFlags() == AAPPacket.SYN_ACK_FLAG){
		    			sendAAPacket = new AAPPacket(0, 0, AAPPacket.ACK_FLAG,
		    					(short)0, "ACK".getBytes());
		    	    	socket.send(new DatagramPacket(sendAAPacket.getPacketData(),
		    					  AAPPacket.PACKET_SIZE,InetAddress.getByName(remoteSocketAddress), remoteSocketPort));
		    	    	
		    	    	this.inputStream = new AAPInputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
		    			this.outputStream = new AAPOutputStream(socket,0, remoteSocketAddress, remoteSocketPort, this);
		    			break;
	
		    		}
		    		
		    	}catch(InterruptedIOException e){
		    		tries--;
		    		if(tries == 0){
		    			throw new ServerNotRespondingException("Server is not responding. Please reset sockt.");
		    		}
		    	}
		    	catch(FlagNotFoundException | PayLoadSizeTooLargeException | PacketCorruptedException e){
		    			e.printStackTrace();
		    	}
	    	}
	   }
}
