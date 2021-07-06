package WZMprocess;

public interface Iwzm {

	enum State {
		MAINTAINING, FREE, WORKING, BROKEN
	}

	void executeService();

	State getState();

}
