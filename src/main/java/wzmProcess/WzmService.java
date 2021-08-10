package wzmProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WzmService implements InterfaceWzmService {

		private static final Logger logger = LoggerFactory.getLogger(WzmService.class);

		private boolean isActive = false;
		 
		public void applyService() {
		// Function to apply Milling on the part Once in the WZM
				logger.info("Milling in progress...");
				isActive = true;
				
				try {
					Thread.sleep(5000); // 5 seconds hypothetical Milling time
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				logger.info("Milling finished.");
				isActive = false;
			}

		public boolean isActive() {
			return isActive;
		}
	}