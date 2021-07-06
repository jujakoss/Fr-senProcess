package WZMprocess;

public class Wzm implements Iwzm {

	private State state;

	@Override
	public void executeService() {
		// do stuff
		try {
			state = State.WORKING;
			Thread.sleep(1000);
			state = State.FREE;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public State getState() {
		return state;
	}

}
