package WZMprocess;

public class Roboter implements IRoboter{
	private RoboterTask task;
	
	public Roboter() {
		task = new RoboterTask();
	}
	
	@Override
	public RoboterTask getTask() {
		return task;
	}
	
}
