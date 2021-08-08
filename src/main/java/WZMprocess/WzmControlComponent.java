package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WzmControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(WzmControlComponent.class);

	public static final String OPMODE_BASIC = "BSTATE";
	public static final String OPMODE_EXCUTE = "EXECUTE_SERVICE";

	private Iwzm wzm;
	
	
	public WzmControlComponent(Iwzm wzm) {
		this.wzm = wzm;
		addControlComponentChangeListener(this);
	}
	
	public static void startMyControlComponent(Wzm wzm) {

		ControlComponent cc = new WzmControlComponent(wzm);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
		System.out.println("Wzm Control Component server started");
	}
	
	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("wzmControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_EXCUTE)) {
				wzm.executeService(); // Calls method to excute the service needed
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	@Override
	public void onVariableChange(String varName, Object newValue) {
	}

	@Override
	public void onNewOccupier(String occupierId) {
	}

	@Override
	public void onNewOccupationState(OccupationState state) {
	}

	@Override
	public void onChangedExecutionMode(ExecutionMode newExecutionMode) {
	}

	@Override
	public void onChangedOperationMode(String newOperationMode) {
	}

	@Override
	public void onChangedWorkState(String newWorkState) {
	}

	@Override
	public void onChangedErrorState(String newWorkState) {
	}

}
