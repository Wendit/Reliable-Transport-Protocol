import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FAA_UI {

	protected final static int BUF_SIZE = 256;
    protected enum MODE {CLIENT,SERVER};
    protected enum COMMAND {WINDOW, TERMINATE, CONNECT, GET, POST, DISCONNECT, UNKNOWN};
    protected static boolean running = true;
    //protected enum 
    private MODE mode;
    protected static int port;
    protected static String emu_addr;
    protected static int emu_port;

    protected static int windowSize;
    protected static byte[] recvBuff = new byte[BUF_SIZE];

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
	
    protected static void sendFile(String filePath, OutputStream out, InputStream in) throws IOException {
    	
    	//read the file from path	
    		File toSend = new File(filePath);
    		FileInputStream fis = new FileInputStream(toSend);

    		try {
    	//ack receiver the transmission
    	//	out.write(new String("#ready to transfer#").getBytes());
    	
    	//file transfer
    		byte[] sendByteBuff = new byte[BUF_SIZE];
    		int byteBuffSize = 0;
    		int size = in.read(recvBuff);
    		String response = new String(recvBuff, 0, size);
    		if(response.equalsIgnoreCase("#ready to receive#")) {
	    		while((byteBuffSize = fis.read(sendByteBuff)) != -1) {
	    			//out.write(sendByteBuff, 0, byteBuffSize);
	    			out.write(sendByteBuff);
	    			out.flush();
	    		}
    		}

    		/*
    		int c;
    		while((c = bufferedReader.read()) >=0 ) {
    			out.write(c);
    		}
    	*/
    	//ack reciever the end of file transmission
    		out.flush();
    		//Thread.sleep(500);
        	out.write(new String("#end of transmission#").getBytes());
        	out.flush();
    	} catch (IOException e) {
    		fis.close();
    		throw e;
    	}
    		fis.close();
    }
  
    protected static void recvFile(String filePath, InputStream in, OutputStream out) throws IOException, FileTransferException {

    	System.out.println("start receiving file from " + filePath);
    		File toRecv = new File(filePath);
    		FileOutputStream fos = new FileOutputStream(toRecv);
		
    		try {
	    		out.write(new String("#ready to receive#").getBytes());
		    	String response = "";
		    	int size = 0;

		    	size = in.read(recvBuff);
		    	response = new String(recvBuff, 0, size);
		      while(!response.equalsIgnoreCase("#end of transmission#")) {
		    		if(response.contains("#discard#")) {
		    			toRecv.delete();
		    			fos.close();
		    			throw new FileTransferException();
		    		}
		    		fos.write(recvBuff, 0, size);
		    		System.out.println("successful wrote into file.");
		    		size = in.read(recvBuff);
		    		System.out.println("new size " + size);
		    		response = new String(recvBuff, 0, size);
		    	}

    		} catch(IOException e) {
    			fos.close();
    			throw e;
    		}
    		System.out.println("end of receive.");
	    fos.close();
    }
	
}
