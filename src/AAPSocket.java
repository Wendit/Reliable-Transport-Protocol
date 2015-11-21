import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AAPSocket {
	private String remoteSocketAddress;
	private int remoteSocketPort;
	private int localBindPort;
	
	/**
	 * Default constructor
	 */
	public AAPSocket(String remoteSocketAddress,int remoteSocketPort, int localBindPort){
		this.remoteSocketAddress = remoteSocketAddress;
		this.remoteSocketPort = remoteSocketPort;
		this.localBindPort = localBindPort;
	}
	
	public AAPInputStream getInputStream() throws UnknownHostException, SocketException{
		return new AAPInputStream(localBindPort,0, remoteSocketAddress, remoteSocketPort);
	}
	
	public AAPOutputStream getOutputStream(){
		return null;
	}
	
	public void close(){
		
	}
	
	public InetAddress getRemoteSocketAddress(){
		return null;
	}
}
