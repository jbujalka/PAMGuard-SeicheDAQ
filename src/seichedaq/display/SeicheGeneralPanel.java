package seichedaq.display;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.PamGui;
import PamView.dialog.PamGridBagContraints;
import seichedaq.PacketWarningHandler;
import seichedaq.SeicheDataHeader;
import seichedaq.SeicheNetworkDaq;

/**
 * Panel for general display of DAQ information
 * @author dg50
 *
 */
public class SeicheGeneralPanel {

	private SeicheNetworkDaq seicheNetworkDaq;

	private QuickValue streamerId, secondsCount, packetCount, nPhones, adcResolution, sampleRate, gainSetting, lowCutFilter;
	private QuickValue lostPackets, lostPacketsPercent;
	private JButton optionsButton;
	private SeicheDaqDisplayPanel seicheDaqDisplayPanel;

	private JPanel genPanel;
	JPanel leftPanel;
	
	public SeicheGeneralPanel(SeicheNetworkDaq seicheNetworkDaq, SeicheDaqDisplayPanel seicheDaqDisplayPanel) {
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.seicheDaqDisplayPanel = seicheDaqDisplayPanel;
		leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(new TitledBorder("General Information"));
//		genPanel.setBorder(new TitledBorder("DAQ data information"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridy = 0;
		add(streamerId = new QuickValue("Streamer Id ", 5, ""),c);
		add(nPhones = new QuickValue(" Number of channels ", 5, ""),c);
		add(adcResolution = new QuickValue(" ADC Resolution ", 5, "bit"),c);
		add(sampleRate = new QuickValue(" Sample Rate ", 5, "Hz"),c);
		c.gridx = 0;
		c.gridy++;
		add(gainSetting = new QuickValue("Gain ", 5, "dB"),c);
		add(lowCutFilter = new QuickValue(" Low Cut Filter ", 5, "kHz"),c);
		add(secondsCount = new QuickValue(" Seconds Count ", 5, ""),c);
		add(packetCount = new QuickValue(" Packet Count ", 7, ""),c);
		c.gridx = 0;
		c.gridy++;
		add(lostPackets = new QuickValue("Lost Packets ", 5, ""),c);
		add(lostPacketsPercent = new QuickValue(" Percent lost ", 5, ""),c);
		c.gridx += 2;
		c.gridwidth = 2;
		leftPanel.add(optionsButton = new JButton("More Options ..."), c);
		optionsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptionsdialog();
			}
		});
		
		genPanel = new JPanel(new BorderLayout());
		genPanel.add(BorderLayout.WEST, leftPanel);
	}
	
	protected void showOptionsdialog() {
		seicheNetworkDaq.showOptions(null);		
	}

	private void add(QuickValue quickValue, GridBagConstraints c) {
		leftPanel.add(quickValue.getDataLabel(), c);
		c.gridx++;
		leftPanel.add(quickValue.getDataValue(), c);
		c.gridx++;
	}
	
	public JComponent getComponent() {
		return genPanel;
		
	}
	
	
	/**
	 * Update data display. 
	 * @param seicheDataHeader
	 */
	public void updateDaqHeader(SeicheDataHeader seicheDataHeader) {
		streamerId.setIntText(seicheDataHeader.streamerId);
		secondsCount.setIntText(seicheDataHeader.secondsCounter);
		packetCount.setIntText(seicheDataHeader.packetCount);
		nPhones.setIntText(seicheDataHeader.nPhones);
		adcResolution.setIntText(seicheDataHeader.adcResolution, "bit");
		sampleRate.setIntText(seicheDataHeader.sampleRate, "kHz");
		gainSetting.setIntText(seicheDataHeader.gainSettings, "dB");
		lowCutFilter.setIntText(seicheDataHeader.lowCutFilter, "Hz");
		PacketWarningHandler pw = seicheNetworkDaq.getPacketWarningHandler();
		long goodPackets = pw.getGoodPacketCount();
		long missed = pw.getMissedPacketCount();
		lostPackets.setIntText(missed);
		double perc = (100. * missed) / ((double) goodPackets + missed);
		lostPacketsPercent.setText(perc);
		
	}

}
