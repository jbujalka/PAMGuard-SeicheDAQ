package seichedaq.display;

import java.util.Hashtable;

import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;

public class HeadingGraph extends CAPTGraph {

	private static final double[] defaultRange = {0., 360.};
//	private static final String[] names = {"Heading", "Pitch", "Roll"};
	
	public HeadingGraph(SeicheNetworkDaq seicheNetworkDaq, int graphNumber) {
		super(seicheNetworkDaq, DAQDATANAMES.COMPASS1HEADING, "Heading", "degrees", defaultRange);
//		setKeyItems(names);
	}

//	@Override
//	protected Double[] getDataValues(Hashtable<DAQDATANAMES, Number> hashtable) {
//		Double[] values = new Double[1];
//		values[0] = (Double) hashtable.get(DAQDATANAMES.COMPASS1HEADING);
////		if (values[0] != null) {
////			values[0] = PamUtils.PamUtils.constrainedAngle(values[0], 180);
////		}
////		values[1] = (Double) hashtable.get(DAQDATANAMES.COPMPASS1PITCH);
////		values[2] = (Double) hashtable.get(DAQDATANAMES.COMPASS1ROLL);
//		return values;
//	}

}
