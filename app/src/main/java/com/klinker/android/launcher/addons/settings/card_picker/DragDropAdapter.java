package com.klinker.android.launcher.addons.settings.card_picker;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.api.Card;

public final class DragDropAdapter extends BaseAdapter {

    private int[] mIds;
    private int[] mLayouts;
    private LayoutInflater mInflater;
    private ArrayList<Card> mContent;

    public DragDropAdapter(Context context, ArrayList<Card> content) {
        init(context, content);
    }

    private void init(Context context, ArrayList<Card> content) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        mIds = new int[] {R.id.text_view, R.id.icon_view};
        mLayouts = new int[] {R.layout.drag_item};
        mContent = content;
    }

    /**
     * The number of items in the list
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return mContent.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Card getItem(int position) {
        return mContent.get(position);
    }

    /**
     * Use the array index as a unique id.
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayouts[0], null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(mIds[0]);
            holder.icon = (ImageView) convertView.findViewById(mIds[1]);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        holder.text.setText(mContent.get(position).getTitle());
        holder.icon.setImageDrawable(mContent.get(position).getIcon());

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }
}