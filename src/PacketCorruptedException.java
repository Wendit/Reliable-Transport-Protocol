
public class PacketCorruptedException extends Exception{

	public PacketCorruptedException(String message) {
		super("Checksum does not match. The packet is corrupted.");
	}

}
