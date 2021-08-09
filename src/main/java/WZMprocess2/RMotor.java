package WZMprocess2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMotor implements RMotorInterface {
	private static final Logger logger = LoggerFactory.getLogger(RMotor.class);

	private boolean isActive = false;

	public void loadPart() {

		// Function to move the part from the conveyor to the WZM

		logger.info("roboter Arm: Loading Part...");
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isActive = true;

	}

	public void unloadPart() {

		// Function to move the part from the conveyor to the WZM
		
		logger.info("roboter Arm: Unloading Part...");
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isActive = true;
	}

	public boolean isActive() {
		return isActive;
	}
}