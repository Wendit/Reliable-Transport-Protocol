import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
		Socket client = new Socket(emu_addr, emu_port);
		InputStream in = client.getInputStream();
	    OutputStream out = client.getOutputStream();
		
		
		while(running) {
			
			handleCommands(userCommand(), client);
			
		}

	}
	
	protected static void handleCommands(COMMAND command, Socket client) {
		//protected static void handleCommands(COMMAND command, AAPServerSocket server) {
		if(command == COMMAND.CONNECT) {
			
		} else if (command == COMMAND.GET) {
			
		} else if (command == COMMAND.POST) {
			
			
		} else if (command == COMMAND.INVALID){
			
		} else {}
		}
	
	
	private static boolean get(String fileName) {
		
		return false;
	}
	
	private static boolean post(String fileName) {
		
		return false;
	}

	private static boolean connect() {
		
		return false;
	}
	
	private static boolean disconnet() {
		
		return false;
	}

}
