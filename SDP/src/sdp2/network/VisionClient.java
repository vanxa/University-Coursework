package sdp2.network;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class VisionClient {
	private final static int PORT = 6767;
	private final static String HOSTNAME = "localhost";
	private final String CRLF = "\r\n";
	private static BufferedReader msgFromServer;
	private static DataInputStream inFromServer;
	private static DataOutputStream outToServer;
	private static Socket clientSocket;


	public static void main(String[] args) throws Exception{
		try{
			clientSocket = new Socket(HOSTNAME,PORT);
			System.out.println("Connected to Server");
		}
		catch(Exception e) {
			System.out.println("Server is not available!");
			e.printStackTrace();
		}
		msgFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		inFromServer = new DataInputStream(clientSocket.getInputStream());
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		System.out.println(msgFromServer.readLine());
		
		
		while(true) {
			byte[] test = new byte[20];
			inFromServer.readFully(test);
			//System.out.println(bytesToShort(test));	
			outToServer.writeInt(0);
			//System.out.println(bytesToShort(test));
		}
	}
	
	public static short bytesToShort(byte[] seqBytes){
		short seq = 0;
		ByteBuffer sequence_buffer = ByteBuffer.allocate(4);
		sequence_buffer.order(ByteOrder.LITTLE_ENDIAN);
		sequence_buffer.put(seqBytes);
		seq = sequence_buffer.getShort();
		return seq;
	}
}