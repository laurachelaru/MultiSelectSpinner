package com.androidbuts.multispinnerfilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MultiSpinner extends AppCompatSpinner implements DialogInterface.OnCancelListener {
	private static final String TAG = MultiSpinner.class.getSimpleName();
    public static android.app.AlertDialog.Builder builder;
    public static android.app.AlertDialog ad;
    MultiSpinner.MyAdapter adapter;
	private List<KeyPairBoolData> items;
	private String defaultText = "";
	private String spinnerTitle = "";
	private SpinnerListener listener;
	private int limit = -1;
	private int selected = 0;
	private MultiSpinner.LimitExceedListener limitListener;
	public MultiSpinner(Context context) {
		super(context);
	}
	public MultiSpinner(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		TypedArray a = arg0.obtainStyledAttributes(arg1, R.styleable.MultiSpinnerSearch);
		for (int i = 0; i < a.getIndexCount(); ++i) {
			int attr = a.getIndex(i);
			if (attr == R.styleable.MultiSpinnerSearch_hintText) {
				spinnerTitle = a.getString(attr);
				defaultText = spinnerTitle;
				break;
			}
		}
		Log.i(TAG, "spinnerTitle: "+spinnerTitle);
		a.recycle();
	}
	public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }
	
	public void setLimit(int limit, MultiSpinner.LimitExceedListener listener) {
		this.limit = limit;
		this.limitListener = listener;
	}

	public List<KeyPairBoolData> getSelectedItems() {
		List<KeyPairBoolData> selectedItems = new ArrayList<>();
		for(KeyPairBoolData item : items){
			if(item.isSelected()){
				selectedItems.add(item);
			}
		}
		return selectedItems;
	}

	public List<Long> getSelectedIds() {
		List<Long> selectedItemsIds = new ArrayList<>();
		for(KeyPairBoolData item : items){
			if(item.isSelected()){
				selectedItemsIds.add(item.getId());
			}
		}
		return selectedItemsIds;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// refresh text on spinner

		StringBuilder spinnerBuffer = new StringBuilder();

		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isSelected()) {
				spinnerBuffer.append(items.get(i).getName());
				spinnerBuffer.append(", ");
			}
		}

		String spinnerText = spinnerBuffer.toString();
		if (spinnerText.length() > 2)
			spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
		else
			spinnerText = defaultText;

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[]{spinnerText});
		setAdapter(adapterSpinner);

		if (adapter != null)
			adapter.notifyDataSetChanged();

		listener.onItemsSelected(items);
	}

	@Override
	public boolean performClick() {

		builder = new android.app.AlertDialog.Builder(getContext(), R.style.myDialog);
		builder.setTitle(spinnerTitle);

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = inflater.inflate(R.layout.alert_dialog_listview_search, null);
		builder.setView(view);

		final ListView listView = (ListView) view.findViewById(R.id.alertSearchListView);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setFastScrollEnabled(false);
		adapter = new MultiSpinner.MyAdapter(getContext(), items);
		listView.setAdapter(adapter);

		final TextView emptyText = (TextView) view.findViewById(R.id.empty);
		listView.setEmptyView(emptyText);

		final EditText editText = (EditText) view.findViewById(R.id.alertSearchEditText);
		editText.setVisibility(GONE);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, " ITEMS : " + items.size());
				dialog.cancel();
			}
		});

		builder.setOnCancelListener(this);
		ad = builder.show();
		ad.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		return true;
	}

	public void setItems(List<KeyPairBoolData> items, int position, SpinnerListener listener) {

		this.items = items;
		this.listener = listener;

		StringBuilder spinnerBuffer = new StringBuilder();

		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isSelected()) {
				spinnerBuffer.append(items.get(i).getName());
				spinnerBuffer.append(", ");
			}
		}
		//if (spinnerBuffer.length() > 2)
		//	defaultText = spinnerBuffer.toString().substring(0, spinnerBuffer.toString().length() - 2);

		String spinnerText = spinnerBuffer.toString();
		if (spinnerText.length() > 2)
			spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
		else
			spinnerText = defaultText;

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[]{spinnerText});
		setAdapter(adapterSpinner);

		if (position != -1) {
			items.get(position).setSelected(true);
			//listener.onItemsSelected(items);
			onCancel(null);
		}
	}

	public interface LimitExceedListener {
		void onLimitListener(KeyPairBoolData data);
	}

	//Adapter Class
	public class MyAdapter extends BaseAdapter implements Filterable {

		List<KeyPairBoolData> arrayList;
		List<KeyPairBoolData> mOriginalValues; // Original Values
		LayoutInflater inflater;

		public MyAdapter(Context context, List<KeyPairBoolData> arrayList) {
			this.arrayList = arrayList;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return arrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "getView() enter");
			MultiSpinner.MyAdapter.ViewHolder holder;

			if (convertView == null) {
				holder = new MultiSpinner.MyAdapter.ViewHolder();
				convertView = inflater.inflate(R.layout.item_listview_multiple,  parent, false);
				holder.textView = (TextView) convertView.findViewById(R.id.alertTextView);
				holder.checkBox = (CheckBox) convertView.findViewById(R.id.alertCheckbox);

				convertView.setTag(holder);
			} else {
				holder = (MultiSpinner.MyAdapter.ViewHolder) convertView.getTag();
			}

			//final int backgroundColor = (position%2 == 0) ? R.color.list_even : R.color.list_odd;
			//convertView.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColor));

			final KeyPairBoolData data = arrayList.get(position);

			holder.textView.setText(data.getName());
			holder.textView.setTypeface(null, Typeface.NORMAL);
			holder.textView.setTextColor(ContextCompat.getColor(getContext(), R.color.ColorDarkGrey));
			holder.checkBox.setChecked(data.isSelected());

			convertView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(data.isSelected()) { // deselect
						selected--;
					/*} else if(selected == limit) { // select with limit
						if(limitListener != null)
							limitListener.onLimitListener(data);
						return;*/
					} else { // selected
						selected++;
					}

					final MultiSpinner.MyAdapter.ViewHolder temp = (MultiSpinner.MyAdapter.ViewHolder) v.getTag();
					temp.checkBox.setChecked(!temp.checkBox.isChecked());

					data.setSelected(!data.isSelected());
					Log.i(TAG, "On Click Selected Item : " + data.getName() + " : " + data.isSelected());
					notifyDataSetChanged();
				}
			});
			if (data.isSelected()) {
				holder.textView.setTypeface(null, Typeface.BOLD);
				holder.textView.setTextColor(ContextCompat.getColor(getContext(), R.color.ColorPrimary));
				//convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.list_selected));
			}
			holder.checkBox.setTag(holder);

			return convertView;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public Filter getFilter() {
			return new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {

					arrayList = (List<KeyPairBoolData>) results.values; // has the filtered values
					notifyDataSetChanged();  // notifies the data with new filtered values
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
					List<KeyPairBoolData> FilteredArrList = new ArrayList<>();

					if (mOriginalValues == null) {
						mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
					}

					/********
					 *
					 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
					 *  else does the Filtering and returns FilteredArrList(Filtered)
					 *
					 ********/
					if (constraint == null || constraint.length() == 0) {

						// set the Original result to return
						results.count = mOriginalValues.size();
						results.values = mOriginalValues;
					} else {
						constraint = constraint.toString().toLowerCase();
						for (int i = 0; i < mOriginalValues.size(); i++) {
							Log.i(TAG, "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).isSelected());
							String data = mOriginalValues.get(i).getName();
							if (data.toLowerCase().contains(constraint.toString())) {
								FilteredArrList.add(mOriginalValues.get(i));
							}
						}
						// set the Filtered result to return
						results.count = FilteredArrList.size();
						results.values = FilteredArrList;
					}
					return results;
				}
			};
		}

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }
	}
}