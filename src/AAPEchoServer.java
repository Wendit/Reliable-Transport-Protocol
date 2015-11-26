import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.io.*;   // for IOException and Input/OutputStream

public class AAPEchoServer {
	static byte[] receiveBuf;
  private static final int BUFSIZE = 32;   // Size of receive buffer

	  public static void main(String[] args) throws IOException, ServerNotRespondingException {
	
	    if (args.length != 1)  // Test for correct # of args
	      throw new IllegalArgumentException("Parameter(s): <Port>");
	
	    int servPort = Integer.parseInt(args[0]);
	
	    // Create a server socket to accept client connection requests
	    AAPServerSocket servSock = new AAPServerSocket(servPort);
	
	    int recvMsgSize;   // Size of received message
	    receiveBuf = new byte[BUFSIZE];  // Receive buffer
	    AAPSocket clntSock;
		    while (true) { // Run forever, accepting and servicing connections
		      
				      clntSock = servSock.accept();     // Get client connection
				      receiveBuf = new byte[BUFSIZE];  // Receive buffer
				      try{
					      String clientAddress = clntSock.getRemoteSocketAddress().toString();
					      System.out.println("Handling client at " + clientAddress);
					      
					      SWInputStream in = clntSock.getSWInputStream();
					      SWOutputStream out = clntSock.getSWOutputStream();
					
					      // Receive until client closes connection, indicated by -1 return
					      
					      recvMsgSize = waitUntilRead(in);
					      if(recvMsgSize != -1)
					      out.write(receiveBuf, 0, recvMsgSize);
					      
					      if(!clntSock.socket.isClosed()){
					    	  DebugUtils.debugPrint("Close the socket for current user ");
					    	  clntSock.close();  // Close the socket.  We are done with this client!
				      }
		  	  }catch(Exception e){
				  e.printStackTrace();
				  if(!clntSock.socket.isClosed()){
			    	  DebugUtils.debugPrint("Close the socket for current user ");
			    	  clntSock.close();  // Close the socket.  We are done with this client!
				  }
		  	  }
		    }
		    /* NOT REACHED */

	}
	  
	  protected static int waitUntilRead(SWInputStream in) throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException {
		  	int size = 0;
		  	while((size = in.read(receiveBuf)) <= 0) {
		  		if(size == -1) {
		  			break;
		  		}
		  	}
		  	return size;
		  }
}