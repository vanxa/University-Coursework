package sdp2.slider;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import sdp2.actionListener.ContrastActionListener;
import sdp2.changeListener.ContrastChangeListener;
import sdp2.resetListener.ContrastResetListener;
import sdp2.vision.Vision_Beta;


public class ContrastSlider {
	private JSlider slider;
	private Vision_Beta vision;
	private JTextField contrast_lvl;
	private JLabel contrast_label;
	private JPanel panel;
	private JButton reset;
	private int default_contrast;
	
	public ContrastSlider(Vision_Beta vision) {
		this.vision = vision;
		
		// Init components
		slider = new JSlider(-100,100,vision.getContrastLvl());
		default_contrast = vision.getContrastLvl();
		contrast_lvl = new JTextField(Integer.toString(vision.getContrastLvl()),5);
		contrast_label = new JLabel("contrast level:");
		reset = new JButton("Reset");
		
		// Add listeners
		slider.addChangeListener(new ContrastChangeListener(this));
		contrast_lvl.addActionListener(new ContrastActionListener(this));
		reset.addActionListener(new ContrastResetListener(this));
		
		// Create and populate panel
		panel = new JPanel();
		panel.add(contrast_label);
		panel.add(slider);
		panel.add(contrast_lvl);
		panel.add(reset);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	public JTextField getValue() {
		return contrast_lvl;
	}
	
	public JLabel getLabel() {
		return contrast_label;
	}
	
	public Vision_Beta getVisionSystem() {
		return vision;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public int getDefaultContrast() {
		return default_contrast;
	}
}
