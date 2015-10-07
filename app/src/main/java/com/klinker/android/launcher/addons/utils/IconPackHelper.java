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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.AppSettings;

public class IconPackHelper {

    static final String ICON_MASK_TAG = "iconmask";
    static final String ICON_BACK_TAG = "iconback";
    static final String ICON_UPON_TAG = "iconupon";
    static final String ICON_SCALE_TAG = "scale";

    public final static String[] sSupportedActions = new String[] {
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
    };

    public static final String[] sSupportedCategories = new String[] {
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
    };

    // Holds package/class -> drawable
    private Map<String, String> mIconPackResources;
    private final Context mContext;
    private String mLoadedIconPackName;
    private Resources mLoadedIconPackResource;
    private Drawable[] mIconBack;
    private Drawable mIconUpon, mIconMask;
    private Drawable mAllApps;
    private float mIconScale;

    public Drawable getIconBack() {
        int index = randInt(0, mIconBack.length - 1);
        try {
            Drawable back = mIconBack[index];
            return back;
        } catch (ArrayIndexOutOfBoundsException e) {
            if (mIconBack.length > 0) {
                return mIconBack[0];
            } else {
                return null;
            }
        }
    }

    public Drawable getIconMask() {
        return mIconMask;
    }

    public Drawable getIconUpon() {
        return mIconUpon;
    }

    public float getIconScale() {
        return mIconScale;
    }

    public Drawable getAllAppsButton() {
        return mAllApps;
    }

    public IconPackHelper(Context context) {
        mContext = context;
        mIconPackResources = new HashMap<String, String>();
    }

    private Drawable getDrawableForName(String name) {
        if (isIconPackLoaded()) {
            String item = mIconPackResources.get(name);
            if (!TextUtils.isEmpty(item)) {
                int id = getResourceIdForDrawable(item);
                if (id != 0) {
                    return mLoadedIconPackResource.getDrawable(id);
                }
            }
        }
        return null;
    }

    public static Map<String, IconPackInfo> getSupportedPackages(Context context) {
        Intent i = new Intent();
        Map<String, IconPackInfo> packages = new HashMap<String, IconPackInfo>();
        PackageManager packageManager = context.getPackageManager();
        for (String action : sSupportedActions) {
            i.setAction(action);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sSupportedCategories) {
            i.addCategory(category);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
            i.removeCategory(category);
        }
        return packages;
    }

    private static void loadResourcesFromXmlParser(XmlPullParser parser,
                                                   Map<String, String> iconPackResources) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        iconPackResources.put("number_backs", "0");
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName().equalsIgnoreCase(ICON_UPON_TAG)) {
                String icon = parser.getAttributeValue(null, "img");
                if (icon == null) {
                    if (parser.getAttributeCount() == 1) {
                        icon = parser.getAttributeValue(0);
                    }
                }
                iconPackResources.put(parser.getName().toLowerCase(), icon);
                continue;
            }

            if (parser.getName().equalsIgnoreCase(ICON_MASK_TAG)) {
                String icon = parser.getAttributeValue(null, "img");
                if (icon == null) {
                    if (parser.getAttributeCount() == 1) {
                        icon = parser.getAttributeValue(0);
                    }
                }
                iconPackResources.put(parser.getName().toLowerCase(), icon);
                continue;
            }

            if (parser.getName().equalsIgnoreCase(ICON_BACK_TAG)) {
                int number = parser.getAttributeCount();
                iconPackResources.put("number_backs", "" + number);

                for (int i = 1; i < number; i++) {
                    String icon = parser.getAttributeValue(null, "img" + i);
                    if (icon == null) {
                        if (parser.getAttributeCount() == 1) {
                            icon = parser.getAttributeValue(0);
                        }
                    }

                    Log.v("launcher_icons", "" + icon);
                    iconPackResources.put(parser.getName().toLowerCase() + "_" + i, icon);
                }
                continue;
            }

            if (parser.getName().equalsIgnoreCase(ICON_SCALE_TAG)) {
                String factor = parser.getAttributeValue(null, "factor");
                if (factor == null) {
                    if (parser.getAttributeCount() == 1) {
                        factor = parser.getAttributeValue(0);
                    }
                }
                iconPackResources.put(parser.getName().toLowerCase(), factor);
                continue;
            }

            if (!parser.getName().equalsIgnoreCase("item")) {
                continue;
            }

            String component = parser.getAttributeValue(null, "component");
            String drawable = parser.getAttributeValue(null, "drawable");

            //Log.v("icon_pack", component);

            // Validate component/drawable exist
            if (TextUtils.isEmpty(component) || TextUtils.isEmpty(drawable)) {
                continue;
            }

            // Validate format/length of component
            if (!component.startsWith("ComponentInfo{") || !component.endsWith("}")
                    || component.length() < 16) {
                continue;
            }

            // Sanitize stored value
            component = component.substring(14, component.length() - 1).toLowerCase();

            ComponentName name = null;
            if (!component.contains("/")) {
                // Package icon reference
                iconPackResources.put(component, drawable);
            } else {
                name = ComponentName.unflattenFromString(component);
                if (name != null) {
                    iconPackResources.put(name.getPackageName(), drawable);
                    iconPackResources.put(name.getPackageName() + "." + name.getClassName(), drawable);
                }
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        try {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            return randomNum;
        } catch (Exception e) {
            return min;
        }

    }

    private static void loadApplicationResources(Context context,
                                                 Map<String, String> iconPackResources, String packageName) {
        Field[] drawableItems = null;
        try {
            Context appContext = context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            drawableItems = Class.forName(packageName+".R$drawable",
                    true, appContext.getClassLoader()).getFields();
        } catch (Exception e){
            return;
        }

        for (Field f : drawableItems) {
            String name = f.getName();

            String icon = name.toLowerCase();
            name = name.replaceAll("_", ".");

            iconPackResources.put(name, icon);

            int activityIndex = name.lastIndexOf(".");
            if (activityIndex <= 0 || activityIndex == name.length() - 1) {
                continue;
            }

            String iconPackage = name.substring(0, activityIndex);
            if (TextUtils.isEmpty(iconPackage)) {
                continue;
            }
            iconPackResources.put(iconPackage, icon);

            String iconActivity = name.substring(activityIndex + 1);
            if (TextUtils.isEmpty(iconActivity)) {
                continue;
            }
            iconPackResources.put(iconPackage + "." + iconActivity, icon);
        }
    }

    public boolean loadIconPack(String packageName) {
        mIconPackResources = getIconPackResources(mContext, packageName);
        Resources res = null;
        try {
            res = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        mLoadedIconPackResource = res;
        mLoadedIconPackName = packageName;

        try {
            int appButton = mLoadedIconPackResource.getIdentifier("all_apps_button", "drawable", AppSettings.getInstance(mContext).iconPack);
            mAllApps = mLoadedIconPackResource.getDrawable(appButton);
        } catch (Exception e) {
            mAllApps = mContext.getResources().getDrawable(getAllAppsStyledButton(mContext));
        }

        int numberBacks = Integer.parseInt(mIconPackResources.get("number_backs"));
        if (numberBacks > 1) {
            numberBacks--;
        }
        Log.v("launcher_icons", "number backs = " + numberBacks);
        mIconBack = new Drawable[numberBacks];
        for (int i = 0; i < numberBacks; i++) {
            mIconBack[i] = getDrawableForName(ICON_BACK_TAG + "_" + (i+1));
        }

        mIconMask = getDrawableForName(ICON_MASK_TAG);
        mIconUpon = getDrawableForName(ICON_UPON_TAG);
        String scale = mIconPackResources.get(ICON_SCALE_TAG);
        if (scale != null) {
            try {
                mIconScale = Float.valueOf(scale);
            } catch (NumberFormatException e) {

            }
        }
        return true;
    }

    public static Map<String, String> getIconPackResources(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        Resources res = null;
        try {
            res = context.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        XmlPullParser parser = null;
        InputStream inputStream = null;
        Map<String, String> iconPackResources = new HashMap<String, String>();

        try {
            inputStream = res.getAssets().open("appfilter.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");
        } catch (Exception e) {
            // Catch any exception since we want to fall back to parsing the xml/
            // resource in all cases
            int resId = res.getIdentifier("appfilter", "xml", packageName);
            if (resId != 0) {
                parser = res.getXml(resId);
            }
        }

        if (parser != null) {
            try {
                loadResourcesFromXmlParser(parser, iconPackResources);
                return iconPackResources;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Cleanup resources
                if (parser instanceof XmlResourceParser) {
                    ((XmlResourceParser) parser).close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // Application uses a different theme format (most likely launcher pro)
        int arrayId = res.getIdentifier("theme_iconpack", "array", packageName);
        if (arrayId == 0) {
            arrayId = res.getIdentifier("icon_pack", "array", packageName);
        }

        if (arrayId != 0) {
            String[] iconPack = res.getStringArray(arrayId);
            for (String entry : iconPack) {

                if (TextUtils.isEmpty(entry)) {
                    continue;
                }

                String icon = entry.toLowerCase();
                entry = entry.replaceAll("_", ".");

                iconPackResources.put(entry, icon);

                int activityIndex = entry.lastIndexOf(".");
                if (activityIndex <= 0 || activityIndex == entry.length() - 1) {
                    continue;
                }

                String iconPackage = entry.substring(0, activityIndex);
                if (TextUtils.isEmpty(iconPackage)) {
                    continue;
                }
                iconPackResources.put(iconPackage, icon);

                String iconActivity = entry.substring(activityIndex + 1);
                if (TextUtils.isEmpty(iconActivity)) {
                    continue;
                }
                iconPackResources.put(iconPackage + "." + iconActivity, icon);
            }
        } else {
            loadApplicationResources(context, iconPackResources, packageName);
        }
        return iconPackResources;
    }

    public void unloadIconPack() {
        mLoadedIconPackResource = null;
        mLoadedIconPackName = null;
        mIconPackResources = null;
        mIconMask = null;
        mIconBack = null;
        mIconUpon = null;
        mIconScale = 1f;
    }

    public boolean isIconPackLoaded() {
        return mLoadedIconPackResource != null &&
                mLoadedIconPackName != null &&
                mIconPackResources != null;
    }

    private int getResourceIdForDrawable(String resource) {
        int resId = mLoadedIconPackResource.getIdentifier(resource, "drawable", mLoadedIconPackName);
        return resId;
    }

    public Resources getIconPackResources() {
        return mLoadedIconPackResource;
    }

    private static SharedPreferences sharedPrefs = null;
    private static List<String> customIcons = null;

    public int getResourceIdForActivityIcon(ActivityInfo info) {

        String string = info.packageName.toLowerCase() + "." + info.name.toLowerCase();

        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if (customIcons == null) {
            customIcons = Arrays.asList(sharedPrefs.getString("launcher_custom_icon_names", "").split(":|:"));
        }

        if (customIcons.contains(string)) {
            return getResourceIdForDrawable(sharedPrefs.getString(string, ""));
        } else {
            String drawable = mIconPackResources.get(string);

            if (drawable == null) {
                // Icon pack doesn't have an icon for the activity, fallback to package icon
                drawable = mIconPackResources.get(info.packageName.toLowerCase());
                if (drawable == null) {
                    return 0;
                }
            }

            return getResourceIdForDrawable(drawable);
        }
    }

    static class IconPackInfo {
        String packageName;
        CharSequence label;
        Drawable icon;

        IconPackInfo(ResolveInfo r, PackageManager packageManager) {
            packageName = r.activityInfo.packageName;
            icon = r.loadIcon(packageManager);
            label = r.loadLabel(packageManager);
            Log.v("klinker_launcher", "label: " + label);
        }

        IconPackInfo(){
        }

        public IconPackInfo(String label, Drawable icon, String packageName) {
            this.label = label;
            this.icon = icon;
            this.packageName = packageName;
        }
    }

    private static class IconPack {
        public CharSequence packageName;
        public String text;
        public int icon;
        public Drawable actualIcon;

        public IconPack(String text, Drawable icon, CharSequence packageName) {
            this.text = text;
            this.actualIcon = icon;
            this.packageName = packageName;
        }
    }

    public static void pickIconPack(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final Map<String, IconPackInfo> supportedPackages = getSupportedPackages(context);

        AppSettings settings = AppSettings.getInstance(context);

        final CharSequence[] dialogEntries = new CharSequence[supportedPackages.size() + 1];
        supportedPackages.keySet().toArray(dialogEntries);

        final String defaultIcons = context.getResources().getString(R.string.default_iconpack_title);
        dialogEntries[dialogEntries.length - 1] = defaultIcons;
        Arrays.sort(dialogEntries);

        final IconPack[] iconPacks = new IconPack[dialogEntries.length];

        for (int i = 0; i < iconPacks.length; i++) {
            try {
                CharSequence entry = dialogEntries[i];

                if (entry.equals(defaultIcons)) {
                    iconPacks[i] = new IconPack(context.getResources().getString(R.string.default_iconpack_title),
                            context.getResources().getDrawable(R.mipmap.ic_launcher_home), defaultIcons);
                } else {
                    CharSequence pack = supportedPackages.get(entry).packageName;
                    iconPacks[i] = new IconPack(supportedPackages.get(entry).label.toString(),
                            packageManager.getApplicationIcon(packageManager.getApplicationInfo(pack.toString(), 0)), pack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Arrays.sort(iconPacks, new Comparator<IconPack>() {
            @Override
            public int compare(IconPack lhs, IconPack rhs) {
                if (lhs.text.compareTo(rhs.text) < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        ListAdapter adapter = new ArrayAdapter<IconPack>(
                context,
                R.layout.pack_picker_item,
                R.id.label,
                iconPacks) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ImageView iv = (ImageView) v.findViewById(R.id.icon);
                TextView tv = (TextView) v.findViewById(R.id.label);
                iv.setImageDrawable(iconPacks[position].actualIcon);
                tv.setText(iconPacks[position].text);
                return v;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_pick_iconpack_title);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CharSequence selectedPackage = iconPacks[which].packageName;
                if (selectedPackage.equals(defaultIcons)) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putString("icon_pack", "")
                            .commit();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putString("icon_pack", supportedPackages.get(selectedPackage).packageName)
                            .commit();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static int getAllAppsStyledButton(Context context) {
        int allAppsDrawable = R.drawable.ic_allapps;
        return allAppsDrawable;
    }
}