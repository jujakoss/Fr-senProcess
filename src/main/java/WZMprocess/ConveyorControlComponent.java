package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConveyorControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ConveyorControlComponent.class);

	//public static final String OPMODE_BASIC = "BSTATE";
	public static final Object OPMODE_SENSOR_BLOCKED = "ACTION_Bauteil_IN";
	public static final Object OPMODE_SENSOR_FREE = "ACTION_Bauteil_OUT";

	private IConveyor conveyor;

	public ConveyorControlComponent(Conveyor conveyor) {
		this.conveyor = conveyor;
		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("conveyorControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_SENSOR_BLOCKED)) {
				stopMotor();
			} else if (this.getOperationMode().equals(OPMODE_SENSOR_FREE)) {
				startMotor();
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	protected void startMotor() {
		new Thread(() -> { conveyor.getMotor().activate(); } 
		).start();
	}
	
	protected void stopMotor() {
		new Thread(() -> { conveyor.getMotor().deactivate(); } 
		).start();
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