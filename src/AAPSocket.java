import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AAPSocket {
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
}
