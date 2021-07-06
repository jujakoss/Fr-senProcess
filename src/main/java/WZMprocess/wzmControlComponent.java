package WZMprocess;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class wzmControlComponent extends SimpleControlComponent implements ControlComponentChangeListener  {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(wzmControlComponent.class);

public static final String OPMODE_BASIC = "BSTATE";
public static final String OPMODE_OP_EXE = "EXECUTE_SERVICE"; /* to fix #### */

private Iwzm wzm;

public wzmControlComponent(Iwzm wzm) {
	this.wzm = wzm;
	addControlComponentChangeListener(this);
}

@Override
public void onChangedExecutionState(ExecutionState newExecutionState) {
	logger.info("wzmControlComponent: new execution state: " + newExecutionState);
	if (newExecutionState == ExecutionState.EXECUTE) {
		if (this.getOperationMode().equals(OPMODE_OP_EXE)) {
			wzm.executeService();
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
