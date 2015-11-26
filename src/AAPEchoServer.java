import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.io.*;   // for IOException and Input/OutputStream

public class AAPEchoServer {

  private static final int BUFSIZE = 32;   // Size of receive buffer

	  public static void main(String[] args) throws IOException {
	
	    if (args.length != 1)  // Test for correct # of args
	      throw new IllegalArgumentException("Parameter(s): <Port>");
	
	    int servPort = Integer.parseInt(args[0]);
	
	    // Create a server socket to accept client connection requests
	    AAPServerSocket servSock = new AAPServerSocket(servPort);
	
	    int recvMsgSize;   // Size of received message
	    byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
	    try{
		    while (true) { // Run forever, accepting and servicing connections
		      AAPSocket clntSock = servSock.accept();     // Get client connection
		
		      String clientAddress = clntSock.getRemoteSocketAddress().toString();
		      System.out.println("Handling client at " + clientAddress);
		      
		      AAPInputStream in = clntSock.getInputStream();
		      AAPOutputStream out = clntSock.getOutputStream();
		
		      // Receive until client closes connection, indicated by -1 return
		      while ((recvMsgSize = in.read(receiveBuf)) != -1) {
		        out.write(receiveBuf, 0, recvMsgSize);
		      }
		      if(!clntSock.socket.isClosed())
		    	  clntSock.close();  // Close the socket.  We are done with this client!
		    }
		    /* NOT REACHED */
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	}
}