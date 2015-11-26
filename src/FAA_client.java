import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
import java.net.UnknownHostException;

/*
 * 
 * Command-line: FxA-client X A P 

 The command-line arguments are: 
	
 X: the port number at which the FxA-client’s UDP socket should bind to (even number). Please remember that this port number should be equal to the server’s port number minus 1. 
	
 A: the IP address of NetEmu
 P: the UDP port number of NetEmu 
	
 Command: connect - The FxA-client connects to the FxA-server (running at the same IP host). 
	
 Command: get F - The FxA-client downloads file F from the server (if F exists in the same directory with the FxA-server program). 
	
 Command: post F - The FxA-client uploads file F to the server (if F exists in the same directory with the FxA-client program). This feature will be treated as extra credit for up to 20 project points.
	
 (FAA_UI)Command: window W (only for projects that support configurable flow window) W: the maximum receiver’s window-size at the FxA-Client (in segments). 
	
 Command: disconnect - The FxA-client terminates gracefully from the FxA-server. 


 * 
 * */
public class FAA_client extends FAA_UI{
	
    private static AAPSocket client;
    private static AAPInputStream in;
    private static AAPOutputStream out;
    private static boolean connected = false;
    private final static String CLIENT_DOWNLOAD_PATH = "/home/lu/Desktop/GIT/Reliable-Transport-Protocol/src/downloads/";
    private final static String FILE_PATH = "/home/lu/Desktop/GIT/Reliable-Transport-Protocol/src/test_files/";

    public FAA_client() {
	super(MODE.CLIENT);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {

	validateInput(args);
	
	/*
	  AAPSocket client = new AAPSocket(emu_addr, emu_port);
	  AAPInputStream in = client.getInputStream();
	  AAPOutputStream out = client.getOutputStream();
	*/
	/*
	  client = new Socket(emu_addr, emu_port);
	  InputStream in = client.getInputStream();
	  OutputStream out = client.getOutputStream();
	*/
		
	while(running) {
	    System.out.println("Connected: " + connected);
	    handleCommands(userCommand());	
	}

    }
	
    protected static void handleCommands(COMMAND command) throws IOException {
	//protected static void handleCommands(COMMAND command, AAPServerSocket server) {
		if(command == COMMAND.CONNECT && !connected) {
		    connect();
		} else if (command == COMMAND.GET && connected) {
			//if
		    get(cmd_extra);
		} else if (command == COMMAND.POST  && connected) {
			//if
		    post(cmd_extra);
		} else if (command == COMMAND.UNKNOWN  && connected){
		    System.out.println("Invalid command input, please retry");
		} else if(command == COMMAND.DISCONNECT  && connected){
			try {
				disconnect();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
    }
	
	
    private static boolean get(String fileName) {
    	System.out.println("Handling get: ");
    	try {
	    	out.write(new String("get " + cmd_extra).getBytes());
	    	String response = "";
	    	int size = 0;
	    	//************************************
	    //	int size = in.read(recvBuff);
	    //	while((size = in.read(recvBuff)) <= 0){}
	    	if ((size = waitUntilRead(in)) == -1) {
	    		return false;
	    	}
	    	response = new String(recvBuff, 0, size);
	    	if(response.equalsIgnoreCase("#ready to transfer#")) {
	    		System.out.println("start receiving from server");
	    		recvFile(CLIENT_DOWNLOAD_PATH + cmd_extra, in);
	    	}
	    } catch (Exception e) {
	    		System.out.println("Experiencing " + e.getMessage() + ". Please retry later.");
	    		return false;
	    	}
    	return true;
    }
	
    private static boolean post(String fileName) {
    	try {
        	out.write(new String("post " + cmd_extra).getBytes());
        	String response = "";
        	
        	int size = 0;
        	//***********************************************
        	//size = in.read(recvBuff);
        	//while((size = in.read(recvBuff)) <= 0) {}
        	if ((size = waitUntilRead(in)) == -1) {
	    		return false;
	    	}
        	response = new String(recvBuff, 0, size);
        	if(response.equalsIgnoreCase("#ready to receive#")) {
        		sendFile(FILE_PATH + cmd_extra, out);
        	}
        	} catch (Exception e) {
        		System.out.println("Experiencing " + e.getMessage() + ". Please retry later.");
        		return false;
        	}
    	return true;
    }

    private static void connect() throws UnknownHostException, IOException {
	try {
		
	    client = new AAPSocket(emu_addr, emu_port,port);
		in = client.getInputStream();
		out = client.getOutputStream();
		
			/*
			//client = new Socket();
			//client.bind(new InetSocketAddress(emu_addr, port));
			
			System.out.println("bind to local host: "  + " port: " + client.getLocalPort());
			client.connect(new InetSocketAddress(emu_addr, emu_port));
			in = client.getInputStream();
			out = client.getOutputStream();
			*/
		Thread.sleep(1000);
		out.write(new String("connect").getBytes());
		connected = true;
	} catch(Exception e) {
		System.out.println("Error happens " + e.getMessage() + ". please re-run.");
	}
	//	return true;
    }
	
    private static void disconnect() throws IOException, PayLoadSizeTooLargeException, ServerNotRespondingException, ConnectionAbortEarlyException, InterruptedException {
 		out.write(new String("disconnect").getBytes());
 		client.close();
 		connected = false;
	//return false;
    }

}
