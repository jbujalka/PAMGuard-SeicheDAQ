package seichedaq.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import seichedaq.SeicheNetworkDaq;

public class CAPTDataBlock extends PamDataBlock {

	private SeicheNetworkDaq seicheNetworkDaq;

	public CAPTDataBlock(SeicheNetworkDaq seicheNetworkDaq) {
		super(CAPTDataUnit.class, "Seiche Daq System", null, 0);
		this.seicheNetworkDaq = seicheNetworkDaq;
	}

}
