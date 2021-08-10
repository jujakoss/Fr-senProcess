package millingProcess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Control Component for controlling the conveyor. Has an additional operation mode named HEAT.
 * This is a "black-box" example for a control component for the HandsOn.
 */
public class ConveyorControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
        private static final Logger logger = LoggerFactory.getLogger(ConveyorControlComponent.class);

	public static final String OPMODE_BASIC = "BSTATE";
	public static final String OPMODE_OPERATION_CONVEYOR = "MOVE_PART";

	private Conveyor conveyor;
	
	public ConveyorControlComponent(Conveyor conveyor) {
		this.conveyor = conveyor;
		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("ConveyorControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_OPERATION_CONVEYOR)) {
				controlMotor();
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	protected void controlMotor() {
		new Thread(() -> {
			for (int i = 0; i < 50; i++) { 
				if (conveyor.getSensor().readState() == false) {
					conveyor.getMotor().activate();
				} else {
					conveyor.getMotor().deactivate();
				}
				try {
					Thread.sleep(1000); // time of excution x50 in seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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