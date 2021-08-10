package millingProcess;

import java.util.function.Function;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.AASRegistryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
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
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;


public class Scenario2CC {
	public static void main(String[] args) throws Exception {
		// Create and provide the control component from the previous HandsOn
		Conveyor myConveyor = new Conveyor();
		Scenario1CC.startMyControlComponent(myConveyor);
		startMyAssetAdministrationShell(myConveyor);
	}
 
	public static void startMyAssetAdministrationShell(Conveyor myConveyor) {

		Submodel sensorSubModel = new Submodel("Sensor", new ModelUrn("urn:org.eclipse.basyx:SensorSubmodel"));
		// Create a lambda property containing the current sensor temperature
		Property statusProperty = new Property("currentState", ValueType.Boolean);
		AASLambdaPropertyHelper.setLambdaValue(statusProperty, () -> {
			return myConveyor.getSensor().readState();
		}, null);
 
		// Adds a reference to a semantic ID to specify the property semantics (see eCl@ss)
		statusProperty.setSemanticId(
				new Reference(new Key(KeyElements.PROPERTY, false, "0173-1#02-AAV232#002", KeyType.IRDI)));
		sensorSubModel.addSubmodelElement(statusProperty);
 

		Submodel motorSubModel = new Submodel("Control", new ModelUrn("urn:org.eclipse.basyx:SensorSubmodel"));
		// Create an operation that uses the control component to set conveyor on and off
		Function<Object[], Object> opInvokable = (params) -> {
			// From: HandsOn 04
			// Connect to the control component
			VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));
 
			// Select the operation from the control component
			proxy.setValue("status/opMode", ConveyorControlComponent.OPMODE_OPERATION_CONVEYOR);
 
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
		Operation operation = new Operation("movePart");
		operation.setInvokable(opInvokable);
		motorSubModel.addSubmodelElement(operation);
 

 
		Asset asset = new Asset("conveyorAsset", new ModelUrn("urn:org.eclipse.basyx:ConveyorAsset"), AssetKind.INSTANCE);
 
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:ConveyorAAS");
		AssetAdministrationShell aas = new AssetAdministrationShell("conveyor", aasURN, asset);
		// Note: The submodels are not directly integrated into the AAS model. This makes it possible to distribute
		// submodels to different nodes
		// The header contains references to the previously created submodels.
		// Here, the submodel endpoints are not yet known. They can be specified as soon as the real endpoints are known
 

		// AASModelProvider and SubModelProvider implement the IModelProvider interface
		AASModelProvider aasProvider = new AASModelProvider(aas);
		SubmodelProvider sensorSMProvider = new SubmodelProvider(sensorSubModel);
		SubmodelProvider motorSMProvider = new SubmodelProvider(motorSubModel);
 
		// Add the independent providers to the MultiSubmodelProvider that can be deployed on a single node
		MultiSubmodelProvider fullProvider = new MultiSubmodelProvider();
		fullProvider.setAssetAdministrationShell(aasProvider);
		fullProvider.addSubmodel(sensorSMProvider);
		fullProvider.addSubmodel(motorSMProvider);
 
		// Although the providers for aas/submodels implement the AAS API, they are still IModelProviders!
		// IModelProvider aasIModelProvider = fullProvider;
 

		// Now, the IModelProvider is given to a HTTP servlet that gives access to the model in the next steps
		// => The model will be published using an HTTP-REST interface
		HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
 
		// For this HandsOn, create an InMemoryRegistry for registering the AAS
		IAASRegistry registry = new InMemoryRegistry();
		IModelProvider registryProvider = new AASRegistryModelProvider(registry);
		HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
 
		// now add the references of the submodels to the AAS header
		aas.addSubmodel(sensorSubModel);
		aas.addSubmodel(motorSubModel);
 
		// Register the VAB model at the directory (locally in this case)
		AASDescriptor aasDescriptor = new AASDescriptor(aas, "http://localhost:4000/handson/conveyor/aas");
		// Explicitly create and add submodel descriptors
		SubmodelDescriptor sensorSMDescriptor = new SubmodelDescriptor(sensorSubModel, "http://localhost:4000/handson/conveyor/aas/submodels/Sensor");
		SubmodelDescriptor motorSMDescriptor = new SubmodelDescriptor(motorSubModel, "http://localhost:4000/handson/conveyor/aas/submodels/Control");
		aasDescriptor.addSubmodelDescriptor(sensorSMDescriptor);
		aasDescriptor.addSubmodelDescriptor(motorSMDescriptor);
		registry.register(aasDescriptor);
 
		// Deploy the AAS on a HTTP server
		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4000);
		context.addServletMapping("/conveyor/*", aasServlet);
		context.addServletMapping("/registry/*", registryServlet);
		BaSyxHTTPServer httpServer = new BaSyxHTTPServer(context);
 
 
		httpServer.start();
		
 

	}
}