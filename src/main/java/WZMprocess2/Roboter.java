package WZMprocess2;

public class Roboter implements RoboterInterface {

	private RMotor motor;

	public Roboter() {
		motor = new RMotor();

	}

	public RMotor getTask() {
		return motor;
	}

}