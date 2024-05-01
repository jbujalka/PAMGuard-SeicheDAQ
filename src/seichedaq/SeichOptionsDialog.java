package seichedaq;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class SeichOptionsDialog extends PamDialog {

	private static SeichOptionsDialog singleInstance;
	private SeicheNetworkDaq seicheNetworkDaq;
	private SeicheDaqParams daqParams;
	
	private JTextField dbInterval;
	private JTextField graphLength;
	
	private SeichOptionsDialog(Window parentFrame, SeicheNetworkDaq seicheNetworkDaq) {
		super(parentFrame, "Seiche Net DAQ options", false);
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.daqParams = seicheNetworkDaq.getSeicheDaqParams().clone();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel loggingPanel = new JPanel(new GridBagLayout());
		loggingPanel.setBorder(new TitledBorder("Logging"));
		GridBagConstraints c = new PamGridBagContraints();
		loggingPanel.add(new JLabel("Database logging interval ", SwingConstants.RIGHT));
		c.gridx++;
		loggingPanel.add(dbInterval = new JTextField(5), c);
		c.gridx++;
		loggingPanel.add(new JLabel(" s ", SwingConstants.LEFT));
		mainPanel.add(loggingPanel);
		
		JPanel displayPanel = new JPanel(new GridBagLayout());
		displayPanel.setBorder(new TitledBorder("Display"));
		c = new PamGridBagContraints();
		displayPanel.add(new JLabel("CAPT graph duration ", SwingConstants.RIGHT));
		c.gridx++;
		displayPanel.add(graphLength = new JTextField(5), c);
		c.gridx++;
		displayPanel.add(new JLabel(" s ", SwingConstants.LEFT));
		mainPanel.add(displayPanel);
		
		setDialogComponent(mainPanel);
	}

	public static SeicheDaqParams showDialog(Window parentFrame, SeicheNetworkDaq seicheNetworkDaq) {
		if (singleInstance == null || seicheNetworkDaq != singleInstance.seicheNetworkDaq || parentFrame != singleInstance.getOwner()) {
			singleInstance = new SeichOptionsDialog(parentFrame, seicheNetworkDaq);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.daqParams;
	}
	
	private void setParams() {
		dbInterval.setText(String.format("%d", daqParams.databseLoggingInterval));
		graphLength.setText(String.format("%d", daqParams.graphDuration));
	}

	@Override
	public boolean getParams() {
		try {
			daqParams.databseLoggingInterval = Integer.valueOf(dbInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid database logging interval");
		}
		try {
			daqParams.graphDuration = Integer.valueOf(graphLength.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid graph duration");
		}
		if (daqParams.graphDuration <= 0) {
			return showWarning("Invalid graph duration. Must be > 0");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		daqParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
