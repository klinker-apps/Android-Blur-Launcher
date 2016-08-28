package xyz.klinker.blur.addons.pages;

import android.support.v4.view.ViewPager;
import android.view.View;

import xyz.klinker.blur.extra_pages.calc_page.Utils;

public class PageSlideTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            float vertMargin = 0;
            float horzMargin = pageWidth * Utils.toDP(view.getContext(), 25);
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
