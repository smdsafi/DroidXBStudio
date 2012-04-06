package net.safik.bfx;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class BfxTree extends TableLayout {

	private BfxElement element;

	private Context context;
	private static final android.widget.TableRow.LayoutParams params = new android.widget.TableRow.LayoutParams(
			30, 30);

	public BfxTree(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.context = context;
	}

	public BfxElement getElement() {
		return element;
	}

	public void setElement(BfxElement element) {
		this.element = element;
	}

	private BfxTree getThis() {
		return this;
	}

	public void buildTree() {

		if (this.element == null)
			return;

		removeAllViews();

		TableRow row = new TableRow(context);

		LinearLayout linearLayout = new LinearLayout(context);

		final TextView textView = new TextView(context);
		textView.setText(element.getId());
		textView.setTextSize(22);
		textView.setClickable(true);
		textView.setLongClickable(true);

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			// Called when the user long-clicks on someView
			public boolean onLongClick(View view) {

				// Start the CAB using the ActionMode.Callback defined above
				startActionMode(new BfxActionModeCallBack(getThis(), textView,
						element));
				view.setSelected(true);
				return true;
			}
		});

		ImageView imageView = new ImageView(context);
		imageView.setImageResource(element.getConfig(BfxKey.icon_));

		TextView txtProp = new TextView(context);
		StringBuffer sb = new StringBuffer();
		if (element.getProperties().size() > 0) {
			sb.append("  [");
			for (String k : element.getProperties().keySet()) {
				sb.append(k + "=" + element.getProperties().get(k));
				sb.append("\t");
			}
			sb.append("]");
		}
		txtProp.setText(sb.toString());

		linearLayout.addView(imageView, params);
		linearLayout.addView(textView);
		linearLayout.addView(txtProp);
		row.addView(linearLayout);
		addView(row);

		buildChild(element.getChildren());

	}

	public void updateDB() {
		DBHelper dbHelper = new DBHelper(getContext());
		dbHelper.update(element);
		dbHelper.close();
	}

	private void buildChild(List<BfxElement> children) {

		if (children == null || children.size() == 0)
			return;

		for (final BfxElement bfxElement : children) {

			LinearLayout tableLayout = new LinearLayout(context);

			TableRow row2 = new TableRow(context);
			row2.setClickable(true);

			for (int i = 0; i < bfxElement.getParentCount(); i++) {
				ImageView space = new ImageView(context);
				row2.addView(space, params);
			}

			final TextView textView2 = new TextView(context);
			textView2.setLongClickable(true);
			textView2.setTextSize(22);
			textView2.setText(bfxElement.getId());
			ImageView imageView2 = new ImageView(context);
			imageView2.setImageResource(bfxElement.getConfig(BfxKey.icon_));

			textView2.setOnLongClickListener(new View.OnLongClickListener() {
				// Called when the user long-clicks on someView
				public boolean onLongClick(View view) {

					// Start the CAB using the ActionMode.Callback defined above
					startActionMode(new BfxActionModeCallBack(getThis(),
							textView2, bfxElement));
					view.setSelected(true);
					return true;
				}
			});

			for (int i = 0; i < bfxElement.getParentCount(); i++) {
				ImageView space = new ImageView(context);
				row2.addView(space, params);
			}
			
			TextView txtProp = new TextView(context);
			StringBuffer sb = new StringBuffer();
			if (bfxElement.getProperties().size() > 0) {
				sb.append("  [");
				for (String k : bfxElement.getProperties().keySet()) {
					sb.append(k + "=" + bfxElement.getProperties().get(k));
					sb.append("\t");
				}
				sb.append("]");
			}
			txtProp.setText(sb.toString());

			row2.addView(imageView2, params);
			row2.addView(textView2);
			row2.addView(txtProp);

			tableLayout.addView(row2);
			TableRow row = new TableRow(context);
			row.addView(tableLayout);
			addView(row);

			buildChild(bfxElement.getChildren());
		}

	}

}