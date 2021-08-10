package millingProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSensor implements InterfaceCsensor {
	private static final Logger logger = LoggerFactory.getLogger(CSensor.class);
	private boolean currentSensorState = false;

	public CSensor(final CMotor motor) {

// here the sensor should be stimulated multiple times for test

		int numberOfparts = 5;

		new Thread(() -> {
			int limit = 0;
			for (limit = 0; limit < numberOfparts; limit++) { 

				currentSensorState = true;
				logger.info("Part reached sensor");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentSensorState = false;
				logger.info("Part left sensor");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public boolean readState() {
		// returns the current state of the sensor
		return currentSensorState;

	}
}