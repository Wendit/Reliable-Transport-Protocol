import java.io.IOException;


public class FileTransferException extends Exception{

	private final static String MSG = "File transfer fails.";
	public FileTransferException() {
		this(MSG);
	}

	public FileTransferException(String msg) {
		super(msg);
	}
}
