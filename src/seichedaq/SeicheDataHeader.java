package seichedaq;

/**
 * Structure read from the top of every data packet. <p>
 * This includes the official header information plus
 * the error flags and the end marker characters from the end 
 * of the data packet. 
 * @author Doug Gillespie
 *
 */
public class SeicheDataHeader {
	
	public char[] startMark; // characters marking the start of the packet '*S'
	public int streamerId; // streamer Id
	public int secondsCounter; // seconds counter
	public long packetCount; // packet incremental counter
	public long sCycleSounter; 
	public int sdCycleCounter; // slow data cyclel counter (0 - 79)
	public byte syncByte;
	public int nSamplePoints; // number of samples in data (12)
	public int nPhones; // number of channels (32)
	public int sampleLength; // length of each sample in bytes (3)
	public int adcResolution; // bit depth of the ADCs (20)
	public short sampleRate; // sample rate (48000)
	public int preampGain; // premap gain - fixed preamp gain of 24, not used
	public int gainSettings; // selected additional gain
	public short lowCutFilter; // low cut filter (Hz)
	public int dgbyteLength; // total number of bytes per datagram
	public char[] endHeadMark; // marker characters for the end of the header '*H'
	public int errorFlags; // TLA Board Status flags - not strictly part of the header, but easiest to store here. 
	public char[] endMarker; // '*EEE'
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Packet %d, gain %ddB, set gain %ddB, filter %dHz, Error flags 0X%4X", 
				packetCount, preampGain, gainSettings, lowCutFilter, errorFlags);
	}
	
	/**
	 * Have any of the gain or filter settings changed compared to another header object ? 
	 * @param o Other DataHeader object. 
	 * @return true if they are the same.
	 */
	public boolean equals(SeicheDataHeader o) {
		if (o == null) return false;
		if (o.gainSettings != gainSettings) return false;
		if (o.preampGain != preampGain) return false;
		if (o.lowCutFilter != lowCutFilter) return false;
		if (o.errorFlags != errorFlags) return false;
		
		return true;
	}

}
