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

package com.klinker.android.launcher.vertical_app_page;

import android.app.Activity;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.klinker.android.launcher.api.BaseLauncherPage;
import com.klinker.android.launcher.api.Card;
import com.klinker.android.launcher.api.CardsLayout;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.info_page.Utils;
import com.klinker.android.launcher.info_page.cards.calendar.CalendarUtils;
import com.klinker.android.launcher.info_page.widgets.LockableScrollView;
import com.klinker.android.launcher.info_page.widgets.swipe_refresh_layout.OffsetSwipeRefreshLayout;
import org.javia.arity.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment to display all of the info cards that are created and implemented by library
 */
public class LauncherFragment extends BaseLauncherPage {

    public Context context;
    private IconCache mCache;
    private ResourceHelper resHelper;

    // root view of the fragment
    private View rootView;
    private View background;
    private LinearLayout gridContainer;
    private HeaderGridView mGrid;

    private AppAdapter mAdapter;

    private int screenWidth;
    private int numberColumns;

    private ArrayList<String> hiddenPackages = new ArrayList<String>();

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
        return new View[] {background};
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] flattenedPackages = sp.getString("hidden_apps", "").split("\\|");
        for (String flat : flattenedPackages) {
            ComponentName cmp = ComponentName.unflattenFromString(flat);
            if (cmp != null) {
                hiddenPackages.add(cmp.getPackageName());
            }
        }
        // add blur to the hidden apps
        ComponentName cmp = ComponentName.unflattenFromString("com.klinker.android.launcher/com.klinker.android.launcher.launcher3.Launcher");
        hiddenPackages.add(cmp.getPackageName());

        mCache = new IconCache(context);

        // inflate our view to be displayed with the helper
        rootView = resHelper.getLayout("app_drawer_layout");
        background = rootView.findViewById(resHelper.getId("vertical_drawer_background"));
        gridContainer = (LinearLayout) rootView.findViewById(resHelper.getId("grid_container"));
        mGrid = new HeaderGridView(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mGrid.setLayoutParams(params);

        // make a header for the status bar, we don't want the apps underneath it
        if (Build.VERSION.SDK_INT >= 19) {
            AbsListView.LayoutParams statusBar = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    com.klinker.android.launcher.vertical_app_page.Utils.getStatusBarHeight(context) + Utils.toDP(context, 20));
            View statusBarView = new View(context);
            statusBarView.setLayoutParams(statusBar);
            mGrid.addHeaderView(statusBarView);
        }

        gridContainer.addView(mGrid);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        numberColumns = Integer.parseInt(sp.getString("col_count_all_apps", "4"));

        mGrid.setNumColumns(numberColumns);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ResolveInfo> list = getAllApps();

                if (appsList == null || list.size() != appsList.size()) {
                    appsList = list;
                    realData = convertToRealData(appsList);

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            } else {
                                mAdapter = new AppAdapter(context, realData, screenWidth / numberColumns);
                                mGrid.setAdapter(mAdapter);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    List<ResolveInfo> appsList;
    List<AppAdapter.AppInfo> realData;

    public List<ResolveInfo> getAllApps() {
        final PackageManager pm = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

        return appList;
    }

    public List<AppAdapter.AppInfo> convertToRealData(List<ResolveInfo> apps) {
        List<AppAdapter.AppInfo> info = new ArrayList<AppAdapter.AppInfo>();

        PackageManager pm = context.getPackageManager();

        for (ResolveInfo i : apps) {
            AppAdapter.AppInfo appInfo = new AppAdapter.AppInfo();

            appInfo.nameString = i.loadLabel(pm).toString();
            try {
                appInfo.icon = ((BitmapDrawable)mCache.getFullResIcon(i)).getBitmap();
            } catch (Exception e) {
                appInfo.icon = null;
            }

            ActivityInfo activity = i.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            appInfo.openIntent = new Intent(Intent.ACTION_MAIN);

            appInfo.openIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            appInfo.openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            appInfo.openIntent.setComponent(name);

            if (!hiddenPackages.contains(i.activityInfo.packageName)) {
                info.add(appInfo);
            }
        }

        int extra = info.size() % numberColumns;
        extra = numberColumns - extra;

        Log.v("blur_page", "extra: " + extra);

        // need to add a space to the bottom, but I don't have the grid view set up for footer views
        // so we just fill the last row with null items
        if (Utils.hasNavBar(context) && Build.VERSION.SDK_INT >= 19) {
            for (int i = 0; i < numberColumns + extra; i++) {
                info.add(null);
            }
        }

        return info;
    }
}
