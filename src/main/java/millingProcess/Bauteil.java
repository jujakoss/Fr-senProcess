package millingProcess;

public class Bauteil implements IBauteil {

	private State state;

	@Override
	public void requestedService() {
	}

	@Override
	public State getState() {
		return state;
	}

}
