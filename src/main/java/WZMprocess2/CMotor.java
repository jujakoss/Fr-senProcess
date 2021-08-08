package WZMprocess2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMotor implements CMotorInterface {
	private static final Logger logger = LoggerFactory.getLogger(CMotor.class);
	private boolean isActive = false;
 
	public void activate() {
		if (!isActive) {
			logger.info("conveyor : Running");
			isActive = true;
		}
	}
 
	public void deactivate() {
		if (isActive) {
			logger.info("Conveyor: Stopped");
			isActive = false;
		}
	}
 
	public boolean isActive() {
		return isActive;
	}
}