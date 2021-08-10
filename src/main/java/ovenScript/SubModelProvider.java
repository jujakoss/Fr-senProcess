package ovenScript;


import java.util.function.Function;
import java.util.function.Supplier;
import javax.servlet.http.HttpServlet;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
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
	// Initializes a logger for the output
	private static final Logger logger = LoggerFactory.getLogger(SubModelProvider.class);
	
	
	public static Submodel createMyOvenModel(Oven oven) {
		// Create an empty Submodel
		Submodel ovenSubmodel = new Submodel();
 
		// Set its idShort
		ovenSubmodel.setIdShort("Oven");
 
		// Set its unique identification
		ovenSubmodel.setIdentification(new ModelUrn("heater1"));
 
		// Now we want to create a dynamic property that can resolve its value during runtime
		// 1. Create a supplier function that can determine the oven temperature using the sensor
		Supplier<Object> lambdaReadFunction = () -> oven.getSensor().readValue();
		// 2. Create a new empty Property
		Property dynamicTemperatureProperty = new Property();
		// 3. Set the id of the new Property
		dynamicTemperatureProperty.setIdShort("temperature");
		// 4. Use the AASLambdaPropertyHelper to add the Getter to the new Property
		// NOTE: A setter function is not required (=> null), because a sensor temperature is "read only"
		AASLambdaPropertyHelper.setLambdaValue(dynamicTemperatureProperty, lambdaReadFunction, null);
		// 5. Add that lambda property to the model
		ovenSubmodel.addSubmodelElement(dynamicTemperatureProperty);
 
		// Add a function that activates the oven and implements a functional interface
		Function<Object[], Object> activateFunction = (args) -> {
			oven.getHeater().activate();
			return null;
		};
		// Encapsulate the function in an operation
		Operation activateOperation = new Operation(activateFunction);
		// Set the id of the operation
		activateOperation.setIdShort("activateOven");
		// Add an operation that activates the oven and implements a functional interface
		ovenSubmodel.addSubmodelElement(activateOperation);
 
 
		// Add a function that deactivates the oven and implements a functional interface
		Function<Object[], Object> deactivateFunction = (args) -> {
			oven.getHeater().deactivate();
			return null;
		};
		// Encapsulate the function in an operation
		Operation deactivateOperation = new Operation(deactivateFunction);
		// Set the id of the operation
		deactivateOperation.setIdShort("deactivateOven");
		// Add an operation that deactivates the oven and implements a functional interface
		ovenSubmodel.addSubmodelElement(deactivateOperation);
 
		// Return the Submodel
		return ovenSubmodel;
	}

	public static void main(String[] args) throws Exception {
		// First, a local model is created that is wrapped by a model provider (see previous step 3)
		Submodel ovenModel = createMyOvenModel(new Oven());
		// Now wrap the model in a SubmodelProvider
		IModelProvider modelProvider = new SubmodelProvider(ovenModel);
		// Up to this point, everything is known from the previous step example
 
 
		// Now, create the servlet that will provide the http/REST interface for accessing the oven Submodel
		// => Every servlet that is provided by this node is available at http://localhost:4001/handson/
		BaSyxContext context = new BaSyxContext("/handson", "", "localhost", 4001);
 
		// Now, the model provider is attached to a HTTP servlet that enables access to the model in the next steps through a HTTP rest interface
		// => The model will be published using an HTTP-REST interface
		HttpServlet modelServlet = new VABHTTPInterface<IModelProvider>(modelProvider);
		logger.info("Created a servlet for the oven model");
 
		// The provider will be available at http://localhost:4001/handson/oven/
                // And submodel can be accessed at: http://localhost:4001/handson/oven/submodel
   		context.addServletMapping("/oven/*", modelServlet);
 
		// Start the local HTTP server
		BaSyxHTTPServer server = new BaSyxHTTPServer(context);
		server.start();
		logger.info("HTTP server started");
		
		// The URN is a unique name, and refers to the asset
		ModelUrn aasURN = new ModelUrn("urn:org.eclipse.basyx:OvenAAS");
	 
		// A digital representation of the asset
		Asset asset = new Asset("ovenAsset", aasURN, AssetKind.INSTANCE);
	 
		// Create the Asset Administration Shell object
		AssetAdministrationShell aas = new AssetAdministrationShell("oven", aasURN, asset);
		
		
		// Now add the references of the submodels to the AAS header
		aas.addSubmodel(ovenModel);	
		
		
		
		// Get the Identification of the Submodel
			String id = ovenModel.getIdentification().getId();
			System.out.println("Heater id: " + id);
		 
		// The operations can be invoked via the model provider like this:
			IOperation activateOperation = ovenModel.getOperations().get("activateOven");
			activateOperation.invoke();
			

			
			
	}
}