package edu.wm.cs.cs301.NathanOwen.ui;

import edu.wm.cs.cs301.NathanOwen.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AMazeActivity extends Activity {
	
	private static final String TAG = "AMazeActivity";
	public String builderSelected = "Default";
	public String solverSelected = "Manual";
	public int skillLevel = 0;

	
//=============================================Init and Override Methods====================================//
	/**
	 * Initializes this activity and sets all the appropriate widgets.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_amaze);
		
		Spinner builderSpinner = (Spinner) findViewById(R.id.spinner1);
		Spinner driverSpinner = (Spinner) findViewById(R.id.spinner2);
		CheckBox genMaze = (CheckBox) findViewById(R.id.checkBox2);
		LinearLayout titleScreen = (LinearLayout) findViewById(R.id.titleScreen);
		
		titleScreen.setBackgroundColor((130)<<24|((154)<<16)|((205)<<8)|(50));
		builderSpinner.setOnItemSelectedListener(new builderSelectedListener());
		driverSpinner.setOnItemSelectedListener(new solverSelectedListener());
		
		genMaze.setChecked(true);
		setBuilerSpinner(builderSpinner);
		setdriverSpinner(driverSpinner);
		
//		alertGrader();
		
	}


	/**
	 * Handles the population of the actions in the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.amaze, menu);
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
	
//=========================================Methods that GUI Widgets Call=======================================// 
	
	/**
	 * Called when the build maze button is clicked - makes a call to the startMaze() method in Maze application.
	 */
	public void buildMaze(View view){
		Log.v(TAG,"Sending the user's choices to generatingActivity");
		//Start next activity and start building the maze.
		CheckBox fileMaze = (CheckBox) findViewById(R.id.checkBox1);
		CheckBox saveMaze = (CheckBox) findViewById(R.id.saveMaze);
		SeekBar difficulty = (SeekBar) findViewById(R.id.seekBar1);
		

		System.out.println(solverSelected);
		Intent intent = new Intent(this, GeneratingActivity.class);
		
	
		intent.putExtra("Solver", solverSelected);
		
		
		//Pass inent for Builder
		if (builderSelected.equals("Aldous-Broder"))
			intent.putExtra("Builder", 2);
		if (builderSelected.equals("Prim"))
			intent.putExtra("Builder", 1);
		else
			intent.putExtra("Builder", 0);
		
		//Pass skill level
		intent.putExtra("Difficulty", difficulty.getProgress());
		
		//Pass if maze from file
		intent.putExtra("FromFile",  fileMaze.isChecked());
		
		//Pass if we are to save the maze
		intent.putExtra("SaveMaze", saveMaze.isChecked());
		
		startActivity(intent);
	}
	
	
	
	/**
	 * Handles the usage of the two box checks, accounts for disabling one of the spinners if 
	 * maze from file is chosen.
	 * @param view
	 */
	public void boxCheck(View view){
		Log.v(TAG, "Box was checked");
		Spinner builders = (Spinner) findViewById(R.id.spinner1);
		CheckBox fileMaze = (CheckBox) findViewById(R.id.checkBox1);
		CheckBox genMaze = (CheckBox) findViewById(R.id.checkBox2);
		SeekBar difficulty = (SeekBar) findViewById(R.id.seekBar1);
		CheckBox saveMaze = (CheckBox) findViewById(R.id.saveMaze);
		
		
		Boolean checked = ((CheckBox) view).isChecked();
		
		switch (view.getId()){
		
		case (R.id.checkBox1):
			if (checked){
				builders.setVisibility(View.INVISIBLE);
				saveMaze.setVisibility(View.INVISIBLE);
				genMaze.setChecked(false);
				difficulty.setMax(4);}
			else{
				builders.setVisibility(View.VISIBLE);
				saveMaze.setVisibility(View.VISIBLE);
				genMaze.setChecked(true);
				difficulty.setMax(15);
			}
			break;
		
		case (R.id.checkBox2):
			if (checked) {
				builders.setVisibility(View.VISIBLE);
				saveMaze.setVisibility(View.VISIBLE);
				fileMaze.setChecked(false);
				difficulty.setMax(15);}
			else{
				builders.setVisibility(View.INVISIBLE);
				saveMaze.setVisibility(View.INVISIBLE);
				fileMaze.setChecked(true);
				difficulty.setMax(4);
			}
			break;
		
		case (R.id.saveMaze):
			if (checked) {
				difficulty.setMax(4);
			}
			else{
				difficulty.setMax(15);
			}
			break;
		
		}
		
	}
	
//====================================Calibrating and Receiving input from GUI widgets============================//
	
	
	/**
	 * Populate the builder spinner with a array of strings.
	 * @param builderSpinner
	 */
	private void setBuilerSpinner(Spinner builderSpinner) {
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.builder_algos, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		builderSpinner.setAdapter(adapter);
		Log.v(TAG, "Populated builderspinner");
	}

	
	
	
	/**
	 * Populate the driver spinner with a array of strings.
	 * @param builderSpinner
	 */
	private void setdriverSpinner(Spinner driverSpinner) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.driver_algos, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		driverSpinner.setAdapter(adapter);
		Log.v(TAG, "Populated builderspinner");
		
	}
	
	
	
	/**
	 * Used to listen to when the builder spinner has a selection made. Sets a field variable to be used by 
	 * start maze.
	 * @author Nate
	 *
	 */
	public class builderSelectedListener implements OnItemSelectedListener {

		/**
		 * Sets a field variable with the item selected.
		 */
		@Override
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			builderSelected = parent.getItemAtPosition(pos).toString();
	    }
		
		/**
		 * Necessary override that is currently empty
		 */
		@Override
	    public void onNothingSelected(AdapterView parent) {
	        // Do nothing.
	    }

	}
	
	
	
	/**
	 * Used to listen to when the solver spinner has a selection made. Sets a field variable to be used by 
	 * start maze.
	 * @author Nate
	 *
	 */
	public class solverSelectedListener implements OnItemSelectedListener {

		/**
		 * Sets a field variable with the item selected.
		 */
		@Override
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			solverSelected = parent.getItemAtPosition(pos).toString();
	    }
		
		/**
		 * Necessary override that is currently empty
		 */
		@Override
	    public void onNothingSelected(AdapterView parent) {
	        // Do nothing.
	    }

	}
	
	/**
	 * Helper method to the GUI Wrapper so that it may retrieve the confetti bitmap from resources.
	 */
	public Bitmap getTitleBMP(){
		Canvas canvas = new Canvas();
		canvas.drawColor(Color.WHITE);   
		BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.hedge);    
		Bitmap title = bd.getBitmap();    
		Paint paint = new Paint();    
		paint.setAlpha(60);                             //you can set your transparent value here    
		canvas.drawBitmap(title, 0, 0, paint);
		
		
		//Bitmap title = BitmapFactory.decodeResource(getResources(), R.drawable.hedge);
		return title;
	}
	
//=================================================Debug Methods============================================//
	
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
	
	
//	private void alertGrader() {
//	new AlertDialog.Builder(this)
//    .setTitle("Extra Credit")
//    .setMessage("This implementation of Maze is capable of using both shared/global data and serialized data by intents to pass information "
//    		+ "from the generatingActivity to the playActivity. Refer to the pdf file located in reports in order to learn how to operate these two variants.")
//    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) { 
//            // continue with delete
//        }
//     })
//    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) { 
//            // do nothing
//        }
//     })
//    .setIcon(android.R.drawable.ic_dialog_alert)
//     .show();
//	
//}
}
