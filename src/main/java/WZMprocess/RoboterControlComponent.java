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


public class RoboterControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(RoboterControlComponent.class);

	// public static final String OPMODE_BASIC = "BSTATE";
	public static final Object OPMODE_LOAD_UNLOAD = "LOADING_UNLOAD_PIECE";

	private IRoboter roboter;
	private IConveyor conveyor;
	private Iwzm wzm;
	private RoboterTask roboterTask;
	
	public static void startMyControlComponent(Roboter roboter) {

		ControlComponent cc = new RoboterControlComponent(roboter);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
		System.out.println("Roboter Control Component server started");
	}
	
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

			switch (wzm.getState()) {
			case READY:
				if (conveyor.getSensor().readStatus() == true) {
					roboter.getTask().load();
					logger.info("Loading component");
					break;
				}
			case FINISHED:
				if (!roboterTask.readStatus()) {
					roboter.getTask().unload();
					break;}
					
			case WORKING:
				if (conveyor.getSensor().readStatus() == true) {
					logger.info("Machine tool is occupied");
					break;
				}

			case OUT_OF_ORDER:
				logger.warn("Machine tool is out out of order !");
				break;

			default:
				logger.warn("Waiting for a component ...");
				break;
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