package WZMprocess2;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MyControlComponent.class);

	public static final String OPMODE_BASIC = "BSTATE";
	public static final String OP_MODE_MILLING = "MILLING";

	private Conveyor conveyor;
	private Roboter roboter;
	private Wzm wzm;

	public MyControlComponent(Roboter roboter, Conveyor conveyor, Wzm wzm) {
		this.conveyor = conveyor;
		this.roboter = roboter;
		this.wzm = wzm;

		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("ConveyorControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OP_MODE_MILLING)) {
				processSequence();
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	protected void processSequence() {
		new Thread(() -> {

			for (int i = 0; i < 20; i++) { // time of the test process in seconds

				if (!conveyor.getSensor().readState()) {
					conveyor.getMotor().activate();

				} else {

					conveyor.getMotor().deactivate();

					roboter.getTask().loadPart();

					conveyor.getMotor().activate();

					wzm.getService().applyService();

					roboter.getTask().unloadPart();

				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			System.out.println("_____________");
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