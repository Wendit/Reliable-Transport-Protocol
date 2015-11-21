import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
		//startServer(port);
		
		while (running) {
			//if(userCommand()
			handleCommands(userCommand(), server);
			handleClient(server.accept());
			
		}
		
		server.close();
	}
	
	private static void handleClient(Socket clientSocket) throws IOException { 
	//private static void handleClient(AAPSocket clientSocket) {
		COMMAND recvcmd;
		/*
		AAPInputStream in = clientSocket.getInputStream();
	    AAPOutputStream out = clientSocket.getOutputStream();
		*/
		InputStream in = clientSocket.getInputStream();
	    OutputStream out = clientSocket.getOutputStream();
	    
	    while()
	    
		
		clientSocket.close();
		return;
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
