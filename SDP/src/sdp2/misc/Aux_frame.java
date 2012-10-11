package sdp2.misc;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sdp2.panel.*;
import sdp2.slider.*;
import sdp2.vision.Vision_Beta;


public class Aux_frame {
	private BrightnessSlider brightness;
	private ContrastSlider contrast;
	private BallColourPanel ball;
	private YellowRobotColourPanel yellowRobot;
	private BlueRobotColourPanel blueRobot;
	private Vision_Beta vision;
	
	public Aux_frame(Vision_Beta vision) {
		this.vision = vision;
		JFrame frame = new JFrame("Auxiliary Panel");
		brightness = new BrightnessSlider(vision);
		contrast = new ContrastSlider(vision);
		ball = new BallColourPanel(vision);
		yellowRobot = new YellowRobotColourPanel(vision);
		blueRobot = new BlueRobotColourPanel(vision);
		
		JPanel panel = new JPanel();
		panel.add(brightness.getPanel());
		panel.add(contrast.getPanel());
		
		panel.add(ball.getPanel());
		panel.add(yellowRobot.getPanel());
		panel.add(blueRobot.getPanel());
		
		frame.add(panel);
		frame.setSize(600,600);
		frame.setVisible(true);
	}

}
