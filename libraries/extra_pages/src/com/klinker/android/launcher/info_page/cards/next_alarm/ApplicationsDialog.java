/*
 * Copyright (C) 2012 The CyanogenMod Project
 * This code has been modified. Portions copyright (C) 2012, ParanoidAndroid Project.
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

package com.klinker.android.launcher.info_page.cards.next_alarm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.info_page.Utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ApplicationsDialog {

    public PackageManager mPackageManager;
    public ResourceHelper helper;
    
    public AppAdapter createAppAdapter(Context context, List<ResolveInfo> installedAppsInfo){
        mPackageManager = context.getPackageManager();
        helper = new ResourceHelper(context, Utils.PACKAGE_NAME);
        return new AppAdapter(context, installedAppsInfo);
    }

    public class AppItem implements Comparable<AppItem> {
        public CharSequence title;
        public String packageName;
        public Drawable icon;

        @Override
        public int compareTo(AppItem another) {
            return this.title.toString().compareTo(another.title.toString());
        }
    }

    public class AppAdapter extends BaseAdapter {
        protected List<ResolveInfo> mInstalledAppInfo;
        protected List<AppItem> mInstalledApps = new LinkedList<AppItem>();
        
        private Context mContext;
        
        public AppAdapter(Context context, List<ResolveInfo> installedAppsInfo) {
            mInstalledAppInfo = installedAppsInfo;
            mContext = context;
        }

        private void reloadList() {
            final Handler handler = new Handler();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (mInstalledApps) {
                        mInstalledApps.clear();
                        for (ResolveInfo info : mInstalledAppInfo) {
                            final AppItem item = new AppItem();
                            item.title = info.loadLabel(mPackageManager);
                            item.icon = info.loadIcon(mPackageManager);
                            item.packageName = info.activityInfo.packageName;
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    int index = Collections.binarySearch(mInstalledApps, item);
                                    if (index < 0) {
                                        index = -index - 1;
                                        mInstalledApps.add(index, item);
                                    }
                                    notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }).start();
        }

        public void update() {
            reloadList();
        }

        @Override
        public int getCount() {
            return mInstalledApps.size();
        }

        @Override
        public AppItem getItem(int position) {
            return mInstalledApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mInstalledApps.get(position).packageName.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = helper.getLayout("app_item");
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.title = (TextView) convertView.findViewById(helper.getId("app_title"));
                holder.icon = (ImageView) convertView.findViewById(helper.getId("app_icon"));
            }

            AppItem applicationInfo = getItem(position);

            if (holder.title != null) {
                holder.title.setText(applicationInfo.title);
            }

            if (holder.icon != null) {
                Drawable loadIcon = applicationInfo.icon;
                holder.icon.setImageDrawable(loadIcon);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        ImageView icon;
    }
}