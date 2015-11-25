import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;


public class FAA_UI {

    protected enum MODE {CLIENT,SERVER};
    protected enum COMMAND {WINDOW, TERMINATE, CONNECT, GET, POST, DISCONNECT, UNKNOWN};
    protected static boolean running = true;
    //protected enum 
    private MODE mode;
    protected static int port;
    protected static String emu_addr;
    protected static int emu_port;

    protected static int windowSize;
    protected static byte[] recvBuff = new byte[256];

    protected static COMMAND command;
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
    	System.out.println("Welcome to FAA, please input your command");
    	Scanner keyboard = new Scanner(System.in);
    	String input = keyboard.nextLine();
    	
    	try{
	    command = processCommand(input, false);
    	} catch(IOException e) {
	    throw e;
    	}
    	
    	System.out.println("You have typed in " + input);
    	return command;
    }
    
    protected static COMMAND processCommand(String input, boolean req) throws IOException {
    	String cmd = formatInput(input, req);
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
			default:			retcmd = COMMAND.UNKNOWN;
			    break;
    	}
    	return retcmd;
    }

    private static String formatInput(String input, boolean req) {
	// TODO Auto-generated method stub
		input = input.replaceAll("(\\s|\\t)+"," ");
		input = input.trim();
		String[] format = input.split("\\s");
		if(!req && format.length > 1) {
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
	
    protected static void sendFile(String filePath, OutputStream out) throws IOException {
    	//read the file from path
    
    	//	Path path = new Path(filePath);
    		File toSend = new File(filePath);
    		FileReader fileReader = new FileReader(toSend);
    		BufferedReader bufferedReader = new BufferedReader(fileReader);

    		try {
    	//ack receiver the transmission
    	//	out.write(new String("#ready to transfer#").getBytes());
    	
    	//file transfer
    		char[] sendBuff = new char[1024];
    		int size = 0;
    		while((size = bufferedReader.read(sendBuff)) > 0 ) {
    			out.write(new String(sendBuff,0,size).getBytes());
    		}
    	
    	//ack reciever the end of file transmission
        	out.write(new String("#end of transmission#").getBytes());
    	} catch (IOException e) {
    		bufferedReader.close();
    		throw e;
    	}
        	bufferedReader.close();
    }
  
    protected static void recvFile(String filePath, InputStream in) throws IOException, FileTransferException {
    	//File toRecv;
    	//BufferedWriter bufferwriter;
   
    		File toRecv = new File(filePath);
    		FileWriter fileWriter = new FileWriter(toRecv);
    		BufferedWriter bufferwriter= new BufferedWriter(fileWriter);
    		try {
	    	//	out.write(new String("#ready to receive#").getBytes());
		    	String response = "";
		    	int size = in.read(recvBuff);
		    	response = new String(recvBuff, 0, size);
		    	while(!response.equalsIgnoreCase("#end of transmission#")) {
		    		if(response.equalsIgnoreCase("#discard#")) {
		    			toRecv.delete();
		    			bufferwriter.close();
		    			throw new FileTransferException();
		    		}
		    		bufferwriter.write(response,0,size);
		    		size = in.read(recvBuff);
		    		response = new String(recvBuff, 0, size);
		    	}
    		} catch(IOException e) {
    			bufferwriter.close();
    			throw e;
    		}
	    bufferwriter.close();
    }
	
}
