package WZMprocess2;

import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.AASLambdaPropertyHelper;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubModelProvider {

	private static final Logger logger = LoggerFactory.getLogger(SubModelProvider.class);

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

	public static void main(String[] args) throws Exception {

		Submodel conveyorModel = createConveyorModel(new Conveyor());
		IModelProvider conveyorModelProvider = new SubmodelProvider(conveyorModel);

		Submodel roboterModel = createRoboterModel(new Roboter());
		IModelProvider roboterModelProvider = new SubmodelProvider(roboterModel);

		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4001);

		HttpServlet model1Servlet = new VABHTTPInterface<IModelProvider>(conveyorModelProvider);
		logger.info("Created a servlet for the roboter model");
		HttpServlet model2Servlet = new VABHTTPInterface<IModelProvider>(roboterModelProvider);
		logger.info("Created a servlet for the conveyor model");

		context.addServletMapping("/conveyor/*", model1Servlet);
		context.addServletMapping("/roboter/*", model2Servlet);

		BaSyxHTTPServer server = new BaSyxHTTPServer(context);
		server.start();
		logger.info("HTTP server started");
	}
}
