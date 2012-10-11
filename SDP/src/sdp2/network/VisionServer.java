package sdp2.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sdp2.vision.Vision_Beta;

/**
 * 
 * Message will consist of:
 * 1. 4 bytes for ball coordinates
 * 2. 4 bytes for blue robot coord
 * 3. 4 bytes for yellow robot coord
 * 4. 4 bytes for blue robot direction
 * 5. 4 bytes for yellow robot direction
 * 6. 8 bytes for the time elapsed since the beginning of the match in milliseconds
 * In total: 20 bytes
 * @author Ivan Konstantinov
 *
 */
public class VisionServer implements Runnable {
	
	// Sockets and configurations
	private ServerSocket welcomeSocket;
	private BufferedReader ack;
	private final int PORT = 6767;
			
	// Data from Vision
	private byte[] data = new byte[28];
	
	// Exit flag
	private boolean exit = false;
	
	/**
	 * Constructor. Add functionality if needed
	 * 
	 */
	public VisionServer() {}
	
	@Override
	public void run() {
		try {
			welcomeSocket = new ServerSocket(PORT);
			System.out.println("### Server Started ###\n### Awaiting incoming connections... ###");
			connect();
		}
		catch(Exception e) {
			System.out.println("### Connection terminated... ###");
			if(!welcomeSocket.isClosed())
				cleanup();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			restart();
		}
		
	}
		
	public void connect() throws IOException {
		Socket connectionSocket = welcomeSocket.accept();
		System.out.println("### Connection Established ###");
		DataOutputStream message = new DataOutputStream(connectionSocket.getOutputStream());
		ack = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
		while(!exit) {
			if(ack.readLine().equals("Go")) {
				System.out.println("### Sending Data ###\n");
				message.write(data);
					
			}
				
		}
			System.out.println("### Finished. Closing connection... ###");
			welcomeSocket.close();
	}
		
	
	/**
	 *  Sets the data. Used by the Vision system. 
	 *  
	 * @param msg
	 */
	public void setData(int[] data,long tm) {
		byte[] bytes = new byte[28];
		ByteBuffer buffer = ByteBuffer.allocate(28);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort(0,(short)data[0]);
		buffer.putShort(2,(short)data[1]);
		buffer.putShort(4,(short)data[2]);
		buffer.putShort(6,(short)data[3]);
		buffer.putShort(8,(short)data[4]);
		buffer.putShort(10,(short)data[5]);
		buffer.putShort(12,(short)data[6]);
		buffer.putShort(14,(short)data[7]);
		buffer.putShort(16,(short)data[8]);
		buffer.putShort(18,(short)data[9]);
		buffer.putLong(20, (long)tm);
		for(int i=0;i<28;i++) {
			bytes[i] = buffer.get(i);
		}
		this.data = bytes;
	}
	
	public byte[] getData() {
		return data;
	}
		
	public void setFlag(boolean bool) {
		exit = bool;
	}
	
	public void cleanup() {
		System.out.println("### Cleaning up... ###");
		ack = null;
		try {
			welcomeSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void restart() {
		System.out.println("### Restarting server... ###");
		run();
	}

}
