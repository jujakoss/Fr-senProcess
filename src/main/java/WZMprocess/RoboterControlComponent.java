package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoboterControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(RoboterControlComponent.class);

	//public static final String OPMODE_BASIC = "BSTATE";
	public static final Object OPMODE_LOAD = "LOADING_PIECE";
	public static final Object OPMODE_UNLOAD= "UNLOADING_PIECE";

	private IRoboter roboter;

	public RoboterControlComponent(Roboter roboter) {
		this.roboter = roboter;
		addControlComponentChangeListener(this);
	}

	
	
	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("roboterControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_LOAD)) {
				loadBt();
			} else if (this.getOperationMode().equals(OPMODE_UNLOAD)) {
				unloadBt();
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}

	
	
	
	
	protected void loadBt() {
		new Thread(() -> { roboter.getTask().load(); } 
		).start();
	}
	
	protected void unloadBt() {
		new Thread(() -> { roboter.getTask().unload(); } 
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