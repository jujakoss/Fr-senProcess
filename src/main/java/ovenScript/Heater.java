package ovenScript;


/**
 * Simple heater with two states: activated or deactivated
 *
 */
public class Heater implements IHeater {
	private boolean isActive = false;
 
	public void activate() {
		if (!isActive) {
			System.out.println("Heater: activated");
			isActive = true;
		}
	}
 
	public void deactivate() {
		if (isActive) {
			System.out.println("Heater: deactivated");
			isActive = false;
		}
	}
 
	public boolean isActive() {
		return isActive;
	}
}