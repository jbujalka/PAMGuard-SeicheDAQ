package seichedaq.slowdata;

public class DoubleDataThing extends SlowDataThing<Double> {

	private double dataLow, dataHigh;
	private int rangeLow, rangeHigh;
	
	private int nErrors = 0;

	/**
	 * Get a floating point number with an fixed number of 4 chars of data
	 * @param name Name of the data thing
	 * @param refByte Byte number for the identifying character (counting from 0)
	 * @param refChar identifying character
	 * @param rangeLow low range of integer values
	 * @param rangeHigh high range of integer values
	 * @param dataLow low range of converted double values
	 * @param dataHigh high range of converted double values. 
	 */
	public DoubleDataThing(DAQDATANAMES name, int refByte, char refChar, boolean isSigned, int rangeLow, int rangeHigh, double dataLow, double dataHigh) {
		super(name, refChar, refByte, 4, isSigned);
		this.rangeLow = rangeLow;
		this.rangeHigh = rangeHigh;
		this.dataLow = dataLow;
		this.dataHigh = dataHigh;
	}
	/**
	 * Get a floating point number with an optional number of chars (2,3 or 4)
	 * @param name Name of the data thing
	 * @param refByte Byte number for the identifying character (counting from 0)
	 * @param refChar identifying character
	 * @param nChars number of bytes to read
	 * @param isSigned underlying hex data are a signed integer (not unsigned)
	 * @param rangeLow low range of integer values
	 * @param rangeHigh high range of integer values
	 * @param dataLow low range of converted double values
	 * @param dataHigh high range of converted double values. 
	 */
	public DoubleDataThing(DAQDATANAMES name, int refByte, char refChar, int nChars, boolean isSigned, int rangeLow, int rangeHigh, double dataLow, double dataHigh) {
		super(name, refChar, refByte, nChars, isSigned);
		this.rangeLow = rangeLow;
		this.rangeHigh = rangeHigh;
		this.dataLow = dataLow;
		this.dataHigh = dataHigh;
	}
	
	public Double getValue(byte[] dataArray) {
		double intVal = 0;
		try {
			intVal = getRawValue(dataArray);
		} catch (SlowDataUnavailableException e) {
			if (nErrors++ < 2) {
				System.out.println(getName().toString() + ": " + e.getMessage());
			}
			return null;
		}
		double doubleVal = (intVal - (double) rangeLow) / (double) (rangeHigh-rangeLow) * (dataHigh-dataLow) + dataLow;
		return doubleVal;
	}

}
