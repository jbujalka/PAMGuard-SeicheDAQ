package seichedaq.display;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;

public class DAQPanel {

	private SeicheNetworkDaq seicheNetworkDaq;
	private int daqIndex;
	private JPanel mainPanel;
	
	private QuickValue vPlus, vMinus, serialNo, gain, filter;
	private JPanel leftPanel;
	
	public DAQPanel(SeicheNetworkDaq seicheNetworkDaq, int daqIndex) {
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.daqIndex = daqIndex;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("DAQ " + daqIndex));
		leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(BorderLayout.WEST, leftPanel);
		mainPanel.add(BorderLayout.NORTH, northPanel);
		
		add(serialNo = new QuickValue("Serial No. ", 5, ""), c);
		c.gridx = 0; 
		c.gridy++;
		add(gain = new QuickValue("Gain ", 5, "dB"), c);
		c.gridx = 0; 
		c.gridy++;
		add(filter = new QuickValue("Filter ", 5, "kHz"), c);
		c.gridx = 0; 
		c.gridy++;
		add(vPlus = new QuickValue("V+ ", 5, "V"), c);
		c.gridx = 0; 
		c.gridy++;
		add(vMinus = new QuickValue("V- ", 5, "V"), c);
		c.gridx = 0; 
		c.gridy++;
		
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	private void add(QuickValue quickValue, GridBagConstraints c) {
		leftPanel.add(quickValue.getDataLabel(), c);
		c.gridx++;
		leftPanel.add(quickValue.getDataValue(), c);
		c.gridx++;
	}

	public void updateDaqData(Hashtable<DAQDATANAMES, Number> hashtable) {
		Double dVal;
		Integer iVal;
		iVal = (Integer) hashtable.get(DAQDATANAMES.SERIALNUMBER);
		serialNo.setText(iVal);
		iVal = (Integer) hashtable.get(DAQDATANAMES.GAIN);
		gain.setText(iVal);
		iVal = (Integer) hashtable.get(DAQDATANAMES.FILTER);
		filter.setText(iVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.SUPPLYPLUS);
		vPlus.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.SUPPLYMINUS);
		vMinus.setText(dVal);
	}
}
