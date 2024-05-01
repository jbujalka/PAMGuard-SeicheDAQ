package seichedaq;

import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Manage warnings about missed UDP packets. 
 * @author dg50
 *
 */
public class PacketWarningHandler {

	
	private PamWarning packetWarning; // warning message for missed packets. 
	
	private int packetWarningInterval = 5*4000; // update packet warning every n seconds
	
	private int lastWarningMillis = 0; // time of last warning update. 
	
	private int goodPacketCount = 0;
	
	private int missedPacketCount = 0;
	
	public PacketWarningHandler(SeicheNetworkDaq seicheNetworkDaq) {
		packetWarning = new PamWarning(seicheNetworkDaq.getDeviceName(), "Warning", 0);
	}
	
	public void updateGoodCounts(int goodPackets) {
		goodPacketCount += goodPackets;
		if (goodPacketCount%packetWarningInterval == 0) {
			reportPacketWarning();
		}
	}
	
	private void reportPacketWarning() {
		if (missedPacketCount == 0) {
			WarningSystem.getWarningSystem().removeWarning(packetWarning);
		}
		else {
			double misPerc = ((double) missedPacketCount * 100) / (missedPacketCount+goodPacketCount);
			String warnString = String.format("%d missing packets (%3.3f%%) in %d seconds", missedPacketCount, misPerc, 5);
			packetWarning.setWarningMessage(warnString);
			packetWarning.setWarnignLevel(missedPacketCount <= 2 ? 1 : 2);
			WarningSystem.getWarningSystem().addWarning(packetWarning);
		}
		missedPacketCount = goodPacketCount = 0;
	}

	public void updateBadCount(int badPackets) {
		missedPacketCount += badPackets;
	}

	/**
	 * @return the goodPacketCount
	 */
	public int getGoodPacketCount() {
		return goodPacketCount;
	}

	/**
	 * @return the missedPacketCount
	 */
	public int getMissedPacketCount() {
		return missedPacketCount;
	}
	

}
