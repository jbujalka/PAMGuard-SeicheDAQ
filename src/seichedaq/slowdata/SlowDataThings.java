package seichedaq.slowdata;

import java.util.ArrayList;

/**
 * A list of all the references to all the 
 * different types of CAPT and DAQ data. 
 * @author dg50
 *
 */
public class SlowDataThings {
	
	private static ArrayList<SlowDataThing> daqDataThings = new ArrayList<>();
	private static ArrayList<SlowDataThing> captDataThings = new ArrayList<>();
	

	static {
		// daq things ...
		daqDataThings.add(new IntDataThing(DAQDATANAMES.SERIALNUMBER, 'U', 6, 4));
		daqDataThings.add(new DoubleDataThing(DAQDATANAMES.SUPPLYPLUS, 11, '+', 3, false, 0, 4095, 0, 16.5));
		daqDataThings.add(new DoubleDataThing(DAQDATANAMES.SUPPLYMINUS, 15, '-', 3, false, 0, 4095, 0, 16.5));
		daqDataThings.add(new IntDataThing(DAQDATANAMES.GAIN, 'G', 19, 2));
		daqDataThings.add(new IntDataThing(DAQDATANAMES.FILTER, 'F', 22, 4));
		
		// capt things
		captDataThings.add(new IntDataThing(DAQDATANAMES.NODEID, 'N', 3, 2));
		captDataThings.add(new IntDataThing(DAQDATANAMES.SERIALNUMBER, 'U', 6, 4));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.SUPPLYPLUS, 11, '+', 3, false, 0, 4095, 0, 16.5));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.SUPPLYMINUS, 15, '-', 3, false, 0, 4095, 0, 16.5));
		captDataThings.add(new IntDataThing(DAQDATANAMES.GAIN, 'G', 19, 2));
		captDataThings.add(new IntDataThing(DAQDATANAMES.FILTER, 'F', 22, 4));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.PRESSURE, 29, 'P', false, 16384, 49152, 0, 30));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.PRESSURETEMPERATURE, 34, 't', false,  384, 32768, -50, 50));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.TEMPERATURE, 45, 'T', false, 0, 2047, -10, 60));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COMPASS1HEADING, 56, 'h', false, 0, 3600, 0, 360));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COMPASS1PITCH, 61, 'p', true, -1800, 1800, -180, 180));
		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COMPASS1ROLL, 66, 'r', true, -1800, 1800, -180, 180));
//		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COMPASS2HEADING, 155, 'h', false, 0, 3600, 0, 360));
//		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COPMPASS2PITCH, 160, 'p', true, -1800, 1800, -180, 180));
//		captDataThings.add(new DoubleDataThing(DAQDATANAMES.COMPASS2ROLL, 165, 'r', true, -1800, 1800, -180, 180));
		
		
	}

	/**
	 * @return the daqDataThings
	 */
	public static ArrayList<SlowDataThing> getDaqDataThings() {
		return daqDataThings;
	}

	/**
	 * @return the captDataThings
	 */
	public static ArrayList<SlowDataThing> getCaptDataThings() {
		return captDataThings;
	}
}
