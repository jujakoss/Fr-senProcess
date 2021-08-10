package ovenScript;

/**
 * Oven containing a heater and a temperature sensor
 */
public class Oven implements IOven {
	private Heater heater;
	private TemperatureSensor sensor;
 
 
	public Oven() {
		heater = new Heater();
		sensor = new TemperatureSensor(heater);
	}
 
	public Heater getHeater() {
		return heater;
	}
 
	public TemperatureSensor getSensor() {
		return sensor;
	}
}