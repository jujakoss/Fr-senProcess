package WZMprocess;

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
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

public class AasBauteil {

	public static void startMyAssetAdministrationShell(Bauteil bauteil) {


		// *********************Service (Control) Submodel*********************

		Submodel serviceSubModel = new Submodel("Service", new ModelUrn("urn:org.eclipse.basyx:ServiceSubmodel"));

		// Create an operation that uses the control component to stop the wzm
		Function<Object[], Object> opInvokable = (params) -> {
			// Connect to the control component
			VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));

			// Select the operation from the control component
			proxy.setValue("status/opMode", WzmControlComponent.OPMODE_EXCUTE);

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

		Operation operationStart = new Operation("startWzm");
		operationStart.setInvokable(opInvokable);
		serviceSubModel.addSubmodelElement(operationStart);

		Operation operationStop = new Operation("stopWzm");
		operationStop.setInvokable(opInvokable);
		serviceSubModel.addSubmodelElement(operationStop);

		// AAS Information

		Asset asset = new Asset("WzmAsset", new ModelUrn("urn:org.eclipse.basyx:WzmAsset"), AssetKind.INSTANCE);
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:WzmAAS");
		AssetAdministrationShell aas = new AssetAdministrationShell("wzm", aasURN, asset);

		// Wraping the model in an IModelProvider (now specific to the AAS and submodel)

		AASModelProvider aasProvider = new AASModelProvider(aas);
		SubmodelProvider serviceSMProvider = new SubmodelProvider(serviceSubModel);

		MultiSubmodelProvider fullProvider = new MultiSubmodelProvider();
		fullProvider.setAssetAdministrationShell(aasProvider);
		fullProvider.addSubmodel(serviceSMProvider);

		// Deployment

		// IModelProvider is given to a HTTP servlet that gives access to the model in
		// the next steps
		HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);

		// create an InMemoryRegistry for registering the AAS
		IAASRegistry registry = new InMemoryRegistry();
		IModelProvider registryProvider = new AASRegistryModelProvider(registry);
		HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);

		// now add the references of the submodels to the AAS header
		aas.addSubmodel(serviceSubModel);

		// Register the VAB model at the directory ''local''

		AASDescriptor aasDescriptor = new AASDescriptor(aas, "http://localhost:4000/wzm/wzm/aas");

		// Explicitly create and add submodel descriptors
		SubmodelDescriptor serviceSMDescriptor = new SubmodelDescriptor(serviceSubModel,
				"http://localhost:4000/wzm/wzm/aas/submodels/Control");
		aasDescriptor.addSubmodelDescriptor(serviceSMDescriptor);
		registry.register(aasDescriptor);

		// Deploying the AAS on a HTTP server

		BaSyxContext context = new BaSyxContext("/wzm", "", "localhost", 4000);
		context.addServletMapping("/wzm/*", aasServlet);
		context.addServletMapping("/registry/*", registryServlet);
		BaSyxHTTPServer httpServer = new BaSyxHTTPServer(context);

		// start server
		httpServer.start();
		System.out.println("AAS server started");
	}

}
