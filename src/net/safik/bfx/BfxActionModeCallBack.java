package net.safik.bfx;

import java.util.ArrayList;
import java.util.List;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class BfxActionModeCallBack implements ActionMode.Callback {

	private BfxElement element = null;
	private BfxTree tree = null;
	private View view;
	private Context context;

	public BfxActionModeCallBack(BfxTree tree, View view, BfxElement element) {
		this.element = element;
		this.tree = tree;
		this.view = view;
		this.context = tree.getContext();
	}

	// Called when the action mode is created; startActionMode() was called
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// Inflate a menu resource providing context menu items
		MenuInflater inflater = mode.getMenuInflater();
		if (element.getParent() != null
				&& element.getParent().getType().equals(BfxType.PROCESS))
			inflater.inflate(element.getConfig(BfxKey.menu_process_), menu);
		else
			inflater.inflate(element.getConfig(BfxKey.menu_), menu);
		return true;
	}

	// Called each time the action mode is shown. Always called after
	// onCreateActionMode, but
	// may be called multiple times if the mode is invalidated.
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false; // Return false if nothing is done
	}

	// Called when the user selects a contextual menu item
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

		String menuClicked = (String) item.getTitle();

		// to add child element, child reference element
		if (menuClicked.contains("Add New")) {
			addNewElement(menuClicked);
			mode.finish(); // Action picked, so close the CAB
			return true;
			// To build new root element
		} else	if (menuClicked.contains("Add ")) {
				addElement(menuClicked);
				mode.finish(); // Action picked, so close the CAB
				return true;
				// To build new root element
		} else if (menuClicked.contains("Create")) {
			createElement(menuClicked);
			mode.finish(); // Action picked, so close the CAB
			return true;
		} else if (menuClicked.contains("Delete")) {
			deleteElement();
			mode.finish(); // Action picked, so close the CAB
			return true;
			// edit properties
		} else if (menuClicked.contains("Edit")) {
			editElementProperty();
			mode.finish(); // Action picked, so close the CAB
			return true;
			// To open root element in new studio
		} else if (menuClicked.contains("Open")) {
			openElementActivity(element.getBase());
			mode.finish(); // Action picked, so close the CAB
			return true;

		}

		return false;

	}

	private void createElement(String title) {
		final String ty = title.substring(7, title.length()).toLowerCase()
				.trim();
		final BfxType basety = BfxType.valueOf(ty.toUpperCase());

		final BfxElement root = BfxFactory.createElement(null, basety);

		final AlertDialog dialog = createDialog(title,
				root.getConfig(BfxKey.layout_create_),root);

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface intf, int whichButton) {

						BfxElement child = BfxFactory.createElement(null,
								BfxFactory.getRefType(basety), element);
						child.setBase(root);

						if (element.getType() == BfxType.PROCESS) {
							root.setProcess(element);
							child.setProcess(element);
						} else {
							root.setProcess(element.getProcess());
							child.setProcess(element.getProcess());
						}

						processVlaues(dialog, root);

						child.setId(root.getId());

						DBHelper dbHelper = new DBHelper(context);
						dbHelper.insert(root);
						dbHelper.close();
						dialog.dismiss();
						openElementActivity(root);

						tree.buildTree();
						tree.updateDB();
					}
				});

		dialog.show();
	}

	private void processVlaues(AlertDialog dialog, BfxElement root) {

		ViewGroup table = (ViewGroup) dialog.findViewById(R.id.table);
		int rowCount = table.getChildCount();
		for (int i = 0; i < rowCount; i++) {

			ViewGroup row = (ViewGroup) table.getChildAt(i);
			if (!(row.getChildAt(0) instanceof TextView))
				continue;
			TextView tv = (TextView) row.getChildAt(0);
			String prop = (String) tv.getText();
			String value = null;

			View view = row.getChildAt(1);

			if (view instanceof EditText) {
				value = ((EditText) view).getText().toString();

			} else if (view instanceof Spinner) {
				value = ((Spinner) view).getSelectedItem().toString();

			} else if (view instanceof TextView) {
				value = (String) ((TextView) view).getText();
			} else if (view instanceof CheckBox) {
				value = "\""+ ((CheckBox) view).isChecked()+"\"";
			}

			if (value != null && !value.isEmpty()) {
				if ("id".equalsIgnoreCase(prop)) {
					root.setId(value);

				} else
					root.getProperties().put(prop, value);
			}

		}

	}

	private void openElementActivity(BfxElement e) {

		Intent intent = new Intent(context, BfxStudioActivity.class);
		intent.putExtra("id", e.getId());
		intent.putExtra("type", e.getType().name());
		context.startActivity(intent);

	}

	private AlertDialog createDialog(String title, int resid,BfxElement b) {

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		Context mContext = this.tree.getContext();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(resid, null);
		processOgnl(layout, b);
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		
		alertDialog = builder.create();

		alertDialog.setTitle(title);

		return alertDialog;
	}

	private void processOgnl(View dialog, BfxElement element) {

		ViewGroup table = (ViewGroup) dialog.findViewById(R.id.table);
		if(table==null)
			return;
		int rowCount = table.getChildCount();
		for (int i = 0; i < rowCount; i++) {

			ViewGroup row = (ViewGroup) table.getChildAt(i);
			
			View view = row.getChildAt(1);

			if (view instanceof Spinner) {
				Spinner spin = (Spinner) view;
				int id = spin.getId();
				String tag = (String) spin.getTag();

				if (id == R.id._list)
					processOGNLList(spin, element, tag);
				else if (id == R.id._ognl)
					processOGNLValue(spin, element, tag);
				else if (id == R.id._ognl)
					processOGNLString(spin, element, tag);
				else if (id == R.id._array)
					processStringArray(spin, element, tag);

			}
		}

	}

	private void editElementProperty() {

		final AlertDialog dialog = createDialog("Edit Properties",
				this.element.getConfig(BfxKey.layout_property_),element);

		//processOgnl(dialog, element);
		
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface intf, int which) {
						// TODO Auto-generated method stub

						element.getProperties().clear();

						processVlaues(dialog, element);

						tree.buildTree();
						tree.updateDB();

					}
				});

		dialog.show();

	}

	private void processOGNLString(Spinner spin, BfxElement element2, String tag) {
		String value = BfxFactory.getOgnlValue(tag, element2);

		ArrayAdapter<CharSequence> mAdapter1 = ArrayAdapter.createFromResource(
				context, BfxFactory.getStringArray(value),
				android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(mAdapter1);

	}

	private void processStringArray(Spinner spin, BfxElement element2,
			String tag) {
		
		String id = (String)BfxFactory.getOgnlValue(tag, element2);
		ArrayAdapter<CharSequence> mAdapter1 = ArrayAdapter.createFromResource(
				context, BfxFactory.getStringArray(id),
				android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(mAdapter1);

	}

	private void processOGNLValue(Spinner spin, BfxElement element2, String tag) {
		String value = BfxFactory.getOgnlValue(tag, element2);
		String obj[] = { value };
		ArrayAdapter mAdapter1 = new ArrayAdapter(context,
				android.R.layout.simple_spinner_dropdown_item, obj);
		spin.setAdapter(mAdapter1);

	}

	private void processOGNLList(Spinner spin, BfxElement element2, String tag) {

		List list = BfxFactory.getOgnlList(tag, element2);
		ArrayAdapter mAdapter1 = new ArrayAdapter(context,
				android.R.layout.simple_spinner_dropdown_item, list);
		spin.setAdapter(mAdapter1);

	}

	private void deleteElement() {

	}

	/**
	 * Create - creating new type, persisting to db, having basic information
	 * Add - Add whatever
	 * 
	 * @param title
	 */
	private void addNewElement(String title) {

		final String ty = title.substring(8, title.length()).toLowerCase()
				.trim();

		final BfxElement b = BfxFactory.createElement(null,
				BfxType.valueOf(ty.toUpperCase()), element);

		
		final AlertDialog alertDialog = createDialog("Add New " + ty, b.getConfig(BfxKey.layout_create_),b);

	//	processOgnl(alertDialog, b);
		
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface intf, int which) {
				
				processVlaues(alertDialog, b);
				
				
				tree.buildTree();
				tree.updateDB();
				
			}
		});

		
		
		alertDialog.show();

	}
	
	
	private void addElement(String title) {

		final String ty = title.substring(4, title.length()).toLowerCase()
				.trim();

		final BfxElement b = BfxFactory.createElement(null,
				BfxType.valueOf(ty.toUpperCase()), element);

		
		
		final AlertDialog alertDialog = createDialog("Add " + ty, b.getConfig(BfxKey.layout_create_),b);

	//	processOgnl(alertDialog, b);
		
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface intf, int which) {
				
				processVlaues(alertDialog, b);
				
				
				tree.buildTree();
				tree.updateDB();
				
			}
		});

		

		alertDialog.show();

	}

	// Called when the user exits the action mode
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		this.view.setSelected(false);
	}

};
