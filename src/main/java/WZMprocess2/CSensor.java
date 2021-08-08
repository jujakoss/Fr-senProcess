package WZMprocess2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSensor implements CSensorInterface {
	private static final Logger logger = LoggerFactory.getLogger(CSensor.class);
	private boolean currentSensorState = false;

	public CSensor(final CMotor motor) {

// here the sensor should be stimulated multiple times for test

		int numberOfparts = 5;

		new Thread(() -> {
			int limit = 0;
			for (limit = 0; limit < numberOfparts; limit++) {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (limit % 2 == 0) {
					logger.info("Part reached sensor ");
					currentSensorState = true;
				}
				else {
					currentSensorState = false;
				}
			}
		}).start();
	}

	public boolean readState() {
		// returns the current state of the sensor
		return currentSensorState;

	}
}