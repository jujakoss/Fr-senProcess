package WZMprocess;

public class Wzm implements Iwzm {

	private State state;

	@Override
	public void executeService() {
		// testing Fräsen Operation as an excuted service
		try {
			//random testing scenario 
			state = State.WORKING;
			Thread.sleep(1000);
			state = State.READY;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public State getState() {
		return state;
	}

}
