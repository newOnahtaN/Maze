package edu.wm.cs.cs301.NathanOwen.ui;

import edu.wm.cs.cs301.NathanOwen.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.wm.cs.cs301.NathanOwen.falstad.*;

public class GeneratingActivity extends Activity {
	
	private static final String TAG = "GeneratingActivity";
	public ProgressBar progressBar;
	public Handler progressBarHandler = new Handler();
	public Intent intentFromTitle;
	public Maze maze;
	public MazeGuiWrapper panel;
	public String solver; 
	public int builder;
	public int difficulty;
	boolean fromFile;
	private TextView generatingText;
	private boolean saveMaze;
	private MazeFileReader mfr;
	
	
	/**
	 * Initializes this activity, sets all the appropriate widgets.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generating);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setIndeterminate(false);
		generatingText = (TextView) findViewById (R.id.generatingText);
		LinearLayout generatingScreen = (LinearLayout) findViewById(R.id.generatingScreen);
		
		generatingScreen.setBackgroundColor((130)<<24|((154)<<16)|((205)<<8)|(50));
		
		initMaze();
	}
	
	/*
	 * Create or retrieve a maze object, and give it the information that the suer chose. Then build the maze.
	 */
	private void initMaze() {
		Log.v(TAG,"Creating Maze Object");
		getIntents();
		if (!fromFile){
			
			//Global/Shared Data implementation
				maze = ((Maze)getApplicationContext());
			
			//Message Passing / Intent Serialization implementation
				//maze = new Maze();
			
				
			generatingText.setText("Generating Maze");
			maze.setBuilder(builder);
			maze.setGeneratingActivity(this);
			maze.init();
			
			if (saveMaze)
				maze.saveMazeIndex = difficulty;
			
			maze.build(difficulty);
		}
		else{
			
			generatingText.setText("Loading Maze from File");
			progressBar.setIndeterminate(true);
			
			
			System.out.println("MazeApplication: loading maze from file with difficulty of " + Integer.toString(difficulty));
			maze = ((Maze)getApplicationContext()); 
			maze.setGeneratingActivity(this);
			maze.init();
			
			
			mfr = new MazeFileReader(getFilesDir() + "MazeSize" + Integer.toString(difficulty) + ".xml", maze) ;
			
		}
		
	}
	
	/**
	 * Get the intents from the title activity.
	 */
	private void getIntents(){
		Log.v(TAG, "Recieving intents from TitleActivity");
		intentFromTitle = getIntent();
		solver = intentFromTitle.getStringExtra("Solver");
		builder = intentFromTitle.getIntExtra("Builder", 0);
		difficulty = intentFromTitle.getIntExtra("Difficulty", 0);
		fromFile = intentFromTitle.getBooleanExtra("FromFile", false);
		saveMaze = intentFromTitle.getBooleanExtra("SaveMaze", false);
	}
	
	
	/*
	 * This is called at the end of execution by the MazeFileReader (only when a file is read)
	 */
	public void finishLoadingMaze(){
		maze.mazeh = mfr.getHeight() ;
		maze.mazew = mfr.getWidth() ;
		Distance d = new Distance(mfr.getDistances()) ;
		maze.newMaze(mfr.getRootNode(),mfr.getCells(),d,mfr.getStartX(), mfr.getStartY(), true) ;
	}
	


	/**
	 * Starts the PlayActivity - dereferences unserializable objects that maze references so that it may be serialized and sent as an intent.
	 */
	public void startPlay(){
		
		Log.v(TAG,"Starting PlayActivity");
		//dereference non-serializable objects
		maze.dereferenceMazeBuilder();
		maze.dereferenceGeneratingActivity();
		
		
		Intent intent = new Intent(this, PlayActivity.class);
		intent.putExtra("Solver", solver);
		//intent.putExtra("Maze", maze);
		startActivity(intent);
	}
	


	/**
	 * Handles the population of the actions in the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.generating, menu);
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

		return super.onOptionsItemSelected(item);
	}
	
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


	public Handler getProgressBarHandler() {
		return progressBarHandler;
	}


	public ProgressBar getProgressBar() {
		return progressBar;
	}
}
