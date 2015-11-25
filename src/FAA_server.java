import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;


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
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException {
	// TODO Auto-generated method stub
	validateInput(args);
	System.out.println("Server: openning port " + port);
	ServerSocket server = new ServerSocket(port);
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
	    if(running)
	    	handleClient(server.accept());
	}
	server.close();
    }
	
    private static void handleClient(Socket clientSocket) throws IOException {
	//private static void handleClient(AAPSocket clientSocket) {
	COMMAND recvcmd;
	boolean curClient = true;
	/*
	  AAPInputStream in = clientSocket.getInputStream();
	  AAPOutputStream out = clientSocket.getOutputStream();
	*/
	InputStream in = clientSocket.getInputStream();
	OutputStream out = clientSocket.getOutputStream();
	    
	/*
	 * Test purpose
	 * */
	SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
	System.out.println("Server: Handling client at " + clientAddress);
	    
	while ( curClient && (recvcmd = getRequest(in)) != COMMAND.DISCONNECT) {
	    processRequest(in, out, recvcmd, clientSocket);
	}
		
	System.out.println("Clent close the connection");
	clientSocket.close();
	return;
    }


    private static void processRequest(InputStream in, OutputStream out, COMMAND recvcmd, Socket clientSocket) throws IOException {
	if(recvcmd == COMMAND.CONNECT) {
	    System.out.println("Handling Client: " + new String(clientSocket.getInetAddress().getAddress()));
	} else if(recvcmd == COMMAND.GET) {
	    sendFile(cmd_extra, out);
	} else if (recvcmd == COMMAND.POST) {
	    recvFile(cmd_extra, in);
	}

    }

    private static COMMAND getRequest(InputStream in) throws IOException {
	int recvSize = 0;
	String request = "";
	if((recvSize = in.read(recvBuff)) != -1) {
	    request = new String(recvBuff, 0, recvSize);
	}
	return processCommand(request, false); // ***********************************
    }


    protected static void handleCommands(COMMAND command, ServerSocket server) {
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
