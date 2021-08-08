package WZMprocess2;


public class Wzm implements WzmInterface {
	private WzmService service;

	public Wzm() {
		service = new WzmService();
	}
 
	public WzmService getService() {
		return service;
	}
 
}