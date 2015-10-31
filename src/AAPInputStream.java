import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AAPInputStream {
	private DatagramSocket recvSocket;
	private DatagramPacket  recvPacket;
	
	public AAPInputStream(String address, int port) throws UnknownHostException, SocketException  {
	}
	
	public Byte read(){
		return null;
	}
	
	public int read(byte[] recvBuffer){
		return 0;
	}
	
	public int read(byte[] recvBuffer, int off, int len){
		return 0;
	}
	
	public void close(){
		
	}


}
