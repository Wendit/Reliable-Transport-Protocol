
public class AAPUtils {
	public static int incrementSeqNum(int seqNum){
		if(seqNum == Integer.MAX_VALUE){
			seqNum = 0;
		}else{
			seqNum++;
		}
		return seqNum;
	}
}
