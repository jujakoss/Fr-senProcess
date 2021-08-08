package WZMprocess2;

import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scenario1 {

	private static final Logger logger = LoggerFactory.getLogger(Scenario1.class);

	public static void main(String[] args) throws Exception {

		Conveyor Conveyor1 = new Conveyor();
		Roboter Roboter1 = new Roboter();
		Wzm Wzm1 = new Wzm();

		startMyControlComponent(Roboter1, Conveyor1, Wzm1);

		VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));

		proxy.setValue("STATUS/OPMODE", MyControlComponent.OP_MODE_MILLING);

		proxy.invokeOperation("OPERATIONS/START");

		logger.info("Using the control component to start the control of the conveyor");

		for (int i = 0; i < 200; i++) { // checking state (once every 100 millisecond for 20 seconds)
			Thread.sleep(100);
			String currentState = (String) proxy.getValue("STATUS/EXST");
			if (currentState.equals(ExecutionState.COMPLETE.getValue())) {

				proxy.invokeOperation("OPERATIONS/RESET");
				break;
			}
		}

	}

	public static void startMyControlComponent(Roboter roboter, Conveyor conveyor, Wzm wzm) {
		ControlComponent cc = new MyControlComponent(roboter, conveyor, wzm);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
	}

}