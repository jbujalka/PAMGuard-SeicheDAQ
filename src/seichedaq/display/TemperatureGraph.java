package seichedaq.display;

import java.util.Hashtable;

import PamUtils.LatLong;
import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;

public class TemperatureGraph extends CAPTGraph {
	
	private static double[] range = {0., 50};
	private static String[] dataNames = {"Temp", "Temp (from pressure sens)"};

	public TemperatureGraph(SeicheNetworkDaq seicheNetworkDaq, int graphNumber) {
		super(seicheNetworkDaq, graphNumber, "Temperature", "Temp " + LatLong.deg + "C", range);
		setKeyItems(dataNames);
	}

	@Override
	protected Double[] getDataValues(Hashtable<DAQDATANAMES, Number> hashtable) {
		Double[] values = new Double[2];
		values[0] = (Double) hashtable.get(DAQDATANAMES.TEMPERATURE);
		values[1] = (Double) hashtable.get(DAQDATANAMES.PRESSURETEMPERATURE);
		return values;
	}

}
