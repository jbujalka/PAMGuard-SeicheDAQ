package seichedaq;

import seichedaq.slowdata.SlowData;

/**
 * Interface for things wanting to observe data changes in the 
 * slow data. 
 * @author dg50
 *
 */
public interface SlowObserver {

	public void daqHeadChange(SeicheDataHeader seicheDataHeader);
	
	public void newSlowData(SlowData slowData);
}
