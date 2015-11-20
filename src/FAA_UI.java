import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;


public class FAA_UI {

    protected enum MODE {CLIENT,SERVER};
    protected enum COMMAND {WINDOW, TERMINATE, CONNECT, GET, POST, DISCONNECT, INVALID};
    //protected enum 
    private MODE mode;
    protected static int port;
    protected static String emu_addr;
    protected static int emu_port;
    
    protected static COMMAND command;
    protected static int windowSize;
    protected static String cmd_extra;
    

    public FAA_UI(MODE mode) {
    	this.mode = mode;
    }

    /*
     * Here are methods for user input
     * */
    protected static void validateInput(String[] args) throws IOException {
		if(args.length != 3) {
		    throw new IllegalArgumentException("Parameter(s): X<Port>  A<Add> P<NetEmu port>");
		   // return false;
		} else {
			try{
				port = Integer.parseInt(args[0]);
				emu_addr = args[1];
				emu_port = Integer.parseInt(args[2]);
			} catch (IllegalArgumentException e) {
				throw e;
			}
			
		}
    }
    
    protected static COMMAND userCommand() throws IOException{	
		Scanner keyboard = new Scanner(System.in);
    	String input = keyboard.nextLine();
    	
    	try{
    		command = processCommand(input);
    	} catch(IOException e) {
    		throw e;
    	}

    	return command;
    }
    
    protected static COMMAND processCommand(String input) throws IOException {
    	String cmd = formatInput(input);
    	COMMAND retcmd;
    	
    	switch(cmd) {
    	case "window":		retcmd = COMMAND.WINDOW;
    						try {
    							windowSize = Integer.parseInt(cmd_extra);
    						} catch (Exception e) {
    							throw new InvalidCommandException();
    						}
    						break;
    	case "terminate":	retcmd = COMMAND.TERMINATE;
							break;
    	case "connect":		retcmd = COMMAND.CONNECT;
							break;
    	case "disconnect":	retcmd = COMMAND.DISCONNECT;
							break;
		case "get":			retcmd = COMMAND.GET;
							break;
		case "post":		retcmd = COMMAND.POST;
							break;
		default:			retcmd = COMMAND.INVALID;
							break;
    	}
    	return retcmd;
    }

	private static String formatInput(String input) {
		// TODO Auto-generated method stub
		input = input.replaceAll("(\\s|\\t)+"," ");
		input = input.trim();
		String[] format = input.split("\\s");
		if(format.length > 1) {
			cmd_extra = format[1];
		}
		return format[0].toLowerCase();
	}
	
	/*
	 * Here are commands
	 * */
	protected static boolean setWindowSize(int newSize) {
		
		return false;
	}
	

	
	/*
	 * Here are methods for file sending and receiving
	 * */
	
	protected static boolean sendFile(String filePath, OutputStream out) throws FileNotFoundException {
		File toSend = new File(filePath);
	    FileReader fileReader = new FileReader(toSend);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);
	    
	    
		return false;
	}
  
	protected static boolean recvFile(File file, InputStream in) {
		
		return false;
	}
	
}
