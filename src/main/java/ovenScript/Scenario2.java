package ovenScript;

import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProvider;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.eclipse.basyx.vab.registry.api.IVABRegistryService;
import org.eclipse.basyx.vab.registry.memory.VABInMemoryRegistry;
import org.eclipse.basyx.vab.registry.restapi.VABRegistryModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expected console output in this HandsOn:
 * - the heater id
 * - oven is activated and deactivated multiple times
 * - temperature values between 30 and 40
 */
public class Scenario2 {
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(Scenario2.class);
 
	public static void main(String[] args) throws Exception {
		// First, a local model is created that is wrapped by a model provider (see first HandsOn)
		Map<String, Object> model = Scenario1.createMyOvenModel(new Oven());
		IModelProvider modelProvider = new VABLambdaProvider(model);
		// Up to this point, everything is known from the previous HandsOn
 
		// Now, the model provider is given to a HTTP servlet that gives access to the model in the next steps
		// => The model will be published using an HTTP-REST interface
		HttpServlet modelServlet = new VABHTTPInterface<IModelProvider>(modelProvider);
		logger.info("Created a servlet for the oven model");
 
		// Second, create a directory that can store endpoints for VAB models
		IVABRegistryService directory = new VABInMemoryRegistry();
 
                // Register the VAB model at the directory (locally in this case)
		directory.addMapping("oven", "http://localhost:4001/handson/oven");
                logger.info("Oven model registered!");
 
                // Similar to the IModelProvider for the local oven model, a IModelProvider for the directory is created
		IModelProvider directoryProvider = new VABRegistryModelProvider(directory);
                // Next, this model provider is given to a HTTP servlet that gives access to the directory
		HttpServlet directoryServlet = new VABHTTPInterface<IModelProvider>(directoryProvider);
		logger.info("Created a servlet for the directory");
 
		// Now, define a context to which multiple servlets can be added
		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4001);
		// => Every servlet contained in this context is available at http://localhost:4001/handson/
		context.addServletMapping("/oven/*", modelServlet);
		// The model will be available at http://localhost:4001/handson/oven/
		context.addServletMapping("/directory/*", directoryServlet);
		// The directory will be available at http://localhost:4001/handson/directory/
		BaSyxHTTPServer server = new BaSyxHTTPServer(context);
		// Finally, the HTTP-REST server with this context is started.
		server.start();
		logger.info("HTTP server started");
	}
}