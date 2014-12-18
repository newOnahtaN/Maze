package edu.wm.cs.cs301.NathanOwen.falstad;

import java.util.ArrayList;
import java.util.Random;

import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Direction;
import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Turn;

public class Gambler extends AutomaticDriver {
	
	BasicRobot robot;

	
	public Gambler(){
		super();
	}

	public Gambler(String navigatorAlgorithm) {
		super(navigatorAlgorithm);
	}

	@Override
	public void setBasicRobot(BasicRobot robot){
		this.robot = robot;
	}
	
	@Override
	public boolean drive2Exit() throws Exception{
		Direction directionToRotate;
		ArrayList<Direction> possDirs;
		int numPoss;
		Random random = new Random();
		int randSelection;
		
		while (this.robot.isAtGoal() == false){ 
			
			if (robot.maze.killDriver == true){
				return false;
			}
			
			if (robot.maze.pauseDriver == true){
				Thread.sleep(500);
				continue;
			}
			
			possDirs = new ArrayList<Direction>();
			
			if (robot.batteryLevel <= 0)
				return false;
			
			//Sense in all directions to get possible moves
			if (robot.distanceToObstacle(Direction.FORWARD) != 0)
				possDirs.add(Direction.BACKWARD);
			if (robot.distanceToObstacle(Direction.LEFT) != 0)
				possDirs.add(Direction.RIGHT);
			
			robot.rotate(Turn.AROUND);
			
			if (robot.distanceToObstacle(Direction.FORWARD) != 0)
				possDirs.add(Direction.FORWARD);
			if (robot.distanceToObstacle(Direction.LEFT) != 0)
				possDirs.add(Direction.LEFT);
			
			
			//Randomly choose of the possibilities to rotate.
			numPoss = possDirs.size();
			randSelection = random.nextInt(numPoss);
			directionToRotate = possDirs.get(randSelection);
			
			rotate(directionToRotate);
			
			robot.move(1);
			
		}
		return true;
	}

	/*
	 * Given a direction enum, this method has the robot rotate appropriately
	 */
	private void rotate(Direction directionToRotate) throws Exception {
		if (directionToRotate == Direction.LEFT)
			robot.rotate(Turn.LEFT);
		if (directionToRotate == Direction.RIGHT)
			robot.rotate(Turn.RIGHT);
		if (directionToRotate == Direction.BACKWARD)
			robot.rotate(Turn.AROUND);
		
	}
}
