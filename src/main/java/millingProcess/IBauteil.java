package millingProcess;


public interface IBauteil {

	enum State {
		NON_PROCESSED, PROCESSED
	}

	void requestedService();

	State getState();

}
