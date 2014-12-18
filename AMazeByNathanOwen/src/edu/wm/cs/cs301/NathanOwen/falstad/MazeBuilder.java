package edu.wm.cs.cs301.NathanOwen.falstad;

import android.os.AsyncTask;


/**
 * This class has the responsibility to create a maze of given dimensions (width, height) together with a solution based on a distance matrix.
 * The Maze class depends on it. The MazeBuilder performs its calculations within its own separate thread such that communication between 
 * Maze and MazeBuilder operates as follows. Maze calls the build() method and provides width and height. Maze has a call back method newMaze that
 * this class calls to communicate a new maze and a BSP root node and a solution. 
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class MazeBuilder extends AsyncTask<Void,Void,Integer> {
	// Given input information: 
	public int width, height ; 	// width and height of maze, 
	Maze maze; 				// reference to the maze that is constructed, results are returned by calling maze.newMaze(..)
	public int rooms; 		// requested number of rooms in maze, a room is an area with no walls larger than a single cell
	public int expectedPartiters; 	// user given limit for partiters
	
	// Produced output information to create the new maze
	// root, cells, dists, startx, starty
	public int startx, starty ; // starting position inside maze for entity to search for exit
	// conventional encoding of maze as a 2 dimensional integer array encapsulated in the Cells class
	// a single integer entry can hold information on walls, borders/bounds
	public Cells cells; // the internal representation of a maze as a matrix of cells
	public Distance dists ; // distance matrix that stores how far each position is away from the exit positino

	// class internal local variables
	protected SingleRandom random ; // random number stream, used to make randomized decisions, e.g for direction to go

	public Thread buildThread; // computations are performed in own separated thread with this.run()

	//int colchange; // randomly selected in run method of this thread, used as parameter to Segment class constructor

	/**
	 * Constructor for a randomized maze generation
	 */
	public MazeBuilder(){
		random = SingleRandom.getRandom();
	}
	/**
	 * Constructor with option to make maze generation deterministic or random
	 */
	public MazeBuilder(boolean deterministic){
		if (true == deterministic)
		{
			SingleRandom.setSeed(123);

		}
		random = SingleRandom.getRandom();
	}

	/**
	 * Provides the sign of a given integer number
	 * @param num
	 * @return -1 if num < 0, 0 if num == 0, 1 if num > 0
	 */
	static int getSign(int num) {
		return (num < 0) ? -1 : (num > 0) ? 1 : 0;
	}

	/**
	 * This method generates a maze.
	 * It computes distances, determines a start and exit position that are as far apart as possible. 
	 */
	protected void generate() {
		// generate paths in cells such that there is one strongly connected component
		// i.e. between any two cells in the maze there is a path to get from one to the other
		// the search algorithms starts at some random point
		generatePathways(); 

		final int[] remote = dists.computeDistances(cells) ;

		// identify cell with the greatest distance
		final int[] pos = dists.getStartPosition();
		startx = pos[0] ;
		starty = pos[1] ;

		// make exit position at true exit in the cells data structure
		cells.setExitPosition(remote[0], remote[1]);
	}

	/**
	 * This method generates pathways into the maze.
	 */
	protected void generatePathways() {
		int[][] origdirs = new int[width][height] ; 
		int x = random.nextIntWithinInterval(0, width-1) ;
		int y = 0; 
		final int firstx = x ; 
		final int firsty = y ;
		int dir = 0; 	 	
		int origdir = dir; 	
		cells.setCellAsVisited(x, y); 
		while (true) { 		
			int dx = Constants.DIRS_X[dir];
			int dy = Constants.DIRS_Y[dir];
			if (!cells.canGo(x, y, dx, dy)) { 
				dir = (dir+1) & 3; 
				if (origdir == dir) { 
					if (x == firstx && y == firsty)
						break; 
					int odr = origdirs[x][y];
					dx = Constants.DIRS_X[odr];
					dy = Constants.DIRS_Y[odr];
					x -= dx;
					y -= dy;
					origdir = dir = random.nextIntWithinInterval(0, 3);
				}
			} else {
				cells.deleteWall(x, y, dx, dy);
				x += dx;
				y += dy;
				cells.setCellAsVisited(x, y);
				origdirs[x][y] = dir;
				origdir = dir = random.nextIntWithinInterval(0, 3);
			}
		}
	}
	
	static final int MIN_ROOM_DIMENSION = 3 ;
	static final int MAX_ROOM_DIMENSION = 8 ;
	/**
	 * Allocates space for a room of random dimensions in the maze.
	 * The position of the room is chosen randomly. The method is not sophisticated 
	 * such that the attempt may fail even if the maze has ample space to accommodate 
	 * a room of the chosen size. 
	 * @return true if room is successfully placed, false otherwise
	 */
	private boolean placeRoom() {
		// get width and height of random size that are not too large
		// if too large return as a failed attempt
		final int rw = random.nextIntWithinInterval(MIN_ROOM_DIMENSION, MAX_ROOM_DIMENSION);
		if (rw >= width-4)
			return false;

		final int rh = random.nextIntWithinInterval(MIN_ROOM_DIMENSION, MAX_ROOM_DIMENSION);
		if (rh >= height-4)
			return false;
		
		// proceed for a given width and height
		// obtain a random position (rx,ry) such that room is located on as a rectangle with (rx,ry) and (rxl,ryl) as corner points
		// upper bound is chosen such that width and height of room fits maze area.
		final int rx = random.nextIntWithinInterval(1, width-rw-1);
		final int ry = random.nextIntWithinInterval(1, height-rh-1);
		final int rxl = rx+rw-1;
		final int ryl = ry+rh-1;
		// check all cells in this area if they already belong to a room
		// if this is the case, return false for a failed attempt
		if (cells.areaOverlapsWithRoom(rx, ry, rxl, ryl))
			return false ;
		// since the area is available, mark it for this room and remove all walls
		// from this on it is clear that we can place the room on the maze
		cells.markAreaAsRoom(rw, rh, rx, ry, rxl, ryl); 
		return true;
	}

	static void dbg(String str) {
		System.out.println("MazeBuilder: "+str);
	}



	/**
	 * Fill the given maze object with a newly computed maze according to parameter settings
	 * @param mz maze to be filled
	 * @param w width of requested maze
	 * @param h height of requested maze
	 * @param roomct number of rooms
	 * @param pc number of expected partiters
	 */
	public void build(Maze mz, int w, int h, int roomct, int pc) {
		init(mz, w, h, roomct, pc);
		this.execute();
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		// try-catch block to recognize if thread is interrupted
		try {
			// create an initial invalid maze where all walls and borders are up
			cells.initialize();
			// place rooms in maze
			generateRooms();
			
			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			// put pathways into the maze, determine its starting and end position and calculate distances
			generate();

			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			final int colchange = random.nextIntWithinInterval(0, 255); // used in the constructor for Segments  class Seg
			final BSPBuilder b = new BSPBuilder(maze, dists, cells, width, height, colchange, expectedPartiters) ;
			BSPNode root = b.generateBSPNodes();
			
			if (maze.saveMazeIndex >= 0) {
				//Write a new maze file.
				MazeFileWriter writeMaze = new MazeFileWriter();
				writeMaze.store(maze.getGeneratingActivity().getFilesDir() + "MazeSize" + Integer.toString(maze.saveMazeIndex) + ".xml", width, height, rooms, 
						expectedPartiters, root, cells, dists.getDists(), startx, starty);
			}
			

			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			// dbg("partiters = "+partiters);
			// communicate results back to maze object
			maze.newMaze(root, cells, dists, startx, starty, false);
		}
		catch (InterruptedException ex) {
			// necessary to catch exception to avoid escalation
			// exception mechanism basically used to exit method in a controlled way
			// no need to clean up internal data structures
			dbg("Catching signal to stop") ;
		}
		return 1;
	}
	
	protected void onPostExecute(Integer returnVal){
		maze.getGeneratingActivity().progressBar.setProgress(100);
		maze.getGeneratingActivity().startPlay();
	}
	
	/**
	 * Initialize internal attributes, method is called by build() when input parameters are provided
	 * @param mz maze to be filled
	 * @param w width of requested maze
	 * @param h height of requested maze
	 * @param roomct number of rooms
	 * @param pc number of expected partiters
	 */
	private void init(Maze mz, int w, int h, int roomct, int pc) {
		// store parameters
		maze = mz;
		width = w;
		height = h;
		rooms = roomct;
		expectedPartiters = pc;
		// initialize data structures
		cells = new Cells(w,h) ;
		dists = new Distance(w,h) ;
		//colchange = random.nextIntWithinInterval(0, 255); // used in the constructor for Segments  class Seg
	}
	
	static final long SLEEP_INTERVAL = 100 ; //unit is millisecond
	/**
	 * Main method to run construction of a new maze with a MazeBuilder in a thread of its own.
	 * This method is called internally by the build method when it sets up and starts a new thread for this object.
	 */
//	public void run() {
//		// try-catch block to recognize if thread is interrupted
//		try {
//			// create an initial invalid maze where all walls and borders are up
//			cells.initialize();
//			// place rooms in maze
//			generateRooms();
//			
//			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop
//
//			// put pathways into the maze, determine its starting and end position and calculate distances
//			generate();
//
//			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop
//
//			final int colchange = random.nextIntWithinInterval(0, 255); // used in the constructor for Segments  class Seg
//			final BSPBuilder b = new BSPBuilder(maze, dists, cells, width, height, colchange, expectedPartiters) ;
//			BSPNode root = b.generateBSPNodes();
//			
//			//Write a new maze file.
//			//MazeFileWriter writeMaze = new MazeFileWriter();
//			//writeMaze.store("MazeSkillFour.xml", width, height, rooms, 
//					//expectedPartiters, root, cells, dists.getDists(), startx, starty);
//
//			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop
//
//			// dbg("partiters = "+partiters);
//			// communicate results back to maze object
//			maze.newMaze(root, cells, dists, startx, starty);
//		}
//		catch (InterruptedException ex) {
//			// necessary to catch exception to avoid escalation
//			// exception mechanism basically used to exit method in a controlled way
//			// no need to clean up internal data structures
//			dbg("Catching signal to stop") ;
//		}
//	}
	
	static final int MAX_TRIES = 250 ;

	/**
	 * Generate all rooms in a given maze where initially all walls are up. Rooms are placed randomly and of random sizes
	 * such that the maze can turn out to be too small to accommodate the requested number of rooms (class attribute rooms). 
	 * In that case less rooms are produced.
	 * @return generated number of rooms
	 */
	private int generateRooms() {
		// Rooms are randomly positioned such that it may be impossible to place the all rooms if the maze is too small
		// to prevent an infinite loop we limit the number of failed to MAX_TRIES == 250
		int tries = 0 ;
		int result = 0 ;
		while (tries < MAX_TRIES && result <= rooms) {
			if (placeRoom())
				result++ ;
			else
				tries++ ;
		}
		return result ;
	}

	/**
	 * Notify the maze builder thread to stop the creation of a maze and to terminate
	 */
	public void interrupt() {
		buildThread.interrupt() ;
	}
	
	public Thread getBuildThread(){
		return buildThread;
	}


	
	




}
