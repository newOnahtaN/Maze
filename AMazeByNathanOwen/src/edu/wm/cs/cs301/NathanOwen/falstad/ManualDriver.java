package edu.wm.cs.cs301.NathanOwen.falstad;

import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Turn;

public class ManualDriver implements RobotDriver {
	
	public int notice;
	private int pathLength;
	private int[] dimensions = new int[2];
	private Distance distance;
	private Robot robot;
	private BasicRobot robotNotifier;

//===================================================Main Driver Method====================================================//
	@Override
	public boolean drive2Exit() throws Exception {
		
		//This is where an algorithm might be implemented using the different sensors available to us.
		
		
		switch(this.notice){
		case 0: //Up arrow
			this.robot.move(1);
			this.pathLength += 1;
			break;
		case 1: //Left arrow
			this.robot.rotate(Turn.LEFT);
			break;
		case 2: //Right arrow
			this.robot.rotate(Turn.RIGHT);
			break;
		case 3: //Down arrow
			this.robot.rotate(Turn.AROUND);
			break;
		}
		
		if (this.robot.hasStopped())
			return true;
		else
			return false;
	}
	
//======================================================Getters and Setters=================================================//
	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		this.robot = r;

	}

	@Override
	public void setDimensions(int width, int height) {
		this.dimensions[0] = width;
		this.dimensions[1] = height;
	}

	@Override
	public void setDistance(Distance distance) {
		this.distance = distance;

	}
	@Override
	public float getEnergyConsumption() {
		return this.robotNotifier.getBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return this.pathLength;
	}
	
	/**
	 * Used by the robot class in order to reset the path length to zero when the
	 * maze application is restarted.
	 * @param i
	 */
	public void setPathLength(int i) {
		this.pathLength = i;	
	}

	
//=======================================Notifier Methods=============================================//
	
	public void setRobotNotifier(BasicRobot robot){
		this.robotNotifier = robot;
		this.robotNotifier.setParentDriver(this);
	}
	
	public void notifyManualDriver(int notice) throws Exception{
		this.notice = notice;
		drive2Exit();
	}


}
