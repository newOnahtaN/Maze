package edu.wm.cs.cs301.NathanOwen.falstad;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;

/**
 * This class encapsulates all access to a grid of cells. Each cell encodes whether walls or borders/bounds to rooms 
 * or to the outer border of the maze exist.
 * The class resulted from refactoring the int[][] cells area in the original Maze and Mazebuilder classes into a class of its own.
 * The internal two-dimensional array matches with a grid of cells as follows:
 * cells[0,y] form the left border, hence there is a wall on  left.
 * cells[width-1,y] form the right border, hence there is a wall on right.
 * cells[x,0] form the top border, hence there is a wall on top.
 * cells[x,height-1] form the bottom border, hence there is a wall on bottom.
 * The upper left corner is seen as position [0][0].
 * 
 * Note that for a calculated maze, at least one cell on the border will have a missing wall for an exit somewhere.
 * 
 * Walls and borders are separated concepts. A border is not removed by the maze generation procedure. It is used to mark 
 * the outside border of the maze but also internal rooms. Walls can be taken down by the maze generation procedure.
 * 
 * The internal encoding of walls for each cell into a single integer per cell is performed with bit operations (&,|) and 
 * thus error prone. An encapsulation within this class localizes all bit operations for this encoding.
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class Cells implements Serializable {
	
	int width;
	private int height ;
	private int[][] cells; // width x height array of cells, each cell contains an integer which encodes presence/absence of walls
	
	/**
	 * Constructor
	 * @param w width
	 * @param h height
	 * @precondition 0 < w, 0 < h
	 */
	public Cells(int w, int h) {
		width = w ;
		height = h ;
		cells = new int[w][h];
	}

	/**
	 * Constructor that dimensions and initializes cells with the values from the given matrix.
	 * @param input provides input data to copy cell content from
	 * @precondition input != null
	 */
	public Cells(int[][] input){
		this(input.length, input[0].length);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = input[i][j];
			}
		}
	}
	
	/**
	 * Initialize maze such that all cells have not been visited, all walls inside the maze are up,
	 * and borders form a rectangle on the outside of the maze.
	 */
	public void initialize() {
		int x, y;
	
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				setBitToOne(x, y, (Constants.CW_VISITED | Constants.CW_ALL));
			}
		} 
		for (x = 0; x < width; x++) {
			setBitToOne(x, 0, Constants.CW_TOP_BOUND);
			setBitToOne(x, height-1, Constants.CW_BOT_BOUND);
		} 
		for (y = 0; y < height; y++) {
			setBitToOne(0, y, Constants.CW_LEFT_BOUND);
			setBitToOne(width-1, y, Constants.CW_RIGHT_BOUND);
		}
	}
	
	/**
	 * Equals method that checks if the other object matches in dimensions and content.
	 * @param other provides fully functional cells object to compare its content
	 */
	public boolean equals(Object other){
		// trivial special cases
		if (this == other)
			return true ;
		if (null == other)
			return false ;
		if (getClass() != other.getClass())
			return false ;
		// general case
		final Cells o = (Cells)other ; // type cast safe after checking class objects
		if ((width != o.width)||(height != o.height))
			return false ;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (cells[i][j] != o.cells[i][j])
					return false ;
			}
		}
		return true ;
	}

	/**
	 * Get the value of a cell at the given position (x,y).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 * @return value with internal encoding of walls and other attributes for the cell at position (x,y)
	 */
	public int getValueOfCell( int x, int y )
	{
		return cells[x][y] ;
	}
	

	/**
	 * checks if adjacent cells (x,y) and its neighbor (x+dx,y+dy) are not separated by a border 
	 * and (x+dx,y+dy) has not been visited before.
	 * This method is used in the MazeBuilder class.
	 * @precondition borders limit the outside of the maze area
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @precondition 0 <= x < width, 0 <= y < height
	 * @return true if neighbor at (x+dx,y+dy) is new and wall can be taken down, false otherwise
	 */
	public boolean canGo(int x, int y, int dx, int dy) {
		// borders limit rooms (but for doors) and the outside limit of the maze
		if (hasMaskedBitsTrue(x, y, (getBit(dx, dy) << Constants.CW_BOUND_SHIFT)))
			return false;
		// if there is no border, neighbor should be in legal range of values
		// return true if neighbor has not been visited before
		return isFirstVisit(x+dx, y+dy);
	}

	/// Methods that deal with visiting a particular cell //////////////////////////////////
	// life cycle of visited flag
	// stage 1: 0 after instantiation
	// stage 2: 1 after initialization with method initialize()
	// stage 3: 0 after any call to method setCellAsVisited()
	// it is used to differentiate new cells that were not explored from
	// cells that have been visited and explored
	/**
	 * Tells if the given position is visited for the first time.
	 * This is true after cells.initialize() and before setCellAsVisited(x,y).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position is visited for the first time
	 */
	private boolean isFirstVisit(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_VISITED);
	}
	
	/**
	 * Marks the given cell at position (x,y) as visited
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setCellAsVisited(int x, int y) {
		setBitToZero(x,y,Constants.CW_VISITED) ; 
	}
	
	/**
	 * Establish exit position by breaking down wall to outside area.
	 * If (x,y) is not located next to an outside wall, the method no effect.
	 * @param x
	 * @param y
	 */
	public void setExitPosition(int x, int y) {
		int bit = 0;
		// find direction to outside wall
		if (x == 0)
			bit = Constants.CW_LEFT;
		else if (x == width-1)
			bit = Constants.CW_RIGHT;
		else if (y == 0)
			bit = Constants.CW_TOP;
		else if (y == height-1)
			bit = Constants.CW_BOT;
		else
		{
			dbg("set exit position failed for position " + x + ", " + y);
			return ;
		}
		setBitToZero(x, y, bit);
		//System.out.println("exit position set to zero: " + remotex + " " + remotey + " " + bit + ":" + cells.hasMaskedBitsFalse(remotex, remotey, bit)
		//		+ ", Corner case: " + ((0 == remotex && 0 == remotey) || (0 == remotex &&  height-1 == remotey) || (width-1 == remotex && 0 == remotey) || (width-1 == remotex && height-1 == remotey)));
	}
	/**
	 * Tells if current position is an exit position. 
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 * @return true if position is on the border and there is no wall to the outside, false otherwise
	 */
	public boolean isExitPosition(int x, int y) {
		int bit = 0;
		// find direction to outside wall as in method setExitPosition
		if (x == 0)
			bit = Constants.CW_LEFT;
		else if (x == width-1)
			bit = Constants.CW_RIGHT;
		else if (y == 0)
			bit = Constants.CW_TOP;
		else if (y == height-1)
			bit = Constants.CW_BOT;
		else
		{
			return false ;
		}
		return hasMaskedBitsFalse(x, y, bit) ;
	}
	// Methods that deal with rooms ///////////////////////////////////
	// lifecycle of room bit
	// stage 1: 0 after instantiation, unchanged in initialization phase
	// stage 2: 1 by calling markAreaAsRoom or by setInRoomToOne
	//
	/**
	 * Sets the InRoom bit to one for a given cell and direction
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setInRoomToOne(int x, int y) {
		setBitToOne(x, y, Constants.CW_IN_ROOM);
	}
	/**
	 * Tells if the given position is inside a room.
	 * This is false after cells.initialize() and before calling setInRoomToOne() or markAreaAsRoom().
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 * @return true if (x,y) position resides in an area marked as a room before, false otherwise
	 */
	public boolean isInRoom(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_IN_ROOM);
	}
	/**
	 * Checks if there is a cell in the given area that belongs to a room.
	 * The first corner (rx,ry) is at the upper left position, the second corner (rxl,ryl) is at the lower right position.
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 * @precondition 0 <= rx <= rxl < width, 0 <= ry <= ryl < height
	 * @return true if area contains a cell that is already in a room or if it is too close to the border, false otherwise
	 */
	public boolean areaOverlapsWithRoom(int rx, int ry, int rxl, int ryl) {
		// loop start and end are chosen such that there is at least one cell 
		// between area and any existing room or the outside border
		int startX = rx-1 ;
		int startY = ry-1 ;
		int stopX = rxl+1 ;
		int stopY = ryl+1 ;
		// check if room is too close to border
		if (((startX < 0)||(startY < 0))||((stopX >= width)||(stopY >= height)))
			return true ;
		// check area
		for (int x = startX; x <= stopX; x++)
		{
			for (int y = startY; y <= stopY; y++)
			{
				if (isInRoom(x, y))
					return true ;
			}
		}
		return false ;
	}
	/**
	 * Marks a given area as a room on the maze and positions up to five doors randomly.
	 * The first corner is at the upper left position, the second corner is at the lower right position.
	 * Assumes that given area is located on the map and does not intersect with any existing room.
	 * The walls of a room are declared as borders to prevent the generation mechanism from tearing them down.
	 * Of course there must be a few segments where doors can be created so the border protection is removed there.
	 * @param rw room width
	 * @param rh room height
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 */
	public void markAreaAsRoom(int rw, int rh, int rx, int ry, int rxl, int ryl) {
		// clear all cells in area of room from all walls and borders
		// mark all cells in area as being inside the room
		int x;
		int y;
		for (x = rx; x <= rxl; x++)
			for (y = ry; y <= ryl; y++) { 
				setAllToZero(x, y);
				setInRoomToOne(x, y);
			} 
		// set bounds at the perimeter
		encloseArea(rx, ry, rxl, ryl);
		// knock down some walls for doors
		int wallct = (rw+rh)*2; // counter for the total number of walls
		SingleRandom random = SingleRandom.getRandom() ;
		// check at most 5 walls
		for (int ct = 0; ct != 5; ct++) { 
			int door = random.nextIntWithinInterval(0, wallct-1); // pick a random wall
			// calculate position and direction of this wall
			int dx, dy;
			if (door < rw*2) {
				y = (door < rw) ? 0 : rh-1;
				dy = (door < rw) ? -1 : 1;
				x = door % rw;
				dx = 0;
			} else {
				door -= rw*2;
				x = (door < rh) ? 0 : rw-1;
				dx = (door < rh) ? -1 : 1;
				y = door % rh;
				dy = 0;
			} 
			// tear down the border protection.
			// It remains a wall that the generation mechanism can then tear down.
			deleteBound(x+rx, y+ry, dx, dy);
		}
	}
	/**
	 * Sets bounds on perimeter on internal area with bound and wall to enclose area.
	 * Bounds and walls are added from both directions.
	 * with upper left corner (rx,ry), lower right corner (rxl,ryl).
	 * @param rx
	 * @param ry
	 * @param rxl
	 * @param ryl
	 */
	private void encloseArea(int rx, int ry, int rxl, int ryl) {
		// add a bound and a wall all around the area 
		// top and bottom
		for (int x = rx; x <= rxl; x++) {
			addBoundWall(x, ry, 0, -1);
			addBoundWall(x, ryl, 0, 1);
		} 
		// left and right
		for (int y = ry; y <= ryl; y++) {
			addBoundWall(rx, y, -1, 0);
			addBoundWall(rxl, y, 1, 0);
		}
	}
	// Methods that deal with walls and bounds  ///////////////////////	
	/**
	 * Sets the wall bit to zero for a given cell and direction
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	private void setWallToZero(int x, int y, int dx, int dy) {
		setBitToZero(x, y, getBit(dx, dy));
	}
	/**
	 * Sets the bound bit to zero for a given cell and direction
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setBoundToZero(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		setBitToZero(x,y,(bit << Constants.CW_BOUND_SHIFT)) ; 
	}
	
	/**
	 * Sets the bound and wall bit to one for a given cell and direction
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setBoundAndWallToOne(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		setBitToOne(x, y, (bit | (bit<< Constants.CW_BOUND_SHIFT)));
	}

	/**
	 * Sets the bit for the top wall to 1
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setTopToOne(int x, int y) {
		setBitToOne(x, y, Constants.CW_TOP);
	}

	/**
	 * Delete a border/bound between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom.
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	private void deleteBound(int x, int y, int dx, int dy) {
		// same code as for delwall but we need to shift the bit for the direction to the byte that encodes the bounds
		setBoundToZero(x, y, dx, dy);
		setBoundToZero(x+dx, y+dy, -dx, -dy) ;
	}

	/**
	 * Add a wall and a border/bound between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom.
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	public void addBoundWall(int x, int y, int dx, int dy) {

		setBoundAndWallToOne(x, y, dx, dy);
		setBoundAndWallToOne(x+dx, y+dy, -dx, -dy);
	}
	/**
	 * Delete a wall between to adjacent cells (x,y) and (x+dx,y+dy).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	public void deleteWall(int x, int y, int dx, int dy) {
		// delete wall on (x,y)
		setWallToZero(x, y, dx, dy);
		// delete same wall but for adjacent cell
		setWallToZero(x+dx, y+dy, -dx, -dy);
		/////////////////// THE FOLLOWING 2 LINES ARE USED FOR GRADING PROJECT 2, DO NOT ALTER OR DELETE /////////////////
		if (deepdebugWall) // for debugging: track sequence of walls that are deleted
			logWall( x,  y,  dx,  dy);
		/////////////////// END OF SPECIAL CODE FOR GRADING //////////////////////////////////////////////////////////////
	}

	//////////////////// get methods (is..., has...) for various attributes ///////////////////////


	/**
	 * Tells if the given position has a wall on its right side (or east position).
	 * This is true after cells.initialize() and before deleting this wall. 
	 * A wall can be deleted by directly calling deleteWall() or by removing all walls within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position has wall on right side.
	 */
	public boolean hasWallOnRight(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_RIGHT);
	}
	/**
	 * Tells if the given position has a wall on its left side (or west position).
	 * This is true after cells.initialize() and before deleting this wall. 
	 * A wall can be deleted by directly calling deleteWall() or by removing all walls within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position has wall on left side.
	 */
	public boolean hasWallOnLeft(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_LEFT);
	}
	/**
	 * Tells if the given position has a wall on its top side (or north position).
	 * This is true after cells.initialize() and before deleting this wall. 
	 * A wall can be deleted by directly calling deleteWall() or by removing all walls within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position has wall on top side.
	 */
	public boolean hasWallOnTop(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_TOP);
	}
	/**
	 * Tells if the given position has a wall on its bottom side (or south position).
	 * This is true after cells.initialize() and before deleting this wall. 
	 * A wall can be deleted by directly calling deleteWall() or by removing all walls within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position has wall on bottom side.
	 */
	public boolean hasWallOnBottom(int x, int y) {
		return hasMaskedBitsTrue(x, y, Constants.CW_BOT);
	}
	
	////// convenience methods for readability, negated existence test for a specific wall
	public boolean hasNoWallOnBottom(int x, int y) {
		return !hasMaskedBitsTrue(x, y, Constants.CW_BOT);
	}
	public boolean hasNoWallOnTop(int x, int y) {
		return !hasMaskedBitsTrue(x, y, Constants.CW_TOP);
	}
	public boolean hasNoWallOnLeft(int x, int y) {
		return !hasMaskedBitsTrue(x, y, Constants.CW_LEFT);
	}
	public boolean hasNoWallOnRight(int x, int y) {
		return !hasMaskedBitsTrue(x, y, Constants.CW_RIGHT);
	}
	
	////////////////// low level methods operating on bits and bitmasks //////////////////////////////////////////
	//  long term goal is to make all of these methods private to encapsulate the encoding ///////////////////////
	/**
	 * sets given bit in to zero in given cell
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param cw_bit like CW_LEFT, CW_RIGHT, CW_TOP, CW_BOTTOM
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setBitToZero(int x, int y, int cw_bit) {
		cells[x][y] &= ~cw_bit;
	}
	/**
	 * Sets all wall bits to zero for a given cell
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setAllToZero(int x, int y) {
		setBitToZero(x, y, Constants.CW_ALL) ;
	}

	public boolean hasMaskedBitsTrue(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) != 0;
	}
	public boolean hasMaskedBitsFalse(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) == 0;
	}
	// unclear at this point why greater is checked instead of inequality to zero in code
	// method resulted from refactoring
	public boolean hasMaskedBitsGTZero(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) > 0;
	}
	/**
	 * encodes (dx,dy) into a bit pattern for right, left, top, bottom direction
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @return bit pattern, 0 in case of an error
	 */
	private int getBit(int dx, int dy) {
		int bit = 0;
		switch (dx + dy * 2) {
		case 1:  bit = Constants.CW_RIGHT; break; //  dx=1,  dy=0
		case -1: bit = Constants.CW_LEFT;  break; //  dx=-1, dy=0
		case 2:  bit = Constants.CW_BOT;   break; //  dx=0,  dy=1
		case -2: bit = Constants.CW_TOP;   break; //  dx=0,  dy=-1
		default: dbg("getBit problem "+dx+" "+dy); break;
		}
		return bit;
	}
	/**
	 * Sets bits to 1 for given bitmask
	 * @param x  coordinate of cell
	 * @param y coordinate of cell
	 * @param bitmask
	 * @precondition 0 <= x < width, 0 <= y < height
	 */
	public void setBitToOne(int x, int y, int bitmask) {
		cells[x][y] |= bitmask ;
	}

	///////////////// code for debugging ///////////////////////////////////////
	private void dbg(String str) {
		System.out.println("Cells: "+str);
	}
	/**
	 * Methods dumps internal data into a string, intended usage is for debugging purposes. 
	 * Maze is represent as a matrix of integer values.
	 */
	public String toString() {
		String s = "" ;
		for (int i = 0 ; i < width ; i++)
		{
			for (int j = 0 ; j < height ; j++)
				s += " i:" + i + " j:" + j + "=" + cells[i][j] ;
			s += "\n" ;
		}
		return s ;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// THE FOLLOWING CODE IS USED FOR GRADING PROJECT 2 ///////////////////////////////
	//////////////////////////// DO NOT ALTER THE CODE BELOW ////////////////////////////////////////////////////
	// flag to trigger that a log is constructed that lists the sequence of walls that are deleted
	static boolean deepdebugWall = false;
	static final String deepedebugWallFileName = "logDeletedWalls.txt" ;
	StringBuffer traceWall = (deepdebugWall) ? new StringBuffer("x  y  dx  dy\n") : null ;

	/**
	 * Append wall information to logging data. Currently used to log the sequence of walls that are deleted in the maze generation phase
	 * @param x current position, x coordinate
	 * @param y current position, y coordinate
	 * @param dx direction, x coordinate, -1 <= dx <= 1
	 * @param dy direction, y coordinate, -1 <= dy <= 1
	 */
	private void logWall(int x, int y, int dx, int dy) {
		if (null != traceWall)
		{
			traceWall.append(x + " " + y + " " + dx + " " + dy + "\n");
		}
	}
	/**
	 * Write log data to given file
	 * @param filename
	 */
	public void saveLogFile( String filename )
	{
		try {  
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));  
	        out.write(traceWall.toString());   
	        out.close(); 
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}


	
}