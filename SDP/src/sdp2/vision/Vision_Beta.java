package sdp2.vision;


/**
 * Written by Ivan Konstantinov
 * Version 2.0 
 *
 * Uses PApplet for image processing functions
 * Uses colour thresholding for locating the two robot plates and the ball
 * Uses blob detection algorithm for finding the black spot on the plates
 * Might need a kind of adaptive thresholding algorithm! Adjust brightness, contrast, colour values
 * 
 */


import hypermedia.video.Blob;
import hypermedia.video.OpenCV;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import sdp2.misc.Aux_frame;
import sdp2.misc.VisionKeyAdapter;
import sdp2.network.VisionServer;

public class Vision_Beta extends PApplet {
	private static final String NAME = "sdp2.vision.Vision_Beta";
	private OpenCV cv = null;	// OpenCV object
	private static VisionServer server;
	private static Thread serverThread;
	private VisionKeyAdapter keyAdapter;
	private Aux_frame frame;
	
	// Image adjustment variables
	public static int brightness = -70; // Min -120; Max 120
	public static int contrast = 40; // Min -100 Max 100
	public static int smoothing_index = 3;
	
	public final int DEFAULT_BRIGHTNESS = -70;
	public final int DEFAULT_CONTRAST = 40;
		
	// Colour threshold values
	public int[] blue = new int[3];
	public int[] red = new int[3];
	public int[] yellow = new int[3];
		
	// Coordinates of the ball and the two robots
	private Point ball = new Point(0,0);
	private Point blueRobot = new Point(0,0);
	private Point yellowRobot = new Point(0,0);
	
	// Robot directions
	private Point blueD = new Point(0,0);
	private Point yellowD = new Point(0,0);
		
	// Time: records the time when the ball is detected
	private volatile long tm;
	
	// Flags
	public boolean time = false;
	public boolean draw = false;
	public boolean print = false;
	public boolean printFrameRate = false;
		
	// Old robot coordinates
	private Point oldBlueCenter = new Point(0,0);
	private Point oldYellowCenter = new Point(0,0);
	private Point oldBall = new Point(0,0);
			
	// Image Size parameters Default: 640x480; Used: 480x360
	static final int WIDTH = 640;
	static final int HEIGHT = 480;
	
	// Pitch dimensions: Used: x=65;y=60;w=400;h=240
	static final int DIMX = 80;
	static final int DIMY = 118;
	static final int DIMW = 550;
	static final int DIMH = 302;
		
	// Data sent to server
	private volatile int[] data = new int[10];
	
	// Variables for estimating the centre points of the robot plates and the ball
	private Point totBlue;
	private int counterBlue;
	private Point totYellow;
	private int counterYellow;
	private Point totBall;
	private int counterBall;
	
	
	/**
	 * Initialise Objects.
	 */
	@Override
	public void setup() {
		
		// PApplet setup
		size( WIDTH, HEIGHT );
		background(0);
		frameRate(40);
		// OpenCV setup
		cv = new OpenCV( this );
		cv.capture( WIDTH, HEIGHT );
		
		// Initialize colour thresholds
		red[0] = 140;
		blue[1] = 150;
		blue[2] = 140;
		yellow[0] = 140;
		yellow[1] = 140;
		yellow[2] = 150;		
		
		// Add aux frame
		frame = new Aux_frame(this);
		
		// Bind keys to specific functions
		//keyAdapter = new VisionKeyAdapter(this);
		//addKeyListener(keyAdapter);	
				
		// Bind mouse clicks to specific functions
		this.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(e.getButton()==MouseEvent.BUTTON1) {
							System.out.println("Pixel at location ("+e.getX()+","+e.getY()+")");
							int color = get(e.getX(),e.getY());
							System.out.println("Color: (" + red(color)+","+green(color)+","+blue(color)+")");
						}
					}
				}
		);
	}
	
	/**
	 * Display the image and perform all detection procedures
	 */
	@Override
	public void draw() {
		// Read image from camera
		cv.read();
		
		//Print framerate
		if(printFrameRate) {
			System.out.println(frameRate);
		}
		
		// Adjust brightness and contrast values
		cv.blur(OpenCV.GAUSSIAN, smoothing_index);
		cv.brightness(brightness);
		cv.contrast(contrast);
		
		// Display image
		image(cv.image(),0,0);
		//Main Detection Method		
		detect();
		// Get blue robot direction
		setRobotData(blueRobot,"blue");
		// Get yellow robot direction
		setRobotData(yellowRobot,"yellow");
			
		// Set the data variables to be sent to the server
		data[0] = ball.x;
		data[1] = ball.y;
		data[2] = blueRobot.x;
		data[3] = blueRobot.y;
		data[4] = blueD.x;
		data[5] = blueD.y;    
		data[6] = yellowRobot.x;    
		data[7] = yellowRobot.y;    
		data[8] = yellowD.x;    
		data[9] = yellowD.y;    
				     		     
		// Send the data to the server
		if(server != null)
			server.setData(data, tm);
		
		//Draw the central point of the ball and the robots
		if(draw) {
			int color = color(150, 132, 177);
			fill(color);
			rect(ball.x,ball.y,7,7);
			rect(blueRobot.x,blueRobot.y,3,3);
			rect(yellowRobot.x,yellowRobot.y,3,3);
		}
				
	}	
	/**
	 * Scans the whole frame and searches for the ball and the two robots. 
	 * @return the middle point of the objects
	 *
	 */
	public void detect() {
						
		// Init vars
		totBlue = new Point(0,0);
		counterBlue=0;
		totYellow= new Point(0,0);
		counterYellow=0;
		totBall=new Point(0,0);
		counterBall=0;
		ball = new Point(0,0);
		
		// Locate the ball and the robots
		for(int x = DIMX;x<=DIMW+DIMX;x++){
			for(int y = DIMY; y<=DIMH+DIMY; y++){
				
				// Find the centre point of the red ball
				if(col(x,y) == "red") {
						
					// Save the time of detection
					tm = System.currentTimeMillis();
					if(time) {
						System.out.println("Ball detected. Time: "+tm);
					}
					totBall.x += x;
					totBall.y += y;
					counterBall++;
				}// End red
				
				//Find the middle point of the blue robot
				if(col(x,y) == "blue"){
					totBlue.x +=x;
					totBlue.y +=y;
					counterBlue++;
				}
				
				//Find the middle point of the yellow robot
				if(col(x,y) == "yellow"){
					totYellow.x +=x;
					totYellow.y +=y;
					counterYellow++;
				}
			}// End inner loop
		}// End of loop
		
		// Temporary values
		int tmpx = 0;
		int tmpy = 0;
		// Calculate the middle point of the robots
		if(counterBlue!=0) {
			tmpx = totBlue.x/counterBlue;
			tmpy = totBlue.y/counterBlue;
			if(Math.abs(tmpx-oldBlueCenter.x) >= 3) {
				blueRobot.x = tmpx;
				oldBlueCenter.x = blueRobot.x;
			}
				
			if(Math.abs(tmpy-oldBlueCenter.y)>= 3) {
				blueRobot.y = tmpy;
				oldBlueCenter.y = blueRobot.y;
			}
		}
		if(counterYellow!=0) {
			tmpx = totYellow.x/counterYellow;
			tmpy = totYellow.y/counterYellow;
			if(Math.abs(tmpx - oldYellowCenter.x) >= 3) {
				yellowRobot.x = tmpx;
				oldYellowCenter.x = yellowRobot.x;
			}
			if(Math.abs(tmpy - oldYellowCenter.y) >= 3) {
				yellowRobot.y = tmpy;
				oldYellowCenter.y = yellowRobot.y;
			}
		}
		if(counterBall!=0) {
			ball.x = totBall.x/counterBall;
			ball.y = totBall.y/counterBall;
			
		}
		// Print ball coordinates
		if(print)
			System.out.println("Ball coordinates: ("+ball.x+","+ball.y+")");
		
	} // End detect function
	
	
	/**
	 * Main method. Initialize and start the Vision program
	 */
	
	public static void main( String[] args ) {
		server = new VisionServer();
		serverThread = new Thread(server);
		serverThread.start();
		PApplet.main( new String[]{NAME} ); // Start parrent program giving the name
		System.out.println("Vision system started. Welcome!\nPress 'h' to see the help menu");
	}
	
	/**
	 * Check the color values of a given pixel and estimate its color
	 * @param x: X coordinate of the pixel
	 * @param y: Y coordinate of the pixel
	 * @return the color of the pixel
	 */
	public String col(int x,int y){
		int pix = get(x,y);
		String color = "";
		if(red(pix)>=red[0] && green(pix)==red[1] && blue(pix)==red[2])
			color = "red";		
		else if(red(pix) == blue[0] && green(pix) <= blue[1] && blue(pix)>= blue[2])
			color = "blue";
		else if(red(pix) >= yellow[0] && green(pix) >= yellow[1] && blue(pix)<=yellow[2])
			color = "yellow";
				
		return color;
	}
	
	public void setRobotData(Point p,String color) {
		cv.ROI(p.x-35,p.y-35,70,70);
		Blob[] blobs = cv.blobs(40, 80,5, true);
		for(Blob b: blobs) {
			if(col(b.centroid.x,b.centroid.y)!= "black") {
				// Do nothing if the blob is not the black dot
			}
			else {
				if(color == "blue") {
					/*if(blue.x != 0 && blue.y != 0) {
						if(b.centroid.x - oldBlueCenter.x >=3) {
							blueD.x = blue.x - b.centroid.x;
							oldBlueCenter.x = b.centroid.x;
						}
						if(b.centroid.y - oldBlueCenter.y >= 3) {
							blueD.y = blue.y - b.centroid.y;
							oldBlueCenter.y = b.centroid.y;
						}
					}*/
					blueD.x = blueRobot.x - b.centroid.x;
					blueD.y = blueRobot.y - b.centroid.y;
				}
				else if(color == "yellow") {
					/*if(yellow.x != 0 && yellow.y !=0) {
						if(b.centroid.x - oldYellowCenter.x >=3) {
							yellowD.x = yellow.x - b.centroid.x;
							oldYellowCenter.x = b.centroid.x;
						}
						if(b.centroid.x - oldYellowCenter.y >=3) {
							yellowD.y = yellow.y - b.centroid.y;
							oldYellowCenter.y = b.centroid.y;
						}
						
					}*/
					yellowD.x = yellowRobot.x - b.centroid.x;
					yellowD.y = yellowRobot.y - b.centroid.y;
				}
				if(print) { 
					System.out.println("Black dot coordinates for "+color+" robot: (" + b.centroid.x+","+b.centroid.y+")");
					if(color == "blue")
						System.out.println("Center point of blue robot: ("+blueRobot.x+","+blueRobot.y+")"
								+"\nDirection of blue robot: ("+blueD.x+","+blueD.y+")");
					else if(color == "yellow")
						System.out.println("Center point of yellow robot: ("+blueRobot.x+","+yellowRobot.y+")"
								+"\nDirection of yellow robot: ("+yellowD.x+","+yellowD.y+")");				
				}
				if(draw) {
						int white = color(255,255,255);
						fill(white);
						rect(b.rectangle.x,b.rectangle.y,b.rectangle.width,b.rectangle.height);
						int black = color(0,0,0);
						fill(black);
						rect(b.centroid.x,b.centroid.y,3,3);
						if(color == "blue")
							line(blueRobot.x,blueRobot.y,b.centroid.x,b.centroid.y);
						else if(color == "yellow")
							line(blueRobot.x,blueRobot.y,b.centroid.x,b.centroid.y);
				}
				
				
			}
			
			}
		cv.ROI(null);
			
		
	}
	
	public VisionServer getServer() {
		return server;
	}
	
	public void setServer(VisionServer srv) {
		server = srv;
	}
	
	public int getBrightnessLvl() {
		return brightness;
	}
	
	public void setBrightnessLvl(int lvl) {
		brightness = lvl;
	}
	
	public int getContrastLvl() {
		return contrast;
	}
	
	public void setContrastLvl(int lvl) {
		contrast = lvl;
	}
	
	public void setSmooth_Index(int lvl) {
		smoothing_index = lvl;
	}
	
	public int getSmooth_Index() {
		return smoothing_index;
	}
	
	public void setAuxFrame(Aux_frame frame){
		this.frame = frame;
	}
	
	public Aux_frame getAuxFrame() {
		return frame;
	}
	
	public Vision_Beta getVisionSystem() {
		return this;
	}
	
}
 