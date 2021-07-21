package WZMprocess;

public class RoboterTask {
	private boolean isActive = false;

	public void load() {
		if (isActive) {
			System.out.println("Roboter: loading");
			isActive = true;
			//loading operation
			isActive = false;
		}
	}

	public void unload() {
		if (isActive) {
			System.out.println("Roboter: unloading");
			isActive = true;
			//unloading operation
			isActive = false;
		}
	}

	public boolean readStatus() {
		return isActive;
	}
}