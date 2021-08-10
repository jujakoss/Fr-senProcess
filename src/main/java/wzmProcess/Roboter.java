package wzmProcess;

public class Roboter implements InterfaceRoboter {

	private RMotor motor;

	public Roboter() {
		motor = new RMotor();

	}

	public RMotor getTask() {
		return motor;
	}

}