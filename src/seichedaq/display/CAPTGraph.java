package seichedaq.display;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Hashtable;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import Layout.PamFramePlots;
import PamView.LineKeyItem;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.SymbolKeyItem;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamguardMVC.PamDataBlock;
import seichedaq.SeicheNetworkDaq;
import seichedaq.logging.CAPTDataBlock;
import seichedaq.logging.CAPTDataUnit;
import seichedaq.slowdata.DAQDATANAMES;
import seichedaq.slowdata.SlowDataHandler;

public class CAPTGraph {

	private JPanel mainPanel;
	private PamAxisPanel axisPanel;
	
	private String graphName, axisName;
	private double[] defaultRange;
	private boolean autoScale;
	private PamAxis xAxis, yAxis;
	private PlotPanel plotPanel;
	private SeicheNetworkDaq seicheNetworkDaq;
	private int graphNumber;
	private long updateTime;
	private DAQDATANAMES dataName;
	
	/**
	 * Constructor to make a graph for a single channel with one or more data items to plot
	 * @param seicheNetworkDaq
	 * @param graphNumber
	 * @param graphName
	 * @param axisName
	 * @param defaultRange
	 */
	public CAPTGraph(SeicheNetworkDaq seicheNetworkDaq, int graphNumber, String graphName, String axisName, double[] defaultRange) {
		super();
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.graphNumber = graphNumber;
		this.graphName = graphName;
		this.axisName = axisName;
		this.defaultRange = defaultRange;
	
		buildPanel();
	}
	
	/**
	 * Constructor to construct a single plot, but on all channels. 
	 * @param seicheNetworkDaq
	 * @param dataName
	 * @param graphName
	 * @param axisName
	 * @param defaultRange
	 */
	public CAPTGraph(SeicheNetworkDaq seicheNetworkDaq, DAQDATANAMES dataName, String graphName, String axisName, double[] defaultRange) {
		super();
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.dataName = dataName;
		this.graphNumber = -1;
		this.graphName = graphName;
		this.axisName = axisName;
		this.defaultRange = defaultRange;
	
		buildPanel();
	}
	
	private void buildPanel() {
		axisPanel = new PamAxisPanel();
		xAxis = new PamAxis(0, 1, 0, 1, -seicheNetworkDaq.getSeicheDaqParams().graphDuration, 0, 
				PamAxis.BELOW_RIGHT, "Time (s)", PamAxis.LABEL_NEAR_CENTRE, "%d");
		yAxis = new PamAxis(0, 1, 0, 1, defaultRange[0], defaultRange[1], PamAxis.ABOVE_LEFT, axisName, PamAxis.LABEL_NEAR_CENTRE, "%d");
		yAxis.setInterval((defaultRange[1]-defaultRange[0])/4.);
		axisPanel.setWestAxis(yAxis);
		axisPanel.setSouthAxis(xAxis);
		axisPanel.setInnerPanel(plotPanel = new PlotPanel());
		axisPanel.setMinNorth(10);
		axisPanel.setMinEast(10);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder(graphName));
		mainPanel.add(axisPanel);
		
		if (graphNumber < 0) {
			makeChannelKey();
		}
	}
	
	/**
	 * Make a simple pamkey. 
	 * @param keyNames
	 */
	public void setKeyItems(String[] keyNames) {
		if (keyNames == null) {
			plotPanel.setKeyPanel(null);
			return;
		}
		KeyPanel keyPanel = new KeyPanel(graphName, 0);
		for (int i = 0; i < keyNames.length; i++) {
			Color col = PamColors.getInstance().getWhaleColor(i+1);
			keyPanel.add(new LineKeyItem(col, keyNames[i]));
		}
		plotPanel.setKeyPanel(keyPanel);
	}
	
	private void makeChannelKey() {
		KeyPanel keyPanel = new KeyPanel("Key", 0);
		for (int i = 0; i < SlowDataHandler.NCAPT; i++) {
			Color col = PamColors.getInstance().getWhaleColor(i+1);
			String str = String.format("CAPT %d", i);
			keyPanel.add(new LineKeyItem(col, str));
		}
		plotPanel.setKeyPanel(keyPanel);
	}

	public JComponent getComponent() {
		return mainPanel;
	}
	
	private class PlotPanel extends JPanelWithPamKey {

		private static final float LINEWIDTH = 1;

		public PlotPanel() {
			super();
			setBackground(Color.white);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(LINEWIDTH, BasicStroke.CAP_ROUND,	BasicStroke.JOIN_MITER));
			if (graphNumber >= 0) {
				// draw a single graph
				drawGraph(g, graphNumber);
			}
			else {
				/**
				 * draw a set of graphs, one for each channel. 
				 */
				for (int i = 0; i < SlowDataHandler.NCAPT; i++) {
					drawGraph(g, i);
				}
			}
			g2d.setStroke(oldStroke);
		}

		/**
		 * Draw graphs for a single CAPT channel. 
		 * @param g
		 * @param captChannel
		 */
		private void drawGraph(Graphics g, int captChannel) {
			checkTimeScale();
			
			CAPTDataBlock captDataBlock = seicheNetworkDaq.getCaptDataBlock();
			if (captDataBlock == null) return;
			Double[] prevValues = null;
			Double[] values;
			CAPTDataUnit captDataUnit;
			int x, y1, y2, lastX = 0;
			
//			System.out.println("Draw CAPT with n entries " + captDataBlock.getUnitsCount());
			synchronized (captDataBlock) {
				ListIterator<CAPTDataUnit> it = captDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
				while (it.hasPrevious()) {
					captDataUnit = it.previous();
					if (captDataUnit.getChannel() != captChannel) continue;
					double t = (captDataUnit.getTimeMilliseconds()-updateTime) / 1000.;
					x = (int) xAxis.getPosition(t);
					values = getDataValues(captDataUnit.getCaptData());
					if (prevValues != null) {
						for (int i = 0; i < prevValues.length; i++) {
							if (prevValues[i] == null || values[i] == null) {
								continue;
							}
							y1 = (int) yAxis.getPosition(prevValues[i]);
							y2 = (int) yAxis.getPosition(values[i]);
							g.setColor(PamColors.getInstance().getWhaleColor(captChannel+1));
							g.drawLine(lastX, y1, x, y2);
						}
					}
					lastX = x;
					prevValues = values;
				}
			}
		}

		private void checkTimeScale() {
			if (seicheNetworkDaq.getSeicheDaqParams().graphDuration != (int) -xAxis.getMinVal()) {
				xAxis.setMinVal(-seicheNetworkDaq.getSeicheDaqParams().graphDuration);
				axisPanel.repaint();
			}
		}
	}
	
	public void updateGraph(long updateTime) {
		this.updateTime = updateTime;
		plotPanel.repaint();
	}
	
	/**
	 * Get one or more values to plot. If values are null, then
	 * they won't get drawn. Easy !
	 * @param hashtable hashtable containing a single channel of CAPT data
	 * @return array of one or more values to plot. 
	 */
	protected Double[] getDataValues(Hashtable<DAQDATANAMES, Number> hashtable) {
		if (dataName == null) {
			return null;
		}
		Double[] values = new Double[1];
		values[0] = (Double) hashtable.get(dataName);
		return values;
	}

	/**
	 * @return the graphName
	 */
	public String getGraphName() {
		return graphName;
	}

	/**
	 * @return the axisName
	 */
	public String getAxisName() {
		return axisName;
	}

	/**
	 * @return the graphNumber
	 */
	public int getGraphNumber() {
		return graphNumber;
	}
}
