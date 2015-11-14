
public class DebugUtils {
	public static boolean DEBUG_FLAG = false;
	public DebugUtils() {
	}
	
	public static void debugPrint(String message){
		if(DEBUG_FLAG)
			System.out.println(message);
	}

}
