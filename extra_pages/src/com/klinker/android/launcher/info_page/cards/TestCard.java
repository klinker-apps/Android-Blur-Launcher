/*
 * Copyright 2014 Klinker Apps Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.launcher.info_page.cards;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.klinker.android.launcher.api.BaseCard;
import com.klinker.android.launcher.api.CardsLayout;
import com.klinker.android.launcher.info_page.Utils;

/**
 * Simple test card that displays and empty view
 */
public class TestCard extends BaseCard {

    private static final String LOGTAG = "TestCard";

    private static final int DEFAULT_HEIGHT = 200;

    // values to randomize our refresh rate between
    private static final int MIN_REFRESH = 1000;
    private static final int MAX_REFRESH = 5000;

    public BaseCard getCard(Context context) {
        return new TestCard(context);
    }

    /**
     * Default constructor
     * @param context context of the fragment
     * @param parent parent of this card, ie the CardsLayout
     */
    public TestCard(Context context) {
        super(context);
    }

    /**
     * Refresh the card with just a random sleep time
     */
    @Override
    public void onRefresh() {
        int refreshTime = MIN_REFRESH + (int) (Math.random() * ((MAX_REFRESH - MIN_REFRESH) + 1));
        Log.v(LOGTAG, "starting refresh for: " + refreshTime);
        try { Thread.sleep(refreshTime); } catch (Exception e) { }
        Log.v(LOGTAG, "finished refresh");
    }

    /**
     * Creates a view for the card, just a blank view 200dp tall
     */
    @Override
    public void setUpCardLayout() {
        // Just create an empty view with the specified size
        View v = new View(getContext());
        v.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.toDP(getContext(), DEFAULT_HEIGHT)));

        // add this view to the linear layout to display
        addView(v);
    }

    @Override
    public void refreshCardLayout() {
        // do nothing to this test card
    }

    /**
     * Callback for when the view was pressed
     * @param view view that was pressed
     */
    @Override
    public void onCardPressed(View view) {
        // do nothing when pressed
    }
}
