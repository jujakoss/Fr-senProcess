package WZMprocess2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WzmService {

		private static final Logger logger = LoggerFactory.getLogger(RMotor.class);

		private boolean isActive = false;
		 
		public void applyService() {
		// Function to apply Milling on the part Once in the WZM
				logger.info("Milling in progress...");
				try {
					Thread.sleep(5000); // 5 seconds hypothetical Milling time
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				isActive = true;
			}

		public boolean isActive() {
			return isActive;
		}
	}