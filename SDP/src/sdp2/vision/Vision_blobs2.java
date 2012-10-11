package sdp2.vision;
//Written by Abraham Polishchuk using
//Parag K. Mital's tutorial class and Ivan Konstantinov's Vision and Vision_Beta classes. Ivan's Vision Beta class is especially heavily used, it is virtually relied on to deal with situations when for whatever reasons blobs don't work 
//I also use parts of (my own) Vision_blobs class.
//To do: Ball detection in/near corners
//Fix black dot detection by the wall. Probably can not be done, the shadow is visible on the image and encompasses the black ball. In that case, come up with an alternate detection/direction mechanism at walls.
//Occasionally(10% of the time?), the frame rate drops down to 2 or 3 frames a second, stays there a couple of seconds, then climbs back up to 5-7. Why?

import java.awt.*;			// java awt library

import processing.core.*;	// all processing functionality
import sdp2.misc.StopWatch;
import sdp2.network.VisionServer;

import hypermedia.video.*;
/* OpenCV: http://ubaa.net/shared/processing/opencv
 * javadocs: http://ubaa.net/shared/processing/opencv/javadoc/
 * add the .jnlib file to your /Library/Java/Extensions/ folder
 */


public class Vision_blobs2 extends PApplet {

//	 camera width/height
	static final int WIDTH = 480;
	static final int HEIGHT = 360;
	private static final int SMOOTHING_INDEX = 3;
	private static final String NAME = "Vision_blobs2";
	public static VisionServer server;
	public static StopWatch stopwatch = new StopWatch();
	public static Point ball = new Point(0, 0);
	public static Point[] black = new Point[2];
	public static Point yellow = null;
	public static Point blue = null;
	public static Point blackYellow = null;
	public static Point blackBlue = null;
	public static int[] dirY = new int[2];
	public static int[] dirB = new int[2];
	OpenCV cv = null;
	private final static int CONTRAST = 40, BRIGHTNESS = -60;
	boolean write_framerate=true;
	boolean draw_found = true;
	boolean print_coordinates = false;
	static final int DIMX = 65;
	static final int DIMY = 60;
	static final int DIMW = 400;
	static final int DIMH = 240;
	private int totBlueX;
	private int totBlueY;
	private int counterBlue;
	private int totYellowX;
	private int totYellowY;
	private int counterYellow;
	boolean blackFlag1 = false;
	boolean blackFlag2 = false;
	int[] yellowXY = new int[2];
	int[] ballXY = new int[2];
	int[] blueXY = new int[2];
	
	//Sets up initial OpenCV values
	public void setup() {
		size(WIDTH, HEIGHT);
		background(0);
		frameRate(40);
		cv = new OpenCV(this);
		cv.capture(WIDTH, HEIGHT);
	}
	
	public void draw() {
		if(write_framerate) {
			System.out.println("The framerate is:" + frameRate);
		}
		cv.read();
		cv.blur(OpenCV.GAUSSIAN, SMOOTHING_INDEX);
		cv.brightness(BRIGHTNESS);
		cv.contrast(CONTRAST);
		image(cv.image(),0,0);
		Blob[] blobs = cv.blobs(20, 1100,5, true); //extracts the five necessary blobs
		detect(blobs); //calls the detection function
		if(draw_found) {//draws all of the detected bastards
			int color = color(200, 200, 200);
			fill(color);
			try {
				rect(ball.x - 3, ball.y - 3, 7, 7);
				color = color(255,255,0);
				fill (color);
				rect(blue.x -3, blue.y -3, 7, 7);
				color = color(0,0,255);
				fill(color);
				rect(yellow.x - 3, yellow.y -3, 7, 7);
				color = color(255,255,255);
				fill(color);
				rect(black[0].x - 3, black[0].y -3, 7, 7);
				rect(black[1].x - 3, black[1].y -3, 7, 7);
			} catch (NullPointerException x) {
					
			}
		}
		if(print_coordinates) {
			try {
				System.out.println ("Yellow " + yellow.x + " " + yellow.y);
				System.out.println ("Blue " + blue.x + " " + blue.y);
				System.out.println("First Black " + black[0].x + " " + black[0].y);
				System.out.println("Second Black " + black[1].x + " " + black[1].y);
				System.out.println("Ball " + ball.x + " " + ball.y);
			} catch (NullPointerException x) {
				
			}
		}
		findDirection(blobs, 0, yellow);
		findDirection(blobs, 1, blue);
		try {
			ballXY[0] = ball.x;
			ballXY[1] = ball.y;
			blueXY[0] = blue.x;
			blueXY[1] = blue.y;
			yellowXY[0] = yellow.x;
			yellowXY[1] = yellow.y;
		//	server.setData(ballXY, blueXY, dirB, yellowXY, dirY, stopwatch.getElapsedTime());
		} catch (NullPointerException x) {
			
		}
		if(frameRate <= 5) {
			System.gc();
		}
	}
	
	public void detect(Blob[] blobs) {
		boolean blueFlag = false;
		boolean yellowFlag = false;
		boolean ballFlag = false;
		int blueX = 0;
		int blueY = 0;
		int yellowX = 0;
		int yellowY = 0;
		blackFlag1 = false;
		blackFlag2 = false;
		totYellowX = 0;
		totYellowY = 0;
		totBlueX = 0;
		totBlueY = 0;
		counterBlue = 0;
		counterYellow = 0;
		for(int b = 0; b < blobs.length; b++) { //iterates over all blobs
			if(blobs[b].isHole) { //checks whether the blob is contained in another blob
				if((blackFlag1 == false) &&(blobs[b].area <= 30)) { //checks whether one black blob has already been found
					black[0] = new Point(blobs[b].centroid.x, blobs[b].centroid.y);
					blackFlag1 = true;
				} else if (blobs[b].area <= 30){
					black[1] = new Point(blobs[b].centroid.x, blobs[b].centroid.y);
					blackFlag2 = true;
				} 		
			} else { //checks whether the blob is the ball
				if((col(blobs[b].centroid.x, blobs[b].centroid.y) == "red")&&(ballFlag == false)) {
					ball = new Point(blobs[b].centroid.x, blobs[b].centroid.y);
					ballFlag = true;
				}
				for (Point p : blobs[b].points) { //iterates over the points within the blob checking if it is blue/yellow
					if((col(p.x,p.y) == "yellow")&&(yellowFlag == false)) {
						yellow = new Point(blobs[b].centroid.x, blobs[b].centroid.y);
						yellowFlag = true;
					} else if((col(p.x,p.y) == "blue")&&(blueFlag == false)) {
						blue = new Point(blobs[b].centroid.x, blobs[b].centroid.y);
						blueFlag = true;
					}
				}
			}
		}
		if((blueFlag == false)||(yellowFlag == false)|| (ballFlag == false)){
			for(int x = DIMX;x<=DIMW+DIMX;x++){
				for(int y = DIMY; y<=DIMH+DIMY; y++){
					if((col(x,y) == "red")&&(ballFlag ==false)) {
						if((Math.abs(x - ball.x) >= 10)&&(Math.abs(y - ball.y) >= 10)) {
							ball = new Point(x,y);
							ballFlag = true;
						}
					} else if((col(x,y) == "blue")&&(blueFlag == false)){
						totBlueX +=x;
						totBlueY +=y;
						counterBlue++;
					} else if((col(x,y) == "yellow")&&(yellowFlag == false)){
						totYellowX +=x;
						totYellowY +=y;
						counterYellow++;
					}
					if ((blueFlag == true)&&(yellowFlag == true)&&(ballFlag == true)) {
						break;
					}
				}
				if ((blueFlag == true)&&(yellowFlag == true)&&(ballFlag == true)) {
					break;
				}
			}
			if(counterBlue!=0) {
				blueX = totBlueX/counterBlue;
				blueY = totBlueY/counterBlue;
				blue = new Point(blueX, blueY);
			}
			if(counterYellow!=0) {
				yellowX = totYellowX/counterYellow;
				yellowY = totYellowY/counterYellow;
				yellow = new Point(yellowX, yellowY);
			}
		}
	}

	public void findDirection(Blob[] blobs, int color, Point p) {
		int distance1 = 0;
		int distance2 = 0;
		if((blackFlag1 == true)&&(blackFlag2 == true)) {
			if(black[1] == null) {
				
			} else if(yellow == null){
				
			} else {
				distance1 = (int)Math.sqrt(Math.pow((black[0].x - yellow.x), 2) + Math.pow((black[0].y - yellow.y), 2));
				distance2 = (int)Math.sqrt(Math.pow((black[1].x - yellow.x), 2) + Math.pow((black[1].y - yellow.y), 2));
				if((distance1<=distance2)&&(distance1<=25)) {
					dirY[0] = yellow.x - black[0].x;
					dirY[1] = yellow.y - black[0].y;
					if(draw_found) {
						line(black[0].x, black[0].y, yellow.x, yellow.y);
					}
				} else if (distance2<=25){
					dirY[0] = yellow.x - black[1].x;
					dirY[1] = yellow.y - black[1].y;
					if(draw_found) {
						line(black[1].x, black[1].y, yellow.x, yellow.y);
					}
				}
			}
			if(blue == null) {
					
			} else {
				distance1 = (int)Math.sqrt(Math.pow((black[0].x - blue.x), 2) + Math.pow((black[0].y - blue.y), 2));
				distance2 = (int)Math.sqrt(Math.pow((black[1].x - blue.x), 2) + Math.pow((black[1].y - blue.y), 2));
				if((distance1 <= distance2)&&(distance1<=25)) {
					dirB[0] = blue.x - black[0].x;
					dirB[1] = blue.y - black[0].y;
					if(draw_found) {
						line(black[0].x, black[0].y, blue.x, blue.y);
					}
				} else if (distance2<=25){
					dirB[0] = blue.x - black[1].x;
					dirB[1] = blue.y - black[1].y;
					if(draw_found) {
						line(black[1].x, black[1].y, blue.x, blue.y);
					}
				}
			}
		} else if(blackFlag1 == true) {
			if((yellow != null) && (blue!=null)) {
				distance1 = (int)Math.sqrt(Math.pow((black[0].x - yellow.x), 2) + Math.pow((black[0].y - yellow.y), 2));
				distance2 = (int)Math.sqrt(Math.pow((black[0].x - blue.x), 2) + Math.pow((black[0].y - blue.y), 2));
				if((distance1<=distance2)&&(distance1<=100)) {
					dirY[0] = yellow.x - black[0].x;
					dirY[1] = yellow.y - black[0].y;
					if(draw_found) {
						line(black[0].x, black[0].y, yellow.x, yellow.y);
					}
				}else if (distance2<=25) {
					dirB[0] = blue.x - black[0].x;
					dirB[1] = blue.y - black[0].y;
					dirY[0] = 0;
					if(draw_found) {
						line(black[0].x, black[0].y, blue.x, blue.y);
					}
				}
			}
	    }else {
			try {
				cv.ROI(p.x-20,p.y-20,40,40);
				Blob[] blobs2 = cv.blobs(20, 80, 1, true);
				for(Blob b: blobs2) {
					if(draw_found) {
						int white = color(255,255,255);
						fill(white);
						rect(b.rectangle.x,b.rectangle.y,b.rectangle.width,b.rectangle.height);
						int black = color(0,0,0);
						fill(black);
						rect(b.centroid.x,b.centroid.y,3,3);
					}
				}
				cv.ROI(null);
			}catch (NullPointerException x) {
				
			}
		}
	}
	
	public String col(int x,int y){
		int pix = get(x,y);
		String color = "";
	
		if((red(pix)>=150 && green(pix)<=30 && blue(pix)<=40) ||(red(pix)>=220 && green(pix)<= 130 && blue(pix) <= 130))
			color = "red";			
		else if(red(pix) <= 5 && green(pix) <= 130 && blue(pix)>= 150)
			color = "blue";
		else if(red(pix) >= 140 && green(pix) >= 150 && blue(pix)<=60)
			color = "yellow";
		else if(red(pix) <=60 && green(pix) >= 100 && blue(pix)<=80)
			color = "green";
		else if(red(pix) <=20 && green(pix)<=20 && blue(pix)<=0)
			color = "black";
		
		return color;
	}
	
	public static void main(String args[]) {
		stopwatch.start();
		server = new VisionServer();
		Thread serverThread = new Thread(server);
		serverThread.start();
		PApplet.main( new String[]{NAME} ); // Start parrent program giving the name
	}
	
	
	public void keyPressed() {
		if (key == 'f' ) {
			write_framerate = !write_framerate;
		}
		if(key == 'b') {
			draw_found = !draw_found;
		}
		if(key == 'p') {
			print_coordinates = !print_coordinates;
		}
	}
	
}
