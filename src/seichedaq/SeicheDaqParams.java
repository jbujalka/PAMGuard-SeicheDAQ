package seichedaq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parameters for the Seiche DAQ system. These get stored in 
 * psf files between runs. 
 * @author dg50
 *
 */
public class SeicheDaqParams implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	private static final int MAX_LISTED_ADDRESSES = 10;

	/*
	 * Low cut filter value in Hz.
	 */
	public int lowCutFilter = SeicheNetworkDaq.LOWCUTFILTER[0];
	
	/**
	 * Gain value in dB
	 */
	public int gain = SeicheNetworkDaq.GAINS[0];
	
	/**
	 * Channel map (initially 0xFFFFFFFF == -1 since no unsigned ints in Java)
	 * All channels are always read, but this is used to specify which ones 
	 * actually get used. 
	 */
	public int channelMap = -1;
	
	/**
	 * Port for receiving data from the DAQ
	 */
	public int rxPort = 60020;
	
	/**
	 * Network port to send configuration data to
	 */
	public int txPort = 6000;

	/**
	 * Current address of DAQ system. 
	 */
	private static final String DEFAULT_HOST_ADDRESS = "192.168.10.77";
	private ArrayList<String> recentHosts = new ArrayList<>();

	/**
	 * Interval for updating streamer data (depth, head, pitch, roll). 
	 */
	public int streamerUpdateInterval = 2;
	
	public int databseLoggingInterval = 10;
	
	public int graphDuration = 600; // seconds. 
//	
//	public boolean updateDepth = true;
//	
//	public boolean updateHeading = false;
//	
//	public boolean updatePitch = false;
//	
//	public boolean updateRoll = false;
//	
//	public int[] captToStreamer = new int[SlowDataHandler.NCAPT];
	/**
	 * 
	 */
	public SeicheDaqParams() {
		super();
		recentHosts.add(DEFAULT_HOST_ADDRESS);
//		for (int i = 0; i < SlowDataHandler.NCAPT; i++) {
//			captToStreamer[i] = i;
//		}
	}
	
	public String getHostAddress() {
		if (recentHosts.size() < 1) {
			return DEFAULT_HOST_ADDRESS;
		}
		return recentHosts.get(0);
	}
	
	public void setHostAddress(String hostAddress) {
		addHostAddress(hostAddress, 0);
	}
	public void addHostAddress(String hostAddress, int listPosition) {
		// first remove this address if it's already in the list. 
		Iterator<String> it = recentHosts.iterator();
		int n = 0;
		while (it.hasNext()) {
			String oldHost = it.next();
			n++;
			if (oldHost.equals(hostAddress) || n >= MAX_LISTED_ADDRESSES) {
				it.remove();
			}
		}
		// and add the new address at the start of the list
		listPosition = Math.min(recentHosts.size(), listPosition);
		recentHosts.add(listPosition, hostAddress);
	}

	/**
	 * 
	 * @return the index of the gain value. Useful when selecting from the
	 * drop down list in the dialog
	 */
	public int getGainIndex() {
		for (int i = 0; i < SeicheNetworkDaq.GAINS.length; i++) {
			if (SeicheNetworkDaq.GAINS[i] == gain) {
				return i;
			}
		}
		return 0;
	}
	
	/**
	 * Set the gain by index
	 * @param gainIndex the gain index. 
	 */
	public void setGainIndex(int gainIndex) {
		gain = SeicheNetworkDaq.GAINS[gainIndex];
	}
	/**
	 * 
	 * @return the index of the filter value. Useful when selecting from the
	 * drop down list in the dialog
	 */
	public int getFilterIndex() {
		for (int i = 0; i < SeicheNetworkDaq.LOWCUTFILTER.length; i++) {
			if (SeicheNetworkDaq.LOWCUTFILTER[i] == lowCutFilter) {
				return i;
			}
		}
		return 0;
	}
	
	/**
	 * Set the low cut filter by index
	 * @param gainIndex the filter index. 
	 */
	public void setFilterIndex(int filterIndex) {
		lowCutFilter = SeicheNetworkDaq.LOWCUTFILTER[filterIndex];
	}


	@Override
	protected SeicheDaqParams clone() {
		try {
			SeicheDaqParams sdp = (SeicheDaqParams) super.clone();
			if (sdp.recentHosts == null) {
				sdp.recentHosts = new ArrayList<>();
				sdp.recentHosts.add(DEFAULT_HOST_ADDRESS);
			}
			if (sdp.databseLoggingInterval == 0) {
				sdp.databseLoggingInterval = 10;
			}
			if (sdp.graphDuration == 0) {
				sdp.graphDuration = 600;
			}
			return sdp;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * @return the recentHosts
	 */
	public ArrayList<String> getRecentHosts() {
		return recentHosts;
	}
}
