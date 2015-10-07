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

package com.klinker.android.launcher.addons.settings;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.klinker.android.launcher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HiddenAppsActivity extends ListActivity {

    private boolean mSaved;

    private static final int MENU_RESET = 0;

    private PackageManager mPackageManager;

    private AppsAdapter mAppsAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setUpWindow();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.hidden_apps);
        setContentView(R.layout.hidden_apps_list);

        setProgressBarIndeterminateVisibility(true);
        setProgressBarIndeterminate(true);

        mPackageManager = getPackageManager();
        mAppsAdapter = new AppsAdapter(this, R.layout.hidden_apps_list_item);
        mAppsAdapter.setNotifyOnChange(true);

        setListAdapter(mAppsAdapter);

        AsyncTask<Void, Void, List<AppEntry>> refreshAppsTask = new AsyncTask<Void, Void, List<AppEntry>>() {

            @Override
            protected void onPostExecute(List<AppEntry> apps) {
                mAppsAdapter.clear();
                mAppsAdapter.addAll(apps);
                restoreCheckedItems();
                setProgressBarIndeterminateVisibility(false);
                setProgressBarIndeterminate(false);
            }

            @Override
            protected List<AppEntry> doInBackground(Void... params) {
                return refreshApps();
            }
        };
        refreshAppsTask.execute(null, null, null);

        SettingsActivity.prefChanged = true;
    }

    public void setUpWindow() {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .6f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .85), (int) (height * .63));
        } else {
            getWindow().setLayout((int) (width * .5), (int) (height * .8));
        }
    }

    @Override
    public void onPause() {
        save();
        super.onPause();
    }

    private void save() {
        if (mSaved) {
            return;
        }
        String string = "";

        SparseBooleanArray checked = getListView().getCheckedItemPositions();

        AppsAdapter listAdapter = (AppsAdapter) getListAdapter();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                AppEntry app = listAdapter.getItem(checked.keyAt(i));
                if (!string.isEmpty())
                    string += "|";
                string += app.componentName.flattenToString();
            }
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("hidden_apps", string);
        editor.commit();

        mSaved = true;
    }

    private void restoreCheckedItems() {
        List<ComponentName> apps = new ArrayList<ComponentName>();
        String[] flattened = PreferenceManager.getDefaultSharedPreferences(this).getString("hidden_apps", "").split("\\|");
        for (String flat : flattened) {
            apps.add(ComponentName.unflattenFromString(flat));
        }

        AppsAdapter listAdapter = (AppsAdapter) getListAdapter();

        for (int i = 0; i < listAdapter.getCount(); i++) {
            AppEntry info = listAdapter.getItem(i);
            if (apps.contains(info.componentName)) {
                getListView().setItemChecked(i, true);
            }
        }

        mSaved = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_RESET, 0, getResources().getString(R.string.clear))
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    private void reset() {
        for (int i = 0; i < getListView().getCount(); i++) {
            getListView().setItemChecked(i, false);
        }

        mSaved = false;
    }

    private List<AppEntry> refreshApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(mPackageManager));
        List<AppEntry> appEntries = new ArrayList<AppEntry>(apps.size());
        for (ResolveInfo info : apps) {
            if (!info.activityInfo.packageName.equals("com.klinker.android.launcher")) {
                appEntries.add(new AppEntry(info));
            }
        }
        return appEntries;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == MENU_RESET) {
            reset();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mSaved = false;
    }

    private final class AppEntry {

        public final ComponentName componentName;
        public final String title;

        public AppEntry(ResolveInfo info) {
            componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
            title = info.loadLabel(mPackageManager).toString();
        }
    }

    /**
     * App view holder used to reuse the views inside the list.
     */
    private static class AppViewHolder {
        public final TextView title;
        public final ImageView icon;

        public AppViewHolder(View parentView) {
            icon = (ImageView) parentView.findViewById(R.id.icon);
            title = (TextView) parentView.findViewById(R.id.title);
        }
    }

    public class AppsAdapter extends ArrayAdapter<AppEntry> {

        private final LayoutInflater mInflator;

        private ConcurrentHashMap<String, Drawable> mIcons;
        private Drawable mDefaultImg;
        private List<AppEntry> mApps;

        public AppsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            mApps = new ArrayList<AppEntry>();

            mInflator = LayoutInflater.from(context);

            // set the default icon till the actual app icon is loaded in async
            // task
            mDefaultImg = context.getResources().getDrawable(android.R.mipmap.sym_def_app_icon);
            mIcons = new ConcurrentHashMap<String, Drawable>();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            AppViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.hidden_apps_list_item, parent, false);
                viewHolder = new AppViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (AppViewHolder) convertView.getTag();
            }

            AppEntry app = getItem(position);

            viewHolder.title.setText(app.title);

            Drawable icon = mIcons.get(app.componentName.getPackageName());
            viewHolder.icon.setImageDrawable(icon != null ? icon : mDefaultImg);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            // If we have new items, we have to load their icons
            // If items were deleted, remove them from our mApps
            List<AppEntry> newApps = new ArrayList<AppEntry>(getCount());
            List<AppEntry> oldApps = new ArrayList<AppEntry>(getCount());
            for (int i = 0; i < getCount(); i++) {
                AppEntry app = getItem(i);
                if (mApps.contains(app)) {
                    oldApps.add(app);
                } else {
                    newApps.add(app);
                }
            }

            if (newApps.size() > 0) {
                new LoadIconsTask().execute(newApps.toArray(new AppEntry[] {}));
                newApps.addAll(oldApps);
                mApps = newApps;
            } else {
                mApps = oldApps;
            }
        }

        /**
         * An asynchronous task to load the icons of the installed applications.
         */
        private class LoadIconsTask extends AsyncTask<AppEntry, Void, Void> {
            @Override
            protected Void doInBackground(AppEntry... apps) {
                for (AppEntry app : apps) {
                    try {
                        if (mIcons.containsKey(app.componentName.getPackageName())) {
                            continue;
                        }
                        Drawable icon = mPackageManager.getApplicationIcon(app.componentName
                                .getPackageName());
                        mIcons.put(app.componentName.getPackageName(), icon);
                        publishProgress();
                    } catch (PackageManager.NameNotFoundException e) {
                        // ignored; app will show up with default image
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... progress) {
                notifyDataSetChanged();
            }
        }
    }
}