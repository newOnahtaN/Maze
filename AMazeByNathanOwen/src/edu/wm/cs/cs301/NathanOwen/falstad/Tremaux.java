package edu.wm.cs.cs301.NathanOwen.falstad;

import java.util.ArrayList;
import java.util.Random;

import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Direction;
import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Turn;

public class Tremaux extends AutomaticDriver {
	
	BasicRobot robot;
	ArrayList<int[]> visited = new ArrayList<int[]>();
	ArrayList<int[]> visitedTwice = new ArrayList<int[]>();
	ArrayList<Direction> possDirsFirstChoice;
	ArrayList<Direction> possDirsSecondChoice;
	ArrayList<Direction> possDirsThirdChoice;
	
	
	
	public Tremaux(){
		super();
	}

	public Tremaux(String navigatorAlgorithm) {
		super(navigatorAlgorithm);
	}

	@Override
	public void setBasicRobot(BasicRobot robot){
		this.robot = (BasicRobot)robot;
	}
	
	
	@Override
	public boolean drive2Exit() throws Exception{
		Direction directionToRotate;
		int numPoss;
		Random random = new Random();
		int randSelection;
		
		if (robot.isInsideRoom()){
			actAsWallFollowerInitial();
		}
		
		
		while (this.robot.isAtGoal() == false){ 
			
			if (robot.maze.killDriver == true){
				return false;
			}
			
			if (robot.maze.pauseDriver == true){
				Thread.sleep(500);
				continue;
			}
			
			possDirsFirstChoice = new ArrayList<Direction>();
			possDirsSecondChoice = new ArrayList<Direction>();
			possDirsThirdChoice = new ArrayList<Direction>();
			
			if (robot.batteryLevel <= 0)
				return false;
			
			//If inside room, operate as a Wall-Follower
			if (robot.isInsideRoom()){
				actAsWallFollowerInRoom();
				continue;
			}
			
			
			if (robot.isAtJunction()){
				
				//check the cell to the left, see if we can move there, see if visited once
				if (robot.distanceToObstacle(Direction.LEFT) != 0){
					
					if (timesDirectionVisited(Direction.LEFT) == 0){
						possDirsFirstChoice.add(Direction.LEFT);
					}
					if (timesDirectionVisited(Direction.LEFT) == 1){
						possDirsSecondChoice.add(Direction.LEFT);
					}
					if (timesDirectionVisited(Direction.LEFT) >= 2){
						possDirsThirdChoice.add(Direction.LEFT);
					}
				}
				
				//check the cell to the right, see if we can move there, see if visited once
				if (senseRight(Direction.RIGHT) != 0){
					
					if (timesDirectionVisited(Direction.RIGHT) == 0){
						possDirsFirstChoice.add(Direction.RIGHT);
					}
					if (timesDirectionVisited(Direction.RIGHT) == 1){
						possDirsSecondChoice.add(Direction.RIGHT);
					}
					if (timesDirectionVisited(Direction.RIGHT) >= 2){
						possDirsThirdChoice.add(Direction.RIGHT);
					}
				}
				
				
				//check the cell to the front, see if we can move there, see if visited once
				if (robot.distanceToObstacle(Direction.FORWARD) != 0){
					
					if (timesDirectionVisited(Direction.FORWARD) == 0){
						possDirsFirstChoice.add(Direction.FORWARD);
					}
					if (timesDirectionVisited(Direction.FORWARD) == 1){
						possDirsSecondChoice.add(Direction.FORWARD);
					}
					if (timesDirectionVisited(Direction.FORWARD) >= 2){
						possDirsThirdChoice.add(Direction.FORWARD);
					}
				}
				
				int x = robot.getCurrentPosition()[0];
				int y = robot.getCurrentPosition()[1];
				
				if (possDirsFirstChoice.size() == 0 && timesVisited(x,y) == 0){
					robot.rotate(Turn.AROUND);
					setCurrentVisited();
					robot.move(1);	
					continue;
				}
				
			}
	
			else{
				if (robot.distanceToObstacle(Direction.FORWARD) == 0){
					robot.rotate(Turn.AROUND);//Getting here means there is a wall on both sides and in front; turn around
				}
			}
			
			
			
			
			
			//If we came to a junction and there were things that we had not visited, here we
			//choose randomly among them.
			if (possDirsFirstChoice.size() != 0){
				numPoss = possDirsFirstChoice.size();
				randSelection = random.nextInt(numPoss);
				directionToRotate = possDirsFirstChoice.get(randSelection);
				rotate(directionToRotate);
			}
			else{
				if (possDirsSecondChoice.size() != 0){
					numPoss = possDirsSecondChoice.size();
					randSelection = random.nextInt(numPoss);
					directionToRotate = possDirsSecondChoice.get(randSelection);
					rotate(directionToRotate);
				}
				else{
					if (possDirsThirdChoice.size() != 0){
						numPoss = possDirsThirdChoice.size();
						randSelection = random.nextInt(numPoss);
						directionToRotate = possDirsThirdChoice.get(randSelection);
						rotate(directionToRotate);
					}
				}
			}
			
			
			setCurrentVisited();
			robot.move(1);	
		}
		
		
		return true; //while loop has finished, return true
	}
	
	/*
	 * Adds the current position to the visited list if it hasn't been already.
	 */
	private void setCurrentVisited() throws Exception{
		int visitedX = robot.getCurrentPosition()[0];
		int visitedY = robot.getCurrentPosition()[1];
		visited.add(new int[]{visitedX, visitedY});

	}
	private void actAsWallFollowerInRoom() throws UnsupportedOperationException, Exception{
		if (robot.distanceToObstacle(Direction.LEFT) == 0){
			if (robot.distanceToObstacle(Direction.FORWARD) != 0){
				setCurrentVisited();
				robot.move(1);
			}
			else{
				robot.rotate(Turn.RIGHT);
				setCurrentVisited();
				robot.move(1);
			}
		}
		else{
			robot.rotate(Turn.LEFT);
			setCurrentVisited();
			robot.move(1);
		}
	}
	
	/*
	 * Used before the while loop in drive2Exit in order to have the robot act like a 
	 * wall-follower does if it gets stuck in a room at the very beginning. It also 
	 * acts like a wall follower in the while loop as well.
	 */
	private void actAsWallFollowerInitial() throws UnsupportedOperationException, Exception{
		if (robot.distanceToObstacle(Direction.LEFT) != 0){
			robot.rotate(Turn.LEFT);
			robot.move(robot.distanceToObstacle(Direction.FORWARD));
			robot.rotate(Turn.RIGHT);
			setCurrentVisited();
		}
	}
	
	/*
	 * This is used because sensing in the right direction is not permitted for this algorithm.
	 * Rotates right, senses forward, turns left again.
	 */
	private int senseRight(Direction right) throws Exception {
		robot.rotate(Turn.RIGHT);
		int distance = robot.distanceToObstacle(Direction.FORWARD);
		robot.rotate(Turn.LEFT);
		return distance;
	}
	/*
	 * Test if the coordinate provided isn't in the visited list.
	 */
	private int timesVisited(int visitedX, int visitedY) {
		int timesVisited = 0;
		for (int i = 0; i < visited.size(); i++){
			if (visited.get(i)[0] == visitedX && visited.get(i)[1] == visitedY)
				timesVisited += 1;
		}
		return timesVisited;
	}

	/*
	 * Return the number of times the relative direction from the robot's current direction has been visited.
	 */
	private int timesDirectionVisited(Direction direction) throws Exception{
		int x = robot.getCurrentPosition()[0];
		int y = robot.getCurrentPosition()[1];
		
		int dx = robot.getCurrentDirection()[0];
		int dy = robot.getCurrentDirection()[1];
		
		//Have the direction look in the opposite direction
		int arounddx = -dx;
		int arounddy = -dy;
		
		//Have the direction look to the right
		int rightdx = dy;
		int rightdy = -dx;
		
		//Have the direction look to the left
		int leftdx = -dy;
		int leftdy = dx;
		
		if (direction == Direction.FORWARD){
			return timesVisited(x+dx, y+dy);
		}
		else if (direction == Direction.BACKWARD){
			return timesVisited(x+arounddx, y+arounddy);
		}
		else if (direction == Direction.LEFT){
			return timesVisited(x+leftdx, y+leftdy);
		}
		else if (direction == Direction.RIGHT){
			return timesVisited(x+rightdx, y+rightdy);
		}
		
		return 0;
		
	}
	
	/*
	 * Provided a direction enum, rotate the robot in the appropriate direction.
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
