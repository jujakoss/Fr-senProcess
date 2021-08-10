package ovenScript;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control Component for controlling the oven. Has an additional operation mode named HEAT.
 * This is a "black-box" example for a control component for the HandsOn.
 */
public class OvenControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
        private static final Logger logger = LoggerFactory.getLogger(OvenControlComponent.class);
 
	public static final String OPMODE_BASIC = "BSTATE";
	public static final String OPMODE_HEAT = "HEAT";
 
	private Oven oven;
 
	public OvenControlComponent(Oven oven) {
		this.oven = oven;
		addControlComponentChangeListener(this);
	}
 
	@Override
	public void onChangedExecutionState(ExecutionState newExecutionState) {
		logger.info("OvenControlComponent: new execution state: " + newExecutionState);
		if (newExecutionState == ExecutionState.EXECUTE) {
			if (this.getOperationMode().equals(OPMODE_HEAT)) {
				controlHeater();
			} else {
				setExecutionState(ExecutionState.COMPLETE.getValue());
			}
		}
	}
 
	protected void controlHeater() {
		new Thread(() -> {
			for (int i = 0; i < 50; i++) {
				if (oven.getSensor().readTemperature() < 30.0d) {
					oven.getHeater().activate();
				} else {
					oven.getHeater().deactivate();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			oven.getHeater().deactivate();
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