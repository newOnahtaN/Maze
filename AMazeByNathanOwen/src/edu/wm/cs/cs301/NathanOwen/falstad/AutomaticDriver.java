package edu.wm.cs.cs301.NathanOwen.falstad;

import android.os.AsyncTask;


public class AutomaticDriver extends Thread implements RobotDriver {

	private int pathLength;
	private int[] dimensions = new int[2];
	private Distance distance;
	private Robot robot;
	private BasicRobot basicRobot;
	private String navigatorAlgorithm;
	private RobotDriver childDriver;
	
	public AutomaticDriver(){
		super();
	}
	
	public AutomaticDriver(String navigatorAlgorithm){
		this.navigatorAlgorithm = navigatorAlgorithm;
		System.out.println("Automatic driver recieved direction to run with the " + navigatorAlgorithm + " algorithm.");
	}

//===================================================Main Driver Method====================================================//
	@Override
	public void run() {
		try {
			drive2Exit();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public boolean drive2Exit() throws Exception {
		//Have the maze and solution shown on screen.
		this.basicRobot.maze.mapMode = true;
		this.basicRobot.maze.showSolution = true;
		this.basicRobot.maze.showMaze = true;
		this.basicRobot.maze.notifyViewerRedraw();
		
		
		if (navigatorAlgorithm.equals("Wizard")){
			
			System.out.println("Alla-kazam!");
			Wizard wizard = new Wizard();
			wizard.setDistance(this.distance);
			wizard.setBasicRobot(this.basicRobot);
			this.childDriver = wizard;
			if (wizard.drive2Exit())
				return true;
			else
				return false;
		}
		if (navigatorAlgorithm.equals("Wall-Follower")){
				System.out.println("Hug the wall until you're free!");
				WallFollower wallFollower = new WallFollower();
				wallFollower.setBasicRobot(this.basicRobot);
				this.childDriver = wallFollower;
				if (wallFollower.drive2Exit())
					return true;
				else
					return false;
		}
		if (navigatorAlgorithm.equals("Gambler")){
				System.out.println("I have no idea what I'm doing.");
				Gambler gambler = new Gambler();
				gambler.setBasicRobot(this.basicRobot);
				this.childDriver = gambler;
				if (gambler.drive2Exit())
					return true;
				else
					return false;
		}
		if (navigatorAlgorithm.equals("Tremaux")){
				System.out.println("Which way did I come from again?");
				Tremaux tremaux = new Tremaux();
				tremaux.setBasicRobot(this.basicRobot);
				this.childDriver = tremaux;
				if (tremaux.drive2Exit())
					return true;
				else
					return false;
		}
		return true;
	}
	
//======================================================Getters and Setters=================================================//
	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		this.robot = (BasicRobot)r;
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
		return this.robot.getBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return this.childDriver.getPathLength();
	}
	
	/**
	 * Used by the robot class in order to reset the path length to zero when the
	 * maze application is restarted.
	 * @param i
	 */
	public void setPathLength(int i) {
		this.pathLength = i;	
	}
	public void setBasicRobot(BasicRobot r){
		this.basicRobot = r;
	}
	







}
