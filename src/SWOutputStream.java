import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

public class SWOutputStream {
	private static final int MAX_CONNECTION_TRY = 5;
	private static final int TIMEOUT = 3000;
	private DatagramSocket sendSocket;
	private byte[] packetBuffer = new byte[AAPPacket.PACKET_SIZE];
	private int currentSeqNum;
	private String recverAddress;
	private int recverPort;
	private DatagramPacket recvPacket;
	private int currentConnectionTries;
	private LinkedList<AAPPacket> packetsList;
	private AAPPacket sendAAPacket;

	public SWOutputStream(DatagramSocket sendSocket, int initSeqNum, String recverAddress, int recverPort)
			throws UnknownHostException, SocketException {
		this.sendSocket = sendSocket;
		sendSocket.setSoTimeout(TIMEOUT);
		this.currentSeqNum = AAPUtils.alterBit(initSeqNum);
		this.recverAddress = recverAddress;
		this.recverPort = recverPort;
		this.recvPacket = new DatagramPacket(packetBuffer, AAPPacket.PACKET_SIZE);
		this.currentConnectionTries = MAX_CONNECTION_TRY;
		this.packetsList = new LinkedList();
	}

	public void write(byte b) throws IOException {
		byte[] temp = { b };
		send(temp);
	}

	public void write(byte[] bArray) throws IOException {
		DebugUtils.debugPrint("Write string to: " + recverAddress + " " + recverPort);
		send(bArray);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		byte[] temp = Arrays.copyOfRange(b, off, off + len);
		send(temp);
	}

	private void send(byte[] bArray) throws IOException {
		putPacketInQueue(bArray);
		sendPacketInQueue();
	}

	private void putPacketInQueue(byte[] b) throws IOException {
		DebugUtils.debugPrint("Put packet in queue to: " + recverAddress + " " + recverPort);
		int byteLeft = b.length;
		int currentPos = 0;
		try {
			while (byteLeft - AAPPacket.MAX_PAYLOAD_SIZE > 0) {
				sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.NULL_FLAG, (short) 0,
						Arrays.copyOfRange(b, currentPos, currentPos + AAPPacket.MAX_PAYLOAD_SIZE));
				byteLeft -= AAPPacket.MAX_PAYLOAD_SIZE;
				currentPos = currentPos + AAPPacket.MAX_PAYLOAD_SIZE;
				packetsList.add(sendAAPacket);
				currentSeqNum = AAPUtils.alterBit(currentSeqNum);
			}
			sendAAPacket = new AAPPacket(currentSeqNum, 0, AAPPacket.END_OF_PACKET_FLAG, (short) 0,
					Arrays.copyOfRange(b, currentPos, currentPos + byteLeft));
			packetsList.add(sendAAPacket);
			currentSeqNum = AAPUtils.alterBit(currentSeqNum);
		} catch (FlagNotFoundException e) {
			e.printStackTrace();
		} catch (PayLoadSizeTooLargeException e) {
			e.printStackTrace();
		}
	}

	private void sendPacketInQueue() {
		while (packetsList.size() != 0) {
			sendSinglePacket();
		}
	}

	private void sendSinglePacket() {
		boolean recievedAck = false;
		try {
			while (!recievedAck) {
				AAPPacket recvAAPPacket;
				AAPPacket temp = packetsList.getFirst();
				sendSocket.send(new DatagramPacket(temp.getPacketData(), AAPPacket.PACKET_SIZE,
						InetAddress.getByName(recverAddress), recverPort));
				sendSocket.receive(recvPacket);
				//If packet recieved
				 recvAAPPacket = AAPUtils.getRecvAAPPacket(AAPUtils.getAAPPacketData(recvPacket));
				 if(recvAAPPacket.getFlags() == AAPPacket.ACK_FLAG && recvAAPPacket.getAckNum() != temp.getSeqNum()){
					 recievedAck = true;
					 packetsList.poll();
				 }
			}
		} catch (InterruptedIOException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlagNotFoundException e) {
			e.printStackTrace();
		} catch (PacketCorruptedException e) {
			e.printStackTrace();
		}
	}
}
