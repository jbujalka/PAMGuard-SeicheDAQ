package seichedaq.logging;

import java.sql.Types;
import java.util.Hashtable;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import seichedaq.SeicheNetworkDaq;
import seichedaq.slowdata.DAQDATANAMES;

public class CAPTSqlLogging extends SQLLogging {

	private CAPTDataBlock captDataBlock;
	private SeicheNetworkDaq seicheNetworkDaq;
	private PamTableItem captChannel;
	private PamTableDefinition captTableDef;

	public CAPTSqlLogging(SeicheNetworkDaq seicheNetworkDaq, CAPTDataBlock captDataBlock) {
		super(captDataBlock);
		this.seicheNetworkDaq = seicheNetworkDaq;
		this.captDataBlock = captDataBlock;
		captTableDef = new PamTableDefinition("CAPTData", SQLLogging.UPDATE_POLICY_OVERWRITE);
		captTableDef.addTableItem(captChannel = new PamTableItem("Channel", Types.INTEGER));
		captTableDef.addTableItem(new CAPTTableItem("NodeId", DAQDATANAMES.NODEID, Types.INTEGER));
		captTableDef.addTableItem(new CAPTTableItem("Gain", DAQDATANAMES.GAIN, Types.INTEGER));
		captTableDef.addTableItem(new CAPTTableItem("Filter", DAQDATANAMES.FILTER, Types.INTEGER));
		captTableDef.addTableItem(new CAPTTableItem("Pressure", DAQDATANAMES.PRESSURE, Types.DOUBLE));
		captTableDef.addTableItem(new CAPTTableItem("PressureTemp", DAQDATANAMES.PRESSURETEMPERATURE, Types.DOUBLE));
		captTableDef.addTableItem(new CAPTTableItem("Temperature", DAQDATANAMES.TEMPERATURE, Types.DOUBLE));
		captTableDef.addTableItem(new CAPTTableItem("Heading", DAQDATANAMES.COMPASS1HEADING, Types.DOUBLE));
		captTableDef.addTableItem(new CAPTTableItem("Pitch", DAQDATANAMES.COMPASS1PITCH, Types.DOUBLE));
		captTableDef.addTableItem(new CAPTTableItem("Roll", DAQDATANAMES.COMPASS1ROLL, Types.DOUBLE));
		setTableDefinition(captTableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		CAPTDataUnit captDataUnit = (CAPTDataUnit) pamDataUnit;
		Hashtable<DAQDATANAMES, Number> captData = captDataUnit.getCaptData();
		int captChan = captDataUnit.getChannel();
		captChannel.setValue(captChan);
		int n = captTableDef.getTableItemCount();
		for (int i = 0; i < n; i++) {
			PamTableItem tableItem = captTableDef.getTableItem(i);
			if (tableItem.getClass() != CAPTTableItem.class) {
				continue;
			}
			CAPTTableItem ct = (CAPTTableItem) tableItem;
			ct.setValue(captData.get(ct.dataName));
		}
		
	}
	
	private class CAPTTableItem extends PamTableItem {

		private DAQDATANAMES dataName;

		public CAPTTableItem(String name, DAQDATANAMES dataName, int sqlType) {
			super(name, sqlType);
			this.dataName = dataName;
		}
		
	}

}
