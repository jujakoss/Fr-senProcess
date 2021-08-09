package WZMprocess2;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.AASLambdaPropertyHelper;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scenario1 {

	private static final Logger logger = LoggerFactory.getLogger(Scenario1.class);

	public static Submodel createConveyorModel(Conveyor conveyor) {

		Submodel conveyorSubmodel = new Submodel();

		conveyorSubmodel.setIdShort("Conveyor");

		conveyorSubmodel.setIdentification(new ModelUrn("Conveyor1"));

		Supplier<Object> lambdaReadFunction = () -> conveyor.getSensor().readState();

		Property dynamicConvProperty = new Property();

		dynamicConvProperty.setIdShort("ConveyorProperty");

		AASLambdaPropertyHelper.setLambdaValue(dynamicConvProperty, lambdaReadFunction, null);

		conveyorSubmodel.addSubmodelElement(dynamicConvProperty);

		Function<Object[], Object> activateFunction = (args) -> {
			conveyor.getMotor().activate();
			return null;
		};

		Operation activateOperation = new Operation(activateFunction);

		activateOperation.setIdShort("activateConveyor");

		conveyorSubmodel.addSubmodelElement(activateOperation);

		Function<Object[], Object> deactivateFunction = (args) -> {
			conveyor.getMotor().deactivate();
			return null;
		};

		Operation deactivateOperation = new Operation(deactivateFunction);

		deactivateOperation.setIdShort("deactivateConveyor");

		conveyorSubmodel.addSubmodelElement(deactivateOperation);

		return conveyorSubmodel;
	}

	public static Submodel createRoboterModel(Roboter roboter) {

		Submodel roboterSubmodel = new Submodel();

		roboterSubmodel.setIdShort("Roboter");

		roboterSubmodel.setIdentification(new ModelUrn("Roboter1"));

		Supplier<Object> lambdaReadFunction = () -> roboter.getTask().isActive();

		Property dynamicRoboterProperty = new Property();

		dynamicRoboterProperty.setIdShort("RoboterProperty");

		AASLambdaPropertyHelper.setLambdaValue(dynamicRoboterProperty, lambdaReadFunction, null);

		roboterSubmodel.addSubmodelElement(dynamicRoboterProperty);

		Function<Object[], Object> loadFunction = (args) -> {

			roboter.getTask().loadPart();
			return null;
		};
		Operation loadOperation = new Operation(loadFunction);

		loadOperation.setIdShort("loadPart");

		roboterSubmodel.addSubmodelElement(loadOperation);

		Function<Object[], Object> unloadFunction = (args) -> {

			roboter.getTask().unloadPart();
			return null;
		};
		Operation unloadOperation = new Operation(unloadFunction);

		unloadOperation.setIdShort("unloadPart");

		roboterSubmodel.addSubmodelElement(unloadOperation);

		return roboterSubmodel;
	}

	public static Submodel createWzmModel(Wzm wzm) {

		Submodel wzmSubmodel = new Submodel();

		wzmSubmodel.setIdShort("Wzm");

		wzmSubmodel.setIdentification(new ModelUrn("Wzm1"));

		Supplier<Object> lambdaReadFunction = () -> wzm.getService().isActive();

		Property dynamicWzmProperty = new Property();

		dynamicWzmProperty.setIdShort("WzmProperty");

		AASLambdaPropertyHelper.setLambdaValue(dynamicWzmProperty, lambdaReadFunction, null);

		wzmSubmodel.addSubmodelElement(dynamicWzmProperty);

		Function<Object[], Object> millingFunction = (args) -> {

			wzm.getService().applyService();
			return null;
		};
		Operation millingOperation = new Operation(millingFunction);

		millingOperation.setIdShort("millingPart");

		wzmSubmodel.addSubmodelElement(millingOperation);

		return wzmSubmodel;
	}
	
	public static void startMyControlComponent(Roboter roboter, Conveyor conveyor, Wzm wzm) {
		ControlComponent cc = new MyControlComponent(roboter, conveyor, wzm);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
	}
	
	public static void main(String[] args) throws Exception {

		Conveyor Conveyor1 = new Conveyor();
		Roboter Roboter1 = new Roboter();
		Wzm Wzm1 = new Wzm();

		startMyControlComponent(Roboter1, Conveyor1, Wzm1);

		Submodel conveyorModel = createConveyorModel(new Conveyor());
		IModelProvider conveyorModelProvider = new SubmodelProvider(conveyorModel);

		Submodel roboterModel = createRoboterModel(new Roboter());
		IModelProvider roboterModelProvider = new SubmodelProvider(roboterModel);
		
		Submodel wzmModel = createWzmModel(new Wzm());
		IModelProvider wzmModelProvider = new SubmodelProvider(wzmModel);

		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4001);

		HttpServlet model1Servlet = new VABHTTPInterface<IModelProvider>(conveyorModelProvider);
		logger.info("Created a servlet for the roboter model");
		HttpServlet model2Servlet = new VABHTTPInterface<IModelProvider>(roboterModelProvider);
		logger.info("Created a servlet for the conveyor model");
		HttpServlet model3Servlet = new VABHTTPInterface<IModelProvider>(wzmModelProvider);
		logger.info("Created a servlet for the WZM model");

		context.addServletMapping("/conveyor/*", model1Servlet);
		context.addServletMapping("/roboter/*", model2Servlet);
		context.addServletMapping("/wzm/*", model3Servlet);

		BaSyxHTTPServer server = new BaSyxHTTPServer(context);
		server.start();
		logger.info("HTTP server started");
		Thread.sleep(5000);
		
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

}