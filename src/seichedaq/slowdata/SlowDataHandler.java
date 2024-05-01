package seichedaq.slowdata;

import java.io.DataInputStream;
import java.io.IOException;

import seichedaq.SeicheDataHeader;

/**
 * Class to handle repackaging of slow data from the DAQ
 * @author dg50
 *
 */
public class SlowDataHandler {

	private byte[][] daqDataArray = new byte[NDAQ][DAQSIZE];
	private byte[][] captDataArray = new byte[NCAPT][CAPTSIZE];
	private byte[] freshData = new byte[48];
	private int slowDataPackets;
	private int daqPos, captPos;
	private static final int SDSIZE = 3200;
	private static final int SDPACKETSIZE = 48;
	public static final int NDAQ = 16;
	public static final int NCAPT = 4;
	private static final int DAQSIZE = 160;
	private static final int CAPTSIZE = 320;
	private static final int DAQCHUNKSIZE = 2;
	private static final int CAPTCHUNKSIZE = 4;
	
	private int slowDataInterval = 1000;
	
	int nComplete = 0;
	private long lastSlowDataTime;
	

	/**
	 * Called for each datagram packet with the datainput stream
	 * aligned to the start of the slow data. 
	 * @param dh Data header stucture already read from the same datagram
	 * @param dis data input stream wrapping the byte data. 
	 * @return A new SLowData object if a cycle was completed on this call, or null. 
	 * @throws IOException
	 */
	public SlowData unpackSlowData(SeicheDataHeader dh, DataInputStream dis) throws IOException {
		if (dh.sdCycleCounter == 0 || daqPos >= DAQSIZE) { // protect against wraps if packets are missed.
			daqPos = captPos = slowDataPackets = 0;
			forceAllValues(daqDataArray, (byte) 1);
		}
		int bytesRead = dis.read(freshData);
		if (bytesRead != SDPACKETSIZE) {
			return null;
		}
		int packetPos = 0;
		// loop through the sequence of copying bytes of data into 
		// their appropriate places in the 16 daqDataArrays and in the 4 CAPT arrays 
		for (int l = 0; l < 2; l++) { // read sequence repeats twice. 
			// copy a single byte of each DAQ section
			for (int i = 0; i < NDAQ; i++) {
				daqDataArray[i][daqPos] = freshData[packetPos++];
			}
			daqPos += 1;
			// copy two bytes for each capt. 
			for (int b = 0; b < 2; b++) {
				for (int i = 0; i < NCAPT; i++) {
					captDataArray[i][captPos] = freshData[packetPos++];
				}
				captPos += 1;
			}
		}
		slowDataPackets++;
		/**
		 * Two things need to happen before we can unpack data. First
		 * we need to be on packet count 79, but we also have to have 
		 * received exactly 80 packets - which will not be the case on 
		 * the first cycle and will also not be the case if packets 
		 * were lost. 
		 */
		if (dh.sdCycleCounter == 0x4f && slowDataPackets == 80) {
			if (++nComplete == 2) {
				dumpDataArray("Daq data array", daqDataArray);
				dumpDataArray("CAPT Data Array", captDataArray);
			}
			long now = System.currentTimeMillis();
			if (now - lastSlowDataTime >= slowDataInterval) {
				lastSlowDataTime = now;
				return unpackSlowData();
			}
		}
		return null;
	}
	
	private void forceAllValues(byte[][] dataArray, byte ch) {
		int nCol = dataArray.length;
		int nRow = dataArray[0].length;
		for (int r = 0; r < nRow; r++) {
			for (int c = 0; c < nCol; c++) {
				dataArray[c][r] = ch;
			}
		}		
	}

	private void dumpDataArray(String title, byte[][] dataArray) {
		int nCol = dataArray.length;
		int nRow = dataArray[0].length;
		String str;
		if (title != null) {
			System.out.println(title);
		}
		for (int r = 0; r < nRow; r++) {
			str = String.format("%3d", r);
			for (int c = 0; c < nCol; c++) {
				str += String.format(" %4d('%c')",  dataArray[c][r],  dataArray[c][r]);
			}
			System.out.println(str);
		}
	}
	/**
	 * Warning received from main DAQ that packets were lost<p>
	 * If the 80th packet 
	 * was missed for some reason, then the above code which 
	 * repacks the slow data will overrun the buffers. So just reset 
	 * everything and it should sort itself out..  
	 */
	public void packetSequenceError() {
		daqPos = captPos = slowDataPackets = 0;
	}

	/**
	 * Unpack the information in the slow data arrays into something more useful
	 * @param slowDataArray
	 * @return Object containing all slow data
	 */
	private SlowData unpackSlowData() {
		// make a hash table for each thing ?		
		SlowData sd = new SlowData();
		for (int i = 0; i < NDAQ; i++) {
			sd.addDaqData(i, daqDataArray[i]);
		}
		for (int i = 0; i < NCAPT; i++) {
			sd.addCaptData(i, captDataArray[i]);
		}
		
		return sd;
	}
	
}
