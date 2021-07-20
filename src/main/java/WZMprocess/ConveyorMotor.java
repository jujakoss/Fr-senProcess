package WZMprocess;

public class ConveyorMotor implements IMotor {
	private boolean isActive = false;

	public void activate() {
		if (!isActive) {
			System.out.println("Conveyor: activated");
			isActive = true;
		}
	}

	public void deactivate() {
		if (isActive) {
			System.out.println("Conveyor: deactivated");
			isActive = false;
		}
	}

	public boolean motorStatus() {
		return isActive;
	}
}