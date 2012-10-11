package sdp2.misc;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import processing.core.PApplet;
import sdp2.vision.Vision_Beta;


public class VisionKeyAdapter extends KeyAdapter {
	private Vision_Beta vision;
		
	public VisionKeyAdapter(Vision_Beta vision) {
		this.vision = vision;
	}
	
	public void setVisionSystem(Vision_Beta vision) {
		this.vision = vision;
	}
	
	public PApplet getVisionSystem() {
		return vision;
	}
	
	public void keyReleased( KeyEvent e ) { 
		if ( e.getKeyCode()==KeyEvent.VK_S ) { // Press "S" to save the frame to disk 
			System.out.println("Save as: (type 'cancel' if you don't want to save the image)");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String filename = null;
			try{
				filename = br.readLine();
			}
			catch(IOException ioe){
				System.out.println("Error");
				ioe.printStackTrace();
			}
			if(filename.equals("cancel")) {
				System.out.println("Save cancelled");
			}
			else {
				vision.save(filename + ".jpeg");
				System.out.println("Saved as "+ filename + ".jpeg");
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_F) { // Print framerate
			vision.printFrameRate = !vision.printFrameRate;
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {  // Draw object central points
			vision.draw = !vision.draw;
		}
		if(e.getKeyCode() == KeyEvent.VK_P)	{ // Print the robots and ball coordinates
			vision.print = !vision.print;
		}
		if(e.getKeyCode() == KeyEvent.VK_T) { // Print the time of the red ball detection
			vision.time = !vision.time;
		}
		if(e.getKeyCode() == KeyEvent.VK_H) { // Print help menu
			System.out.println("Help menu. \nPress one of the following to do the following:" +
					"\nf - display the framerate" +
					"\np - print the coordinates of the ball and the robots" +
					"\ns - save the current frame as a jpeg image" +
					"\nd - draw the coordinates of the robots and the ball" +
					"\nt - print the time moment at which the ball is detected in the image" +
					"\nh - display this help menu" +
					"\nq - open server commands menu" +
					"\na - open adaptive thresholding menu" +
					"\nleft mouse button - click on the image to display the specific pixel's coordinates and its colour values" +
					"\nESC - quit program");
		}
		if(e.getKeyCode() == KeyEvent.VK_Q) { // Advanced commands menu
			System.out.println("Server commands. For now, you can only manually restart the server.\nType one of the following and press ENTER: " +
					"\nback - exit this menu without any changes" +
					"\nrestart - restart the server thread");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String command = null;
			try{
				command = br.readLine();
			}
			catch(IOException ioe){
				System.out.println("Error");
				ioe.printStackTrace();
			}
			while(!command.equals("back")) {
				if(command.equals("restart")) {
					vision.getServer().cleanup();
					try {
						command = br.readLine();
					} catch (IOException e1) {
						e1.printStackTrace();
					}	
					
				}
				else
					System.out.println("Invalid command. ");
				try {
					command = br.readLine();
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
				System.out.println("Nothing changed...");
			
			
		}
		if(e.getKeyCode() == KeyEvent.VK_A ) {
			System.out.println("Adaptive thresholding menu. \nType one of the following commands and press ENTER:" +
					"\nbrightness - enter the brightness adjustment sub-menu" +
					"\ncontrast - enter the contrast adjustment sub-menu" +
					"\nred - enter the red colour adjustment sub-menu" +
					"\nyellow - enter the yellow colour adjustment sub-menu" +
					"\nblue - enter the blue colour adjustment sub-menu" +
					"\nback - return to console");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String command = null;
			try{
				command = br.readLine();
			}
			catch(IOException ioe){
				System.out.println("Error");
				ioe.printStackTrace();
			}
			if(command.equals("back")) {
				System.out.println("Returning to console.");
			}
			else {
				if(command.equals("brightness")) {
					System.out.println("Brightness level adjustment sub-menu.\nEnter +/- to increase/decrease the brightness level\nor type in an integer value to manually set the brightness value.\nType 'reset' to reset the brightness level to its default value.");
					int default_lvl = vision.getBrightnessLvl();
					try {
						command = br.readLine();
					}
					catch(IOException ioe) {
						System.out.println("Error");
						ioe.printStackTrace();
					}
					
				}
			}
		
		}
	}
	
}
