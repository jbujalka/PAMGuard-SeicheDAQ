package seichedaq.display;

import java.util.Hashtable;

import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;

public class PressureGraph extends CAPTGraph {
	
	private static double[] range = {0.,10};
	private static String[] dataNames = {"Pressure"};

	public PressureGraph(SeicheNetworkDaq seicheNetworkDaq, int graphNumber) {
		super(seicheNetworkDaq, graphNumber, "Pressure", "bar", range);
		setKeyItems(dataNames);
	}

	@Override
	protected Double[] getDataValues(Hashtable<DAQDATANAMES, Number> hashtable) {
		Double[] values = new Double[1];
		values[0] = (Double) hashtable.get(DAQDATANAMES.PRESSURE);
		return values;
	}

}
