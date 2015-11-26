
import java.net.SocketException;
import java.util.Arrays;
import java.io.IOException;



public class AAPEchoClient {
	static byte[] data;
  public static void main(String[] args) throws IOException {

    if ((args.length < 2) || (args.length > 3))  // Test for correct # of args
      throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");

    String server = args[0];       // Server name or IP address
    // Convert argument String to bytes using the default character encoding

     //data = args[1].getBytes();
    data = new byte[400];
    Arrays.fill(data, (byte)200);
    int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;

    // Create socket that is connected to server on specified port
    AAPSocket socket;
	try {
		socket = new AAPSocket(server, servPort,8080);
		
	    System.out.println("Connected to server...sending echo string");
	
	    AAPInputStream in = socket.getInputStream();
	    AAPOutputStream out = socket.getOutputStream();
	
	    out.write(data);  // Send the encoded string to the server
	
	    // Receive the same string back from the server
	    int totalBytesRcvd = 0;  // Total bytes received so far
	    int bytesRcvd;           // Bytes received in last read
	    while (totalBytesRcvd < data.length) {    	
	      totalBytesRcvd += waitUntilRead(in);;
	    }  // data array is full
	
	    System.out.println("Received: " + new String(data));
	
	    if(!socket.socket.isClosed())
	    	socket.close(); 
	    // Close the socket and its streams
	} catch (ServerNotRespondingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}catch(ConnectionAbortEarlyException e){
		e.printStackTrace();
	}catch(PayLoadSizeTooLargeException e){
		e.printStackTrace();
	}
  }
  
  protected static int waitUntilRead(AAPInputStream in) throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException {
  	int size = 0;
  	while((size = in.read(data)) <= 0) {
  		if(size == -1) {
  			break;
  		}
  	}
  	DebugUtils.debugPrint("Current buffer:" + new String(data));
  	return size;
  }
}