package sdp2.old;
//Written by Abraham Polishchuk using
//Parag K. Mital's tutorial class and Ivan Konstantinov's Vision class 
//To do: Ball detection in/near corners
//WHAT THE FUCK HAPPENED TO DIRETION -_- FUCKING CUNT OF A FUCKING ASSHOLE BITCH NIGGER
//Fix black dot detection by the wall. Probably can not be done, the shadow is visible on the image and encompasses the black ball. In that case, come up with an alternate detection/direction mechanism at walls.

import java.awt.*;			// java awt library

import processing.core.*;	// all processing functionality
import sdp2.misc.StopWatch;
import sdp2.network.VisionServer;

import hypermedia.video.*;
/* OpenCV: http://ubaa.net/shared/processing/opencv
 * javadocs: http://ubaa.net/shared/processing/opencv/javadoc/
 * add the .jnlib file to your /Library/Java/Extensions/ folder
 */

public class Vision_blobs extends PApplet {
	
//	 camera width/height
	static final int WIDTH = 480;
	static final int HEIGHT = 360;
	private static final int SMOOTHING_INDEX = 3;
	
	// the opencv object that contains all the functionality for 
	// camera/movie/image i/o, and computer vision routines
	private static final String NAME = "Vision_blobs";
	public static VisionServer server;
	public static StopWatch stopwatch = new StopWatch();
	public static Point ball = new Point(WIDTH/2, HEIGHT/2);
	public static Point oldBall = null;
	public static Point[] black = new Point[2];
	public static Point oldBlack1 = new Point(0,0);
	public static Point oldBlack2 = new Point(0,0);
	public static Point blackYellow = new Point(0,0);
	public static Point blackBlue = new Point(0,0);
	public static Point yellowRobot = new Point(WIDTH/2 + 100,HEIGHT/2);
	public static Point oldYellowRobot = null;
	public static Point blueRobot = new Point (WIDTH/2 - 100, HEIGHT/2);
	public static Point oldBlueRobot = null;
	public static int[] yellowDirection = new int[2];
	public static int[] blueDirection = new int[2];
	OpenCV cv = null;


	// threshold for background subtraction
	//int threshold = 125;

	// some variables to control the contrast/brightness of the camera image
	private final static int CONTRAST = 40, BRIGHTNESS = -60;

	// for drawing text
	//PFont font;

	// these boolean values will be used to trigger parts of the code - we
	// can use the keyPressed method to set these boolean values with the keyboard
	boolean draw_blobs=true;
	boolean draw_found = true;


	// this function is called once, so we use it to initialize all of our variables, etc...
	public void setup()	{
		// Size of the window
		size(WIDTH, HEIGHT);

		// Instantiate opencv with an instance of our applet
		cv = new OpenCV(this);

		// Setup our capture device using opencv
		cv.capture(WIDTH, HEIGHT);
		cv.ROI(0, 75, WIDTH, HEIGHT - 133);

		// Setup our face detection (take a look at the other CASCADE's such as
		// FULLBODY, UPPERBODY, FRONTALFACE_ALT...
		//cv.cascade(OpenCV.CASCADE_FRONTALFACE_DEFAULT);

		// Setup font to use the Andale Mono type font (this file is in the data folder)
		//font = loadFont("AndaleMono.vlw");
		//textFont(font);
	}

	// after setup() has run, draw() will run in an infinite loop, also checking any events from
	// i/o such as the mouse and keyboard callbacks (e.g. mousePressed, keyPressed)
	public void draw() {
		// set the background color to black
		//background(0);

		// display the frame per second of our program. if this gets too low,
		// our program will appear less interactive as the latency will be higher
		//text("fps: " + frameRate, 10, HEIGHT+10);
		System.out.println("The framerate is:" + frameRate);
		
		// Read the current frame from our camera device
		cv.read();
		cv.blur(OpenCV.GAUSSIAN, SMOOTHING_INDEX);
		cv.brightness(BRIGHTNESS);
		cv.contrast(CONTRAST);
		image(cv.image(),0,0);

		// and blob detection
		
		Blob[] blobs = doBlobDetection();
		if (ball != null) {
			oldBall = new Point(ball.x, ball.y);
		}
		ball = doBallDetection(blobs);
		if(black[0] != null) {
			oldBlack1 = new Point(black[0].x, black[0].y);
		}
		if(black[1]!= null) {
			oldBlack2 = new Point(black[1].x, black[1].y);
		}
		if(ball != null) {
			doBlackDetection(blobs, ball);
		} else {
			doBlackDetection(blobs,oldBall);
		}
		if(yellowRobot != null) {
			oldYellowRobot = new Point(yellowRobot.x, yellowRobot.y);
		}
		yellowRobot = doYellowTDetection(blobs);
		if (blueRobot != null) {
			oldBlueRobot = new Point(blueRobot.x, blueRobot.y);
		}
		blueRobot = doBlueTDetection(blobs);
		doYellowDirectionCalculation();
		doBlueDirectionCalculation();
		int[] ballXY = new int[2];
		if(ball != null) {
			ballXY[0] =ball.x;
			ballXY[1] = ball.y;
		} else {
			ballXY[0] =oldBall.x;
			ballXY[1] = oldBall.y;
		}
		int[] blueXY = new int[2];
		int[] yellowXY = new int[2];
		if(blueRobot != null) {
			blueXY[0] = blueRobot.x;
			blueXY[1] = blueRobot.y;
		} else {
			blueXY[0] = oldBlueRobot.x;
			blueXY[1] = oldBlueRobot.y;
		}
		if(yellowRobot != null) {
			yellowXY[0] = yellowRobot.x;
			yellowXY[1] = yellowRobot.y;
		} else {
			yellowXY[0] = oldYellowRobot.x;
			yellowXY[1] = oldYellowRobot.y;
		}
		System.out.println(blackYellow.x);
		
		if(black[1] != null) {
			System.out.println(black[0].x);
			System.out.println(black[1].x);
		}
		//server.setData(ballXY, blueXY, blueDirection, yellowXY, yellowDirection, stopwatch.getElapsedTime());

	}

	public Blob[] doBlobDetection() {

		// Do the blob detection
		Blob[] blobs = cv.blobs(10, 1300,5, true);
		// Pushing a matrix allows any transformations such as rotate, translate, or scale,
		// to stay within the same matrix.  once we "popMatrix()", then all the commands for that matrix
		// are gone for any subsequent drawing.
		pushMatrix();
		// since we translate after we pushed a matrix, every drawing command afterwards will be affected
		// up until we popMatrix().  then this translate will have no effect on drawing commands after
		// the popMatrix().
		//translate(WIDTH*2, 0);

		// We are going to keep track of the total x,y centroid locations to find
		// an average centroid location of all blobs
		//int total_x = 0;
		//int total_y = 0;		

		// we loop through all of the blobs found by cv.blobs(..)
		for( int blob_num = 0; blob_num < blobs.length; blob_num++ ) {

			if(draw_blobs)
			{
				// get the bounding box from the blob detection and draw it
				Rectangle bounding_box = blobs[blob_num].rectangle;
				//noFill();
				int color = color(170, 180, 150);
				//stroke(128);
				fill(color);
				this.rect( bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height );
				//System.out.println(blobs[blob_num].centroid.x + " "+  blobs[blob_num].centroid.y);
			}
		}
		popMatrix();
		return (blobs);
	}

	
	public Point doBallDetection(Blob[] blobs) {
		for (Blob b : blobs) {
			if(b.isHole) {
				
			}else {
				if(col(b.centroid.x, b.centroid.y) == "red") {
					if(draw_found) {
						int color = color(255, 0, 0);
						fill(color);
						rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
					}
					return(b.centroid);
				}
			}
		}
		Point ball = null;
		for (int x = 0; x <= WIDTH; x++) {
			//check the top white stripe
			for (int y = 30; y <= 100; y++) {
				if(col(x,y) == "red") {
					ball = new Point(x,y);
					if(draw_found) {
						int color = color(255, 0, 0);
						fill(color);
						rect(x - 3, y - 3, 7, 7);
					}
					return(ball);
				}
			}
			for (int y = 300; y>= 230; y--) {
				if(col(x,y) == "red") {
					ball = new Point(x,y);
					if(draw_found) {
						int color = color(255, 0, 0);
						fill(color);
						rect(x - 3, y - 3, 7, 7);
					}
					return(ball);
				}
			}
			if ((x <= 150)||((x >= 250) && (x <= 290))||(x>=WIDTH-180)) {
				for (int y = 0; y<= 300; y++) {
					if(col(x,y) == "red") {
						ball = new Point(x,y);
						if(draw_found) {
							int color = color(255, 0, 0);
							fill(color);
							rect(x - 3, y - 3, 7, 7);
						}
						return(ball);
					}
				}
			} 
		}
		return null;
	}
	
	public void doBlackDetection(Blob[] blobs, Point ball) {
		int x = 0;
		for (Blob b : blobs) {
			if(b.isHole) {
				black[x] = new Point(b.centroid.x, b.centroid.y);
				x++;
				if(draw_found) {
					Rectangle bounding_box = b.rectangle;
					int color = color(0, 0, 0);
					fill(color);
					this.rect( bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height );
				}
			}
		}
		x = 0;
		if(black[0] == null) {
			for (Blob b : blobs) {
				if (((b.centroid.x <= ball.x +5)||(b.centroid.x >= ball.x-5))&&((b.centroid.y <= ball.y +5)||(b.centroid.y >= ball.y-5))) {
					
				} else if((col(b.centroid.x,b.centroid.y) == "blue")||(col(b.centroid.x,b.centroid.y) == "yellow")){
					
				} else {
					black[x] = new Point (b.centroid.x, b.centroid.y);
					x++;
					if(draw_found) {
						Rectangle bounding_box = b.rectangle;
						int color = color(0, 0, 0);
						fill(color);
						this.rect( bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height );
					}
				}
			}
		} else if (black[1] == null) {
			for (Blob b : blobs) {
				if (((b.centroid.x <= ball.x +5)||(b.centroid.x >= ball.x-5))&&((b.centroid.y <= ball.y +5)||(b.centroid.y >= ball.y-5))) {
					
				} else if (b.isHole) {
					
				} else if((col(b.centroid.x,b.centroid.y) == "blue")||(col(b.centroid.x,b.centroid.y) == "yellow")){
					
				}else {
					black[1] = new Point (b.centroid.x, b.centroid.y);
					if(draw_found) {
						Rectangle bounding_box = b.rectangle;
						int color = color(0, 0, 0);
						fill(color);
						this.rect( bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height );
					}
				}
			}
		}
	}

	
	public Point doYellowTDetection(Blob[] blobs) {
		Point p = null;
		Point p1 = null;
		Point p2 = null;
		for(Blob b : blobs) {
			try {
				if(b.isHole) {
				
				}else if ((b.centroid.x <= ball.x+5 && b.centroid.x >= ball.x-5) || (b.centroid.y <= ball.y +5 && b.centroid.y >= ball.y -5)) {
				
				} else 	if(col(b.centroid.x, b.centroid.y) == "yellow") {
					if(draw_found) {
						int color = color(0,0,0);
						fill(color);
						rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
					}
						for(int y = 0; y < b.points.length; y++) {
							if(black[0] == null) {
								if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y && oldBlack1.y != 0) {
									blackYellow = new Point(oldBlack1.x, oldBlack1.y);
								} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y && oldBlack2.y != 0) {
									blackYellow = new Point(oldBlack2.x, oldBlack2.y);
								}
							} else if (black[1] == null) {
								if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
									blackYellow = new Point(black[0].x, black[0].y);
								} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
									blackYellow = new Point(oldBlack2.x, oldBlack2.y);
								}
							} else {
								if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
									blackYellow = new Point(black[0].x, black[0].y);
								} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y && black[1].y != 0) {
									blackYellow = new Point(black[1].x, black[1].y);
								}
							}
						}
					return (new Point(b.centroid.x, b.centroid.y));
				} else {
					for (int x = 0; x < b.points.length; x++) {
						if (col(b.points[x].x, b.points[x].y) == "yellow") {
							if(draw_found) {
								int color = color(0,0,0);
								fill(color);
								rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
							}
							for(int y = 0; y < b.points.length; y++) {
								if(black[0] == null) {
									if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y) {
										blackYellow = new Point(oldBlack1.x, oldBlack1.y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackYellow = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else if (black[1] == null) {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackYellow = new Point(black[0].x, black[0].y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackYellow = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackYellow = new Point(black[0].x, black[0].y);
									} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y) {
										blackYellow = new Point(black[1].x, black[1].y);
									}
								}
							}
							return (new Point(b.centroid.x, b.centroid.y));
						}
					}
				}
			} catch (NullPointerException nxe) {
				if(b.isHole) {
					
				}else if ((b.centroid.x <= oldBall.x+5 && b.centroid.x >= oldBall.x-5) || (b.centroid.y <= oldBall.y +5 && b.centroid.y >= oldBall.y -5)) {
				
				} else if(col(b.centroid.x, b.centroid.y) == "yellow") {
					if(draw_found) {
						int color = color(0,0,0);
						fill(color);
						rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
					}
					for(int y = 0; y < b.points.length; y++) {
						if(black[0] == null) {
							if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y && oldBlack1.y != 0) {
								blackYellow = new Point(oldBlack1.x, oldBlack1.y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y && oldBlack2.y != 0) {
								blackYellow = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else if (black[1] == null) {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
								blackYellow = new Point(black[0].x, black[0].y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y && oldBlack2.y != 0) {
								blackYellow = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
								blackYellow = new Point(black[0].x, black[0].y);
							} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y && black[1].y != 0) {
								blackYellow = new Point(black[1].x, black[1].y);
							}
						}
					}
					return (new Point(b.centroid.x, b.centroid.y));
				} else {
					for (int x = 0; x < b.points.length; x++) {
						if (col(b.points[x].x, b.points[x].y) == "yellow") {
							if(draw_found) {
								int color = color(0,0,0);
								fill(color);
								rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
							}
							for(int y = 0; y < b.points.length; y++) {
								if(black[0] == null) {
									if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y && oldBlack1.y != 0) {
										blackYellow = new Point(oldBlack1.x, oldBlack1.y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y && oldBlack2.y != 0) {
										blackYellow = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else if (black[1] == null) {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
										blackYellow = new Point(black[0].x, black[0].y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y && oldBlack2.y != 0) {
										blackYellow = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y && black[0].y != 0) {
										blackYellow = new Point(black[0].x, black[0].y);
									} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y && black[1].y != 0) {
										blackYellow = new Point(black[1].x, black[1].y);
									}
								}
							}
							return (new Point(b.centroid.x, b.centroid.y));
						}
					}
				}
			}
		}
		if (black[0] == null) {
			for (int x = 0; x <=100; x++) {
				if ((col(oldBlack2.x+x, oldBlack2.y)=="yellow")&&(oldBlack2.x+x<=WIDTH)) {
					p = new Point(oldBlack2.x+x, oldBlack2.y);
					blackYellow = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x-x, oldBlack2.y)=="yellow")&&(oldBlack2.x-x>=0)) {
					p = new Point(oldBlack2.x-x, oldBlack2.y);
					blackYellow = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x, oldBlack2.y-x)=="yellow")&&(oldBlack2.y-x>=0)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y-x);
						blackYellow = new Point (oldBlack2.x, oldBlack2.y);
					} else {
						p1 = new Point(oldBlack2.x, oldBlack2.y-x);
						break;
					}					
				} else if ((col(oldBlack2.x, oldBlack2.y+x)=="yellow")&&(oldBlack2.y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y+x);
						blackYellow = new Point (oldBlack2.x, oldBlack2.y);
					} else {
						p1 = new Point(oldBlack2.x, oldBlack2.y+x);
						break;
					}
				} else if ((col(oldBlack1.x+x, oldBlack1.y)=="yellow")&&(oldBlack1.x+x<=WIDTH)) {
					p = new Point(oldBlack1.x+x, oldBlack1.y);
					blackYellow = new Point (oldBlack1.x, oldBlack1.y);
				} else if ((col(oldBlack1.x-x, oldBlack1.y)=="yellow")&&(oldBlack1.x-x>=0)) {
					p = new Point(oldBlack1.x-x, oldBlack1.y);
					blackYellow = new Point (oldBlack1.x, oldBlack1.y);
				} else if ((col(oldBlack1.x, oldBlack1.y-x)=="yellow")&&(oldBlack1.y-x>=0)) {
					if (p == null) {
						p = new Point(oldBlack1.x, oldBlack1.y-x);
						blackYellow = new Point (oldBlack1.x, oldBlack1.y);
					} else {
						p1 = new Point(oldBlack1.x, oldBlack1.y-x);
						break;
					}
				} else if ((col(oldBlack1.x, oldBlack1.y+x)=="yellow")&&(oldBlack1.y+x<=HEIGHT)) {
					if (p == null) {
						p = new Point(oldBlack1.x, oldBlack1.y+x);
						blackYellow = new Point (oldBlack1.x, oldBlack1.y);
					} else {
						p1 = new Point(oldBlack1.x, oldBlack1.y+x);
						break;
					}
				}
			}
		} else if (black[1] == null) {
			for (int x = 0; x <=100; x++) {
				if ((col(oldBlack2.x+x, oldBlack2.y)=="yellow")&&(oldBlack2.x+x<=WIDTH)) {
					p = new Point(oldBlack2.x+x, oldBlack2.y);
					blackYellow = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x-x, oldBlack2.y)=="yellow")&&(oldBlack2.x-x>=0)) {
					p = new Point(oldBlack2.x-x, oldBlack2.y);
					blackYellow = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x, oldBlack2.y-x)=="yellow")&&(oldBlack2.y-x>=0)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y-x);
						blackYellow = new Point (oldBlack2.x, oldBlack2.y);
					}else {
						p1 = new Point(oldBlack2.x, oldBlack2.y-x);
						break;
					}
				} else if ((col(oldBlack2.x, oldBlack2.y+x)=="yellow")&&(oldBlack2.y+x<=HEIGHT)) {
					if ( p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y+x);
						blackYellow = new Point (oldBlack2.x, oldBlack2.y);
					}else {
						p1 = new Point(oldBlack2.x, oldBlack2.y+x);
						break;
					}
				} else if ((col(black[0].x+x, black[0].y)=="yellow")&&(black[0].x+x<=WIDTH)) {
					p = new Point(black[0].x+x, black[0].y);
					blackYellow = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x-x, black[0].y)=="yellow")&&(black[0].x-x>=0)) {
					p = new Point(black[0].x-x, black[0].y);
					blackYellow = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x, black[0].y-x)=="yellow")&&(black[0].y-x>=0)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y-x);
						blackYellow = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y-x);
						break;
					}
				} else if ((col(black[0].x, black[0].y+x)=="yellow")&&(black[0].y+x<=HEIGHT)) {
					if (p == null) {
						p = new Point(black[0].x, black[0].y+x);
						blackYellow = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y+x);
						break;
					}
				}
			}
		} else {
			for (int x = 0; x <=100; x++) {
				if ((col(black[0].x+x, black[0].y)=="yellow")&&(black[0].x+x<=WIDTH)) {
					p = new Point(black[0].x+x, black[0].y);
					blackYellow = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x-x, black[0].y)=="yellow")&&(black[0].x-x>=0)) {
					p = new Point(black[0].x-x, black[0].y);
					blackYellow = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x, black[0].y-x)=="yellow")&&(black[0].y-x>=0)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y-x);
						blackYellow = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y-x);
						break;
					}
				} else if ((col(black[0].x, black[0].y+x)=="yellow")&&(black[0].y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y+x);
						blackYellow = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y+x);
						break;
					}
				} else if ((col(black[1].x+x, black[1].y)=="yellow")&&(black[1].x+x<=WIDTH)){
					p = new Point(black[1].x+x, black[1].y);
					blackYellow = new Point (black[1].x, black[1].y);
				} else if ((col(black[1].x-x, black[1].y)=="yellow")&&(black[1].x-x>=0)) {
					p = new Point(black[1].x-x, black[1].y);
					blackYellow = new Point (black[1].x, black[1].y);
				} else if ((col(black[1].x, oldBlack1.y-x)=="yellow")&&(black[1].y-x>=0)) {
					if (p == null) {
						p = new Point(black[1].x, black[1].y-x);
						blackYellow = new Point (black[1].x, black[1].y);
					} else {
						p1 = new Point(black[1].x, black[1].y-x);
						break;
					}
				} else if ((col(black[1].x, black[1].y+x)=="yellow")&&(black[1].y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(black[1].x, black[1].y+x);
						blackYellow = new Point (black[1].x, black[1].y);
					} else {
						p1 = new Point(black[1].x, black[1].y+x);
						break;
					}
				}
			}
		} try {
		p2 = new Point((p.x + p1.x)/2, (p.y + p1.y)/2);
		if(draw_found) {
			int color = color(0,0,0);
			fill(color);
			rect(p2.x - 3, p2.y - 3, 7, 7);
		}
		return p2;
		} catch (NullPointerException npx) {
			return null;
		}
	}
	
	public Point doBlueTDetection(Blob[] blobs) {
		Point p = null;
		Point p1 = null;
		Point p2 = null;
		for(Blob b : blobs) {
			try {
				if(b.isHole) {
				
				}else if ((b.centroid.x <= ball.x+5 && b.centroid.x >= ball.x-5) || (b.centroid.y <= ball.y +5 && b.centroid.y >= ball.y -5)) {
				
				} else 	if(col(b.centroid.x, b.centroid.y) == "blue") {
					if(draw_found) {
						int color = color(255,255,255);
						fill(color);
						rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
					}
					for(int y = 0; y < b.points.length; y++) {
						if(black[0] == null) {
							if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y) {
								blackBlue = new Point(oldBlack1.x, oldBlack1.y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
								blackBlue = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else if (black[1] == null) {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
								blackBlue = new Point(black[0].x, black[0].y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
								blackBlue = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
								blackBlue = new Point(black[0].x, black[0].y);
							} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y) {
								blackBlue = new Point(black[1].x, black[1].y);
							}
						}
					}
					return (new Point(b.centroid.x, b.centroid.y));
				} else {
					for (int x = 0; x < b.points.length; x++) {
						if (col(b.points[x].x, b.points[x].y) == "blue") {
							if(draw_found) {
								int color = color(255,255,255);
								fill(color);
								rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
							}
							for(int y = 0; y < b.points.length; y++) {
								if(black[0] == null) {
									if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y) {
										blackBlue = new Point(oldBlack1.x, oldBlack1.y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackBlue = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else if (black[1] == null) {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackBlue = new Point(black[0].x, black[0].y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackBlue = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackBlue = new Point(black[0].x, black[0].y);
									} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y) {
										blackBlue = new Point(black[1].x, black[1].y);
									}
								}
							}
							if(draw_found) {
								int color = color(255,255,255);
								fill(color);
								rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
							}
							return (new Point(b.centroid.x, b.centroid.y));
						}
					}
				}
			} catch (NullPointerException nxe) {
				if(b.isHole) {
					
				}else if ((b.centroid.x <= oldBall.x+5 && b.centroid.x >= oldBall.x-5) || (b.centroid.y <= oldBall.y +5 && b.centroid.y >= oldBall.y -5)) {
				
				} else if(col(b.centroid.x, b.centroid.y) == "blue") {
					if(draw_found) {
						int color = color(255,255,255);
						fill(color);
						rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
					}
					for(int y = 0; y < b.points.length; y++) {
						if(black[0] == null) {
							if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y) {
								blackBlue = new Point(oldBlack1.x, oldBlack1.y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
								blackBlue = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else if (black[1] == null) {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
								blackBlue = new Point(black[0].x, black[0].y);
							} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
								blackBlue = new Point(oldBlack2.x, oldBlack2.y);
							}
						} else {
							if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
								blackBlue = new Point(black[0].x, black[0].y);
							} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y) {
								blackBlue = new Point(black[1].x, black[1].y);
							}
						}
					}
					return (new Point(b.centroid.x, b.centroid.y));
				} else {
					for (int x = 0; x < b.points.length; x++) {
						if (col(b.points[x].x, b.points[x].y) == "blue") {
							if(draw_found) {
								int color = color(255,255,255);
								fill(color);
								rect(b.centroid.x - 3, b.centroid.y - 3, 7, 7);
							}
							for(int y = 0; y < b.points.length; y++) {
								if(black[0] == null) {
									if (oldBlack1.x == b.points[y].x && oldBlack1.y == b.points[y].y) {
										blackBlue = new Point(oldBlack1.x, oldBlack1.y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackBlue = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else if (black[1] == null) {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackBlue = new Point(black[0].x, black[0].y);
									} else if (oldBlack2.x == b.points[y].x && oldBlack2.y == b.points[y].y) {
										blackBlue = new Point(oldBlack2.x, oldBlack2.y);
									}
								} else {
									if (black[0].x == b.points[y].x && black[0].y == b.points[y].y) {
										blackBlue = new Point(black[0].x, black[0].y);
									} else if (black[1].x == b.points[y].x && black[1].y == b.points[y].y) {
										blackBlue = new Point(black[1].x, black[1].y);
									}
								}
							}
							return (new Point(b.centroid.x, b.centroid.y));
						}
					}
				}
			}
		}if (black[0] == null) {
			for (int x = 0; x <=100; x++) {
				if ((col(oldBlack2.x+x, oldBlack2.y)=="blue")&&(oldBlack2.x+x<=WIDTH)) {
					p = new Point(oldBlack2.x+x, oldBlack2.y);
					blackBlue = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x-x, oldBlack2.y)=="blue")&&(oldBlack2.x-x>=0)) {
					p = new Point(oldBlack2.x-x, oldBlack2.y);
					blackBlue = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x, oldBlack2.y-x)=="blue")&&(oldBlack2.y-x>=0)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y-x);
						blackBlue = new Point (oldBlack2.x, oldBlack2.y);
					} else {
						p1 = new Point(oldBlack2.x, oldBlack2.y-x);
						break;
					}					
				} else if ((col(oldBlack2.x, oldBlack2.y+x)=="blue")&&(oldBlack2.y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y+x);
						blackBlue = new Point (oldBlack2.x, oldBlack2.y);
					} else {
						p1 = new Point(oldBlack2.x, oldBlack2.y+x);
						break;
					}
				} else if ((col(oldBlack1.x+x, oldBlack1.y)=="blue")&&(oldBlack1.x+x<=WIDTH)) {
					p = new Point(oldBlack1.x+x, oldBlack1.y);
					blackBlue = new Point (oldBlack1.x, oldBlack1.y);
				} else if ((col(oldBlack1.x-x, oldBlack1.y)=="blue")&&(oldBlack1.x-x>=0)) {
					p = new Point(oldBlack1.x-x, oldBlack1.y);
					blackBlue = new Point (oldBlack1.x, oldBlack1.y);
				} else if ((col(oldBlack1.x, oldBlack1.y-x)=="blue")&&(oldBlack1.y-x>=0)) {
					if (p == null) {
						p = new Point(oldBlack1.x, oldBlack1.y-x);
						blackBlue = new Point (oldBlack1.x, oldBlack1.y);
					} else {
						p1 = new Point(oldBlack1.x, oldBlack1.y-x);
						break;
					}
				} else if ((col(oldBlack1.x, oldBlack1.y+x)=="blue")&&(oldBlack1.y+x<=HEIGHT)) {
					if (p == null) {
						p = new Point(oldBlack1.x, oldBlack1.y+x);
						blackBlue = new Point (oldBlack1.x, oldBlack1.y);
					} else {
						p1 = new Point(oldBlack1.x, oldBlack1.y+x);
						break;
					}
				}
			}
		} else if (black[1] == null) {
			for (int x = 0; x <=100; x++) {
				if ((col(oldBlack2.x+x, oldBlack2.y)=="blue")&&(oldBlack2.x+x<=WIDTH)) {
					p = new Point(oldBlack2.x+x, oldBlack2.y);
					blackBlue = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x-x, oldBlack2.y)=="blue")&&(oldBlack2.x-x>=0)) {
					p = new Point(oldBlack2.x-x, oldBlack2.y);
					blackBlue = new Point (oldBlack2.x, oldBlack2.y);
				} else if ((col(oldBlack2.x, oldBlack2.y-x)=="blue")&&(oldBlack2.y-x>=0)) {
					if(p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y-x);
						blackBlue = new Point (oldBlack2.x, oldBlack2.y);
					}else {
						p1 = new Point(oldBlack2.x, oldBlack2.y-x);
						break;
					}
				} else if ((col(oldBlack2.x, oldBlack2.y+x)=="blue")&&(oldBlack2.y+x<=HEIGHT)) {
					if ( p == null) {
						p = new Point(oldBlack2.x, oldBlack2.y+x);
						blackBlue = new Point (oldBlack2.x, oldBlack2.y);
					}else {
						p1 = new Point(oldBlack2.x, oldBlack2.y+x);
						break;
					}
				} else if ((col(black[0].x+x, black[0].y)=="blue")&&(black[0].x+x<=WIDTH)) {
					p = new Point(black[0].x+x, black[0].y);
					blackBlue = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x-x, black[0].y)=="blue")&&(black[0].x-x>=0)) {
					p = new Point(black[0].x-x, black[0].y);
					blackBlue = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x, black[0].y-x)=="blue")&&(black[0].y-x>=0)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y-x);
						blackBlue = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y-x);
						break;
					}
				} else if ((col(black[0].x, black[0].y+x)=="blue")&&(black[0].y+x<=HEIGHT)) {
					if (p == null) {
						p = new Point(black[0].x, black[0].y+x);
						blackBlue = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y+x);
						break;
					}
				}
			}
		} else {
			for (int x = 0; x <=100; x++) {
				if ((col(black[0].x+x, black[0].y)=="blue")&&(black[0].x+x<=WIDTH)) {
					p = new Point(black[0].x+x, black[0].y);
					blackBlue = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x-x, black[0].y)=="blue")&&(black[0].x-x>=0)) {
					p = new Point(black[0].x-x, black[0].y);
					blackBlue = new Point (black[0].x, black[0].y);
				} else if ((col(black[0].x, black[0].y-x)=="blue")&&(black[0].y-x>=0)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y-x);
						blackBlue = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y-x);
						break;
					}
				} else if ((col(black[0].x, black[0].y+x)=="blue")&&(black[0].y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(black[0].x, black[0].y+x);
						blackBlue = new Point (black[0].x, black[0].y);
					} else {
						p1 = new Point(black[0].x, black[0].y+x);
						break;
					}
				} else if ((col(black[1].x+x, black[1].y)=="blue")&&(black[1].x+x<=WIDTH)){
					p = new Point(black[1].x+x, black[1].y);
					blackBlue = new Point (black[1].x, black[1].y);
				} else if ((col(black[1].x-x, black[1].y)=="blue")&&(black[1].x-x>=0)) {
					p = new Point(black[1].x-x, black[1].y);
					blackBlue = new Point (black[1].x, black[1].y);
				} else if ((col(black[1].x, oldBlack1.y-x)=="blue")&&(black[1].y-x>=0)) {
					if (p == null) {
						p = new Point(black[1].x, black[1].y-x);
						blackBlue = new Point (black[1].x, black[1].y);
					} else {
						p1 = new Point(black[1].x, black[1].y-x);
						break;
					}
				} else if ((col(black[1].x, black[1].y+x)=="blue")&&(black[1].y+x<=HEIGHT)) {
					if(p == null) {
						p = new Point(black[1].x, black[1].y+x);
						blackBlue = new Point (black[1].x, black[1].y);
					} else {
						p1 = new Point(black[1].x, black[1].y+x);
						break;
					}
				}
			}
		} try {
		p2 = new Point((p.x + p1.x)/2, (p.y + p1.y)/2);
		if(draw_found) {
			int color = color(255,255,255);
			fill(color);
			rect(p2.x - 3, p2.y - 3, 7, 7);
		}
		return p2;
		} catch (NullPointerException npx) {
			return null;
		}
	}
	
	
	public void doYellowDirectionCalculation() {
		if(yellowRobot == null) {
			if(draw_found) {
				line(oldYellowRobot.x, oldYellowRobot.y, blackYellow.x, blackYellow.y);
			}
			yellowDirection[0] = oldYellowRobot.x - blackYellow.x;
			yellowDirection[1] = oldYellowRobot.y - blackYellow.y;
		} else {
			if(draw_found) {
				line(yellowRobot.x, yellowRobot.y, blackYellow.x, blackYellow.y);
			}
			yellowDirection[0] = yellowRobot.x - blackYellow.x;
			yellowDirection[1] = yellowRobot.y - blackYellow.y;
		}
	}
	
	
	public void doBlueDirectionCalculation() {
		if(blueRobot == null) {
			line(oldBlueRobot.x, oldBlueRobot.y, blackBlue.x, blackBlue.y);
			blueDirection[0] = oldBlueRobot.x - blackBlue.x;
			blueDirection[1] = oldBlueRobot.y - blackBlue.y;
		} else {
			line(blueRobot.x, blueRobot.y, blackBlue.x, blackBlue.y);
			blueDirection[0] = blueRobot.x - blackBlue.x;
			blueDirection[1] = blueRobot.y - blackBlue.y;
		}
	}
	// keyPressed is a processing function that lets us know when a user
	// has pressed a key.  the variable, key, will be set to a character of the keyboard.
	public void keyPressed() {
		if (key == 'b' ) {
			draw_blobs = !draw_blobs;
		}
		if(key == 'z') {
			draw_found = !draw_found;
		}
	}
	
	
	public String col(int x,int y){
		int pix = get(x,y);
		String color = "";
	
		if((red(pix)>=180 && green(pix)<=30 && blue(pix)<=40) ||(red(pix)>=220 && green(pix)<= 130 && blue(pix) <= 130))
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

	// including this function lets us run our java program as an application instead of 
	// an applet, centering the program in a full screen environment.
	public static void main(String args[]) {
		stopwatch.start();
		server = new VisionServer();
		Thread serverThread = new Thread(server);
		serverThread.start();
		PApplet.main( new String[]{NAME} ); // Start parrent program giving the name
	}
}