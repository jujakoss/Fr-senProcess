package millingProcess;


public class Conveyor implements InterfaceConveyor {
	private CMotor motor;
	private CSensor sensor;
 
 
	public Conveyor() {
		motor = new CMotor();
		sensor = new CSensor(motor);
	}
 
	public CMotor getMotor() {
		return motor;
	}
 
	public CSensor getSensor() {
		return sensor;
	}
}