package xyz.klinker.blur.addons.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import xyz.klinker.blur.R;

public class BlurPagesUtils {

    public static Page[] PAGES = new Page[] {
            new Page(".extra_pages.weather_page.LauncherFragment", R.string.weather_page),
            new Page(".extra_pages.calendar_page.LauncherFragment", R.string.calendar_page),
            new Page(".extra_pages.calc_page.LauncherFragment", R.string.calculator_page)
    };

    public static Item[] getAvailablePages(Context context) {
        final Item[] items = new Item[getNumPages()];

        for (int i = 0; i < items.length; i++) {
            items[i] = new Item(context.getString(PAGES[i].nameRes),
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    PAGES[i].path,
                    "xyz.klinker.blur");
        }

        return items;
    }

    public static ListAdapter getPackagesAdapter(final Context context, final Item[] items) {
        return new ArrayAdapter<Item>(
                context,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.picker_background));
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].actualIcon, null, null, null);
                tv.setCompoundDrawablePadding((int) (5 * context.getResources().getDisplayMetrics().density + 0.5f));
                tv.setText(items[position].text);
                return v;
            }
        };
    }

    public static int getNumPages() {
        return PAGES.length;
    }

    private static class Page {
        public String path;
        public int nameRes;

        public Page(String path, int nameRes) {
            this.path = path;
            this.nameRes = nameRes;
        }
    }
}
