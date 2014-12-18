package edu.wm.cs.cs301.NathanOwen.falstad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Application;
import edu.wm.cs.cs301.NathanOwen.ui.GeneratingActivity;
import edu.wm.cs.cs301.NathanOwen.ui.PlayActivity;

/**
 * Class handles the user interaction for the maze. 
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user. 
 * After refactoring the original code from an applet into a panel, it is wrapped by a MazeApplication to be a java application 
 * and a MazeApp to be an applet for a web browser. At this point user keyboard input is first dealt with a key listener
 * and then handed over to a Maze object by way of the keyDown method.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
// MEMO: original code: public class Maze extends Applet {
//public class Maze extends Panel {
public class Maze extends Application implements Serializable {

	// Model View Controller pattern, the model needs to know the viewers
	// however, all viewers share the same graphics to draw on, such that the share graphics
	// are administered by the Maze object
	final private ArrayList<Viewer> views = new ArrayList<Viewer>() ; 
	MazeGuiWrapper guiWrapper ; // graphics to draw on, shared by all views
		
	public BasicRobot robot;  //This is the robot that controls this maze.

	private int state;			// keeps track of the current GUI state, one of STATE_TITLE,...,STATE_FINISH, mainly used in redraw()
	// possible values are defined in Constants
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view

	private int percentdone = 0; // describes progress during generation phase
	public boolean showMaze;		 	// toggle switch to show overall maze on screen
	public boolean showSolution;		// toggle switch to show solution in overall maze on screen
	public boolean mapMode; // true: display map of maze, false: do not display map of maze
	// map_mode is toggled by user keyboard input, causes a call to draw_map during play mode

	//static final int viewz = 50;    
	int viewx, viewy, angle;
	int dx, dy;  // current direction
	int px, py ; // current position on maze grid (x,y)
	int walkStep;
	int viewdx, viewdy; // current view direction


	// debug stuff
	boolean deepdebug = false;
	boolean allVisible = false;
	boolean newGame = false;

	// properties of the current maze
	public int mazew; // width of maze
	public int mazeh; // height of maze
	Cells mazecells ; // maze as a matrix of cells which keep track of the location of walls
	Distance mazedists ; // a matrix with distance values for each cell towards the exit
	Cells seencells ; // a matrix with cells to memorize which cells are visible from the current point of view
	// the FirstPersonDrawer obtains this information and the MapDrawer uses it for highlighting currently visible walls on the map
	BSPNode rootnode ; // a binary tree type search data structure to quickly locate a subset of segments
	// a segment is a continuous sequence of walls in vertical or horizontal direction
	// a subset of segments need to be quickly identified for drawing
	// the BSP tree partitions the set of all segments and provides a binary search tree for the partitions
	

	// Mazebuilder is used to calculate a new maze together with a solution
	// The maze is computed in a separate thread. It is started in the local Build method.
	// The calculation communicates back by calling the local newMaze() method.
	MazeBuilder mazebuilder;

	
	// fixing a value matching the escape key
	final int ESCAPE = 27;

	// generation method used to compute a maze
	int method = 0 ; // 0 : default method, Falstad's original code
	// method == 1: Prim's algorithm

	int zscale = Constants.VIEW_HEIGHT/2;
	
	// determines whether to use manual mode for maze solving or to use an automatic method.
	boolean manualMode = true;

	private RangeSet rset;
	private GeneratingActivity generatingActivity;
	private PlayActivity playActivity;
	public boolean pauseDriver = false;
	public boolean killDriver = false;
	public int saveMazeIndex = -1; //Will not save to file if -1, must be 0-4
	public FirstPersonDrawer fpd;
	
	/**
	 * Constructor
	 */
	public Maze() {
		super() ;
		//guiWrapper = new MazeGuiWrapper() ;
	}
	/**
	 * Constructor that also selects a particular generation method
	 */
	public Maze(int method)
	{
		super() ;
		// 0 is default, do not accept other settings but 0 and 1 and 2
		if (1 == method)
			this.method = 1 ;
		guiWrapper = new MazeGuiWrapper() ;
		
		if (2 == method)
			this.method = 2 ;
		guiWrapper = new MazeGuiWrapper() ;
	}
	/**
	 * Method to initialize internal attributes. Called separately from the constructor. 
	 */
	public void init() {
		setState(Constants.STATE_TITLE);
		rset = new RangeSet();
		killDriver = false;
		saveMazeIndex = -1;
		
	}
	
	/**
	 * Method obtains a new Mazebuilder and has it compute new maze, 
	 * it is only used in keyDown()
	 * @param skill level determines the width, height and number of rooms for the new maze
	 */
	public void build(int skill) {
		// switch screen
		setState(Constants.STATE_GENERATING);
		percentdone = 0;
		
		// select generation method
		switch(method){
		case 2 : mazebuilder = new MazeBuilderAldousBroder(); //generate with Aldous-Broder algorithm
		System.out.println("Using Aldous-Broder Algorithm to Build");
		break ;
		case 1 : mazebuilder = new MazeBuilderPrim(); // generate with Prim's algorithm
		System.out.println("Using Prim Algorithm to Build");
		break ;
		case 0 : mazebuilder = new MazeBuilder(); // generate with Falstad's original algorithm (0 and default), note the missing break statement
		System.out.println("Using Default Algorithm to Build");
		break;
		default : mazebuilder = new MazeBuilder(); 
		System.out.println("Using Default Algorithm to Build because something was read wrong.");
		break ;
		}
		// adjust settings and launch generation in a separate thread
		mazew = Constants.SKILL_X[skill];
		mazeh = Constants.SKILL_Y[skill];
		mazebuilder.build(this, mazew, mazeh, Constants.SKILL_ROOMS[skill], Constants.SKILL_PARTCT[skill]);
		// mazebuilder performs in a separate thread and calls back by calling newMaze() to return newly generated maze
	}
	
	/**
	 * Call back method for MazeBuilder to communicate newly generated maze as reaction to a call to build()
	 * @param root node for traversals, used for the first person perspective
	 * @param cells encodes the maze with its walls and border
	 * @param dists encodes the solution by providing distances to the exit for each position in the maze
	 * @param startx current position, x coordinate
	 * @param starty current position, y coordinate
	 */
	public void newMaze(BSPNode root, Cells c, Distance dists, int startx, int starty, boolean fromFile) {
		if (Cells.deepdebugWall)
		{   // for debugging: dump the sequence of all deleted walls to a log file
			// This reveals how the maze was generated
			c.saveLogFile(Cells.deepedebugWallFileName);
		}
		// adjust internal state of maze model
		showMaze = showSolution =  false;
		mazecells = c ;
		mazedists = dists;
		seencells = new Cells(mazew+1,mazeh+1) ;
		rootnode = root ;
		setCurrentDirection(1, 0) ;
		setCurrentPosition(startx,starty) ;
		walkStep = 0;
		viewdx = dx<<16; 
		viewdy = dy<<16;
		angle = 0;
		mapMode = false;
		// set the current state for the state-dependent behavior
		setState(Constants.STATE_PLAY);
		cleanViews() ;
		// register views for the new maze
		// mazew and mazeh have been set in build() method before mazebuider was called to generate a new maze.
		// reset map_scale in mapdrawer to a value of 10
		fpd = new FirstPersonDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,
				Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, root, this) ;
		addView(fpd);
		// order of registration matters, code executed in order of appearance!
		addView(new MapDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, this)) ;

		// notify viewers
		//notifyViewerRedraw() ; - we don't want to do this until mazepanel has a reference to the play activity
	}

	/////////////////////////////// Methods for the Model-View-Controller Pattern /////////////////////////////
	/**
	 * Register a view
	 */
	public void addView(Viewer view) {
		views.add(view) ;
	}
	/**
	 * Unregister a view
	 */
	public void removeView(Viewer view) {
		views.remove(view) ;
	}
	/**
	 * Remove obsolete FirstPersonDrawer and MapDrawer
	 */
	private void cleanViews() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			if ((v instanceof FirstPersonDrawer)||(v instanceof MapDrawer))
			{
				//System.out.println("Removing " + v);
				it.remove() ;
			}
		}

	}
	/**
	 * Notify all registered viewers to redraw their graphics
	 */
	public void notifyViewerRedraw() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			// viewers draw on the buffer graphics
			if (state == Constants.STATE_PLAY){
				v.redraw(guiWrapper, getState(), px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle) ;
				
			}
			if (state == Constants.STATE_FINISH){
				if (robot.getBatteryLevel() > 0)
					playActivity.endGame(true, robot.getBatteryLevel(), robot.getPathLength());
				else
					playActivity.endGame(false, 0, robot.getPathLength());
			}
			
			if (robot != null)
				playActivity.updateBatteryBar(robot.getBatteryLevel());
		}
		
//		playActivity.getHandler().postDelayed(new Runnable() {public void run() {
//			  playActivity.updateGraphics();}}, 50);
		
		playActivity.runOnUiThread(new Runnable() {public void run() {
			  playActivity.updateGraphics();}});
		// update the screen with the buffer graphics
		
	}
	/** 
	 * Notify all registered viewers to increment the map scale
	 */
	private void notifyViewerIncrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.incrementMapScale() ;
		}
		// update the screen with the buffer graphics
		guiWrapper.update() ;
	}
	/** 
	 * Notify all registered viewers to decrement the map scale
	 */
	private void notifyViewerDecrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.decrementMapScale() ;
		}
		// update the screen with the buffer graphics
		guiWrapper.update() ;
	}
	////////////////////////////// get methods ///////////////////////////////////////////////////////////////
	boolean isInMapMode() { 
		return mapMode ; 
	} 
	boolean isInShowMazeMode() { 
		return showMaze ; 
	} 
	boolean isInShowSolutionMode() { 
		return showSolution ; 
	} 
	public String getPercentDone(){
		return String.valueOf(percentdone) ;
	}
	public MazeGuiWrapper getGUIWrapper() {
		return guiWrapper ;
	}
	public float getBatteryLevel(){
		return this.robot.getBatteryLevel();
	}
	public int getPathLength(){
		return this.robot.getPathLength();
	}
	public int getState() {
		return state;
	}
	public Cells getCells() {
		if (mazecells == null)
			throw new NullPointerException();
		return this.mazecells;
	}
	public MazeBuilder getMazeBuilder() {
		return this.mazebuilder;
	}

	public Distance getMazeDists(){
		return mazedists;
	}
	public GeneratingActivity getGeneratingActivity() {
		return generatingActivity;
	}

	////////////////////////////// set methods ///////////////////////////////////////////////////////////////
	public void setGeneratingActivity(GeneratingActivity generatingActivity) {
		this.generatingActivity = generatingActivity;
	}
	public void setBuilder(int builder){
		this.method = builder;
	}
	public void setState(int state) {
		this.state = state;
	}
	public void setMethod(String builderAlgorithm) {
		if (builderAlgorithm == "Aldous-Broder")
			this.method = 2;
		if (builderAlgorithm == "Prim")
			this.method = 1;
		if (builderAlgorithm == "Default (Falstad)")
			this.method = 0;
	}
	public void setPlayActivity(PlayActivity playactivity){
		this.playActivity = playactivity;
	}
	
	////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
	private void setCurrentPosition(int x, int y)
	{
		px = x ;
		py = y ;
	}
	private void setCurrentDirection(int x, int y)
	{
		dx = x ;
		dy = y ;
	}
	
	

	final double radify(int x) {
		return x*Math.PI/180;
	}


	/**
	 * Allows external increase to percentage in generating mode with subsequence graphics update
	 * @param pc gives the new percentage on a range [0,100]
	 * @return true if percentage was updated, false otherwise
	 */
	public boolean increasePercentage(int pc) {
		if (percentdone < pc && pc < 100) {
			percentdone = pc;
			if (getState() == Constants.STATE_GENERATING)
			{
				generatingActivity.getProgressBarHandler().post(new Runnable() {public void run() {
					  generatingActivity.getProgressBar().setProgress(percentdone);}});
	
			}
			else
				dbg("Warning: Receiving update request for increasePercentage while not in generating state, skip redraw.") ;
			return true ;
		}
		return false ;
	}

	



	/////////////////////// Methods for debugging ////////////////////////////////
	private void dbg(String str) {
		//System.out.println(str);
	}

	private void logPosition() {
		if (!deepdebug)
			return;
		dbg("x="+viewx/Constants.MAP_UNIT+" ("+
				viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
				angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
	}
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Helper method for walk()
	 * @param dir
	 * @return true if there is no wall in this direction
	 */
	private boolean checkMove(int dir) {
		// obtain appropriate index for direction (CW_BOT, CW_TOP ...) 
		// for given direction parameter
		int a = angle/90;
		if (dir == -1)
			a = (a+2) & 3; // TODO: check why this works
		// check if cell has walls in this direction
		// returns true if there are no walls in this direction
		return mazecells.hasMaskedBitsFalse(px, py, Constants.MASKS[a]) ;
	}



	private void rotateStep() {
		angle = (angle+1800) % 360;
		viewdx = (int) (Math.cos(radify(angle))*(1<<16));
		viewdy = (int) (Math.sin(radify(angle))*(1<<16));
		moveStep();
	}

	private void moveStep() {
		notifyViewerRedraw() ;
		try {
			Thread.currentThread();
			Thread.sleep(50);
		} catch (Exception e) { }
	}

	private void rotateFinish() {
		setCurrentDirection((int) Math.cos(radify(angle)), (int) Math.sin(radify(angle))) ;
		logPosition();
	}

	private void walkFinish(int dir) {
		
		setCurrentPosition(px + dir*dx, py + dir*dy) ;
		
		if (isEndPosition(px,py)) {
			setState(Constants.STATE_FINISH);
			notifyViewerRedraw() ; //Switch to final activity here
		}
		walkStep = 0;
		logPosition();
	}

	/**
	 * checks if the given position is outside the maze
	 * @param x
	 * @param y
	 * @return true if position is outside, false otherwise
	 */
	public boolean isEndPosition(int x, int y) {
		return x < 0 || y < 0 || x >= mazew || y >= mazeh;
	}



	public synchronized void walk(int dir) {
		if (!checkMove(dir)){
			moveStep();
			fpd.drawDirectionArrow();
			return;
		}
		for (int step = 0; step != 4; step++) {
			walkStep += dir;
			moveStep();
		}
		walkFinish(dir);
		fpd.drawDirectionArrow();
	}

	public synchronized void rotate(int dir) {
		final int originalAngle = angle;
		final int steps = 4;

		for (int i = 0; i != steps; i++) {
			angle = originalAngle + dir*(90*(i+1))/steps;
			rotateStep();
		}
		rotateFinish();
		fpd.drawDirectionArrow();
	}



	
	
	//===========================================New Methods Created by Nathan Owen=====================================//
	
//=================================Methods for use by Android Activitie=======================//	
	public void dereferenceGeneratingActivity(){
		generatingActivity = null;
	}
	public void dereferenceMazeBuilder(){
		mazebuilder = null;
	}
	public void toastCheck(String string) {
		playActivity.toast(string);
	}
	public void makeGUIWrapper() {
		guiWrapper = new MazeGuiWrapper();
	}
	public void pauseDriver(){
		pauseDriver = true;
	}
	public void resumeDriver(){
		pauseDriver = false;
	}
	public void killDriver(){
		killDriver = true;
	}
	
//=========================================GUI Button Calls====================================//
	
	/*
	 * Moves the robot forward one step, called by PlayActivity's up button
	 */
	public void upButton(){
		try {
			this.robot.notifyRobot(0);
		} catch (Exception e) {e.printStackTrace();}
	}
	/*
	 * Turns the robot left, called by PlayActivity's left button
	 */
	public void leftButton(){
		try {
			this.robot.notifyRobot(1);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	/*
	 * Turns the robot right, called by PlayActivity's right button
	 */
	public void rightButton(){
		try {
			this.robot.notifyRobot(2);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	/*
	 * Turns the robot about-face, called by PlayActivity's down button
	 */
	public void downButton(){
		try {
			this.robot.notifyRobot(3);
		} catch (Exception e) {e.printStackTrace();}
	}
	public void showSolution(){
		System.out.println("Tried to toggle Solution");
		showSolution = !showSolution;
		notifyViewerRedraw() ;
	}
	public void showWalls(){
		System.out.println("Tried to toggle Walls");
		showMaze = !showMaze; 
		notifyViewerRedraw() ;
	}
	public void showMap(){
		System.out.println("Tried to toggle Map");
		mapMode = !mapMode; 
		notifyViewerRedraw() ;
	}
	public void incrementMapSize(){
		notifyViewerIncrementMapScale() ;
		notifyViewerRedraw() ;
	}
	public void decrementMapSize(){
		notifyViewerDecrementMapScale() ;
		notifyViewerRedraw() ;
	}
	public void beginGraphics(){
		notifyViewerRedraw();
		fpd.drawDirectionArrow();
	}



}
