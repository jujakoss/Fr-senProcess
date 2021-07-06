package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class roboterControlComponent extends SimpleControlComponent implements ControlComponentChangeListener  {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(roboterControlComponent.class);

public static final String OPMODE_BASIC = "BSTATE";
public static final String OPMODE_OP = "OP"; /* to fix #### */

private Roboter roboter;

public roboterControlComponent(Roboter roboter) {
	this.roboter = roboter;
	addControlComponentChangeListener(this);
}

@Override
public void onChangedExecutionState(ExecutionState newExecutionState) {
	logger.info("roboterControlComponent: new execution state: " + newExecutionState);
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


