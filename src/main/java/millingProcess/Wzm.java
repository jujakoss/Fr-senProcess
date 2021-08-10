package millingProcess;


public class Wzm implements InterfaceWzm {
	private WzmService service;

	public Wzm() {
		service = new WzmService();
	}
 
	public WzmService getService() {
		return service;
	}
 
}