package wzmProcess;


import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;

public class Server {
	// Server URLs
	public static final String REGISTRYPATH = "http://localhost:4000/registry";
	public static final String AASSERVERPATH = "http://localhost:4001/aasServer";
 
	// AAS/Submodel/Property Ids
	public static final IIdentifier CONVEYORAASID = new CustomId("eclipse.basyx.aas.Conveyor");
	public static final IIdentifier ROBOTERAASID = new CustomId("eclipse.basyx.aas.Roboter");
	public static final IIdentifier BAUTEILAASID = new CustomId("eclipse.basyx.aas.Bauteil");
	public static final IIdentifier WZMAASID = new CustomId("eclipse.basyx.aas.Wzm");
	
	public static final IIdentifier DOCUSMID = new CustomId("eclipse.basyx.submodel.documentation");
 
	public static void main(String[] args) {
		// Create Infrastructure
		startRegistry();
		startAASServer();
 
		// Create Manager - This manager is used to interact with an AAS server
		ConnectedAssetAdministrationShellManager manager = 
				new ConnectedAssetAdministrationShellManager(new AASRegistryProxy(REGISTRYPATH));
 
		// Create AAS and push it to server
		Asset convasset = new Asset("conveyorAsset", new CustomId("eclipse.basyx.asset.conveyor"), AssetKind.INSTANCE);
		AssetAdministrationShell convshell = new AssetAdministrationShell("conveyor", CONVEYORAASID, convasset);
		
		Asset robotasset = new Asset("roboterAsset", new CustomId("eclipse.basyx.asset.roboter"), AssetKind.INSTANCE);
		AssetAdministrationShell robotshell = new AssetAdministrationShell("roboter", ROBOTERAASID, robotasset);
		
		Asset bauteilasset = new Asset("bautzeilAsset", new CustomId("eclipse.basyx.asset.bauteil"), AssetKind.INSTANCE);
		AssetAdministrationShell bauteilshell = new AssetAdministrationShell("bauteil", BAUTEILAASID, bauteilasset);
		
		Asset wzmasset = new Asset("wzmAsset", new CustomId("eclipse.basyx.asset.wzm"), AssetKind.INSTANCE);
		AssetAdministrationShell wzmshell = new AssetAdministrationShell("wzm", WZMAASID, wzmasset);
 
		// The manager uploads the AAS and registers it in the Registry server
		manager.createAAS(convshell, AASSERVERPATH);
		manager.createAAS(robotshell, AASSERVERPATH);
		manager.createAAS(bauteilshell, AASSERVERPATH);
		manager.createAAS(wzmshell, AASSERVERPATH);
 
		// Create submodel
		Submodel documentationSubmodel = new Submodel("documentationSm", DOCUSMID);
 
		// - Push the Submodel to the AAS server
		manager.createSubmodel(convshell.getIdentification(), documentationSubmodel);
		manager.createSubmodel(robotshell.getIdentification(), documentationSubmodel);
		manager.createSubmodel(bauteilshell.getIdentification(), documentationSubmodel);
		manager.createSubmodel(wzmshell.getIdentification(), documentationSubmodel);
	}
 
	/**
	 * Starts an empty registry at "http://localhost:4000"
	 */
	private static void startRegistry() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4000, "/registry");
		BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration(RegistryBackend.INMEMORY);
		RegistryComponent registry = new RegistryComponent(contextConfig, registryConfig);
 
		// Start the created server
		registry.startComponent();
	}
 
	/**
	 * Startup an empty server at "http://localhost:4001/"
	 */
	private static void startAASServer() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(4001, "/aasServer");
		BaSyxAASServerConfiguration aasServerConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "", REGISTRYPATH);
		AASServerComponent aasServer = new AASServerComponent(contextConfig, aasServerConfig);
 
		// Start the created server
		aasServer.startComponent();
		System.out.println("Server started.");
	}
}