package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import WZMprocess.Conveyor;
import WZMprocess.Wzm;

public class RoboterControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(RoboterControlComponent.class);

	// public static final String OPMODE_BASIC = "BSTATE";
	public static final Object OPMODE_LOAD_UNLOAD = "LOADING_UNLOAD_PIECE";

	private IRoboter roboter;
	private IConveyor conveyor;
	private Iwzm wzm;

	public RoboterControlComponent(Roboter roboter) {
		this.roboter = roboter;
		addControlComponentChangeListener(this);
	}

	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("roboterControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_LOAD_UNLOAD)) {
				moveBt();
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	
	protected void moveBt() {
		
		new Thread(() -> {

			if (conveyor.getSensor().readStatus() == true ||wzm.State == READY) // use multiple
																									// variables for the
																									// state of the WZM
			{
				roboter.getTask().load();
				logger.info("Loading component");
			}

			else if (conveyor.getSensor().readStatus() == false || wzm.getSensor().readStatus() == WORKING) {
				logger.info("Waiting for a Component ... ");
			}

			else if (wzm.getSensor().readStatus() == WORKING) {
				logger.info("Machine tool is occupied");
			}

			else if (wzm.getSensor().readStatus() == OUT_OF_ORDER) {
				logger.warn("Machine tool is out out of order !");
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