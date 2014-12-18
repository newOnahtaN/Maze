package edu.wm.cs.cs301.NathanOwen.falstad;

import edu.wm.cs.cs301.NathanOwen.falstad.Robot.Turn;

public class Wizard extends AutomaticDriver {
	
	BasicRobot robot;
	Distance distances;
	Cells cells;
	boolean adjacentMoveIsEnd = false;
	enum CardDirection{
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
	
	public Wizard(){
		super();
	}

	public Wizard(String navigatorAlgorithm) {
		super(navigatorAlgorithm);
	}

	@Override
	public void setBasicRobot(BasicRobot robot){
		this.robot = robot;
		this.cells = this.robot.maze.getCells();
	}
	
	@Override
	public void setDistance(Distance distances){
		this.distances = distances;
	}
	
	
	@Override
	public boolean drive2Exit() throws Exception{
		CardDirection directionToMove;
		
		while (this.adjacentMoveIsEnd == false){ 
			
			if (robot.maze.killDriver == true){
				return false;
			}
			
			if (robot.maze.pauseDriver == true){
				Thread.sleep(500);
				continue;
			}
			
			
			
			if (robot.batteryLevel <= 0)
				return false;
			
			int x = robot.getCurrentPosition()[0];
			int y = robot.getCurrentPosition()[1];
			
			directionToMove = chooseDirection(x,y);
			moveCardDirection(directionToMove);
		}
		
		return true;
	}
	
	/*
	 * Given a cardinal direction, this method has the robot rotate toward that cardinal direction.
	 */
	private void moveCardDirection(CardDirection directionToMove) throws Exception {
		int dx = robot.getCurrentDirection()[0];
		int dy = robot.getCurrentDirection()[1];
		
		int xNeeded = getDirectionCoords(directionToMove)[0];
		int yNeeded = getDirectionCoords(directionToMove)[1];
		
		
		//Transform temps of dx and dy to resemble the left direction.
		int Rightdx = dy;
		int Rightdy = -dx;
		
		//Transform temps of dx and dy to resemble the right direction.
		int Leftdx = -dy;
		int Leftdy = dx;
		
		//Transform temps of dx and dy to resemble the around direction.
		int Arounddx = -dx;
		int Arounddy = -dy;
		
		
		if (Arounddx == xNeeded && Arounddy == yNeeded){
			robot.rotate(Turn.AROUND);
		}
		if (Leftdx == xNeeded && Leftdy == yNeeded){
			robot.rotate(Turn.LEFT);
		}
		if (Rightdx == xNeeded && Rightdy == yNeeded){
			robot.rotate(Turn.RIGHT);
		}
		
		robot.move(1);	
	}

	
	/*
	 * This method chooses the cardinal direction that the robot should move based on the 
	 * cell's surrounding distances information.
	 */
	public CardDirection chooseDirection(int x, int y){
		int lowestDistance = -1;
		CardDirection cardDirection = CardDirection.NORTH; // Arbitrary
		
		if (cells.hasNoWallOnLeft(x,y)){
			
			if (this.robot.maze.isEndPosition(x-1, y)){
				this.adjacentMoveIsEnd = true;
				return CardDirection.WEST;
			}
			
			if (distances.getDistance(x-1, y) < lowestDistance || lowestDistance == -1){
				cardDirection = CardDirection.WEST;
				lowestDistance = distances.getDistance(x-1, y);
			}
		}

		if (cells.hasNoWallOnRight(x,y)){
			
			if (this.robot.maze.isEndPosition(x+1, y)){
				this.adjacentMoveIsEnd = true;
				return CardDirection.EAST;
			}
			
			if (distances.getDistance(x+1, y) < lowestDistance || lowestDistance == -1){
				cardDirection = CardDirection.EAST;
				lowestDistance = distances.getDistance(x+1, y);
			}
		}

		if (cells.hasNoWallOnBottom(x,y)){
			
			if (this.robot.maze.isEndPosition(x, y+1)){
				this.adjacentMoveIsEnd = true;
				return CardDirection.SOUTH;
			}
			
			if (distances.getDistance(x, y+1) < lowestDistance || lowestDistance == -1){
				cardDirection = CardDirection.SOUTH;
				lowestDistance = distances.getDistance(x, y+1);
			}
		}

		if (cells.hasNoWallOnTop(x,y)){ 
			
			if (this.robot.maze.isEndPosition(x, y-1)){
				this.adjacentMoveIsEnd = true;
				return CardDirection.NORTH;
			}
			
			if (distances.getDistance(x, y-1) < lowestDistance || lowestDistance == -1){
				cardDirection = CardDirection.NORTH;
				lowestDistance = distances.getDistance(x, y-1);
			}
		}

		return cardDirection;
	}
	
	/*
	 * Given a cardinal direction, this class returns the appropriate dx and dy values.
	 */
	public int[] getDirectionCoords(CardDirection direction){
		if (direction == CardDirection.NORTH)
			return new int[]{0,-1};
		if (direction == CardDirection.SOUTH)
			return new int[]{0,1};
		if (direction == CardDirection.EAST)
			return new int[]{1,0};
		return new int[]{-1,0}; //Returns west
		
	}
	
	
}
