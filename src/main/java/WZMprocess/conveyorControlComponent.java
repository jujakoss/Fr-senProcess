package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class conveyorControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(conveyorControlComponent.class);

	public static final String OPMODE_BASIC = "BSTATE";
	public static final String OPMODE_OP = "SENSOR_STATUS_CHANGED"; // sensor detected an element

	private IConveyor conveyor;

	public conveyorControlComponent(Conveyor conveyor) {
		this.conveyor = conveyor;
		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("conveyorControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_OP)) {
				controlMotor();
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	protected void controlMotor() {

		/* operations to control the Motor */

		new Thread(() -> {
			if (conveyor.getSensor().readStatus()) {
				conveyor.getMotor().stop();
			} else {
				conveyor.getMotor().start();
			}
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