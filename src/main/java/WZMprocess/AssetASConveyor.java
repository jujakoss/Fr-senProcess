package WZMprocess;



import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.aas.registration.restapi.AASRegistryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.AASLambdaPropertyHelper;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class AssetASConveyor {
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(AssetASConveyor.class);
	
	public static void startMyControlComponent(Conveyor conveyor) {
		
		ControlComponent cc = new ConveyorControlComponent(conveyor);
		VABMapProvider ccProvider = new VABMapProvider(cc);
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();

	}

	public static void startMyAssetAdministrationShell(Conveyor conveyorOne) {
		
		//************************Sensor Submodel****************************
		
		Submodel sensorSubModel = new Submodel("Sensor", new ModelUrn("urn:org.eclipse.basyx:SensorSubmodel"));
		
		//lambda property containing the current sensor state
		Property stateProperty = new Property("currentState", ValueType.Boolean);
		AASLambdaPropertyHelper.setLambdaValue(stateProperty, () -> {
			return conveyorOne.getSensor().readStatus();
		}, null);
		
		// Adds a reference to a semantic ID to specify the property semantics
		stateProperty.setSemanticId(
				new Reference(new Key(KeyElements.PROPERTY, false, "0173-1#02-AAV232#002", KeyType.IRDI)));
		sensorSubModel.addSubmodelElement(stateProperty);
		
		
		//*********************Motor (Control) Submodel*********************
		
		Submodel motorSubModel = new Submodel("Control", new ModelUrn("urn:org.eclipse.basyx:MotorSubmodel"));
		
		// Create an operation that uses the control component to stop the conveyor
		Function<Object[], Object> opInvokable = (params) -> {
			// Connect to the control component
			VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));
 
			// Select the first operation from the control component
			proxy.setValue("status/opMode", ConveyorControlComponent.OPMODE_SENSOR_BLOCKED);
 
			// Start the control component operation asynchronous
			proxy.invokeOperation("/operations/service/start");
 
			// Wait until the operation is completed
			while (!proxy.getValue("status/exState").equals(ExecutionState.COMPLETE.getValue())) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			
			
			// Select the second operation from the control component
			proxy.setValue("status/opMode", ConveyorControlComponent.OPMODE_SENSOR_FREE);
 
			// Start the control component operation asynchronous
			proxy.invokeOperation("/operations/service/start");
 
			// Wait until the operation is completed
			while (!proxy.getValue("status/exState").equals(ExecutionState.COMPLETE.getValue())) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
 
			proxy.invokeOperation("operations/service/reset");
			// Then return -> synchronous
			return null;
		};
		
		// Create the Operations
		
		Operation operationStart = new Operation("startConveyor");
		operationStart.setInvokable(opInvokable);
		motorSubModel.addSubmodelElement(operationStart);
		
		Operation operationStop = new Operation("stopConveyor");
		operationStop.setInvokable(opInvokable);
		motorSubModel.addSubmodelElement(operationStop);
		
		
		 //AAS Information
		
		Asset asset = new Asset("WzmAsset", new ModelUrn("urn:org.eclipse.basyx:WzmAsset"), AssetKind.INSTANCE);
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:WzmAAS");
		AssetAdministrationShell aas = new AssetAdministrationShell("wzm", aasURN, asset);	
		
		// Wraping the model in an IModelProvider (now specific to the AAS and submodel)
		
		AASModelProvider aasProvider = new AASModelProvider(aas);
		SubmodelProvider sensorSMProvider = new SubmodelProvider(sensorSubModel);
		SubmodelProvider motorSMProvider = new SubmodelProvider(motorSubModel);
		
		MultiSubmodelProvider fullProvider = new MultiSubmodelProvider();
		fullProvider.setAssetAdministrationShell(aasProvider);
		fullProvider.addSubmodel(sensorSMProvider);
		fullProvider.addSubmodel(motorSMProvider);
		
		//Deployment
		
		// IModelProvider is given to a HTTP servlet that gives access to the model in the next steps
		HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
		
		//create an InMemoryRegistry for registering the AAS
		IAASRegistry registry = new InMemoryRegistry();
		IModelProvider registryProvider = new AASRegistryModelProvider(registry);
		HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);

		// now add the references of the submodels to the AAS header
		
		aas.addSubmodel(sensorSubModel);
		aas.addSubmodel(motorSubModel);
		
		// Register the VAB model at the directory ''local''
		
		AASDescriptor aasDescriptor = new AASDescriptor(aas, "http://localhost:4000/handson/wzm/aas");
		
		// Explicitly create and add submodel descriptors
		SubmodelDescriptor sensorSMDescriptor = new SubmodelDescriptor(sensorSubModel, "http://localhost:4000/handson/conveyor/aas/submodels/Sensor");
		SubmodelDescriptor motorSMDescriptor = new SubmodelDescriptor(motorSubModel, "http://localhost:4000/handson/conveyor/aas/submodels/Control");
		aasDescriptor.addSubmodelDescriptor(sensorSMDescriptor);
		aasDescriptor.addSubmodelDescriptor(motorSMDescriptor);
		registry.register(aasDescriptor);
		
		// Deploying the AAS on a HTTP server
		
		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4000);
		context.addServletMapping("/wzm/*", aasServlet);
		context.addServletMapping("/registry/*", registryServlet);
		BaSyxHTTPServer httpServer = new BaSyxHTTPServer(context);
		
		//start server
		httpServer.start();

	}

	
	
	
	public static void main(String[] args) throws Exception {
		

		Conveyor conveyorOne = new Conveyor();
		startMyControlComponent(conveyorOne);
		startMyAssetAdministrationShell(conveyorOne);
		// Return a AASHTTPRegistryProxy for the registry on localhost at port 4000
		IAASRegistry registry = new AASRegistryProxy("http://localhost:4000/wzm/registry");
 
		// Create a ConnectedAssetAdministrationShell using a ConnectedAssetAdministrationShellManager
		IConnectorFactory connectorFactory = new HTTPConnectorFactory();
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry,
				connectorFactory);
 
		// The ID of the conveyor AAS
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:ConveyorAAS");
		ConnectedAssetAdministrationShell connectedAAS = manager.retrieveAAS(aasURN);
		
		// Connect to the AAS and read the current state of the sensor
		// Either Create a connected property using the connected facades
		Map<String, ISubmodel> submodels = connectedAAS.getSubmodels();
		ISubmodel connectedSensorSM = submodels.get("Sensor");
		Map<String, IProperty> properties = connectedSensorSM.getProperties();
		IProperty stateProperty = properties.get("currentState");
		double state = (double) stateProperty.getValue();
		
		logger.info("The Server has started , the current stateof the sensor is " + state );
		
	}
	
	
	

	
// - AAS: http://localhost:4000/wzm/conveyor/aas/
// - Sensor Submodel: http://localhost:4000/wzm/conveyor/aas/submodels/Sensor/
// - Control(Motor) Submodel: http://localhost:4000/wzm/conveyor/aas/submodels/Control/
// - Show all AAS: http://localhost:4000/wzm/registry/api/v1/registry/
// - Show my AAS: http://localhost:4000/wzm/registry/api/v1/registry/urn:org.eclipse.basyx:ConveyorAAS

// End of script

}	

