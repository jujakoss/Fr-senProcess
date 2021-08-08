package WZMprocess2;

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
import org.eclipse.basyx.models.controlcomponent.ControlComponent;
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
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

public class Scenario2 {
	public static void main(String[] args) throws Exception {

		Conveyor Conveyor1 = new Conveyor();
		Roboter Roboter1 = new Roboter();
		Wzm Wzm1 = new Wzm();
		
		startMyControlComponent(Roboter1, Conveyor1, Wzm1);
		startMyAssetAdministrationShell(Roboter1, Conveyor1, Wzm1);
	}
	public static void startMyControlComponent(Roboter roboter, Conveyor conveyor, Wzm wzm) {
		ControlComponent cc = new MyControlComponent(roboter, conveyor, wzm);
		
		VABMapProvider ccProvider = new VABMapProvider(cc);
		
		BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(ccProvider, 4002);
		server.start();
	}
	public static void startMyAssetAdministrationShell(Roboter roboter, Conveyor conveyor, Wzm wzm) {

		Submodel cSensorSubModel = new Submodel("CSensor", new ModelUrn("urn:org.eclipse.basyx:CSensorSubmodel"));
		Property CProperty = new Property("currentState", ValueType.Double);
		AASLambdaPropertyHelper.setLambdaValue(CProperty, () -> {
			return conveyor.getSensor().readState();
		}, null);
		CProperty.setSemanticId(
				new Reference(new Key(KeyElements.PROPERTY, false, "0173-1#02-AAV232#002", KeyType.IRDI)));
		cSensorSubModel.addSubmodelElement(CProperty);

		Submodel cMotorSubModel = new Submodel("CMotor", new ModelUrn("urn:org.eclipse.basyx:CMotorSubmodel"));
		Submodel rMotorSubModel = new Submodel("RMotor", new ModelUrn("urn:org.eclipse.basyx:RMotorSubmodel"));
		Submodel wzmServiceSubModel = new Submodel("Service", new ModelUrn("urn:org.eclipse.basyx:ServiceSubmodel"));

		Asset conveyorAsset = new Asset("conveyorAsset", new ModelUrn("urn:org.eclipse.basyx:ConveyorAsset"), AssetKind.INSTANCE);
		Asset roboterAsset = new Asset("roboterAsset", new ModelUrn("urn:org.eclipse.basyx:RoboterAsset"), AssetKind.INSTANCE);
		Asset wzmAsset = new Asset("wzmAsset", new ModelUrn("urn:org.eclipse.basyx:WzmAsset"), AssetKind.INSTANCE);
		
		ModelUrn conveyorAasURN = new ModelUrn("urn:org.eclipse.basyx:ConveyorAsset");
		AssetAdministrationShell conveyorAas = new AssetAdministrationShell("conveyor", conveyorAasURN, conveyorAsset);
		
		AASModelProvider conveyorAasProvider = new AASModelProvider(conveyorAas);
		SubmodelProvider cSensorSMProvider = new SubmodelProvider(cSensorSubModel);
		SubmodelProvider cMotorSMProvider = new SubmodelProvider(cMotorSubModel);
		MultiSubmodelProvider conveyorFullProvider = new MultiSubmodelProvider();
		conveyorFullProvider.setAssetAdministrationShell(conveyorAasProvider);
		conveyorFullProvider.addSubmodel(cSensorSMProvider);
		conveyorFullProvider.addSubmodel(cMotorSMProvider);
		
		ModelUrn roboterAasURN = new ModelUrn("urn:org.eclipse.basyx:RoboterAsset");
		AssetAdministrationShell roboterAas = new AssetAdministrationShell("roboter", roboterAasURN, roboterAsset);
		
		AASModelProvider roboterAasProvider = new AASModelProvider(roboterAas);
		SubmodelProvider rMotorSMProvider = new SubmodelProvider(rMotorSubModel);
		MultiSubmodelProvider roboterFullProvider = new MultiSubmodelProvider();
		roboterFullProvider.setAssetAdministrationShell(roboterAasProvider);
		roboterFullProvider.addSubmodel(rMotorSMProvider);
		
		ModelUrn wzmAasURN = new ModelUrn("urn:org.eclipse.basyx:WzmAsset");
		AssetAdministrationShell wzmAas = new AssetAdministrationShell("conveyor", wzmAasURN, wzmAsset);
		
		AASModelProvider wzmAasProvider = new AASModelProvider(wzmAas);
		SubmodelProvider wzmServiceSMProvider = new SubmodelProvider(wzmServiceSubModel);
		MultiSubmodelProvider wzmFullProvider = new MultiSubmodelProvider();
		wzmFullProvider.setAssetAdministrationShell(wzmAasProvider);
		wzmFullProvider.addSubmodel(wzmServiceSMProvider);
		
		
		HttpServlet conveyorAasServlet = new VABHTTPInterface<IModelProvider>(conveyorFullProvider);
		HttpServlet roboterAasServlet = new VABHTTPInterface<IModelProvider>(roboterFullProvider);
		HttpServlet wzmAasServlet = new VABHTTPInterface<IModelProvider>(wzmFullProvider);
 
		IAASRegistry registry = new InMemoryRegistry();
		IModelProvider registryProvider = new AASRegistryModelProvider(registry);
		HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
 
		conveyorAas.addSubmodel(cSensorSubModel);
		conveyorAas.addSubmodel(cMotorSubModel);
		roboterAas.addSubmodel(rMotorSubModel);
		wzmAas.addSubmodel(wzmServiceSubModel);
 
		AASDescriptor conveyorAasDescriptor = new AASDescriptor(conveyorAas, "http://localhost:4000/milling/conveyor/aas");
		SubmodelDescriptor cSensorSMDescriptor = new SubmodelDescriptor(cSensorSubModel, "http://localhost:4000/milling/conveyor/aas/submodels/Sensor");
		SubmodelDescriptor cMotorSMDescriptor = new SubmodelDescriptor(cMotorSubModel, "http://localhost:4000/milling/conveyor/aas/submodels/RMotor");
		conveyorAasDescriptor.addSubmodelDescriptor(cSensorSMDescriptor);
		conveyorAasDescriptor.addSubmodelDescriptor(cMotorSMDescriptor);
		registry.register(conveyorAasDescriptor);
		
		 
		AASDescriptor roboterAasDescriptor = new AASDescriptor(conveyorAas, "http://localhost:4000/milling/roboter/aas");
		SubmodelDescriptor rMotorSMDescriptor = new SubmodelDescriptor(rMotorSubModel, "http://localhost:4000/milling/roboter/aas/submodels/RMotor");
		roboterAasDescriptor.addSubmodelDescriptor(rMotorSMDescriptor);
		registry.register(conveyorAasDescriptor);
		 
		AASDescriptor wzmAasDescriptor = new AASDescriptor(conveyorAas, "http://localhost:4000/milling/wzm/aas");
		SubmodelDescriptor wzmServiceSMDescriptor = new SubmodelDescriptor(wzmServiceSubModel, "http://localhost:4000/milling/wzm/aas/submodels/Service");
		conveyorAasDescriptor.addSubmodelDescriptor(wzmServiceSMDescriptor);
		registry.register(wzmAasDescriptor);
		
		
		BaSyxContext context = new BaSyxContext("/milling", "", "localhost", 4000);
		context.addServletMapping("/operation/*", conveyorAasServlet);
		context.addServletMapping("/operation/*", roboterAasServlet);
		context.addServletMapping("/operation/*", wzmAasServlet);
		
		context.addServletMapping("/registry/*", registryServlet);
		BaSyxHTTPServer httpServer = new BaSyxHTTPServer(context);
		
		Function<Object[], Object> Invokable = (params) -> {
			VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector("localhost", 4002)));

			proxy.setValue("status/opMode", MyControlComponent.OP_MODE_MILLING);
			proxy.invokeOperation("/operations/service/start");
			while (!proxy.getValue("status/exState").equals(ExecutionState.COMPLETE.getValue())) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
 
			proxy.invokeOperation("operations/service/reset");
			return null;
		};
 
		Operation operation = new Operation("milling_operation");
		operation.setInvokable(Invokable);
 
		httpServer.start();
 
	}
	
	/*- AAS: 
			http://localhost:4000/milling/conveyor/aas/
			http://localhost:4000/milling/roboter/aas/
			http://localhost:4000/milling/wzm/aas/

	- Submodels: 
			
			http://localhost:4000/milling/conveyor/aas/submodels/Sensor/
			http://localhost:4000/milling/conveyor/aas/submodels/CMotor/
			http://localhost:4000/milling/roboter/aas/submodels/RMotor/
			http://localhost:4000/milling/wzm/aas/submodels/Service/

	// - Show all AAS: http://localhost:4000/milling/registry/api/v1/registry/

	// - Show particular AAS: 
	 * 
	 		http://localhost:4000/milling/registry/api/v1/registry/urn:org.eclipse.basyx:ConveyorAsset
	 		http://localhost:4000/milling/registry/api/v1/registry/urn:org.eclipse.basyx:RoboterAsset
	 		http://localhost:4000/milling/registry/api/v1/registry/urn:org.eclipse.basyx:WzmAsset

	 */

}