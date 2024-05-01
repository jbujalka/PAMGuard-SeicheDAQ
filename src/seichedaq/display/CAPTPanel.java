package seichedaq.display;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamUtils.LatLong;
import PamView.dialog.PamGridBagContraints;
import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;
import seichedaq.slowdata.DoubleDataThing;

public class CAPTPanel {

	private SeicheNetworkDaq seicheNetworkDaq;
	private int captIndex;
	private JPanel mainPanel;
	private QuickValue vPlus, vMinus, serialNo, gain, filter;
	private QuickValue nodeId, pressure, pressureTemp, temperature, heading1, pitch1, roll1;
//	private QuickValue heading2, pitch2, roll2;
	private JPanel leftPanel;
	//private HeadingGraph headingGraph;
	//private TemperatureGraph tempGraph;
	//private PressureGraph pressureGraph;
	
	public CAPTPanel(SeicheNetworkDaq seicheNetworkDaq, int captIndex) {
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.captIndex = captIndex;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("CAPT " + captIndex));
		leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(BorderLayout.WEST, leftPanel);
		mainPanel.add(BorderLayout.NORTH, northPanel);
		
//		JTabbedPane graphPane = new JTabbedPane();
//		headingGraph  = new HeadingGraph(seicheNetworkDaq, captIndex);
//		tempGraph = new TemperatureGraph(seicheNetworkDaq, captIndex);
//		pressureGraph = new PressureGraph(seicheNetworkDaq, captIndex);
//		graphPane.add("Compass", headingGraph.getComponent());
//		graphPane.addTab("Temp", tempGraph.getComponent());
//		graphPane.addTab("Pressure", pressureGraph.getComponent());
//		mainPanel.add(BorderLayout.CENTER, graphPane);
		
		add(nodeId = new QuickValue("Node Id. ", 5, ""), c);
		c.gridx = 0; 
		c.gridy++;
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
		add(pressure = new QuickValue("Pressure ", 7, "bar"), c);
		c.gridx = 0; 
		c.gridy++;
		add(pressureTemp = new QuickValue("Pressure T ", 5, LatLong.deg), c);
		c.gridx = 0; 
		c.gridy++;
		add(temperature = new QuickValue("Temperature ", 5, LatLong.deg), c);
		c.gridx = 0; 
		c.gridy++;
		add(heading1 = new QuickValue("Heading 1 ", 5, LatLong.deg), c);
		c.gridx = 0; 
		c.gridy++;
		add(pitch1 = new QuickValue("Pitch 1 ", 5, LatLong.deg), c);
		c.gridx = 0; 
		c.gridy++;
		add(roll1 = new QuickValue("Roll 1 ", 5, LatLong.deg), c);
		c.gridx = 0; 
		c.gridy++;
//		add(heading2 = new QuickValue("Heading 2 ", 5, LatLong.deg), c);
//		c.gridx = 0; 
//		c.gridy++;
//		add(pitch2 = new QuickValue("Pitch 2 ", 5, LatLong.deg), c);
//		c.gridx = 0; 
//		c.gridy++;
//		add(roll2 = new QuickValue("Roll 2 ", 5, LatLong.deg), c);
//		c.gridx = 0; 
//		c.gridy++;
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

	public void updateCaptData(Hashtable<DAQDATANAMES, Number> hashtable) {
		Double dVal;
		Integer iVal;
		iVal = (Integer) hashtable.get(DAQDATANAMES.NODEID);
		nodeId.setText(iVal);
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
		dVal = (Double) hashtable.get(DAQDATANAMES.PRESSURE);
		pressure.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.PRESSURETEMPERATURE);
		pressureTemp.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.TEMPERATURE);
		temperature.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.COMPASS1HEADING);
		heading1.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.COMPASS1PITCH);
		pitch1.setText(dVal);
		dVal = (Double) hashtable.get(DAQDATANAMES.COMPASS1ROLL);
		roll1.setText(dVal);
//		dVal = (Double) hashtable.get(DAQDATANAMES.COMPASS2HEADING);
//		heading2.setText(dVal);
//		dVal = (Double) hashtable.get(DAQDATANAMES.COPMPASS2PITCH);
//		pitch2.setText(dVal);
//		dVal = (Double) hashtable.get(DAQDATANAMES.COMPASS2ROLL);
//		roll2.setText(dVal);
//		headingGraph.updateGraph(System.currentTimeMillis());
	}
}
