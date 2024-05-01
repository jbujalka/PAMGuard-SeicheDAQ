package seichedaq.display;

public class QuickValue {
	private DataValue dataValue;
	private DataLabel dataLabel;
	private String dataUnits;
	private String doubleFormat = "%3.2f";
	public QuickValue(String label, int fieldLength, String dataUnits) {
		dataLabel = new DataLabel(label);
		dataValue = new DataValue(fieldLength);
		this.dataUnits = dataUnits;
		dataValue.setEditable(false);
	}
	public void setText(Number txt) {
		if (txt == null) {
			dataValue.setText("-");
			return;
		}
		Class<? extends Number> cls = txt.getClass();
		if (cls == Integer.class) {
			dataValue.setText(String.format("%d %s", (Integer) txt, dataUnits));
		}
		else if (cls == Double.class) {
			dataValue.setText(String.format("%3.2f %s", (Double) txt, dataUnits));
		}
		else {
			dataValue.setText(txt + " " + dataUnits);
		}
	}
	public void setIntText(int val) {
		dataValue.setText(String.format("%d", val));
	}
	public void setIntText(int val, String units) {
		dataValue.setText(String.format("%d %s", val, units));
	}
	public void setIntText(long val) {
		dataValue.setText(String.format("%d", val));			
	}
	/**
	 * @return the dataValue
	 */
	public DataValue getDataValue() {
		return dataValue;
	}
	/**
	 * @return the dataLabel
	 */
	public DataLabel getDataLabel() {
		return dataLabel;
	}
//	public void setText(Number iVal) {
//		if (iVal == null) {
//			dataValue.setText("-");
//		}
//		else {
//			dataValue.setText(iVal.toString());
//		}
//	}
}
