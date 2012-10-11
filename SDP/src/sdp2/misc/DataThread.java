package sdp2.misc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import sdp2.network.VisionServer;


public class DataThread implements Runnable {
	private Socket socket; // Client socket
	private DataOutputStream outMsg;
	private BufferedReader ack;
	private VisionServer server;	
	
	public DataThread(Socket socket, VisionServer server) throws IOException {
		this.socket = socket;
		this.server = server;
		outMsg = new DataOutputStream(socket.getOutputStream());
		ack = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
	}
	
	public void run() {
		System.out.println("Thread started...");
		send();
		
	}
	
	public void send() {
				
		while(!socket.isClosed()) {
			try {
					Thread.sleep(1500);
				} 
			catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			try {
				if(ack.readLine().equals("Go")) {
					System.out.println("SENDING DATA\n");
					outMsg.write(server.getData());
					
				}
			} catch (IOException e) {
				System.out.println("Ooops!");
			}
			
		}
	}

}
