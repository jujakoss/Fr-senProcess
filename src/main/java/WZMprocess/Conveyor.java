package WZMprocess;


public class Conveyor implements IConveyor {
	private ConveyorSensor sensor;
	private ConveyorMotor motor;
	
	public Conveyor() {
		sensor = new ConveyorSensor();
		motor = new ConveyorMotor();
	}
	
	@Override
	public ConveyorSensor getSensor() {
		return sensor;
	}
	
	@Override
	public ConveyorMotor getMotor() {
		return motor;
	}
}
