package WZMprocess;

public class ConveyorMotor implements IMotor {

	private boolean isMoving = true;

	public boolean readStatus() {
		return isMoving;
	}

	@Override
	public void start() {
		isMoving = true;		
	}

	@Override
	public void stop() {
		isMoving = false;
		
	}
}
