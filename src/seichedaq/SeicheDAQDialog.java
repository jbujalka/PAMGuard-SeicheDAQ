package seichedaq;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog panel that inserts into the main Acquisition dialog
 * panel to handle options specific to the Seiche Daq system. 
 * @author dg50
 *
 */
public class SeicheDAQDialog {

	private SeicheNetworkDaq seicheNetworkDaq;
	private AcquisitionControl acquisitionControl;
	private JPanel daqPanel;
	private JComboBox<String> gain;
	private JComboBox<String> filter;
	private JCheckBox[] channelBoxes;
	private AcquisitionDialog acquisitionDialog;
	private JComboBox<String> ipList;
	private JTextField hostPort, rxPort;

	/**
	 * Constructor, needs references back to several other objects. 
	 * @param seicheNetworkDaq Main Seiche Network Daq class
	 * @param acquisitionControl Acquisition controller
	 * @param acquisitionDialog Main acquisition dialog. 
	 */
	public SeicheDAQDialog(SeicheNetworkDaq seicheNetworkDaq, AcquisitionControl acquisitionControl, AcquisitionDialog acquisitionDialog) {
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.acquisitionControl = acquisitionControl;
		this.acquisitionDialog = acquisitionDialog;
		daqPanel = new JPanel();
		daqPanel.setLayout(new BoxLayout(daqPanel, BoxLayout.Y_AXIS));
		
		JPanel ipPanel = new JPanel(new GridBagLayout());
		ipPanel.setBorder(new TitledBorder("Connection"));
		GridBagConstraints c = new PamGridBagContraints();
		ipPanel.add(new JLabel("Daq IP Address ", SwingConstants.RIGHT), c);
		c.gridx++;
		ipPanel.add(ipList = new JComboBox<String>(), c);
		c.gridx = 0;
		c.gridy++;
		ipPanel.add(new JLabel("Daq IP Port ", SwingConstants.RIGHT), c);
		c.gridx++;
		ipPanel.add(hostPort = new JTextField(6), c);
		c.gridx = 0;
		c.gridy++;
		ipPanel.add(new JLabel("Data Port ", SwingConstants.RIGHT), c);
		c.gridx++;
		ipPanel.add(rxPort = new JTextField(6), c);
		c.gridx = 1;
		c.gridy++;
		JButton optsButton = new JButton("Options ...");
		ipPanel.add(optsButton, c);
		optsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsButton();
			}
		});
		
		
		ipList.setEditable(true);
		ipList.setToolTipText("IP Address for DAQ device");
		hostPort.setToolTipText("IP Port for sending commands to the DAQ device");
		rxPort.setToolTipText("IP Port for receiving data from the DAQ device");
		daqPanel.add(ipPanel);
		
		JPanel topPanel = new JPanel();
		daqPanel.add(topPanel);
		topPanel.setBorder(new TitledBorder("Gain and Filter settings"));
		topPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		topPanel.add(new JLabel("Gain ", SwingConstants.RIGHT), c);
		c.gridx++;
		topPanel.add(gain = new JComboBox<>(), c);
		c.gridx ++;
//		c.gridy ++;
		topPanel.add(new JLabel("   Low cut filter ", SwingConstants.RIGHT), c);
		c.gridx++;
		topPanel.add(filter = new JComboBox<>(), c);
		for (int i = 0; i < SeicheNetworkDaq.GAINS.length; i++) {
			gain.addItem(String.format("%d dB", SeicheNetworkDaq.GAINS[i]));
			gain.addActionListener(new GainChange());
		}
		for (int i = 0; i < SeicheNetworkDaq.LOWCUTFILTER.length; i++) {
			filter.addItem(String.format("%d Hz", SeicheNetworkDaq.LOWCUTFILTER[i]));
		}
		
		JPanel chanPanel = new JPanel(new GridBagLayout());
		chanPanel.setBorder(new TitledBorder("Select channels to acquire"));
		daqPanel.add(chanPanel);
		c = new PamGridBagContraints();
		int nPerRow = 8;
		channelBoxes = new JCheckBox[SeicheNetworkDaq.MAXCHANNELS];
		ChannelChange channelChange = new ChannelChange();
		for (int i = 0; i < SeicheNetworkDaq.MAXCHANNELS; i++) {
			chanPanel.add(channelBoxes[i] = new JCheckBox(String.format("ch%d", i)), c);
			channelBoxes[i].addActionListener(channelChange);
			c.gridx++;
			if (c.gridx == nPerRow) {
				c.gridx = 0;
				c.gridy++;
			}
		}
	}

	protected void optionsButton() {
		seicheNetworkDaq.showOptions(acquisitionDialog);
	}

	/**
	 * 
	 * @return The Swing component to be included into the main dialog. 
	 */
	public JComponent getComponent() {
		return daqPanel;
	}
	
	/**
	 * Called every time one of the channel checkboxes is clicked. 
	 * @author dg50
	 *
	 */
	private class ChannelChange implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			channelChange();
		}
	}

	/**
	 * Counts up the total number of selected channels
	 * and sets in the main acquisition dialog
	 */
	private void channelChange() {
		int nChan = 0;	
		for (int i = 0; i < SeicheNetworkDaq.MAXCHANNELS; i++) {
			if (channelBoxes[i].isSelected()) {
				nChan++;
			}
		}
		acquisitionDialog.setChannels(nChan);
	}
	/**
	 * Called if the gain is changed
	 * @author dg50
	 *
	 */
	private class GainChange implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			gainChange();
		}
	}

	/**
	 * Sets the gain value in the main acquisition dialog
	 * (from where it will be picked up and used in level calculations in PAMGuard)
	 */
	private void gainChange() {
		int gainVal = SeicheNetworkDaq.GAINS[gain.getSelectedIndex()];
		acquisitionDialog.setPreampGain(gainVal);
	}
	
	/**
	 * Set parameter values in the dialog panel
	 */
	public void setParams() {
		SeicheDaqParams params = seicheNetworkDaq.getSeicheDaqParams();
		ipList.removeAllItems();
		for (String ipAddr:params.getRecentHosts()) {
			ipList.addItem(ipAddr);
		}
		ipList.setSelectedIndex(0);
		hostPort.setText(String.format("%d", params.txPort));
		rxPort.setText(String.format("%d", params.rxPort));
		
		gain.setSelectedIndex(params.getGainIndex());
		filter.setSelectedIndex(params.getFilterIndex());
		int chMap = params.channelMap;
		for (int i = 0; i < SeicheNetworkDaq.MAXCHANNELS; i++) {
			channelBoxes[i].setSelected((chMap & 1<<i) != 0);
		}
		gainChange();
		channelChange();
		acquisitionDialog.setVPeak2Peak(SeicheNetworkDaq.PEAKTOPEAKVOLTAGE);
		acquisitionDialog.setSampleRate(SeicheNetworkDaq.FIXEDSAMPLERATE);
	}

	/**
	 * Get parameters from the dialog panel. 
	 * @return true if they are OK. 
	 */
	public boolean getParams() {
		SeicheDaqParams params = seicheNetworkDaq.getSeicheDaqParams();
		String ipAddr = (String) this.ipList.getSelectedItem();
		params.setHostAddress(ipAddr);
		try {
			params.txPort = Integer.valueOf(hostPort.getText());
			params.rxPort = Integer.valueOf(rxPort.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Seiche DAQ Configuration", "Invalid ip port address");
		}
		
		params.setGainIndex(gain.getSelectedIndex());
		params.setFilterIndex(filter.getSelectedIndex());
		int chMap = 0;
		for (int i = 0; i < SeicheNetworkDaq.MAXCHANNELS; i++) {
			if (channelBoxes[i].isSelected()) {
				chMap |= (1<<i);
			}
		}
		params.channelMap = chMap;
		return true;
	}

}
