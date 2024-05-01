package seichedaq.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;

/**
 * quick way of checking tables and adding data without having to 
 * use a registered PAmDataBlock. 
 * @author dg50
 *
 */
public class DatabaseLogger {

	private PamDataBlock pamDataBlock;
	private PamConnection checkedConnection;
	private boolean lastCheckResult = false;
	private DBProcess dbProcess;
	
	public DatabaseLogger(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
	}
	
	public boolean checkTable() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false; // no database so can't log. 
		}
		if (dbControl.getConnection() == checkedConnection) {
			return lastCheckResult;
		}
		dbProcess = dbControl.getDbProcess();
		lastCheckResult = dbProcess.checkTable(pamDataBlock.getLogging().getTableDefinition());
		checkedConnection = dbControl.getConnection();
		return lastCheckResult;
	}
	
	public boolean logData(PamDataUnit dataUnit) {
		if (checkTable() == false) {
			return false;
		}
		dbProcess.newData(pamDataBlock, dataUnit);
		return true;
	}
}
