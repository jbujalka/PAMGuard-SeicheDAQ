package seichedaq.slowdata;

public class IntDataThing extends SlowDataThing<Integer> {

	private int nErrors = 0;
	public IntDataThing(DAQDATANAMES name, char refChar, int refByte, int nBytes) {
		super(name, refChar, refByte, nBytes, false);
	}

	public IntDataThing(DAQDATANAMES name, char refChar, int refByte, int nBytes, boolean isSigned) {
		super(name, refChar, refByte, nBytes, isSigned);
	}

	@Override
	public Integer getValue(byte[] dataArray) {
		int intVal = 0;
		try {
			intVal = getRawValue(dataArray);
		} catch (SlowDataUnavailableException e) {
			if (nErrors++ < 2) {
				System.out.println(getName().toString() + ": " + e.getMessage());
			}
			return null;
		}
		return intVal;
	}

}
