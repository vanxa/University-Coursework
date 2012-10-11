package sdp2.slider;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JButton;

import sdp2.actionListener.BrightnessActionListener;
import sdp2.changeListener.BrightnessChangeListener;
import sdp2.resetListener.BrightnessResetListener;
import sdp2.vision.Vision_Beta;


public class BrightnessSlider {
	private JSlider slider;
	private Vision_Beta vision;
	private JLabel brightness_label;
	private JPanel panel;
	private JTextField brightness_lvl;
	private JButton reset;
	
	
	public BrightnessSlider(Vision_Beta vision) {
		this.vision = vision;
		
		// Init components
		slider = new JSlider(-120,120,vision.getBrightnessLvl());
		brightness_label = new JLabel("brightness level:");
		brightness_lvl = new JTextField(Integer.toString(vision.getBrightnessLvl()),5);
		reset = new JButton("Reset");
		
		// Add listeners
		slider.addChangeListener(new BrightnessChangeListener(this));
		brightness_lvl.addActionListener(new BrightnessActionListener(this));
		reset.addActionListener(new BrightnessResetListener(this));
		
		// Create a panel and add the components to the panel
		panel = new JPanel();
		panel.add(brightness_label);
		panel.add(slider);
		panel.add(brightness_lvl);
		panel.add(reset);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	public JTextField getValue() {
		return brightness_lvl;
	}
	
	public JLabel getLabel() {
		return brightness_label;
	}
	
	public Vision_Beta getVisionSystem() {
		return vision;
	}
	
	public JPanel getPanel() {
		return panel;
	}
}
