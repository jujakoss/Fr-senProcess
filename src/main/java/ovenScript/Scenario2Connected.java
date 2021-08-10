package ovenScript;

import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.registry.proxy.VABRegistryProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the connected site in the HandsOn. Although everything is executed locally in this HandsOn,
 * the connection to the model can also be established from distributed locations in the network using this code
 * (using the correct network addresses).
 * 
 */
 
public class Scenario2Connected {
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(Scenario2Connected.class);
 
	public static void main(String[] args) throws Exception {
		// At the connected site, no direct access to the model is possible
		// Every access is done through the network infrastructure
 
		// The Virtual Automation Bus hides network details to the connected site. Only the endpoint of the
		// directory has to be known:
		VABRegistryProxy registryProxy = new VABRegistryProxy("http://localhost:4001/handson/directory/");
 
		// The connection manager is responsible for resolving every connection attempt
		// For this, it needs:
		// - The directory at which all models are registered
		// - A provider for different types of network protocols (in this example, only HTTP-REST)
		VABConnectionManager connectionManager = new VABConnectionManager(registryProxy, new HTTPConnectorFactory());
 
		// It is now one line of code to retrieve a model provider for any registered
		// model in the network
		IModelProvider connectedOven = connectionManager.connectToVABElement("oven");
 
		// Now, implement a simple a simple bang-bang controller as it has been done in the first HandsOn
		// Note, that the IModelProvider completely abstracts from the underlying communication protocol
		for (int i = 0; i < 100; i++) {
			// Pause for 100ms
			Thread.sleep(100);
 
			// Retrieve the current temperature from the model provider
			double temperature = (double) connectedOven.getValue("/properties/temperature");
			logger.info("Current temperature: " + temperature);
 
			// Turn the oven on/off, depending on the defined temperature range
			if (temperature > 40) {
				connectedOven.invokeOperation("/operations/deactivateOven");
			} else if (temperature < 30) {
				connectedOven.invokeOperation("/operations/activateOven");
			}
		}
	}
}