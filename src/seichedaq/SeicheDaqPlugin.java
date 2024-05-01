package seichedaq;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.DaqSystemInterface;

public class SeicheDaqPlugin implements DaqSystemInterface {

	private String jarFileName;
	
	@Override
	public String getDefaultName() {
		return "Seiche Network DAQ";
	}

	@Override
	public String getHelpSetName() {
		return null;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFileName = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFileName;
	}

	@Override
	public String getDeveloperName() {
		return "Seiche Sound and Vibration Measurements";
	}

	@Override
	public String getContactEmail() {
		return "http://www.seiche.com/";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "1.15.09";
	}

	@Override
	public String getPamVerTestedOn() {
		return "1.15.09";
	}

	@Override
	public String getAboutText() {
		return "Proprietory DAQ system developed for Seiche Measurement Ltd, www.seiche.com";
	}

	@Override
	public DaqSystem createDAQControl(AcquisitionControl acquisitionControl) {
		return new SeicheNetworkDaq(acquisitionControl);
	}

}
