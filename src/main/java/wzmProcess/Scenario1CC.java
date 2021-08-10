package wzmProcess;

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

/**
 * The control component provides an additional abstraction for native device handling and has a specified interface.
 * It can also be connected to the virtual automation bus.
 * For more information on control components, see:
 * https://wiki.eclipse.org/BaSyx_/_Documentation_/_API_/_ControlComponent
 * 
 * There, the VAB API is specified:
 * https://wiki.eclipse.org/BaSyx_/_Documentation_/_API_/_ControlComponent#Virtual_Automation_Bus_.28VAB.29_implementation
 * 
 * In this HandsOn, a given control component for the virtual (proprietary) conveyor is utilized via the VAB.
 * 
 * Expected console output in this HandsOn:
 * - state outputs from the ConveyorControlComponent
 * - conveyor is activated and deactivated multiple times (not manually, but automatically using the control component this
 * time)
 * - temperature values at ~30
 * - the conveyor cooling down after the control component is finished
 */
public class Scenario1CC {
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(Scenario1CC.class);
 
	public static void main(String[] args) throws Exception {
		// Create the virtual conveyor specific to this HandsOn
		Conveyor myConveyor = new Conveyor();
 
		// Write a function, that starts a control component for the virtual conveyor
		startMyControlComponent(myConveyor);
 
		// Connect to the control component, see service interface here
		// This code also shows how to directly connect to a known location without
		// using the Registry/ConnectionManager.
		// However, this assumes that the address of the Control Component will never
		// change
		VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));
 
		// Select the operation mode for heating
		proxy.setValue("STATUS/OPMODE", ConveyorControlComponent.OPMODE_OPERATION_CONVEYOR);
 
		// Start the selected operation in the control component
		proxy.invokeOperation("OPERATIONS/START");
		logger.info("Using the control component to start the OPERATION_CONVEYOR operation");
		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
			//logger.info("Current Status, Sensor is : " + myConveyor.getSensor().readState());
			// Return true, if the control component has completed its operation
			String currentState = (String) proxy.getValue("STATUS/EXST");
			if (currentState.equals(ExecutionState.COMPLETE.getValue())) {
				// Reset the control component
				proxy.invokeOperation("OPERATIONS/RESET");
				break;
			}
		}
 
	}
 
	public static void startMyControlComponent(Conveyor conveyor) {
		// Given is a local control component that can directly control the virtual conveyor device
		ControlComponent cc = new ConveyorControlComponent(conveyor);
 
		// Like the VAB model created before, the structure of the control component is a Map
		// Map ccModel = (Map) cc;
 
		// Create a server for the Control Component and provide it in the VAB (at port 4002)
		VABMapProvider ccProvider = new VABMapProvider(cc);
		// This time, a BaSyx-specific TCP interface is used.
		// Likewise, it is also possible to wrap the control component using a http servlet as before
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
	}
	}
