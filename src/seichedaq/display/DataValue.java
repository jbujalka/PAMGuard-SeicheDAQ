package seichedaq.display;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class DataValue extends JTextField {
	public DataValue(String str) {
		super(str, JLabel.LEFT);
	}
	
	public DataValue(int len) {
		super(len);
	}
}
