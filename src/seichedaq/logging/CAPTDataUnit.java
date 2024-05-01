package seichedaq.logging;

import java.util.Hashtable;

import PamguardMVC.PamDataUnit;
import seichedaq.slowdata.DAQDATANAMES;

public class CAPTDataUnit extends PamDataUnit {

	private Hashtable<DAQDATANAMES, Number> captData;
	private int channel;

	public CAPTDataUnit(long timeMilliseconds, int channel, Hashtable<DAQDATANAMES, Number> captData) {
		super(timeMilliseconds);
		this.channel = channel;
		this.captData = captData;
	}

	/**
	 * @return the captData
	 */
	public Hashtable<DAQDATANAMES, Number> getCaptData() {
		return captData;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

}
