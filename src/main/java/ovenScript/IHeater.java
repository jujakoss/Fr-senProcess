package ovenScript;

/**
 * The heater enables controlling of the oven
 */
public interface IHeater {
 
	public void activate();
 
	public void deactivate();
 
	public boolean isActive();
}
