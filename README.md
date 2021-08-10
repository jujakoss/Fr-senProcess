# Fr-senProcess

New update in MillingProcess:
Complete code for conveyor.
Run Scenario1CC to test conveyor
RunS Scenario2CC to have remote access to AAS and Registery
		// - AAS: http://localhost:4000/handson/conveyor/aas/
		// - Sensor Submodel: http://localhost:4000/handson/conveyor/aas/submodels/Sensor/
		// - Control Submodel: http://localhost:4000/handson/conveyor/aas/submodels/Control/
 
		// Similar, the registry also has a HTTP-REST interface. So, it is possible to directly query it:
		// - Show all AAS: http://localhost:4000/handson/registry/api/v1/registry/
		// - Show my AAS: http://localhost:4000/handson/registry/api/v1/registry/urn:org.eclipse.basyx:ConveyorAAS# Fr-senProcess

New update in WZMprocess2:

Scenario1 is similar to Scenario3 in the Oven example, by Running it, it will trigger a test sequence that includes sychronisation between the conveyor, robot, Wzm
stimulating the process.

Scenario2 contain the AAS creation from Scenario4 in Oven example, Still in test.
