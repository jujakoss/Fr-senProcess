package WZMprocess;

public class ConveyorSensor implements ISensor {

	private boolean isObjDetected = true;

	public boolean readStatus() {
		return isObjDetected;
	}

	public void setStatus(boolean isObjDetected) {
		this.isObjDetected = isObjDetected;
	}
}
