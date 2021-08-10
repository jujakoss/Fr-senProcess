package ovenScript;

import java.util.Map;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Now, the result from the last HandsOn is used to set up the Asset Administration Shell.
 * In this HandsOn, the connected site is demonstrated.
 * 
 * Given SDK components will be used to query the registry and access properties from the AAS.
 * 
 * Expected console output:
 * - the temperature value in °C
 */
public class Scenario5 {
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(Scenario5.class);
 
	public static void main(String[] args) throws Exception {
		// Create and provide the asset administration shell from the previous HandsOns
		Oven myOven = new Oven();
		Scenario3.startMyControlComponent(myOven);
		Scenario4.startMyAssetAdministrationShell(myOven);
 
		// Return a AASHTTPRegistryProxy for the registry on localhost at port 4000
		IAASRegistry registry = new AASRegistryProxy("http://localhost:4000/handson/registry");
 
		// Create a ConnectedAssetAdministrationShell using a ConnectedAssetAdministrationShellManager
		IConnectorFactory connectorFactory = new HTTPConnectorFactory();
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry,
				connectorFactory);
 
		// The ID of the oven AAS
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:OvenAAS");
		ConnectedAssetAdministrationShell connectedAAS = manager.retrieveAAS(aasURN);
 
		// Connect to the AAS and read the current temperature
		// Either Create a connected property using the connected facades
		Map<String, ISubmodel> submodels = connectedAAS.getSubmodels();
		ISubmodel connectedSensorSM = submodels.get("Sensor");
		Map<String, IProperty> properties = connectedSensorSM.getProperties();
		IProperty temperatureProperty = properties.get("currentTemperature");
		double temperature = (double) temperatureProperty.getValue();
		// Or get a VABElementProxy to directly query the VAB path of the property
		/*
		 * IModelProvider providerProxy = connectedAAS.getProxy();
		 * String temperatureValuePath = "/submodels/Sensor/submodelElements/currentTemperature/value";
		 * Map<String, Object> ret = (Map<String, Object>) providerProxy.getModelPropertyValue(temperatureValuePath);
		 * double temperature = (double) ret.get(Property.VALUE);
		 */
 
		// Connect to the AAS and read the current temperature
		// Either use the connected variants:
		IProperty unitProperty = properties.get("temperatureUnit");
		String temperatureUnit = (String) unitProperty.getValue();
		// Or get a VABElementProxy to directly query the VAB path of the property
		/*
		 * String temperatureUnitPath =
		 * "/submodels/Sensor/submodelElements/temperatureUnit/value"; ret = (Map<String,
		 * Object>) providerProxy.getModelPropertyValue(temperatureUnitPath); String
		 * temperatureUnit = (String) ret.get(Property.VALUE);
		 */
 
		// Now depending on the semantics of the temperature, calculate the value in °C
		// Usually, these semantics will be stored in a context dictionary that is referenced by the semantic attributes
		// of the property. But this HandsOn demonstrates a simplified scenario.
		if (temperatureUnit.equals("Fahrenheit")) {
			temperature = (temperature - 32.0d) * 5.0d / 9.0d;
		}
 
		logger.info("The sensor temperature is " + temperature + "°C");
	}
}
