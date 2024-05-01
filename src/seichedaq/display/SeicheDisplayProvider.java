package seichedaq.display;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import seichedaq.SeicheNetworkDaq;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

/**
 * Display provider for CAPT information - makes CAPT displays available 
 * in the user display areas. 
 * @author dg50
 *
 */
public class SeicheDisplayProvider implements UserDisplayProvider {

	private SeicheNetworkDaq seicheNetworkDaq;
	
	/**
	 * @param seicheNetworkDaq
	 */
	public SeicheDisplayProvider(SeicheNetworkDaq seicheNetworkDaq) {
		super();
		this.seicheNetworkDaq = seicheNetworkDaq;
	}

	@Override
	public String getName() {
		return seicheNetworkDaq.getUnitType();
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new SeicheDaqDisplayPanel(seicheNetworkDaq, this, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return SeicheDaqDisplayPanel.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

}
