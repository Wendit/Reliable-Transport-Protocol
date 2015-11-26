import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketAddress;


/*
 * 
 * FxA SERVER
 * 
 * Command-line: FxA-server X A P 

 The command-line arguments are:
 X: the port number at which the FxA-server’s UDP socket should bind to (odd number) 

 A: the IP address of NetEmu
 P: the UDP port number of NetEmu
 * 
 * Command: window W (only for projects that support pipelined and bi- directional transfers) 

 W: the maximum receiver’s window-size at the FxA-Server (in segments). 
 * 
 * Command: terminate Shut-down FxA-Server gracefully. 
 * 
 */
public class FAA_server extends FAA_UI/* implements Runnable*/{
	
	private final static String SERVER_DOWNLOAD_PATH = "uploads/";
    private final static String FILE_PATH = "test_files/";

    //private static boolean running = true;
    public FAA_server() {
	super(MODE.SERVER);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException {
	// TODO Auto-generated method stub
		validateInput(args);
		System.out.println("Server: openning port " + port);
		AAPServerSocket server = new AAPServerSocket(port);
		//AAPServerSocket server = new AAPServerSocket(port);
		/*	
			new Thread(new Runnable(){
			public void run(){
			while(userCommand() != COMMAND.TERMINATE) {
			if(userCommand() == COMMAND.WINDOW) {
			//setwindowsize; 
			}
			}
			running = false;
			}
			});
		*/
			
		while (running) {
		    //if(userCommand()
		   // handleCommands(userCommand(), server);
		    if(running) {
		    	try {
		    		handleClient(server.accept());
		    	} catch(Exception e) {
		    		System.out.println(e.getMessage());
		    	}
		    }
		}
		server.close();
    }
	
    private static void handleClient(AAPSocket clientSocket) throws Exception   {
	//private static void handleClient(AAPSocket clientSocket) {
		COMMAND recvcmd;
		boolean curClient = true;
		/*
		  AAPInputStream in = clientSocket.getInputStream();
		  AAPOutputStream out = clientSocket.getOutputStream();
		*/
		AAPInputStream in = clientSocket.getInputStream();
		AAPOutputStream out = clientSocket.getOutputStream();
		    
		/*
		 * Test purpose
		 * */
		//InetAddress clientAddress = clientSocket.getRemoteSocketAddress();
		//System.out.println("Server: Handling client at " + clientAddress.getAddress());
		try {
		while ( curClient && (recvcmd = getRequest(in)) != COMMAND.DISCONNECT) {
		    System.out.println("Server while loop");
			processRequest(in, out, recvcmd, clientSocket);
		}
		} catch(Exception e) {
			clientSocket.close();
			throw e;
		}
			
		System.out.println("Clent close the connection");
		clientSocket.close();
		return;
    }


    private static void processRequest(AAPInputStream in, AAPOutputStream out, COMMAND recvcmd, AAPSocket clientSocket) {
    	System.out.println("processRequest");
    	if(recvcmd == COMMAND.CONNECT) {
		    System.out.println("receive connect request********************************");
		} else if(recvcmd == COMMAND.GET) {
			//System.out.println("receive get request from " + new String(clientSocket.getRemoteSocketAddress().getAddress()));
			try {
				if(handleGet(out)) {
					System.out.println("Sending file successfully.");
				} else {
					System.out.println("Sending file failed.");
				}
			} catch(Exception e) {
				System.out.println("Connection issue: " + e.getMessage());
			}
		} else if (recvcmd == COMMAND.POST) {
			//System.out.println("receive post request from " + new String(clientSocket.getRemoteSocketAddress().getAddress()));
			try {
				if(handlePost(in, out)) {
					System.out.println("Receiving file successfully.");
				} else {
					System.out.println("Receiving file failed.");
				}
			} catch(Exception e) {
				System.out.println("Connection issue: " + e.getMessage());
			}
		}
		//System.out.println("processRequest");

    }
    
    private static boolean handleGet(AAPOutputStream out) throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException, ConnectionAbortEarlyException, InterruptedException {
    	try {
	    	out.write(new String("#ready to transfer#").getBytes());
	    	System.out.println("recieve get" + cmd_extra + "request");
	    	sendFile(FILE_PATH + cmd_extra, out);
    	} catch (Exception e) {
	    	out.write(new String("#discard#").getBytes());
	    	return false;
    	}
    	return true;
    }

    private static boolean handlePost(AAPInputStream in, AAPOutputStream out) throws IOException, FileTransferException, PayLoadSizeTooLargeException, ServerNotRespondingException, ConnectionAbortEarlyException, InterruptedException {
    	try {
    		out.write(new String("#ready to receive#").getBytes());
	    	System.out.println("recieve post" + cmd_extra + "request");
	    	recvFile(SERVER_DOWNLOAD_PATH + cmd_extra, in);
    	} catch (IOException e) {
    		System.out.println(e.getMessage());
    	}
    	return true;
    }
    private static COMMAND getRequest(AAPInputStream in) throws ServerNotRespondingException, ConnectionAbortEarlyException, IOException {
		int recvSize = 0;
		String request = "";
		System.out.println("waiting for request.");
		//****************************************************
		//if((recvSize = in.read(recvBuff)) > -1) {
	//	while((recvSize = in.read(recvBuff)) <= 0) {}
		if ((recvSize = waitUntilRead(in)) == -1) {
    		return COMMAND.DISCONNECT;
    	}
	    request = new String(recvBuff, 0, recvSize);

		System.out.println("get request " + request);
		return processCommand(request, false); // ***********************************
    }


    protected static void handleCommands(COMMAND command, AAPServerSocket server) {
		//protected static void handleCommands(COMMAND command, AAPServerSocket server) {
		if(command == COMMAND.TERMINATE)
		    running = false;
		if(command == COMMAND.WINDOW) {
		    setWindowSize(windowSize);
		}
    }
    /*
      @Override
      public void run() {
      // TODO Auto-generated method stub
      }
    */

}
