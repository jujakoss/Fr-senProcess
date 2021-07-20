package WZMprocess;

public class RoboterTask {
	private boolean isActive = false;

	public void load() {
		if (!isActive) {
			System.out.println("Roboter: loading");
			isActive = true;
		}
	}

	public void unload() {
		if (isActive) {
			System.out.println("Roboter: unloading");
			isActive = false;
		}
	}

	public boolean readStatus() {
		return isActive;
	}
}