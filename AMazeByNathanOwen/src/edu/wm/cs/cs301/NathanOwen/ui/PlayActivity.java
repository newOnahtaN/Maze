package edu.wm.cs.cs301.NathanOwen.ui;

import edu.wm.cs.cs301.NathanOwen.R;
import edu.wm.cs.cs301.NathanOwen.falstad.AutomaticDriver;
import edu.wm.cs.cs301.NathanOwen.falstad.BasicRobot;
import edu.wm.cs.cs301.NathanOwen.falstad.ManualDriver;
import edu.wm.cs.cs301.NathanOwen.falstad.Maze;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class PlayActivity extends Activity {
	
	private static final String TAG = "PlayActivity";
	Button upButton;
	Button downButton;
	Button leftButton;
	Button rightButton;
	Button playPause;
	Intent intentFromTitle;
	String solver;
	Maze maze;
	private Handler handler = new Handler(Looper.getMainLooper());
	private Boolean paused = false;
	private Bitmap bitmap;
	private Canvas canvas;
	private RelativeLayout relativelayout;
	private ProgressBar batteryBar;
	private ManualDriver manualDriver;
	private BasicRobot robot;
	private AutomaticDriver automaticDriver;

//===================================Init Methods and Activity Handlers================================//
	
	/**
	 * Initializes this activity, sets all the appropriate widgets.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intentFromTitle = getIntent();
		
		
		//Global/Shared Data implementation
			maze = ((Maze)getApplicationContext());
		
		//Message Passing / Intent Serialization implementation
			//maze = (Maze) intentFromTitle.getSerializableExtra("Maze");
		
		
		maze.makeGUIWrapper();
		maze.setPlayActivity(this);
		initVisuals();
		considerDPad();
		maze.beginGraphics();
		initDrivers();
	}

	/**
	 * Handles the population of the actions in the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}
	

	/**
	 * Handles usage of the actions in the action bar.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.show_map:
			showMap();
			return true;
		case R.id.show_sol:
			showSolution();
			return true;
		case R.id.show_walls:
			showWalls();
			return true;
		case R.id.incrementMap:
			incrementMapSize();
			return true;
		case R.id.decrementMap:
			decrementMapSize();
			return true;
		case android.R.id.home:
			endGameEarly();
		default:
			return super.onOptionsItemSelected(item);
		
		}

	}
	
	/**
	 * Create the appropriate robot drivers for the maze - manual if the user wants to drive the robot themselves,
	 * or a different automatic driver they may have chosen. Establish references between all of these drivers, robots, and 
	 * the maze so that they may communicate appropriately.
	 */
	public void initDrivers() {

		if (solver.equals("Manual")){
			manualDriver = new ManualDriver();
			Log.v(TAG,"Using Manual algorithm.");
			robot = new BasicRobot();
			robot.setMaze(maze);
			
			try {
				manualDriver.setRobot(robot);
				manualDriver.setRobotNotifier(robot);   } catch (Exception e){;}
			
		}
		else{
			Log.v(TAG, "Using automatic Driver");
			automaticDriver = new AutomaticDriver(solver);
			automaticDriver.setDistance(maze.getMazeDists()); //For use by wizard algorithm
			robot = new BasicRobot(true, true, new boolean[]{true, true, false, false}); //disable right and back sensors due to assignment specifications
			robot.setMaze(maze);
			
			
			try {
				automaticDriver.setRobot(robot);
				automaticDriver.setBasicRobot(robot);
				automaticDriver.start();    
			} catch (Exception e) {e.printStackTrace();}
				         
		}
	}

	
	/**
	 * At the click of one of the two shortcut buttons, changes to the next activity
	 */
	public void endGame(Boolean outcome, float batteryLevel, int pathLength){
		Intent intent = new Intent(this, FinishActivity.class);
		intent.putExtra("Outcome", outcome);
		intent.putExtra("BatteryLevel", batteryLevel);
		intent.putExtra("PathLength", pathLength);
		startActivity(intent);
	}
	
	/**
	 * End the game early by killing the automatic thread if it is still going.
	 */
	public void endGameEarly(){
		maze.killDriver();
	}
	
	/**
	 * Pause the driver
	 */
	public void pauseDriver(){
		maze.pauseDriver();
	}
	
	/**
	 * Resume the driver
	 */
	public void resumeDriver(){
		maze.resumeDriver();
	}
//=====================================Activity GUI Visual Handlers================================//
	
	/**
	 * Create all of the GUI objects that appear on screen
	 */
	public void initVisuals(){
		Log.v(TAG, "Populating the play screen with visuals");
		setContentView(R.layout.activity_play);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		upButton = (Button) findViewById(R.id.buttonUp);
		downButton = (Button) findViewById(R.id.buttonDown);
		leftButton = (Button) findViewById(R.id.buttonLeft);
		rightButton = (Button) findViewById(R.id.buttonRight);
		playPause = (Button) findViewById(R.id.playpause);
		
		batteryBar = (ProgressBar) findViewById(R.id.batterybar);
		batteryBar.setMax(2500);
		batteryBar.setProgress(2500);
		createGraphics();
	}
	
	/**
	 * Establish a bitmap and a canvas for the MazeGuiWrapper to draw to.
	 */
	public void createGraphics(){

		bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		maze.getGUIWrapper().setCanvas(canvas);
		maze.getGUIWrapper().setPlayActivity(this);
		relativelayout = (RelativeLayout) findViewById(R.id.playActivity);
		relativelayout.setBackground(new BitmapDrawable(getResources(), bitmap));

	}
	
	/**
	 * This method is called to update the visuals on screen after the canvas has been modified.
	 */
	public void updateGraphics(){
		relativelayout.setBackground(new BitmapDrawable(getResources(), bitmap));

	}
	
	/**
	 * Update the battery bar to the amount remaining.
	 * @param batteryLevel
	 */
	public void updateBatteryBar(float batteryLevel){
		if (batteryLevel >= 0)
			batteryBar.setProgress((int)batteryLevel);
		else
			batteryBar.setProgress(0);
	}
	
	/**
	 * Hide the D-pad if in automatic mode, make sure it is showing in manual mode.
	 */
	public void considerDPad(){
		solver = intentFromTitle.getStringExtra("Solver");
		if (solver.equals("Manual")){
			showDPad();
		}
		else{
			hideDPad();
		}
	}
	
	/**
	 * Helper method to the GUI Wrapper so that it may retrieve the sky bitmap from resources.
	 */
	public Bitmap getSkyBMP(){
		Bitmap sky = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
		return sky;

	}
	
	/**
	 * Helper method to the GUI Wrapper so that it may retrieve the grass bitmap from resources.
	 */
	public Bitmap getGrassBMP(){
		Bitmap grass = BitmapFactory.decodeResource(getResources(), R.drawable.grass);
		return grass;
	}
	
	
	public void hideDPad(){
		Log.v(TAG, "DPad made invisible");
		upButton.setVisibility(View.INVISIBLE);
		downButton.setVisibility(View.INVISIBLE);
		rightButton.setVisibility(View.INVISIBLE);
		leftButton.setVisibility(View.INVISIBLE);
		playPause.setVisibility(View.VISIBLE);
	}
	
	public void showDPad(){
		upButton.setVisibility(View.VISIBLE);
		downButton.setVisibility(View.VISIBLE);
		rightButton.setVisibility(View.VISIBLE);
		leftButton.setVisibility(View.VISIBLE);
		playPause.setVisibility(View.INVISIBLE);
	}
	
//========================================User Input Methods=====================================//
	
	/**
	 * Toggles the view of the map.
	 */
	public void showMap(){
		Log.v(TAG, "Show Map");
		maze.showMap();
	}
	
	/**
	 * Toggles the view of the surround walls in the map.
	 */
	public void showWalls(){
		Log.v(TAG, "Show Walls");
		maze.showWalls();
	}
	
	/**
	 * Toggles the view of the solution.
	 */
	public void showSolution(){
		Log.v(TAG, "Show Solution");
		maze.showSolution();
	}
	
	/**
	 * Increment the map size - called by a menu object button
	 */
	public void incrementMapSize(){
		Log.v(TAG, "Increased map size");
		maze.incrementMapSize();
	}
	
	/**
	 * Decrement the map size - called by a menu object button
	 */
	public void decrementMapSize(){
		Log.v(TAG, "Decreased map size");
		maze.decrementMapSize();
	}
	
	/**
	 * Will move the robot forward
	 * @param view
	 */
	public void up(View view){
		Log.v(TAG, "Up button pressed");
		maze.upButton();
	}
	
	/**
	 * Will Turn the robot around.
	 * @param view
	 */
	public void down(View view){
		Log.v(TAG, "Down button pressed");
		maze.downButton();
	}
	
	/**
	 * Will rotate the robot left.
	 * @param view
	 */
	public void left(View view){
		Log.v(TAG, "Left button pressed");
		maze.leftButton();
	}
	
	/**
	 * Will rotate the robot right.
	 * @param view
	 */
	public void right(View view){
		Log.v(TAG, "Right button pressed");
		maze.rightButton();
	}
	
	/**
	 * If the back button is pressed, end the game.
	 */
	@Override
	public void onBackPressed() {
	    endGameEarly();
	    super.onBackPressed();
	}
	/**
	 * Either play or pause the automatic driver thread.
	 */
	public void playPause(View view){
		if (paused){
			resumeDriver();
			playPause.setText("Pause");
		}
		else{
			pauseDriver();
			playPause.setText("Play");
		}
		paused = !paused;
	}

	
//===========================================Test and Support Methods====================================//
	/**
	 * Used to send a message to the user throught the GUI (for testing). Also sends the same message to 
	 * logcat through a verbose message.
	 * @param message
	 */
	public void toast(String message){
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
		Log.v(TAG, message);
	}

	public Handler getHandler() {
		return handler;
	}
	

}