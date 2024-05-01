package seichedaq;

import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
//import java.io.ByteArrayOutputStream;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import seichedaq.display.SeicheDisplayProvider;
import seichedaq.logging.CAPTDataBlock;
import seichedaq.logging.CAPTDataUnit;
import seichedaq.logging.CAPTSqlLogging;
import seichedaq.logging.DatabaseLogger;
import seichedaq.slowdata.SlowData;
import seichedaq.slowdata.SlowDataHandler;
import userDisplay.UserDisplayControl;
import wavFiles.ByteConverter;
import wavFiles.ByteConverterAifInt24;

/**
 * Main class to handle acquisition of audio data into PAMGuard from a Seiche
 * Networked DAQ device.
 * 
 * @author dg50
 *
 */
public class SeicheNetworkDaq extends DaqSystem implements PamSettings {

	private AcquisitionControl acquisitionControl; // ref to meain acquisition

	private static final String SEICHENAME = "Seiche Streamer Daq"; // name to
																	// apear in
																	// dialogs.

	protected static final int FIXEDSAMPLERATE = 48000; // fixed sample rate

	private static final int SAMPLESPERDATAUNIT = 4800; // number of samples per
														// data unit.

	protected static final int MAXCHANNELS = 32; // fixed number of channels

	protected static final int[] LOWCUTFILTER = { 10, 100, 1000, 2000 }; // fixed
																			// filter
																			// values

	protected static final int[] GAINS = { 24, 36, 48, 60, 72, 84, 96 }; // fixed
																			// gain
																			// values

	protected static final double PEAKTOPEAKVOLTAGE = 4.096; // this is the correct value.
	
	private double scale20bit = 16.; // adc convert assumes 24 bit, but it's really only 20 

	private SeicheDAQDialog seicheDAQDialog; // reference to the dialog panel
												// for the acquisition dialog

	private SeicheDaqParams seicheDaqParams = new SeicheDaqParams(); // params
																		// controlling
																		// the
																		// readout

	private Thread daqThread; // thread reading the daq

	private volatile boolean continueDaq; // flag used to stop the daqThread

	private SlowDataHandler slowDataHandler = new SlowDataHandler(); // handler for reassembling slow data

	long packetCounter = 0; // data packet counter

	/**
	 * List of incoming datagrams. On arrival, datagrams immediately go into a
	 * queue so that the Network socket can receive the next packet. A separate
	 * thread unpacks the datagrams.
	 */
	private List<DatagramPacket> datagramQueue;

	/**
	 * Byte converter to convert 24 bit data into doubles.
	 */
	private ByteConverter byteConverter;

	/**
	 * Raw 24 bit integer data from the daq
	 */
	private byte[] acousticBytes = new byte[1152];

	/**
	 * Raw double data, divided up by channel.
	 */
	private double[][] acousticDoubles = new double[32][12];

	/**
	 * Main acquisition audio queue. New units of faw data get sent here.
	 */
	private AudioDataQueue newDataUnitList;

	private volatile long lastPacketMillis = 0; // arrival time of last Datagram
												// packet.

	private volatile boolean useAudioData; // flag to say to actually use audio
											// data (i.e. send to the rest of
											// PAMGuard)

	/**
	 * Data units being built, one per channel. With only 12 samples per
	 * datagram, many are collected until there is 1/10s of data per channel to
	 * send off into PAMGuard.
	 */
	private RawDataUnit[] prepDataUnits;

	/**
	 * Number of samples stacked in the prepDataUnits
	 */
	private int[] stackedSamples; // count of samples stacked up for each
									// individual channels data unit.

	/**
	 * Dummy data full of zeros to insert into the output data streams when
	 * packets have been missed.
	 */
	private double[][] missedPacketData; // dummy data array for missed packets.

	/**
	 * Total count of samples, including missed packets.
	 */
	private long totalSampleCount; // sample count for all channels (must be the
									// same for all)

	private PacketWarningHandler packetWarningHandler;
	
	private ArrayList<SlowObserver> slowObservers = new ArrayList<>();
	
	private CAPTDataBlock captDataBlock;
	
	private DatabaseLogger captDatabaseLogger;

	public SeicheNetworkDaq(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		datagramQueue = Collections.synchronizedList(new LinkedList<DatagramPacket>());
		byteConverter = new ByteConverterAifInt24();
		packetWarningHandler = new PacketWarningHandler(this);
		PamSettingManager.getInstance().registerSettings(this);
		UserDisplayControl.addUserDisplayProvider(new SeicheDisplayProvider(this));
		captDataBlock = new CAPTDataBlock(this);
		captDataBlock.SetLogging(new CAPTSqlLogging(this, captDataBlock));
		captDatabaseLogger = new DatabaseLogger(captDataBlock);
		captDataBlock.setNaturalLifetime(seicheDaqParams.graphDuration);
	}

	@Override
	public String getUnitName() {
		return acquisitionControl.getUnitType();
	}

	@Override
	public String getUnitType() {
		return SEICHENAME;
	}

	@Override
	public Serializable getSettingsReference() {
		return seicheDaqParams;
	}

	@Override
	public long getSettingsVersion() {
		return SeicheDaqParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		seicheDaqParams = ((SeicheDaqParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public String getSystemType() {
		return SEICHENAME;
	}

	@Override
	public String getSystemName() {
		return SEICHENAME;
	}

	/**
	 * 
	 * @return reference to DaqDialog component
	 */
	private SeicheDAQDialog getDaqDialog() {
		return seicheDAQDialog;
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		// have to make it here so it can get a reference to the
		// acquisitionDialog
		if (seicheDAQDialog == null) {
			seicheDAQDialog = new SeicheDAQDialog(this, acquisitionControl, acquisitionDialog);
		}
		return getDaqDialog().getComponent();
	}

	@Override
	public void dialogSetParams() {
		getDaqDialog().setParams();
	}

	@Override
	public boolean dialogGetParams() {
		boolean ans = getDaqDialog().getParams();
		if (ans == true) {
			sendDaqConfiguration();
		}
		return ans;
	}

	@Override
	public int getMaxSampleRate() {
		return DaqSystem.PARAMETER_FIXED;
	}

	@Override
	public int getMaxChannels() {
		return DaqSystem.PARAMETER_FIXED;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return DaqSystem.PARAMETER_UNKNOWN;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		
		captDatabaseLogger.checkTable();
		
		newDataUnitList = daqControl.getAcquisitionProcess().getNewDataQueue();
		prepDataUnits = new RawDataUnit[MAXCHANNELS];
		totalSampleCount = 0; // total sample counter
		stackedSamples = new int[MAXCHANNELS]; // bookkeeping for individual channels.
		/*
		 * pinging doesn't work. how do we find if the device is there or not ?
		 * Since we acquire anyway, it should be apparent that a packet has
		 * arrived recently, do return true if a packet arrived in the last
		 * second, waiting for up to a second for a packet to arrive.
		 */
		for (int i = 0; i < 100; i++) {
			if (System.currentTimeMillis() - lastPacketMillis < 1000) {
				return true;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Seiche DAQ device not available");
		return true;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		useAudioData = true;
		return true;
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		useAudioData = false;
	}

	@Override
	public boolean isRealTime() {
		return true;
	}
	
	public boolean showOptions(Window parent) {
		SeicheDaqParams newParams = SeichOptionsDialog.showDialog(parent, this);
		if (newParams != null) {
			seicheDaqParams = newParams;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return false;
	}

	@Override
	public int getDataUnitSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDeviceName() {
		return SEICHENAME;
	}

	/**
	 * @return the seicheDaqParams
	 */
	public SeicheDaqParams getSeicheDaqParams() {
		return seicheDaqParams;
	}

	@Override
	public void setSelected(boolean select) {
		if (select) {
			startDaqLoop();
		} else {
			stopDaqLoop();
		}
	}

	/**
	 * Stop the data acquisition loop.
	 */
	private void stopDaqLoop() {
		continueDaq = false;
		if (daqThread == null) {
			return;
		}
		// try for up to 2 seconds (100 tries with 20ms pause each time)
		for (int i = 0; i < 100; i++) {
			if (daqThread.isAlive()) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		daqThread = null;
	}

	/**
	 * Start the acquisition loop. Two threads are started, <br>
	 * the first acquires data and sends it into an internal queue <br>
	 * the second reads the internal queue, unpacks the data and sends it off to
	 * PAMGuard (if it's wanting it)
	 */
	private void startDaqLoop() {
		continueDaq = true;
		datagramQueue.clear();
		daqThread = new Thread(new DaqLoop());
		daqThread.start();
		daqThread.setPriority(Thread.MAX_PRIORITY);
		// start a different thread to unpack the data.
		new Thread(new UnpackerLoop()).start();
	}

	/**
	 * Thread to start the loop to unpack data.
	 * 
	 * @author dg50
	 *
	 */
	class UnpackerLoop implements Runnable {
		@Override
		public void run() {
			try {
				unpackData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Thread to start to loop to acquire data
	 * 
	 * @author dg50
	 *
	 */
	class DaqLoop implements Runnable {
		@Override
		public void run() {
			try {
				sendDaqConfiguration();
				acquireData();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send the configuration to the daq system.
	 */
	private boolean sendDaqConfiguration() {
		InetAddress in4 = null;
		try {
			in4 = InetAddress.getByName(seicheDaqParams.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println(e.getLocalizedMessage());
			return false;
//			e.printStackTrace();
		}
		byte[] configData = writeConfigData();
		DatagramPacket dgPacket = new DatagramPacket(configData, configData.length, in4, seicheDaqParams.txPort);
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.send(dgPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("Seiche configuration sent to daq on %s port %d\n", in4, seicheDaqParams.txPort);
		// dgPacket.setAddress(seicheDaqParams.hostAddress);
		return true;
	}

	/**
	 * Pack configuration data to send to the DAQ
	 * 
	 * @return byte array of packed data.
	 */
	private byte[] writeConfigData() {
		ByteOutputStream bos;
		DataOutputStream dos = new DataOutputStream(bos = new ByteOutputStream(36));
		try {
			dos.writeByte('S'); // 1
			dos.writeByte('*');
			dos.writeInt(0x00010000);
			dos.writeByte('*');
			dos.writeByte('H');
			dos.writeLong(0); // 9 packet counter 8 bytes. 
			dos.writeShort(0); // 17 - six spare bytes.
			dos.writeShort(0);
			dos.writeShort(0);
			dos.writeShort(seicheDaqParams.lowCutFilter); // 23 lc filter
			dos.writeInt(0); // 25
			dos.writeByte(seicheDaqParams.gain); // 29 gain value.
			dos.writeByte(1); // set packet counter 30
			dos.writeByte(1); // set seconds counter
			dos.writeByte(0); // 32 unused, set 0
			dos.writeByte(1); // 33 set filter flag
			dos.writeByte(1); // 34 set gain flag
			dos.writeShort(0); // 35 - 36 unused, set 0

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.getBytes();
	}

	/**
	 * Reads the data from an internal queue - allows the thread reading the
	 * network socket to return as fast as possible without doing any unpacking.
	 * doing it this ways does however mean more object creation - will see
	 * which is more stable.
	 * 
	 * @throws IOException
	 */
	private void unpackData() throws IOException {
		packetCounter = -1;
		long missedPackets = 0;
		int nRX = 0;
		long tic = System.nanoTime();
		DatagramPacket dgPacket;
		boolean first = true;
		SeicheDataHeader prevHeader = null;
		long displayUpdate = 0;
		while (continueDaq) {
			if (datagramQueue.size() == 0) {
				try {
					Thread.sleep(10);
					continue; // loop around until data exist.
				} catch (InterruptedException e) {
				}
			}
			dgPacket = datagramQueue.remove(0);

			SeicheDataHeader dataHeader = unpackDataObject(dgPacket, packetCounter);
			long now = System.currentTimeMillis();
			if (now - displayUpdate > 2000) {
				displayUpdate = now;
				broadcastDaqHeader(dataHeader);
			}
			if (first || !dataHeader.equals(prevHeader)) {
				checkInetAddress(dgPacket.getAddress());
				System.out.printf("Data received from %s\n", dgPacket.getAddress());
				System.out.println(dataHeader.toString());
				first = false;
				broadcastDaqHeader(dataHeader);
			}
			prevHeader = dataHeader;
			long packetJump = dataHeader.packetCount - packetCounter - 1;
			if (packetCounter >= 0 && packetJump != 0) {
				if (packetJump < 1) {
					System.out.printf("Packet count jump %d counts to %d\n", packetJump, dataHeader.packetCount);
				}
				slowDataHandler.packetSequenceError();
				if (packetJump > 0) {
					missedPackets += packetJump;
				}
				packetWarningHandler.updateBadCount((int) packetJump);
			}
			packetCounter = dataHeader.packetCount;
			nRX++;
			packetWarningHandler.updateGoodCounts(1);
		}
		double toc = System.nanoTime() - tic;
		double millisOfData = (nRX * 12) / 48;
		double millisOfData2 = ((nRX + missedPackets) * 12) / 48;
		double percentMissed = (double) (missedPackets) / ((double) (missedPackets + nRX)) * 100.;
		System.out.printf("%d packets %3.2fs (%3.2fs with missed %3.4f%% packets) received in %3.2f secs\n", nRX,
				millisOfData / 1000., millisOfData2 / 1000., percentMissed, toc / 1.e9);
	}


	/**
	 * Check to see if this address is already in our list of devices. 
	 * @param address
	 */
	private void checkInetAddress(InetAddress address) {
//		System.out.println("UDP data received from " + address.getHostAddress());
		// put into the parameters list at position 2 (just in case it's not
		// the one we want !)
		String currenAddd = seicheDaqParams.getHostAddress();
		if (currenAddd.equals(address.getHostAddress())) {
			return;
		}
		seicheDaqParams.addHostAddress(address.getHostAddress(), 1);
	}

	/**
	 * Loop to acquire UDP packets.
	 * 
	 * @throws IOException
	 */
	public void acquireData() throws IOException {
		int packetLength = 1324;
		int socketTimeout = 1000;
		DatagramSocket socket = new DatagramSocket(seicheDaqParams.rxPort);
		socket.setSoTimeout(socketTimeout);
		DatagramPacket dgPacket = new DatagramPacket(new byte[packetLength], packetLength);
		System.out.println("Acquiring data on port " + seicheDaqParams.rxPort);
		while (continueDaq) {
			try {
				socket.receive(dgPacket);
			} catch (SocketTimeoutException e) {
				socket.close();
				socket = null;
				// System.out.println("Socket timeout");
				socket = new DatagramSocket(seicheDaqParams.rxPort);
				socket.setSoTimeout(socketTimeout);
				continue;
			}
			datagramQueue.add(dgPacket);
			lastPacketMillis = System.currentTimeMillis();
			dgPacket = new DatagramPacket(new byte[packetLength], packetLength);
		}
		if (socket != null) {
			socket.close();
		}
	}

	/**
	 * Unpack a datagram received from the daq.
	 * 
	 * @param dgPacket
	 *            Datagram packet
	 * @param prevPacketCounter
	 *            previous packet counter
	 * @return Header information. Acoustic data get sent off from within this
	 *         function.
	 * @throws IOException
	 */
	private SeicheDataHeader unpackDataObject(DatagramPacket dgPacket, long prevPacketCounter) throws IOException {
		byte[] data = dgPacket.getData();
		int dataLen = dgPacket.getLength();

		// the header (40 bytes) ...
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		SeicheDataHeader dh = unpackDataHeader(dis);

		// System.out.printf("Unpacking data object length %d, slow cycle point
		// %d\n", dataLen, dh.sdCycleCounter);
		SlowData slowData = slowDataHandler.unpackSlowData(dh, dis);
		if (slowData != null) {
			useSlowData(slowData);
		}

		// get the error flags - may as well add these to the header.
		dh.errorFlags = dis.readInt();

		// now the acoustic data.
		dataLen = dh.sampleLength * dh.nSamplePoints * dh.nPhones;
		if (missedPacketData == null) {
			// make a dummy packet of data to handle missed samples.
			missedPacketData = new double[dh.nPhones][dh.nSamplePoints];
		}
		if (acousticBytes.length != dataLen) {
			acousticBytes = new byte[dataLen];
		}
		/*
		 * Check allocation of double data array. 
		 */
		if (acousticDoubles.length != dh.nPhones || acousticDoubles[0].length != dh.nSamplePoints) {
			acousticDoubles = new double[dh.nPhones][dh.nSamplePoints];
		}
		dis.read(acousticBytes);
		byteConverter.bytesToDouble(acousticBytes, acousticDoubles, dataLen);
		/*
		 * Now scale up a bit more since above assumed true 24 bit data, but reality 
		 * is that data are packed into only 20 bits
		 * This is required for correct data calibration. 
		 */
		for (int i = 0; i < acousticDoubles.length; i++) {
			for (int j = 0; j < acousticDoubles[0].length; j++) {
				acousticDoubles[i][j] *= scale20bit;
			}
		}

		// if there were some missed packets, write them here.
		if (packetCounter > 0) {
			long missedPackets = dh.packetCount - prevPacketCounter - 1;
			for (int i = 0; i < missedPackets; i++) {
				useAcousticData(missedPacketData);
			}
		}
		// now the read data.
		useAcousticData(acousticDoubles);

		dh.endMarker = readChars(dis, 4);

		return dh;
	}

	private long lastLogTime = 0;
	/**
	 * stack the acoustic data up into data units and send off to the rest of
	 * PAMGuard.
	 * 
	 * @param acousticData
	 */
	private void useAcousticData(double[][] acousticData) {
		// quit if the flag to say that PAMGuard is actually running is not set.
		if (useAudioData == false) {
			return;
		}
		int chanMap = seicheDaqParams.channelMap;
		int iUsedChan = 0;
		int nChan = acousticData.length;
		// only use the selected channels.
		for (int i = 0; i < nChan; i++) {
			if ((chanMap & 1 << i) != 0) {
				useAcousticData(acousticData[i], iUsedChan++);
			}
		}
		// increment the total sample count for all channels .
		totalSampleCount += acousticData[0].length;
	}

	/**
	 * Stack acoustic data from a single channel. Note that iChan is now the
	 * number of the output channel which may no longer be the original channel
	 * number
	 * 
	 * @param ds
	 *            acoustic data (should be an array of 12 samples)
	 * @param iChan
	 *            channel index.
	 */
	private void useAcousticData(double[] ds, int iChan) {
		if (prepDataUnits[iChan] == null) {
			// we need to make a new data unit for this channel.
			long timeMilliseconds = acquisitionControl.getAcquisitionProcess()
					.absSamplesToMilliseconds(totalSampleCount);
			prepDataUnits[iChan] = new RawDataUnit(timeMilliseconds, 1 << iChan, totalSampleCount, SAMPLESPERDATAUNIT);
			prepDataUnits[iChan].setRawData(new double[SAMPLESPERDATAUNIT]);
			stackedSamples[iChan] = 0;
		}
		double[] data = prepDataUnits[iChan].getRawData();
		for (int i = 0, j = stackedSamples[iChan]; i < ds.length; i++, j++) {
			data[j] = ds[i];
		}
		stackedSamples[iChan] += ds.length;
		if (stackedSamples[iChan] == SAMPLESPERDATAUNIT) {
			// force the amplitude calculation
			prepDataUnits[iChan].setRawData(data, true);
			// send data off to PAMGuard.
			newDataUnitList.addNewData(prepDataUnits[iChan]);
			// set the reference to null so that a new one gets made with the
			// correct millisecond time.
			prepDataUnits[iChan] = null;
		}
	}

	/**
	 * Unpack the header data from the packed data in the datagram.
	 * 
	 * @param dis
	 *            Data input stream wrapped around the byte data.
	 * @return New header object.
	 * @throws IOException
	 */
	private SeicheDataHeader unpackDataHeader(DataInputStream dis) throws IOException {
		SeicheDataHeader dh = new SeicheDataHeader();
		dh.startMark = readChars(dis, 2);
		dh.streamerId = dis.readUnsignedShort();
		dh.secondsCounter = dis.readInt();
		dh.packetCount = dis.readLong();
		dh.sCycleSounter = dis.readLong();
		dh.sdCycleCounter = dis.readUnsignedByte();
		dh.syncByte = dis.readByte();
		dh.nSamplePoints = dis.readUnsignedByte();
		dh.nPhones = dis.readUnsignedByte();
		dh.sampleLength = dis.readUnsignedByte();
		dh.adcResolution = dis.readUnsignedByte();
		dh.sampleRate = dis.readShort();
		dh.preampGain = dis.readUnsignedByte();
		dh.gainSettings = dis.readUnsignedByte();
		dh.lowCutFilter = dis.readShort();
		dh.dgbyteLength = dis.readUnsignedShort();
		dh.endHeadMark = readChars(dis, 2);
		return dh;
	}

	/**
	 * Read a number of one byte chars into a char array.
	 * 
	 * @param dis
	 *            data input stream
	 * @param nChars
	 *            number of characters to read
	 * @return character array
	 * @throws IOException
	 */
	char[] readChars(DataInputStream dis, int nChars) throws IOException {
		char[] charData = new char[nChars];
		for (int i = 0; i < nChars; i++) {
			charData[i] = (char) dis.readByte();
		}
		return charData;
	}
	
	public void addSlowObserver(SlowObserver slowObserver) {
		slowObservers.add(slowObserver);
	}

	public void removeSlowObserver(SlowObserver slowObserver) {
		slowObservers.remove(slowObserver);
	}

	private void useSlowData(SlowData slowData) {
		/**
		 * Need to make one data unit per CAPT channel
		 */
		long now = System.currentTimeMillis();
		if (now-lastLogTime > seicheDaqParams.databseLoggingInterval*1000) {
			captDataBlock.setShouldLog(true);
			lastLogTime = now;
		}
			
		for (int i = 0; i < SlowDataHandler.NCAPT; i++) {
			CAPTDataUnit captDataUnit = new CAPTDataUnit(now, i, slowData.getCaptData(i));
			captDataBlock.addPamData(captDataUnit);
//			if (now-lastLogTime > seicheDaqParams.databseLoggingInterval*1000) {
//				
//				captDatabaseLogger.logData(captDataUnit);
//			}
		}
		captDataBlock.setShouldLog(false);
		acquisitionControl.getAcquisitionProcess().addOutputDataBlock(captDataBlock);
		broadcastSlowData(slowData); // sends to displays
//		logSlowData(slowData);
	}
	
//	private void logSlowData(SlowData slowData) {
//		if (now-lastLogTime < seicheDaqParams.databseLoggingInterval*1000) {
//			return; // too soon !
//		}
//		lastLogTime = now;
//	}

	private void broadcastDaqHeader(SeicheDataHeader dataHeader) {
		for (SlowObserver s:slowObservers) {
			s.daqHeadChange(dataHeader);
		}
	}

	private void broadcastSlowData(SlowData slowData) {
		for (SlowObserver s:slowObservers) {
			s.newSlowData(slowData);
		}
	}

	/**
	 * @return the packetWarningHandler
	 */
	public PacketWarningHandler getPacketWarningHandler() {
		return packetWarningHandler;
	}

	/**
	 * @return the captDataBlock
	 */
	public CAPTDataBlock getCaptDataBlock() {
		return captDataBlock;
	}
}
