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

public class ConveyorControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ConveyorControlComponent.class);
	
	public static void startMyControlComponent(Conveyor conveyor) {

		ControlComponent cc = new ConveyorControlComponent(conveyor);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
		System.out.println("Conveyor Control Component server started");
	}

	// public static final String OPMODE_BASIC = "BSTATE";
	public static final Object OPMODE_SENSOR = "CONVEYOR_ON_OFF";

	private IConveyor conveyor;

	public ConveyorControlComponent(Conveyor conveyor) {
		this.conveyor = conveyor;
		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("conveyorControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_SENSOR)) {
				controlMotor();
			}
			setExecutionState(ExecutionState.COMPLETE.getValue());
		}
	}

	protected void controlMotor() {
		new Thread(() -> {

			if (conveyor.getSensor().readStatus() == true) {
				conveyor.getMotor().deactivate();
			}

			else {
				conveyor.getMotor().activate();
			}
			conveyor.getMotor().deactivate();
			setExecutionState(ExecutionState.COMPLETE.getValue());
		}).start();
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