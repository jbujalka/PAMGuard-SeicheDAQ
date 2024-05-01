package seichedaq.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;
import seichedaq.SeicheDataHeader;
import seichedaq.SeicheNetworkDaq;
import seichedaq.SlowObserver;
import seichedaq.slowdata.DAQDATANAMES;
import seichedaq.slowdata.SlowData;
import seichedaq.slowdata.SlowDataHandler;
import userDisplay.UserDisplayComponent;

/**
 * Panel for display of ALL Seiche DAQ information. 
 * @author dg50
 *
 */
public class SeicheDaqDisplayPanel implements SlowObserver, UserDisplayComponent {

	private JPanel mainPanel;
	
	private SeicheNetworkDaq seicheNetworkDaq;

	private SeicheGeneralPanel generalPanel;
	
	private CAPTPanel[] captPanels;
	
	private DAQPanel[] daqPanels;
	
	private String uniqueName;

	private ArrayList<CAPTGraph> captGraphs = new ArrayList<>();
	
	private static final double[] headingRange = {0., 360.};
	private static final double[] pitchRange = {-180., 180};
	private static final double[] rollRange = {-180., 180};
	private static final double[] tempRange = {0., 50.};
	private static final double[] pressureRange = {0., 10.};
	/**
	 * @param displayPanelContainer 
	 * @param seicheDisplayProider 
	 * @param seicheNetworkDaq
	 * @param uniqueDisplayName 
	 * @param seicheDisplayProvider 
	 */
	public SeicheDaqDisplayPanel(SeicheNetworkDaq seicheNetworkDaq, SeicheDisplayProvider seicheDisplayProvider, String uniqueDisplayName) {
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.uniqueName = uniqueDisplayName;
		mainPanel = new JPanel(new BorderLayout());
		generalPanel = new SeicheGeneralPanel(seicheNetworkDaq, this);
		mainPanel.add(BorderLayout.NORTH, generalPanel.getComponent());
		captPanels = new CAPTPanel[SlowDataHandler.NCAPT];
		daqPanels = new DAQPanel[SlowDataHandler.NDAQ];
		JPanel sPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		sPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(BorderLayout.CENTER, sPanel);
		
//		captGraphs.add(new HeadingGraph(seicheNetworkDaq, -1));
//		captGraphs.add(new PressureGraph(seicheNetworkDaq, -1));
//		captGraphs.add(new TemperatureGraph(seicheNetworkDaq, -1));
		captGraphs.add(new CAPTGraph(seicheNetworkDaq, DAQDATANAMES.COMPASS1HEADING, "Heading", "degrees", headingRange));
		captGraphs.add(new CAPTGraph(seicheNetworkDaq, DAQDATANAMES.COMPASS1PITCH, "Pitch", "degrees", pitchRange));
		captGraphs.add(new CAPTGraph(seicheNetworkDaq, DAQDATANAMES.COMPASS1ROLL, "Roll", "degrees", rollRange));
		captGraphs.add(new CAPTGraph(seicheNetworkDaq, DAQDATANAMES.PRESSURE, "Pressure", "bar", pressureRange));
		captGraphs.add(new CAPTGraph(seicheNetworkDaq, DAQDATANAMES.TEMPERATURE, "Temperature", "degrees C", tempRange));
		
		JPanel captNorthPanel = new JPanel();
		captNorthPanel.setLayout(new GridLayout(1, SlowDataHandler.NCAPT));
		for (int i = 0; i < SlowDataHandler.NCAPT; i++) {
			captPanels[i] = new CAPTPanel(seicheNetworkDaq, i);
			captNorthPanel.add(captPanels[i].getComponent());
		}
		JPanel captCentralPanel = new JPanel(new BorderLayout());
		JTabbedPane captGraphsTabs = new JTabbedPane();
		captCentralPanel.add(captGraphsTabs, BorderLayout.CENTER);
		JPanel captPanel = new JPanel(new GridLayout(2, 1));
		for (CAPTGraph g:captGraphs) {
			captGraphsTabs.add(g.getGraphName(), g.getComponent());
		}
		JScrollPane captScroll = new JScrollPane(captNorthPanel);
		captScroll.setBorder(null);
		captPanel.add(captScroll);
		captPanel.add(captCentralPanel);
		
		JPanel daqPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
//		daqPanel.setLayout(new BoxLayout(daqPanel, BoxLayout.X_AXIS));
		for (int i = 0; i < SlowDataHandler.NDAQ; i++) {
			daqPanels[i] = new DAQPanel(seicheNetworkDaq, i);
			daqPanel.add(daqPanels[i].getComponent(), c);
			c.gridx++;
			if (c.gridx == SlowDataHandler.NDAQ/2) {
				c.gridx = 0;
				c.gridy++;
			}
		}
		JPanel daqNorthPanel = new JPanel(new BorderLayout());
		daqNorthPanel.add(BorderLayout.NORTH, daqPanel);
		

//		JScrollPane captScroll = new JScrollPane(captPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane daqScroll = new JScrollPane(daqNorthPanel);
		tabbedPane.add("CAPT Data", captPanel);
		tabbedPane.add("DAQ Data", daqScroll);
		
		seicheNetworkDaq.addSlowObserver(this);
	}


	@Override
	public void daqHeadChange(SeicheDataHeader seicheDataHeader) {
		generalPanel.updateDaqHeader(seicheDataHeader);
	}

	@Override
	public void newSlowData(SlowData slowData) {
		for (int i = 0; i < SlowDataHandler.NDAQ; i++) {
			daqPanels[i].updateDaqData(slowData.getDaqData(i));
		}
		for(int i = 0; i < SlowDataHandler.NCAPT; i++) {
			captPanels[i].updateCaptData(slowData.getCaptData(i));
		}
		for (CAPTGraph cg:captGraphs) {
			cg.updateGraph(System.currentTimeMillis());
		}
	}


	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponent#getComponent()
	 */
	@Override
	public Component getComponent() {
		return mainPanel;
	}


	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponent#openComponent()
	 */
	@Override
	public void openComponent() {
		seicheNetworkDaq.addSlowObserver(this);
	}


	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponent#closeComponent()
	 */
	@Override
	public void closeComponent() {
		seicheNetworkDaq.removeSlowObserver(this);
	}


	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponent#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getUniqueName() {
		return uniqueName;
	}


	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}


	@Override
	public String getFrameTitle() {
		return "Seiche DAQ";
	}
	
	
}
