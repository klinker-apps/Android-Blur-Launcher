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

package com.klinker.android.launcher.info_page;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.klinker.android.launcher.api.BaseCard;
import com.klinker.android.launcher.api.BaseLauncherPage;
import com.klinker.android.launcher.api.Card;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.api.CardsLayout;
import com.klinker.android.launcher.info_page.cards.calendar.CalendarUtils;
import com.klinker.android.launcher.info_page.cards.weather.WeatherCard;
import com.klinker.android.launcher.info_page.widgets.LockableScrollView;
import com.klinker.android.launcher.info_page.widgets.swipe_refresh_layout.OffsetSwipeRefreshLayout;
import org.javia.arity.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Fragment to display all of the info cards that are created and implemented by library
 */
public class LauncherFragment extends BaseLauncherPage {

    private static final int SEARCH_HEADER_PX = 180;

    public Context context;
    private ResourceHelper resHelper;

    // root view of the fragment
    private View rootView;

    // view which holds all of the cards to be displayed
    private CardsLayout cardsLayout;

    // header at the top with a search bar for Google Now
    private View searchHeader;
    private ImageView headerBackground;

    // the pull to refresh layout
    private OffsetSwipeRefreshLayout mRefreshLayout;

    // A lockable scroll view because the pull to refresh animation
    // gets messed up if you scroll :/
    private LockableScrollView mScroll;

    // ArrayList to hold the cards we want to create
    private ArrayList<Card> cardList;

    /**
     * Creates and instance of this fragment which is then returned to the pager adapter and displayed
     * @param position the position on the pager of this page
     * @return an instance of the LauncherFragment to be displayed
     */
    @Override
    public BaseLauncherPage getFragment(int position) {
        return new LauncherFragment();
    }

    /**
     * Creates a View array which will be faded in and out as the page is opened and closed from the main launcher
     * @return an array of all the views to be faded in and out
     */
    @Override
    public View[] getBackground() {
        return new View[] {rootView.findViewById(resHelper.getId("background")), headerBackground};
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;

        // initialize our resource helper so that we can get layouts, drawables, ect. This is required
        // so that the launcher can get resources from different packages, using R.string.example or
        // R.drawable.example won't work correctly because it will try to grab the resource that the launcher
        // holds at that position, not the resource that this package holds.
        resHelper = new ResourceHelper(getActivity(), Utils.PACKAGE_NAME);


        // get the cards the user has selected and add them to the list
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        cardList = new ArrayList<Card>();

        for (int i = 0; i < sharedPrefs.getInt("launcher_number_of_cards", 0); i++) {
            String pack = sharedPrefs.getString("launcher_card_package_name_" + i, "");
            String path =  sharedPrefs.getString("launcher_card_class_path_" + i, "");

            cardList.add(new Card(pack, path));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.v("info_page", "on create view");

        // inflate our view to be displayed with the helper
        rootView = resHelper.getLayout("info_page_layout");

        setUpLayout();

        if (cardList.isEmpty()) {
            // we will add a card to tell them how to add more
        } else {
            // loop through the current cards
            for (Card card : cardList) {
                try {
                    // use the package and class path for the card
                    String packageName = card.getPackage();
                    String className = card.getClassPath();

                    // create context from that package
                    Context classContext = context.createPackageContext(packageName,
                            Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);

                    // load the class
                    ClassLoader loader = classContext.getClassLoader();
                    Class c = loader.loadClass(packageName + className);

                    Log.v("launcher_card", c.toString());

                    // get the constructor and create the object
                    Constructor constructor = c.getConstructor(Context.class);
                    Object classInstance = constructor.newInstance(context);

                    // create the card from the object
                    Method method = classInstance.getClass().getMethod("getCard", Context.class);
                    Object cardObj = method.invoke(classInstance, context);

                    // add that card to the layout
                    cardsLayout.addCard((FrameLayout) cardObj);
                } catch (InvocationTargetException e) {
                    e.getCause().printStackTrace();
                } catch (Exception e) {
                    // something is wrong with the class path
                    // or they are trying to read from a protected package
                    // which probably means that it was paid on the play store.
                    e.printStackTrace();
                }
            }
        }

        // Add padding at the bottom of the list if the navigation bar is showing and translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Utils.hasNavBar(context)) {
            LinearLayout navBarSpace = new LinearLayout(context);
            navBarSpace.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    Utils.getNavBarHeight(context)
            ));

            cardsLayout.addView(navBarSpace);
        }

        new BlurHeader(context).execute();

        return rootView;
    }

    /**
     * Sets up the view manually instead of inflating it completely from the layout xml. Inflating custom
     * classes does not work with the resource helper because of a system limitation, therefore we much create
     * them manually and wrap the views that we want to use them on here. A little inconvenient, but there is no
     * way around this that I figured out
     */
    public void setUpLayout() {
        // scrollview to hold the card content and header
        mScroll = new LockableScrollView(context);
        mScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mScroll.setVerticalScrollBarEnabled(false);

        // layout which holds the content inside the scrollview such as search header and cards
        LinearLayout content = new LinearLayout(context);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // search header to control the search bar and background at the top
        searchHeader = (RelativeLayout) resHelper.getLayout("search_header");
        headerBackground = (ImageView) searchHeader.findViewById(resHelper.getId("search_background"));
        searchHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SearchManager searchManager =
                        (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

                Intent intent;

                if (Build.VERSION.SDK_INT >= 16) {
                    // we can attempt a global search
                    ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
                    if (globalSearchActivity == null) {
                        // they dont have a global search, so lets just try a web search
                        intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    } else {
                        intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
                        intent.setComponent(globalSearchActivity);
                    }
                } else {
                    // it is only available on jelly bean and up
                    intent = new Intent(Intent.ACTION_WEB_SEARCH);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "No search app found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // create cards layout which will store all of the cards and display them correctly
        cardsLayout = new CardsLayout(context);
        cardsLayout.setOrientation(LinearLayout.VERTICAL);
        cardsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                resHelper.getBoolean("isTablet") ? resHelper.getDimension("card_width") :
                        LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));

        // create a swipe to refresh listener which will be placed under the search header and above the cards layout
        mRefreshLayout = new OffsetSwipeRefreshLayout(context);
        mRefreshLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mRefreshLayout.setOffset(Utils.toDP(context, SEARCH_HEADER_PX));
        mRefreshLayout.setTarget(cardsLayout);
        mRefreshLayout.setScrollView(mScroll);
        mRefreshLayout.setOnRefreshListener(new OffsetSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // dont allow scrolling while refreshing
                mScroll.setScrollingEnabled(false);
                cardsLayout.startRefresh(new CardsLayout.OnCardsRefreshListener() {
                    @Override
                    public void onFinishedRefresh() {
                        // once finished, stop refreshing and allow scrolling again
                        mRefreshLayout.setRefreshing(false);
                        mScroll.setScrollingEnabled(true);
                    }
                });
            }
        });

        // add all of the views to the root
        ((LinearLayout) rootView.findViewById(resHelper.getId("content")))
                .addView(mRefreshLayout);
        mRefreshLayout.addView(mScroll);
        mScroll.addView(content);
        content.addView(searchHeader);
        content.addView(cardsLayout);

        // set our color scheme of some nice holo colors for the refresh layout, similar to
        // Google Now's colors but not quite the same
        mRefreshLayout.setColorScheme(
                resHelper.getColor("holo_orange_light"),
                resHelper.getColor("holo_blue_light"),
                resHelper.getColor("holo_green_light"),
                resHelper.getColor("holo_red_light")
        );
    }

    @Override
    public void onFragmentsOpened() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long currentTime = CalendarUtils.getCurrentTimestamp();

        // if the last time that we refreshed the page was greater than 15 mins ago, we will auto-refresh
        if (currentTime - sharedPrefs.getLong("launcher_info_page_last_time_refresh", 0l) > 900000) {

            // set the layout to refreshing and run the refresh
            mRefreshLayout.setRefreshing(true);
            mScroll.setScrollingEnabled(false);
            cardsLayout.startRefresh(new CardsLayout.OnCardsRefreshListener() {
                @Override
                public void onFinishedRefresh() {
                    // once finished, stop refreshing and allow scrolling again
                    mRefreshLayout.setRefreshing(false);
                    mScroll.setScrollingEnabled(true);
                }
            });

            // update the last refresh time
            sharedPrefs.edit().putLong("launcher_info_page_last_time_refresh", currentTime).commit();
        }
    }

    private class BlurHeader extends AsyncTask<String, Void, Bitmap> {
        Context context;
        public BlurHeader(Context context1) {
            context = context1;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                final BitmapDrawable wallpaperDrawable = (BitmapDrawable) wallpaperManager.getDrawable();
                final Bitmap beforeScale = wallpaperDrawable.getBitmap();
                final Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(beforeScale, Utils.getScreenWidth(context) / 2, Utils.toDP(context, SEARCH_HEADER_PX), ScalingUtilities.ScalingLogic.CROP);
                /*int imageWidth = beforeScale.getWidth();
                int screenWidth = Utils.getScreenWidth(context);
                Log.v("launcher_info_page", "screen width: " + screenWidth + " image width: " + imageWidth);
                if (imageWidth > screenWidth) {
                    float scale = (float) screenWidth / (float) imageWidth;
                    Log.v("launcher_info_page", "scale: " + scale);
                    return Bitmap.createScaledBitmap(beforeScale, screenWidth, (int) ((float) beforeScale.getHeight() * scale), true);
                }*/

                return scaledBitmap;
            } catch (Exception e) {
                // security exception??
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                headerBackground.setImageBitmap(result);
            }
        }
    }
}
