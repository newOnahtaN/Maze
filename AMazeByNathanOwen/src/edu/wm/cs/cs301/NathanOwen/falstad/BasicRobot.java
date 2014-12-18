package edu.wm.cs.cs301.NathanOwen.falstad;

import java.io.Serializable;

public class BasicRobot implements Robot{
	
	public Maze maze; //Main maze for reference
	public ManualDriver driver; //Parent driver for this class
	public float batteryLevel;
	public boolean stopped;
	public float quarterTurnVal = 3;
	public float moveVal = 5;
	public float sensingVal = 1;
	private boolean[] hasDistanceSensor = new boolean[4];
	private boolean hasRoomSensor;
	private boolean hasJunctionSensor;
	private int pathLength = 0;
	
	
	public BasicRobot(){
		this.hasJunctionSensor = true;
		this.hasRoomSensor = true;
		this.hasDistanceSensor = new boolean[]{true,true,true,true};
	}
	
	/**
	 * Alternative constructor that allows for disabling of certain sensors.
	 * 
	 * @param junction : set to true in order to use junction sensor, false to disable
	 * @param room : set to true in order to use junction sensor, false to disable
	 * @param distance : is an array of four boolean values that allows for distance sensing in the {forward, left, backward, right} directions.
	 */
	public BasicRobot(boolean junction, boolean room, boolean[] distance){
		this.hasJunctionSensor = junction;
		this.hasRoomSensor = room;
		if (distance.length == 4)
			this.hasDistanceSensor = distance;
		else
			throw new IllegalArgumentException("The third argument must be a boolean array of size 4.");
	}
	
	
	
//=========================================================Methods For Driving==========================================================//
	
	/**
	 * One of two methods to change the orientation of the robot. Receives either LEFT, RIGHT, or AROUND, and updates
	 * the current direction according to the parameter given.  This is implemented using the maze class' rotate() method
	 * which appropriately updates the dx,dy directions and the GUI as well. Subtracts 3 from the battery level, and 
	 * terminates if battery level reaches zero.
	 */
	@Override
	public void rotate(Turn turn) throws Exception {
		switch(turn){
		case LEFT:
			this.batteryLevel -= 3;
			
			hasStopped();
			
			this.maze.rotate(1);
			
			break;
		case RIGHT:
			this.batteryLevel -= 3;
			
			hasStopped();
			
			this.maze.rotate(-1);
			
			break;
			
		case AROUND:
			
			this.batteryLevel -= 6;
			
			hasStopped();
			
			this.maze.rotate(2);
			
			break;
		}
		
		
	}
	
	/**
	 * The driving method that robot uses to move forward in the maze.  Moves forward the amount of
	 * times specified by the given parameter. Implemented using the maze class' walk() method which
	 * appropriately adjusts the current position and updates the graphics. Subtracts 5 from the battery 
	 * level and terminates if battery level reaches zero.
	 */
	@Override
	public void move(int distance) throws Exception {


		for (int i = 0; i < distance; i++){
			
			this.batteryLevel -= 5;
			this.pathLength += 1;
			this.maze.walk(1);
			
			hasStopped();
		}
		
		//Diagnostic print statements for random exploring
		System.out.println("\n\nMove Number:  " + String.valueOf(getPathLength()) );
		System.out.println("	Current Battery Level:  " + String.valueOf(this.batteryLevel));
		
		/*
		if (hasDistanceSensor(Direction.FORWARD))
			System.out.println("	Distance to Obstacle:   " + String.valueOf(distanceToObstacle(Direction.FORWARD)));
		if (hasJunctionSensor())
			System.out.println("	Is at Junction:         " + String.valueOf(isAtJunction()));
		if (hasRoomSensor())
			System.out.println("	Is in room:             " + String.valueOf(isInsideRoom()));
		System.out.println("	Can see goal in front:  " + String.valueOf(canSeeGoal(Direction.FORWARD)));
		*/
		
	}
	
	
	
	
	/**
	 * Returns true if the robot is at the goal position. Uses maze.isEndPosition()
	 */
	@Override
	public boolean isAtGoal() {
		return this.maze.isEndPosition(this.maze.px, this.maze.py);
	}
	
	
	/**
	 * Returns true if the goal of the maze is in the direct line of sight of the robot. 
	 * Uses the distanceToObstacle method, which returns Integer.MAX_VALUE if there does not
	 * exist an obstacle in the line of sight provided. The only case where this is true is 
	 * if the exit is in the line of sight (there is an absence of a wall)
	 */
	@Override
	public boolean canSeeGoal(Direction direction) throws UnsupportedOperationException {
		
		//Make sure we can use the distance sensor for the method, reset when done.
		int distance = distanceToObstacle(direction);
		
		
		if (distance == Integer.MAX_VALUE)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the true if the robot has stopped, false if it has not.  Stored as a field variable.
	 * The Robot only stops if it has reached its destination or if it has run out of its battery.
	 * Updates the viewer if the battery runs out, because the maze does did not originally have this
	 * capability - this makes sure that the game terminates appropriatley.
	 */
	@Override
	public boolean hasStopped() {
		if (this.batteryLevel <= 0){
			System.out.println("The definitive value of the battery level is: " + String.valueOf(this.batteryLevel));
			this.stopped = true;
		
			//These methods update the GUI to let the game know to terminate when the battery runs out.
			//Use of these methods is mimicked from the walkFinish method in maze, which traditionally 
			//is where the game would end.
			this.maze.setState(Constants.STATE_FINISH);
			this.maze.notifyViewerRedraw();
		}
		
		if (isAtGoal()){
			this.stopped = true;
		}
		
		return this.stopped;
	}
	
	
//=======================================================Getters and Setters============================================================//
	
	/**
	 * Simple setter that provides a maze object for this class. This is required for most other functions of this class.
	 * Passing in a new maze will also reset the battery level and reset the stopped value to false. Also gives the 
	 * maze object a reference to this robot in order to provide movement notifications if needed. Method is used at the initialization
	 * of the Maze Application in the init() method at the same time that the maze object is created.
	 */
	@Override
	public void setMaze(Maze maze) {
		this.maze = maze;
		this.maze.robot = this; //Give the maze the ability to reference this robot for notification
		this.batteryLevel = 2500;
		this.pathLength = 0;
		this.stopped = false;
	}
	
	/**
	 * Returns the current position as an array {x,y} according to the maze object that this class 
	 * holds as a field variable. Throws a general exception if x,y is out of bounds of the dimensions
	 * of the maze.
	 */
	@Override
	public int[] getCurrentPosition() throws Exception {
		int[] result = {this.maze.px, this.maze.py};
		
		if (result[0] > this.maze.mazew -1 || result[0] < 0 || result[1] > this.maze.mazeh -1 || result[1] < 0)
			throw new Exception("Current Position is out of range");
		
		return result;
	}
	
	/**
	 * Returns the current direction according to this class' maze field object.
	 */
	@Override
	public int[] getCurrentDirection() {
		return new int[]{this.maze.dx, this.maze.dy};
	}
		
	/**
	 * Simple getter for the battery level.
	 */
	@Override
	public float getBatteryLevel() {
		return this.batteryLevel;
	}
	
	/**
	 * Setter for the battery level of this robot.
	 */
	@Override
	public void setBatteryLevel(float level) {
		this.batteryLevel = level;
	}
	
	/**
	 * Returns 12 for the energy of a full rotation.
	 */
	@Override
	public float getEnergyForFullRotation() {
		return this.quarterTurnVal * 4;
	}
	
	/**
	 * Returns 5 for the energy of a forward step.
	 */
	@Override
	public float getEnergyForStepForward() {
		return this.moveVal;
	}
	
	/**
	 * Getter for the path length of the driver.
	 * @return
	 */
	public int getPathLength(){
		return this.pathLength ;
	}
	
	/**
	 * Setter for the path length of the driver.  Used only to reset the path length
	 * to zero when the game is restarted. Called in the keydown method at restart.
	 * @param i
	 */
	public void setPathLength(int i){
		this.driver.setPathLength(i);
	}
	
//=======================================================Sensors and hasSensors========================================================//

	
	//=============================Sensors======================================//
	
	
	/**
	 *Returns true if the current position is at a junction, false otherwise. A junction is defined by a position
	 *that has neither a wall on its right or left. This is dependent on the perspective of the robot, hence the following logic:
	 *If x is zero, then the direction is either up or down
	 *If x isn't zero, then the direction is either left or right
	 *If the direction is up or down, then we can use the normal hasWallOnLeft/Right methods
	 *If the direction is left or right, the we must use the hasWallOnTop/Bottom methods
	 */
	@Override
	public boolean isAtJunction() throws UnsupportedOperationException {
		
		if (hasJunctionSensor()){
		
			//Using a sensor decreases the battery level by one
			this.batteryLevel -= 1;
			hasStopped(); //Will terminate process if battery is now zero
			
			int x;
			int y;
			
			try{
				x = getCurrentPosition()[0];
				y = getCurrentPosition()[1];}
			catch (Exception e){
				System.out.println("isAtJunction() returned false because current position was out of range");
				return false;
			}
			
			
			if (getCurrentDirection()[0] != 0) {
				if (this.maze.mazecells.hasNoWallOnTop(x, y) || this.maze.mazecells.hasNoWallOnBottom(x, y))
					return true;
			}
			else{
				if (this.maze.mazecells.hasNoWallOnLeft(x, y) || this.maze.mazecells.hasNoWallOnRight(x, y))
					return true;
			}
			
			return false;
		}
		else
			throw new UnsupportedOperationException("Juntion Sensor not available.");
		
	}
	
	
	/**
	 * Returns true if the current position is inside a room, false otherwise. Implemented using the 
	 * .isInRoom() method from the cells class that is a field variable of this robot's maze class.
	 */
	@Override
	public boolean isInsideRoom() throws UnsupportedOperationException {
		if (hasRoomSensor()){
		
			//Using a sensor decreases the battery level by one
			this.batteryLevel -= 1;
			hasStopped(); //Will terminate process if battery is now zero
			
			return this.maze.mazecells.isInRoom(this.maze.px, this.maze.py);	
		}
		else
			throw new UnsupportedOperationException("Room sensor not available.");
	}
	
	

	/**
	 * Returns the distance from the robot to a wall. 0 is returned if the there is wall on the tile that the robot is currently on. 
	 * Integer.MAX_VALUE- is returned if there is not obstacle to be seen in its direct path. The direction passed in is relative to the direction that
	 * the robot is currently looking. This operation is performed using the cells object that belongs to this class' maze field variable.
	 */
	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		if (hasDistanceSensor(direction)){
		
			//Subtract one from battery level because this is a sensor operation.
			this.batteryLevel -= 1;
			hasStopped(); //Will terminate process if battery is now zero
			
			int dx = getCurrentDirection()[0];
			int dy = getCurrentDirection()[1];
			
			//Change dx and dy to represent the direction relative to the robot's current direction
			int temp;
			if (direction == Direction.BACKWARD){
				dx = -dx;
				dy = -dy;
			}
			if (direction == Direction.LEFT){
				temp = dx;
				dx = -dy;
				dy = temp;
			}		
			if (direction == Direction.RIGHT){
				temp = dx;
				dx = dy;
				dy = -temp;
			}
			//If the direction is forward, we don't change anything.
			
			
			//Starting with the current cell, look at every cell in the current direction until we hit a wall.
			//Return the number of cells we looked at, not including the starting cell. Stop if the number of cells
			//we look at is greater than the width and height of the maze, and instead return Integer.MAX_VALUE.
			int count = 0;
			int tempX = this.maze.px;
			int tempY = this.maze.py;
		
			while (true){
				
				if (dx == 0 && dy == -1){ 
					if (this.maze.mazecells.hasWallOnTop(tempX, tempY))
						return count;
					tempY -= 1;
				}
				if (dx == 1 && dy == 0){
					if (this.maze.mazecells.hasWallOnRight(tempX, tempY))
						return count;
					tempX += 1;
				}
				if (dx == 0 && dy == 1){
					if (this.maze.mazecells.hasWallOnBottom(tempX, tempY))
						return count;
					tempY += 1;
				}
				if (dx == -1 && dy == 0){
					if (this.maze.mazecells.hasWallOnLeft(tempX, tempY))
						return count;
					tempX -= 1;
				}
				
				count += 1;
				
				//The below operation is equivalent to isAtGoal, but reimplemented in order to use the temp X,Y
				if (tempX < 0 || tempY < 0 || tempX >= this.maze.mazew  || tempY >= this.maze.mazeh)
					return Integer.MAX_VALUE;
			}
			
		}
		else
			throw new UnsupportedOperationException("Distance Sensor is unavailable.");
	
	}
	
	//===================================hasSensors==============================================//
	
	
	/**
	 * Returns true if this instance can use a junction sensor. Altered by constructor.
	 */
	@Override
	public boolean hasJunctionSensor() {
		return this.hasJunctionSensor;
	}

	/**
	 * Returns true if this instance can use a room sensor. Altered by constructor.
	 */
	@Override
	public boolean hasRoomSensor() {
		return this.hasRoomSensor;
	}
	

	/**
	 * Returns true if this instance can use a distance sensor. Altered by constructor.
	 */
	@Override
	public boolean hasDistanceSensor(Direction direction) {
		if (direction == Direction.FORWARD && this.hasDistanceSensor[0] == true)  
			return true;
		else if (direction == Direction.LEFT && this.hasDistanceSensor[1] == true)
			return true;
		else if (direction == Direction.BACKWARD && this.hasDistanceSensor[2] == true)
			return true;
		else if (direction == Direction.RIGHT && this.hasDistanceSensor[3] == true)
			return true;
		else
			return false;
	}
	
	
	
	
//===============================================Notification Methods=================================================//
	/**
	 * This method is only used by a manual driver to set this robot's parent as that driver.
	 * When a driver sets this object as its child, it will also call this method and have
	 * this method's parent be the driver. This is used specifically to create a chain from
	 * Maze to ManualDriver in order to give notifications from the keys.
	 * @param driver
	 */
	public void setParentDriver(ManualDriver driver){
		this.driver = driver;
	}
	
	/**
	 * This method is used by the keyDown method in maze in order to pass a notification
	 * to the driver for use. The notifications are ints {0, 1, 2, 3} that represent
	 * Up, Left, Down, Right. The Driver will act on these notifications immediately using
	 * its drive2Exit method.
	 * @param notice
	 * @throws Exception
	 */
	public void notifyRobot(int notice) throws Exception{
		if (this.maze.manualMode == true)
			driver.notifyManualDriver(notice);
		else
			;
	}


	

}
