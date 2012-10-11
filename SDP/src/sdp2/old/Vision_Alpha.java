package sdp2.old;


/**
 * Written by Ivan Konstantinov
 * Version 1.0 
 *
 * Uses PApplet for image processing functions
 * 
 * 
 */
 
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import processing.core.*;
import sdp2.network.VisionServer;
import hypermedia.video.OpenCV;

public class Vision_Alpha extends PApplet {
	private static final String NAME = "Vision_Alpha";
	private OpenCV cv = null;	// OpenCV object
	private static VisionServer server;
	
	// Image adjustment variables
	private static final int BRIGHTNESS = -60;
	private static final int CONTRAST = 40;
	private static final int SMOOTHING_INDEX = 3;
	
	// Coordinate variables for the ball and the two robots
	private int ballX,ballY;
	
	// Flag for the ball
	boolean ballFlag;
				
	// Old ball coordinates, for convenience
	private int oldBallX =0;
	private int oldBallY =0;
	
	// Robot direction and position variables: 
	private int bluebottomY,bluetopY,bluerightX,blueleftX,bluetopX,bluebottomX,blueleftY,bluerightY,blueCX,blueCY;
	private int yellowbottomY,yellowtopY,yellowrightX,yellowleftX,yellowtopX,yellowbottomX,yellowleftY,yellowrightY,yellowCX,yellowCY;
		
	// Image Size parameters Default: 640x480
	static final int WIDTH = 480;
	static final int HEIGHT = 360;
	
	// Pitch dimensions:
	static final int DIMX = 65;
	static final int DIMY = 60;
	static final int DIMW = 400;
	static final int DIMH = 240;
		
	// Data variables
	private int[] blueD = new int[2];
	private int[] yellowD = new int[2];
	private int[] blueXY = new int[2];
	private int[] yellowXY = new int[2];
	private int[] ballXY = new int[2];
	
	
	/**
	 * Initialise Objects.
	 */
	public void setup() {
	
		// PApplet setup
		size( WIDTH, HEIGHT );
		background(0);
		
		// OpenCV setup
		cv = new OpenCV( this );
		cv.capture( WIDTH, HEIGHT );
		
		// Bind keys to specific functions
		this.addKeyListener(
				new KeyAdapter() {
					public void keyReleased( KeyEvent e ) { 
						if ( e.getKeyCode()==KeyEvent.VK_S ) { // Press "S" to save the frame to disk 
							System.out.println("Save as: ");
							BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
							String filename = null;
							try{
								filename = br.readLine();
							}
							catch(IOException ioe){
								System.out.println("Error");
								ioe.printStackTrace();
							}
							save(filename + ".jpeg");
							System.out.println("Saved as "+ filename + ".jpeg");
							
						}
						if (e.getKeyCode() ==KeyEvent.VK_L) { 
							System.out.println("Blue robot left coordinates are: "+"("+blueleftX+","+blueleftY+")");
							
						}
						if (e.getKeyCode() ==KeyEvent.VK_R) { 
							System.out.println("Blue robot right coordinates are: "+"("+bluerightX+","+bluerightY+")");
							
						}
						if (e.getKeyCode() ==KeyEvent.VK_T) { 
							System.out.println("Blue robot top coordinates are: "+"("+bluetopX+","+bluetopY+")");
							
						}
						if (e.getKeyCode() ==KeyEvent.VK_B) { 
							System.out.println("Blue robot bottom coordinates are: "+"("+bluebottomX+","+bluebottomY+")");
							
						}
						if (e.getKeyCode() ==KeyEvent.VK_D) { 
							System.out.println("Blue robot direction is: "+"("+blueD[0]+","+blueD[1]+")");
							
						}
					}
				}
			);	
		
	}
	
	/**
	 * Display the input video stream in invert mode.
	 */
	
	// FUNCTION WHICH REGULATES THE BRIGHTNESS VALUES, DEPENDING ON ROBOT POSITION
	public void draw() {
		// Read image from camera
		cv.read();
		// Adjust brightness and contrast values
		cv.blur(OpenCV.GAUSSIAN, SMOOTHING_INDEX);
		cv.brightness(BRIGHTNESS);
		cv.contrast(CONTRAST);
		// Output the image	
		image(cv.image(),0,0);
		// Main Detection Method		
		detect();
		//This is just an indicator where the ball is on the image
		int color = color(170, 180, 150);
		fill(color);
		rect(ballX-3,ballY-3,7,7);
				
		// Display framerate
		System.out.println(frameRate);
	}	
		
	public void detect() {
		
		// Init the flag
		ballFlag = true;
		
		// Init blue robot position vars
		bluebottomY = DIMY;
		bluetopY = DIMY + DIMH;
		bluerightX = DIMX;
		blueleftX = DIMX + DIMW;
		bluetopX = 0;
		bluebottomX =0 ;
		blueleftY=0;
		bluerightY=0;
		blueCX =0;
		blueCY=0;
		
		// Init yellow robot position vars
		yellowbottomY = DIMY;
		yellowtopY = DIMY + DIMH;
		yellowrightX = DIMX;
		yellowleftX = DIMX + DIMW;
		yellowtopX = 0;
		yellowbottomX =0 ;
		yellowleftY=0;
		yellowrightY=0;
		yellowCX =0;
		yellowCY=0;
		
		
		// Locate the ball and the robots
		for(int x = DIMX;x<=DIMW+DIMX;x++){
			for(int y = DIMY; y<=DIMH+DIMY; y++){
				
				if(ballFlag){ // Start red
					// Thresholds for detecting the red ball
					if(col(x,y) == "red") {
						
						// Save the coordinates of the ball
						if(Math.abs(x - oldBallX) >= 10){
							ballX = x;
							oldBallX = x;
						}
						if(Math.abs(y - oldBallY) >= 10){
							ballY = y;
							oldBallY = y;
						} 					
						
						// Ball detected, switch flag
						ballFlag = false;
					}
				}// End red
				
						// Find and outline the position of the blue robot
						if(col(x,y) == "blue"){
							if(y < bluetopY && (bluebottomY - bluetopY)<40){
								bluetopY = y;
								bluetopX = x;								
							}
							if(y > bluebottomY && (bluebottomY - bluetopY)< 40){
								bluebottomY = y;
								bluebottomX = x;
							}
							if(x < blueleftX && (bluerightX - blueleftX) < 40){
								blueleftX = x;
								blueleftY = y;
							}
							if(x > bluerightX && (bluerightX - blueleftX) < 40){
								bluerightX = x;
								bluerightY = y;
							}						
						}
					
						// Find and outline the position of the yellow robot
						if(col(x,y) == "yellow"){
							if(y < yellowtopY && (yellowbottomY - yellowtopY)<40){
								yellowtopY = y;
								yellowtopX = x;								
							}
							if(y > yellowbottomY && (yellowbottomY - yellowtopY)< 40){
								yellowbottomY = y;
								yellowbottomX = x;
							}
							if(x < yellowleftX && (yellowrightX - yellowleftX) < 40){
								yellowleftX = x;
								yellowleftY = y;
							}
							if(x > yellowrightX && (yellowrightX - yellowleftX) < 40){
								yellowrightX = x;
								yellowrightY = y;
							}						
						}
				
			}// End inner loop
		}// End of loop
		
		// Calculate blue midpoint
		blueCX = (blueleftX + bluerightX)/2;
		blueCY = (bluetopY + bluebottomY)/2;
		
		// Calculate yellow midpoint
		yellowCX = (yellowleftX + yellowrightX)/2;
		yellowCY = (yellowtopY + yellowbottomY)/2;
		
		// Calculate robot directions
		blueD = getDirection(blueCX, blueCY);
		yellowD = getDirection(yellowCX,yellowCY);
		
	
		/** 
		 * Prepare and set the data
		 */
		ballXY[0] = ballX;
		ballXY[1] = ballY;
		blueXY[0] = blueCX;
		blueXY[1] = blueCY;
		yellowXY[0] = yellowCX;
		yellowXY[1] = yellowCY;
		
		
		//System.out.println("Sending packet");
		//server.setData(ballXY, blueXY, blueD, yellowXY, yellowD, 999);
		
		
		/** FOR DEBUGGING
		System.out.println(blueXY[0]);
		System.out.println(blueXY[1]);
		System.out.println(ballXY[0]);
		System.out.println(ballXY[1]);
		System.out.println(blueD[0]);
		System.out.println(blueD[1]);
		System.out.println(yellowXY[0]);
		System.out.println(yellowXY[1]);
		System.out.println(yellowD[0]);
		System.out.println(yellowD[1]);
			 **/		
		int white = color(255,255,255);
		fill(white);
		
		//For blue robot:
		rect(bluetopX,bluetopY,3,3);
		rect(bluebottomX,bluebottomY,3,3);
		rect(blueleftX,blueleftY,3,3);
		rect(bluerightX,bluerightY,3,3);
		line(blueleftX,blueleftY,bluebottomX,bluebottomY);
		line(bluebottomX,bluebottomY,bluerightX,bluerightY);
		line(bluerightX,bluerightY,bluetopX,bluetopY);
		line(bluetopX,bluetopY,blueleftX,blueleftY);
		
		
		// For yellow robot
		rect(yellowtopX,yellowtopY,3,3);
		rect(yellowbottomX,yellowbottomY,3,3);
		rect(yellowleftX,yellowleftY,3,3);
		rect(yellowrightX,yellowrightY,3,3);
		line(yellowleftX,yellowleftY,yellowbottomX,yellowbottomY);
		line(yellowbottomX,yellowbottomY,yellowrightX,yellowrightY);
		line(yellowrightX,yellowrightY,yellowtopX,yellowtopY);
		line(yellowtopX,yellowtopY,yellowleftX,yellowleftY);
		rect(blueCX,blueCY,3,3);
		rect(yellowCX,yellowCY,3,3);
		
				
		
	} // End detect function
	
	/**
	 * Finds the black dot of the robot and calculates the direction
	 * 
	 * @param midx The X coordinate of robot's midpoint
	 * @param midy The Y coordinate of robot's midpoint
	 * @return The direction of the robot
	 */
	public int[] getDirection(int midx, int midy) {
		int[] direction = new int[2];
		int blackX = 0;
		int blackY = 0;
		int maxGreen = 0;
		int green;
		
		// Finds the black pixel which is surrounded by most green pixels
		for(int x = midx-20;x<=midx+20;x++){
			for(int y = midy-20;y <= midy+20;y++){
				green = 0;
				if(col(x,y)=="black"){
						for(int i = x -2;i<=x+2;i++){
							if(col(i,y-2) == "green"){
								green += 1;	
							}
						}
						for(int i = y -2;i<=y+2;i++){
							if(col(x-2,i) == "green"){
								green += 1;
							}
						}
						for(int i = x -2;i<=x+2;i++){
							if(col(i,y+2) == "green"){
								green += 1;
							}
						}
						for(int i = y -2;i<=y+2;i++){
							if(col(x+2,i) == "green"){
								green += 1;
							}
						}
						if(green > maxGreen){
							maxGreen = green;
							blackX = x;
							blackY = y;
						}
				}
				
			}
		}
		/**
		 * 
		 * */
		// DEBUG
		int white = color(255,255,255);		
		fill(white);
		rect(blackX,blackY,3,3);
	//	**/
		
		direction[0] = midx - blackX;
		direction[1] = midy - blackY;
		return direction;
			
				
	}

	/**
	 * Main method. Initialize and start the Vision program
	 */
	public static void main( String[] args ) {
		server = new VisionServer();
		Thread serverThread = new Thread(server);
		serverThread.start();
		PApplet.main( new String[]{NAME} ); // Start parrent program giving the name
		
	}
	
	/**
	 * Check the color values of a given pixel and estimate its color
	 * @param x X coordinate of the pixel
	 * @param y Y coordinate of the pixel
	 * @return the color of the pixel
	 */
	public String col(int x,int y){
		int pix = get(x,y);
		String color = "";
	
		if(red(pix)>=180 && green(pix)<=30 && blue(pix)<=40)
			color = "red";			
		else if(red(pix) <= 5 && green(pix) <= 130 && blue(pix)>= 160)
			color = "blue";
		else if(red(pix) >= 140 && green(pix) >= 150 && blue(pix)<=60)
			color = "yellow";
		else if(red(pix) <=60 && green(pix) >= 100 && blue(pix)<=80)
			color = "green";
		else if(red(pix) <=20 && green(pix)<=20 && blue(pix)<=0)
			color = "black";
		
		return color;
	}
	
}
 