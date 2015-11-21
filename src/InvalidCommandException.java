import java.io.IOException;


public class InvalidCommandException extends IOException{

	private final static String MSG = "Unknow command, please check your input.";
	public InvalidCommandException() {
		super(MSG);
	}
	
	public InvalidCommandException(String msg) {
		super(msg);
	}
}
