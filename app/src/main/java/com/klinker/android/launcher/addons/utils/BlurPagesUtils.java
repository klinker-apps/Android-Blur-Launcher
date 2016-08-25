package com.klinker.android.launcher.addons.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.klinker.android.launcher.R;

public class BlurPagesUtils {

    public static Page[] PAGES = new Page[] {
            new Page(".calc_page.LauncherFragment", R.string.calculator_page)
    };

    public static Item[] getAvailablePages(Context context) {

        final Item[] items = new Item[PAGES.length];

        for (int i = 0; i < items.length; i++) {
            items[i] = new Item(context.getString(PAGES[i].nameRes),
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    PAGES[i].path,
                    "com.klinker.android.launcher");
        }

        return items;
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
