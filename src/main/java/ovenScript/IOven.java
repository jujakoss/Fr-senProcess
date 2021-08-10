package ovenScript;

/**
 * Oven containing a heater and a temperature sensor
 */
public interface IOven {
 
	public Heater getHeater();
 
	public TemperatureSensor getSensor();
}
