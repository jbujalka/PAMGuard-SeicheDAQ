package seichedaq.slowdata;
/**
 * Pointer to some type of slow data. 
 * @author dg50
 *
 */
public abstract class SlowDataThing<T extends Number> {
	private int refByte; // byte number of marker before the data. 
	private int nChars; // number of bytes (2 or 4)
	private byte refChar; //  reference character identifying the data
	private DAQDATANAMES name; // name of the data thing. 
	private boolean isSigned = false;
	private int maxSignedValue;
	private int maxUnsignedValue;
	/**
	 * @param refByte
	 * @param refChar
	 */
	public SlowDataThing(DAQDATANAMES name, char refChar, int refByte, int nChars, boolean isSigned) {
		super();
		this.name = name;
		this.refChar = (byte) refChar;
		this.refByte = refByte;
		this.nChars = nChars;
		this.isSigned = isSigned;
		maxUnsignedValue = 1<<(nChars*4);
		int nBits = nChars*4-1;
		maxSignedValue = (1<<nBits)-1;
	}
	
	/**
	 * Return a value for the data thing
	 * @param dataArray raw CAPT or DAQ data array
	 * @return Value or null if identifier is not found
	 */
	abstract public T getValue(byte[] dataArray);

	/**
	 * Get the raw value, currently as an unsigned integer value
	 * @param dataArray CAPT or DAQ data array
	 * @return Int value
	 * @throws SlowDataUnavailableException thrown if identifier character is not found. 
	 */
	public int getRawValue(byte[] dataArray) throws SlowDataUnavailableException {
		if (dataArray[refByte] != refChar) {
			throw new SlowDataUnavailableException(String.format("Slow data %c not found (got %c(0x%x)) at byte %d", 
					refChar, dataArray[refByte], dataArray[refByte], refByte));
		}
//		int intData = 0;
//		for (int i = 0, j = nBytes-1; i < nBytes; i++, j--) {
//			intData &= (dataArray[refByte+i+1]<<(8*j));
//		}
//		return intData;
		/**
		 * Raw data are in a hext format. Need to 
		 * pull out the appropriate characters and read the string into an integer value. 
		 */
		String hexString = new String(dataArray, refByte+1, nChars);
		try {
			int intVal = Integer.parseInt(hexString, 16);
			if (isSigned && intVal > maxSignedValue) {
				intVal -= maxUnsignedValue;
			}
			return intVal;
		}
		catch (NumberFormatException e) {
			throw new SlowDataUnavailableException(String.format("Slow data %c: cannot read hex string %s", refChar, hexString));
		}
	}

	/**
	 * @return the refByte
	 */
	public int getRefByte() {
		return refByte;
	}

	/**
	 * @return the nBytes
	 */
	public int getnBytes() {
		return nChars;
	}

	/**
	 * @return the refChar
	 */
	public byte getRefChar() {
		return refChar;
	}

	/**
	 * @return the name
	 */
	public DAQDATANAMES getName() {
		return name;
	}
	
}
