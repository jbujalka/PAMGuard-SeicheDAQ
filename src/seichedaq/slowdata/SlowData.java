package seichedaq.slowdata;

import java.util.Hashtable;
import java.util.List;

public class SlowData {

	private Hashtable<DAQDATANAMES, Number>[] daqData;
	private Hashtable<DAQDATANAMES, Number>[] captData;

	private boolean simData = false;
	private static double simHead, simPitch, simRoll;

	public  SlowData() {
		daqData = new Hashtable[SlowDataHandler.NDAQ];
		captData = new Hashtable[SlowDataHandler.NCAPT];
	}

	public void addDaqData(int i, byte[] dataArray) {
		Hashtable<DAQDATANAMES, Number> ht = new Hashtable<>();
		daqData[i] = ht;
		List<SlowDataThing> daqThings = SlowDataThings.getDaqDataThings();
		for (SlowDataThing sdt:daqThings) {
			Number val = sdt.getValue(dataArray);
			if (val != null) {
				ht.put(sdt.getName(), val);
			}
		}
	}
	
	public void addCaptData(int i, byte[] dataArray) {
		Hashtable<DAQDATANAMES, Number> ht = new Hashtable<>();
		captData[i] = ht;
		List<SlowDataThing> daqThings = SlowDataThings.getCaptDataThings();
		for (SlowDataThing sdt:daqThings) {
			Number val = sdt.getValue(dataArray);
			if (val != null) {
				ht.put(sdt.getName(), val);
			}
		}
		if (simData) {
			ht.put(DAQDATANAMES.COMPASS1HEADING, simHead++);
			if (simHead > 360) simHead -= 360;
		}
	}

	public Hashtable<DAQDATANAMES, Number> getDaqData(int daqChannel) {
		return daqData[daqChannel];
	}
	
	public Hashtable<DAQDATANAMES, Number> getCaptData(int captChannel) {
		return captData[captChannel];
	}

}
