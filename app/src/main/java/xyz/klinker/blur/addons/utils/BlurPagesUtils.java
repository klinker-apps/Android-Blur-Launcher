package xyz.klinker.blur.addons.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

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
