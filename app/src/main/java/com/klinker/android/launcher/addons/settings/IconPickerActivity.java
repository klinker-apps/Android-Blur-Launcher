package com.klinker.android.launcher.addons.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.klinker.android.launcher.addons.utils.IconPackHelper;

import java.lang.ref.WeakReference;
import java.util.*;

public class IconPickerActivity extends Activity {

    public static final String SELECTED_RESOURCE_EXTRA = "selected_resource";
    public static final String SELECTED_BITMAP_EXTRA = "bitmap";

    public String iconName;

    public void setUpWindow() {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .87f;  // set it higher if you want to dim behind the window
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpWindow();

        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int iconSize = activityManager.getLauncherLargeIconSize();
        final String pkgName = getIntent().getStringExtra("package");
        iconName = getIntent().getStringExtra("icon_name");

        GridView gridview = new GridView(this);
        gridview.setNumColumns(GridView.AUTO_FIT);
        gridview.setHorizontalSpacing(40);
        gridview.setVerticalSpacing(40);
        gridview.setPadding(20, 20, 20, 0);
        gridview.setFastScrollEnabled(true);
        gridview.setColumnWidth(iconSize);
        gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

        gridview.setAdapter(new ImageAdapter(this, pkgName));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                DrawableInfo d = (DrawableInfo) adapterView.getAdapter().getItem(position);

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String currentList = sharedPrefs.getString("launcher_custom_icon_names", "");
                SharedPreferences.Editor e = sharedPrefs.edit();

                List<String> customNames = Arrays.asList(sharedPrefs.getString("launcher_custom_icon_names", "").split(":|:"));
                if (!customNames.contains(iconName)) {
                    e.putString("launcher_custom_icon_names", currentList + iconName + ":|:");
                }

                e.putString(iconName, d.resource_name);
                e.commit();

                // completely restart the Launcher
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        setContentView(gridview);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mResources;
        private ArrayList<DrawableInfo> mDrawables = new ArrayList<DrawableInfo>();

        public class FetchDrawable extends AsyncTask<Integer, Void, Drawable> {
            WeakReference<ImageView> mImageView;

            FetchDrawable(ImageView imgView) {
                mImageView = new WeakReference<ImageView>(imgView);
            }

            @Override
            protected Drawable doInBackground(Integer... position) {
                DrawableInfo info = getItem(position[0]);
                int itemId = info.resource_id;
                try {
                    Drawable d = mResources.getDrawable(itemId);
                    info.drawable = new WeakReference<Drawable>(d);
                    return d;
                } catch (Resources.NotFoundException e) {
                    // don't know why this would happen, but it did once
                    return null;
                }
            }

            @Override
            public void onPostExecute(Drawable result) {
                if (mImageView.get() != null && result != null) {
                    mImageView.get().setImageDrawable(result);
                }
            }
        }

        public ImageAdapter(Context c, String pkgName) {
            mContext = c;
            Map<String, String> resources = IconPackHelper.getIconPackResources(c, pkgName);
            try {
                mResources = c.getPackageManager().getResourcesForApplication(pkgName);
                ArrayList<String> drawables = new ArrayList<String>(resources.values());

                // creates duplicates for some reason... Hash set will remove them
                HashSet hs = new HashSet();
                hs.addAll(drawables);
                drawables.clear();
                drawables.addAll(hs);

                try {
                    Collections.sort(drawables);
                } catch (Exception e) {
                    
                }

                for (String s : drawables) {
                    if (s == null) {
                        continue;
                    }
                    int id = mResources.getIdentifier(s, "drawable", pkgName);
                    if (id != 0) {
                        mDrawables.add(new DrawableInfo(s, id));
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }

        public int getCount() {
            return mDrawables.size();
        }

        public DrawableInfo getItem(int position) {
            return mDrawables.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(
                        GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                imageView = (ImageView) convertView;
                Object tag = imageView.getTag();
                if (tag != null && tag instanceof FetchDrawable) {
                    ((FetchDrawable) tag).cancel(true);
                }
            }
            FetchDrawable req = new FetchDrawable(imageView);
            imageView.setTag(req);
            req.execute(position);
            return imageView;
        }
    }

    private class DrawableInfo {
        WeakReference<Drawable> drawable;
        final String resource_name;
        final int resource_id;
        DrawableInfo(String n, int i) {
            resource_name = n;
            resource_id = i;
        }
    }
}