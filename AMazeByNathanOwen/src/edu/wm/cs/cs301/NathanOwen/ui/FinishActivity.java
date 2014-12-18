package edu.wm.cs.cs301.NathanOwen.ui;

import edu.wm.cs.cs301.NathanOwen.R;
import edu.wm.cs.cs301.NathanOwen.R.id;
import edu.wm.cs.cs301.NathanOwen.R.layout;
import edu.wm.cs.cs301.NathanOwen.R.menu;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FinishActivity extends Activity {
	private static final String TAG = "FinishActivity";
	
	/**
	 * Initializes this activity and sets all the appropriate widgets.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finish);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView successText = (TextView) findViewById(R.id.textViewSuccess);
		TextView failureText = (TextView) findViewById(R.id.textViewFailure);
		TextView batteryText = (TextView) findViewById(R.id.batteryLevel);
		TextView pathlenText = (TextView) findViewById(R.id.pathLength);
		TextView startovText = (TextView) findViewById(R.id.textView3);
		LinearLayout finishScreen = (LinearLayout) findViewById(R.id.finishScreen);
		
		
		if (getIntent().getBooleanExtra("Outcome", true)){// Means the game was won.
			successText.setBackgroundColor(Color.WHITE);
			batteryText.setBackgroundColor(Color.WHITE);
			pathlenText.setBackgroundColor(Color.WHITE);
			failureText.setBackgroundColor(Color.WHITE);
			startovText.setBackgroundColor(Color.WHITE);
			
			Log.v(TAG, "Game Success");
			failureText.setVisibility(View.INVISIBLE);
			successText.setVisibility(View.VISIBLE);
			Bitmap confetti = getConfettiBMP();
			finishScreen.setBackground(new BitmapDrawable(getResources(), confetti));
		}
		else{
			Log.v(TAG, "Game Failure");
			successText.setVisibility(View.INVISIBLE);
			failureText.setVisibility(View.VISIBLE);
			finishScreen.setBackgroundColor((130)<<24|((178)<<16)|((34)<<8)|(34));
		}
		
		String batteryLevel = Float.toString(getIntent().getFloatExtra("BatteryLevel", 0));
		String pathLength = Integer.toString(getIntent().getIntExtra("PathLength", 0));
		
		batteryText.setText("Battery Level: " + batteryLevel);
		pathlenText.setText("Path Length: " + pathLength + "\n");
		
		
	}

	/**
	 * Handles the population of the actions in the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.finish, menu);
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
	
	/**
	 * Helper method to the GUI Wrapper so that it may retrieve the confetti bitmap from resources.
	 */
	public Bitmap getConfettiBMP(){
		Bitmap confetti = BitmapFactory.decodeResource(getResources(), R.drawable.confetti);
		return confetti;
	}
}
