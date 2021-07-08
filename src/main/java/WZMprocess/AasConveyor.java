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


public class AasConveyor {
	
	private static final Logger logger = LoggerFactory.getLogger(AasConveyor.class);


	public static void startMyControlComponent(Conveyor conveyor) {
		// Given is a local control component that can directly control the virtual oven
		// device
		ControlComponent cc = new ConveyorControlComponent(conveyor);

		// Like the VAB model created before, the structure of the control component is
		// a Map
		// Map ccModel = (Map) cc;

		// Create a server for the Control Component and provide it in the VAB (at port
		// 4002)
		VABMapProvider ccProvider = new VABMapProvider(cc);
		// This time, a BaSyx-specific TCP interface is used.
		// Likewise, it is also possible to wrap the control component using a http
		// servlet as before
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
	}

	public static void startMyAssetAdministrationShell(Conveyor conveyor) {
		/**
		 * Sensor Submodel
		 */
		Submodel sensorSubModel = new Submodel("Sensor", new ModelUrn("urn:org.eclipse.basyx:SensorSubmodel"));
		// Create a lambda property containing the current sensor temperature
		Property temperatureProperty = new Property("currentTemperature", ValueType.Double);
		AASLambdaPropertyHelper.setLambdaValue(temperatureProperty, () -> {
			return conveyor.getSensor().readStatus();
		}, null);

		// Adds a reference to a semantic ID to specify the property semantics (see
		// eCl@ss)
		temperatureProperty.setSemanticId(
				new Reference(new Key(KeyElements.PROPERTY, false, "0173-1#02-AAV232#002", KeyType.IRDI)));
		sensorSubModel.addSubmodelElement(temperatureProperty);

		Property temperatureUnit = new Property("temperatureUnit", ValueType.String);
		temperatureUnit.setValue("Celsius");
		sensorSubModel.addSubmodelElement(temperatureUnit);

		/**
		 * Control Submodel
		 */
		Submodel heaterSubModel = new Submodel("Control", new ModelUrn("urn:org.eclipse.basyx:SensorSubmodel"));
		// Create an operation that uses the control component to set a temperature
		// value
		Function<Object[], Object> heatInvokable = (params) -> {
			// From: HandsOn 04
			// Connect to the control component
			VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));

			// Select the operation from the control component
			proxy.setValue("status/opMode", ConveyorControlComponent.OP_MODE_OBJ_DETECTED);

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

		// Create the Operation
		Operation operation = new Operation("setTemperature");
		operation.setInvokable(heatInvokable);
		heaterSubModel.addSubmodelElement(operation);

		/**
		 * Minimal AAS Information
		 */

		Asset asset = new Asset("ovenAsset", new ModelUrn("urn:org.eclipse.basyx:OvenAsset"), AssetKind.INSTANCE);

		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:OvenAAS");
		AssetAdministrationShell aas = new AssetAdministrationShell("oven", aasURN, asset);
		// Note: The submodels are not directly integrated into the AAS model. This
		// makes it possible to distribute
		// submodels to different nodes
		// The header contains references to the previously created submodels.
		// Here, the submodel endpoints are not yet known. They can be specified as soon
		// as the real endpoints are known

		/**
		 * Again: Wrap the model in an IModelProvider (now specific to the AAS and
		 * submodel)
		 */
		// AASModelProvider and SubModelProvider implement the IModelProvider interface
		AASModelProvider aasProvider = new AASModelProvider(aas);
		SubmodelProvider sensorSMProvider = new SubmodelProvider(sensorSubModel);
		SubmodelProvider heaterSMProvider = new SubmodelProvider(heaterSubModel);

		// Add the independent providers to the MultiSubmodelProvider that can be
		// deployed on a single node
		MultiSubmodelProvider fullProvider = new MultiSubmodelProvider();
		fullProvider.setAssetAdministrationShell(aasProvider);
		fullProvider.addSubmodel(sensorSMProvider);
		fullProvider.addSubmodel(heaterSMProvider);

		// Although the providers for aas/submodels implement the AAS API, they are
		// still IModelProviders!
		// IModelProvider aasIModelProvider = fullProvider;

		/**
		 * Deployment
		 */
		// Now, the IModelProvider is given to a HTTP servlet that gives access to the
		// model in the next steps
		// => The model will be published using an HTTP-REST interface
		HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);

		// For this HandsOn, create an InMemoryRegistry for registering the AAS
		IAASRegistry registry = new InMemoryRegistry();
		IModelProvider registryProvider = new AASRegistryModelProvider(registry);
		HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);

		// now add the references of the submodels to the AAS header
		aas.addSubmodel(sensorSubModel);
		aas.addSubmodel(heaterSubModel);

		// Register the VAB model at the directory (locally in this case)
		AASDescriptor aasDescriptor = new AASDescriptor(aas, "http://localhost:4000/handson/oven/aas");
		// Explicitly create and add submodel descriptors
		SubmodelDescriptor sensorSMDescriptor = new SubmodelDescriptor(sensorSubModel,
				"http://localhost:4000/handson/oven/aas/submodels/Sensor");
		SubmodelDescriptor heaterSMDescriptor = new SubmodelDescriptor(heaterSubModel,
				"http://localhost:4000/handson/oven/aas/submodels/Control");
		aasDescriptor.addSubmodelDescriptor(sensorSMDescriptor);
		aasDescriptor.addSubmodelDescriptor(heaterSMDescriptor);
		registry.register(aasDescriptor);

		// Deploy the AAS on a HTTP server
		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4000);
		context.addServletMapping("/oven/*", aasServlet);
		context.addServletMapping("/registry/*", registryServlet);
		BaSyxHTTPServer httpServer = new BaSyxHTTPServer(context);

		httpServer.start();

		// Now in the browser, look at the various endpoints to see what is returned:
		// - AAS: http://localhost:4000/handson/oven/aas/
		// - Sensor Submodel: http://localhost:4000/handson/oven/aas/submodels/Sensor/
		// - Control Submodel: http://localhost:4000/handson/oven/aas/submodels/Control/

		// Similar, the registry also has a HTTP-REST interface. So, it is possible to
		// directly query it:
		// - Show all AAS: http://localhost:4000/handson/registry/api/v1/registry/
		// - Show my AAS:
		// http://localhost:4000/handson/registry/api/v1/registry/urn:org.eclipse.basyx:OvenAAS
		// Note: the "#" character in the URN s encoded as "%23"

		// The server can also be shut down:
		/*
		 * try { // Wait for 5s and then shutdown the server Thread.sleep(5000);
		 * httpServer.shutdown(); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */
	}

	public void createAas() throws Exception {
		// Create and provide the asset administration shell from the previous HandsOns
		Conveyor conveyor = new Conveyor();
		startMyControlComponent(conveyor);
		startMyAssetAdministrationShell(conveyor);

		// Return a AASHTTPRegistryProxy for the registry on localhost at port 4000
		IAASRegistry registry = new AASRegistryProxy("http://localhost:4000/handson/registry");

		// Create a ConnectedAssetAdministrationShell using a
		// ConnectedAssetAdministrationShellManager
		IConnectorFactory connectorFactory = new HTTPConnectorFactory();
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry,
				connectorFactory);

		// The ID of the oven AAS
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:ConveyorAAS");
		ConnectedAssetAdministrationShell connectedAAS = manager.retrieveAAS(aasURN);

		// Connect to the AAS and read the current temperature
		// Either Create a connected property using the connected facades
		Map<String, ISubmodel> submodels = connectedAAS.getSubmodels();
		ISubmodel connectedSensorSM = submodels.get("Sensor");
		Map<String, IProperty> properties = connectedSensorSM.getProperties();
		IProperty objDetectedProperty = properties.get("isObjDetected");
		boolean objDetected= (boolean) objDetectedProperty.getValue();
		// Or get a VABElementProxy to directly query the VAB path of the property
		/*
		 * IModelProvider providerProxy = connectedAAS.getProxy(); String
		 * temperatureValuePath =
		 * "/submodels/Sensor/submodelElements/currentTemperature/value"; Map<String,
		 * Object> ret = (Map<String, Object>)
		 * providerProxy.getModelPropertyValue(temperatureValuePath); double temperature
		 * = (double) ret.get(Property.VALUE);
		 */
		
		logger.info("Bauteil bereit: " + objDetected);
	}

}
