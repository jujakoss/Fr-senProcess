package WZMprocess;

public interface Iwzm {

	enum State {
		READY, FINISHED, WORKING, OUT_OF_ORDER 
	}

	void executeService();

	State getState();

}
