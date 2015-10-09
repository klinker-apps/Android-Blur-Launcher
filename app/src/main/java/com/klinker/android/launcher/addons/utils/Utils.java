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

package com.klinker.android.launcher.addons.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.receivers.AdminReceiver;
import com.klinker.android.launcher.addons.settings.AppSettings;
import com.klinker.android.launcher.addons.settings.IconPickerActivity;
import com.klinker.android.launcher.api.Card;
import com.klinker.android.launcher.launcher3.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Utils {

    private static final int NUM_INTERNAL_PAGES = 3;
    private static final int NUM_INTERNAL_CARDS = 3;

    public static Item[] getPackageItems(Context context) {

        ArrayList<String> currentPackages = new ArrayList<String>();
        ArrayList<String> currentDefaults = new ArrayList<String>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int i = 0; i < 5; i++) {
            String pack = sharedPrefs.getString("launcher_package_name_" + i, "");
            String path = sharedPrefs.getString("launcher_class_path_" + i, "");

            if (!path.isEmpty()) {
                if (pack.equals("com.klinker.android.launcher")) {
                    currentDefaults.add(path);
                } else {
                    currentPackages.add(pack);
                }
            }
        }

        final PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (int i = 0; i < packages.size(); i++) {
            Bundle metaData = packages.get(i).metaData;
            if (metaData == null || currentPackages.contains(packages.get(i).packageName)) {
                packages.remove(i--);
                continue;
            }

            try {
                boolean frag = metaData.getString("launcher_fragment").startsWith(".");
                if (!frag) {
                    packages.remove(i--);
                }
            } catch (Exception e) {
                // package doesn't exist
                packages.remove(i--);
            }
        }

        int size = NUM_INTERNAL_PAGES;

        if (currentDefaults.contains(".info_page.LauncherFragment")) {
            size--;
        }

        if (currentDefaults.contains(".calc_page.LauncherFragment")) {
            size--;
        }

        if (currentDefaults.contains(".vertical_app_page.LauncherFragment")) {
            size--;
        }

        final Item[] items = new Item[packages.size() + size];

        for (int i = 0; i < packages.size(); i++) {
            items[i] = new Item(packages.get(i).loadLabel(pm).toString(),   // text
                    pm.getApplicationIcon(packages.get(i)),                 // icon
                    packages.get(i).metaData.getString("launcher_fragment"),// class path
                    packages.get(i).packageName
            );
        }

        int insertedPages = 0;

        if (!currentDefaults.contains(".info_page.LauncherFragment")) {
            items[packages.size() + insertedPages] = new Item(context.getString(R.string.info_page),
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    ".info_page.LauncherFragment",
                    "com.klinker.android.launcher");
            insertedPages++;
        }

        if (!currentDefaults.contains(".calc_page.LauncherFragment")) {
            items[packages.size() + insertedPages] = new Item(context.getResources().getString(R.string.calculator_page),
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    ".calc_page.LauncherFragment",
                    "com.klinker.android.launcher");
            insertedPages++;
        }


        if (!currentDefaults.contains(".vertical_app_page.LauncherFragment")) {
            items[packages.size() + insertedPages] = new Item(context.getResources().getString(R.string.vertical_app_drawer),
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    ".vertical_app_page.LauncherFragment",
                    "com.klinker.android.launcher");
            insertedPages++;
        }

        return items;
    }

    public static ListAdapter getPackagesAdapter(final Context context, final Item[] items) {
        return new ArrayAdapter<Item>(
                context,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.picker_background));
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].actualIcon, null, null, null);
                tv.setCompoundDrawablePadding((int) (5 * context.getResources().getDisplayMetrics().density + 0.5f));
                tv.setText(items[position].text);
                return v;
            }
        };
    }

    public static Card[] getPackageCards(Context context, ArrayList<Card> current) {
        // we don't want to include the current cards in our search here
        ArrayList<String> currentPackages = new ArrayList<String>();
        for (Card c : current) {
            currentPackages.add(c.getPackage());
        }
        ArrayList<String> currentDefaults = new ArrayList<String>();
        for (Card c : current) {
            if (c.getPackage().equals("com.klinker.android.launcher")) {
                currentDefaults.add(c.getClassPath());
            }
        }

        final PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (int i = 0; i < packages.size(); i++) {
            Bundle metaData = packages.get(i).metaData;
            if (metaData == null || currentPackages.contains(packages.get(i).packageName)) {
                packages.remove(i--);
                continue;
            }

            try {
                boolean card = metaData.getString("launcher_card").startsWith(".");
                if (!card) {
                    packages.remove(i--);
                }
            } catch (Exception e) {
                // package doesn't exist
                packages.remove(i--);
            }

        }

        int size = NUM_INTERNAL_CARDS;

        if (currentDefaults.contains(".info_page.cards.weather.WeatherCard")) {
            size--;
        }
        if (currentDefaults.contains(".info_page.cards.calendar.NextEventCard")) {
            size--;
        }
        if (currentDefaults.contains(".info_page.cards.next_alarm.NextAlarmCard")) {
            size--;
        }

        final Card[] items = new Card[packages.size() + size];

        for (int i = 0; i < packages.size(); i++) {
            items[i] = new Card(
                    packages.get(i).packageName,                                // package
                    packages.get(i).metaData.getString("launcher_card"),        // class path
                    pm.getApplicationIcon(packages.get(i)),                     // icon
                    packages.get(i).metaData.getString("launcher_card_title")   // text
            );
        }

        int currentIndex = packages.size();

        // we use this to check if the weather card is already on their list
        if (!currentDefaults.contains(".info_page.cards.weather.WeatherCard")) {
            items[currentIndex] = new Card(
                    "com.klinker.android.launcher",
                    ".info_page.cards.weather.WeatherCard",
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    context.getString(R.string.weather_card));
            currentIndex++;
        }

        if (!currentDefaults.contains(".info_page.cards.calendar.NextEventCard")) {
            items[currentIndex] = new Card(
                    "com.klinker.android.launcher",
                    ".info_page.cards.calendar.NextEventCard",
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    context.getString(R.string.calendar_card));
            currentIndex++;
        }

        if (!currentDefaults.contains(".info_page.cards.next_alarm.NextAlarmCard")) {
            items[currentIndex] = new Card(
                    "com.klinker.android.launcher",
                    ".info_page.cards.next_alarm.NextAlarmCard",
                    new ColorDrawable(context.getResources().getColor(android.R.color.transparent)),
                    context.getString(R.string.alarm_card));
            currentIndex++;
        }

        return items;
    }

    public static ListAdapter getCardsAdapter(final Context context, final Card[] items) {
        return new ArrayAdapter<Card>(
                context,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.picker_background));
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].getIcon(), null, null, null);
                tv.setCompoundDrawablePadding((int) (5 * context.getResources().getDisplayMetrics().density + 0.5f));
                tv.setText(items[position].getTitle());
                return v;
            }
        };
    }

    public static boolean isPackageInstalled(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage)) return true;
        }
        return false;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
