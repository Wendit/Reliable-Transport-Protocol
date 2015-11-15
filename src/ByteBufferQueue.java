import java.util.LinkedList;
import java.util.Queue;

public class ByteBufferQueue {
	Queue<Byte> streamBuffer;
	
	public ByteBufferQueue() {
		streamBuffer = new LinkedList();
	}
	
	public void put(byte[] payload){
		for(int i = 0; i < payload.length; i++){
			streamBuffer.add(payload[i]);
		}
	}
	
	public void put(byte b){
		streamBuffer.add(b);
	}
	
	public Byte getByte(){
		return streamBuffer.poll();
	}
	
	public int getLength(){
		return streamBuffer.size();
	}
}
