package com.klinker.android.launcher.vertical_app_page;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.extra_pages.R;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends BaseAdapter {

    private List<AppInfo> data;
    private Context context;
    private ResourceHelper helper;
    private int gridWidth;
    private boolean wantLAnimation = false;

    public AppAdapter(Context context, List<AppInfo> data, int width) {
        this.context = context;
        this.data = data;
        this.helper = new ResourceHelper(context, Utils.PACKAGE_NAME);
        this.gridWidth = width;

        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getString("app_style", "android-l").equals("android-l")) {
            wantLAnimation = true;
        }
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = helper.getLayout("vertical_drawer_app_item");

            int height = convertView.getHeight();

            AbsListView.LayoutParams params;
            if (height > gridWidth) {
                params = new AbsListView.LayoutParams(gridWidth, height);
            } else {
                params = new AbsListView.LayoutParams(gridWidth, gridWidth);
            }
            convertView.setLayoutParams(params);

            ViewHolder holder = new ViewHolder();
            holder.appName = (TextView) convertView.findViewById(helper.getId("drawer_app_name"));
            holder.appIcon = (ImageView) convertView.findViewById(helper.getId("drawer_app_icon"));
            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();

        if (data.get(position) != null) {
            holder.appName.setText(data.get(position).nameString);
            holder.appIcon.setImageBitmap(data.get(position).icon);
        } else {
            holder.appIcon.setImageDrawable(null);
            holder.appName.setText("");
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get(position).openIntent == null) {
                    Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = data.get(position).openIntent;

                if (wantLAnimation && Build.VERSION.SDK_INT < 21) {
                    context.startActivity(i);
                    ((Activity)context).overridePendingTransition(
                            helper.getIdentifier("slide_up", "anim"),
                            helper.getIdentifier("remain", "anim"));
                } else {
                    if (Build.VERSION.SDK_INT >= 16 && !wantLAnimation) {
                        ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                                v.getMeasuredWidth(), v.getMeasuredHeight());
                        context.startActivity(i, opts.toBundle());
                    } else {
                        context.startActivity(i);
                    }
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
    }

    static class AppInfo {
        public Bitmap icon;
        public String nameString;
        public Intent openIntent;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}