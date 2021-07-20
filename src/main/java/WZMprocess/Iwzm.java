package WZMprocess;

public interface Iwzm {

	enum State {
		OUT_OF_ORDER, READY, WORKING
	}

	void executeService();

	State getState();

}
