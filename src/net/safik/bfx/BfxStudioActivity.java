package net.safik.bfx;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class BfxStudioActivity extends Activity {
	DrawView drawView;
	protected TextView title;
	protected ImageView icon;
	private BfxElement bfxElement = null;
	private DBHelper dbHelper = null;
	BfxTree table = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		// Set full screen view
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.windows_title);

		setContentView(R.layout.main);

		String id = null;
		String type = null;
		String process = null;

		if (getIntent() != null && getIntent().getExtras() != null) {
			id = getIntent().getExtras().getString("id");
			type = getIntent().getExtras().getString("type");
			process = getIntent().getExtras().getString("process");
		}

		if (id != null) {
			bfxElement = createElement(id, type);

		} else {

			bfxElement = createElement("Project", BfxType.PROJECT.name());
			
		}

		table = (BfxTree) findViewById(R.id.table);
		table.setElement(bfxElement);
		table.buildTree();

	}

	private BfxElement createElementByAlert() {

		AlertDialog.Builder alert = new AlertDialog.Builder(getBaseContext());

		alert.setTitle("Add New Process");
		alert.setMessage("Enter process id:");

		String value = null;

		// Set an EditText view to get user input
		final EditText input = new EditText(getBaseContext());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				String value = input.getText().toString();

				bfxElement = BfxFactory.createElement(value, BfxType.PROCESS);

			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

		return bfxElement;
	}

	public BfxElement createElement(String elementname, String type) {

		dbHelper = new DBHelper(getBaseContext());

		BfxElement bfxElement = BfxFactory.configureElement(dbHelper,
				elementname, type);

		dbHelper.close();

		return bfxElement;
	}

}