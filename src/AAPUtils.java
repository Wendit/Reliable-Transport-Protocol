import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class AAPUtils {
	public static int incrementSeqNum(int seqNum){
		if(seqNum == Integer.MAX_VALUE){
			seqNum = 0;
		}else{
			seqNum++;
		}
		return seqNum;
	}
	
	public static byte[] getAAPPacketData(DatagramPacket recvPacket){
		return Arrays.copyOfRange(recvPacket.getData(), 0, recvPacket.getLength());		
	}
	
	public static AAPPacket getRecvAAPPacket(byte[] packetData) throws FlagNotFoundException, IOException, PacketCorruptedException{
		return new AAPPacket(packetData);
	}
}
