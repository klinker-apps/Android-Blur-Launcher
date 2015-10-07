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

package com.klinker.android.launcher.addons.settings.page_picker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.SettingsActivity;
import com.klinker.android.launcher.addons.utils.Item;
import com.klinker.android.launcher.addons.utils.Utils;
import com.klinker.android.launcher.api.BaseLauncherPage;

public class ChooserFragment extends Fragment {

    public static final String POSITION = "position";

    protected Context context;
    protected SharedPreferences sharedPrefs;

    private ImageButton addNew;

    private int position = 0;

    private String appTitle;
    private String packageName;
    private String classPath;
    private Drawable icon;

    public ChooserFragment getFragment(int position) {
        ChooserFragment fragment = new ChooserFragment();
        Bundle args = new Bundle(1);
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        context = activity;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.position = getArguments().getInt(BaseLauncherPage.POSITION);

        appTitle = sharedPrefs.getString("launcher_title_" + position, "None");
        packageName = sharedPrefs.getString("launcher_package_name_" + position, "");
        classPath = sharedPrefs.getString("launcher_class_path_" + position, "");

        final PackageManager pm = context.getPackageManager();

        if (packageName.equals("com.klinker.android.launcher")) {
            icon = null;
        } else {
            try {
                icon = pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                icon = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.page_picker, null);

        final TextView current = (TextView) rootView.findViewById(R.id.current_text);
        current.setText(getString(R.string.current) + ": " + getString(R.string.none));

        final ImageButton none = (ImageButton) rootView.findViewById(R.id.dont_use_button);
        none.setOnTouchListener(mHapticFeedbackTouchListener);
        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appTitle = "None";
                packageName = "";
                classPath = "";
                icon = null;

                setCurrent(current);
            }
        });

        addNew = (ImageButton) rootView.findViewById(R.id.add_button);
        addNew.setOnTouchListener(mHapticFeedbackTouchListener);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Item[] items = Utils.getPackageItems(context);

                if (items.length == 0) {
                    Toast.makeText(context, R.string.nothing_to_add, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (Item item : items) {
                        Log.v("LauncherItems", item.classPath + "  " + item.packageName + "  " + item.text);
                    }
                }

                ListAdapter adapter = Utils.getPackagesAdapter(context, items);

                AlertDialog.Builder attachBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
                attachBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        appTitle = items[i].text;
                        classPath = items[i].classPath;
                        packageName = items[i].packageName;

                        if (packageName.equals("com.klinker.android.launcher")) {
                            icon = null;
                        } else {
                            icon = items[i].actualIcon;
                        }

                        setCurrent(current);

                        dialog.dismiss();
                    }

                });

                attachBuilder.create().show();
            }
        });

        setCurrent(current);

        return rootView;
    }

    View.OnTouchListener mHapticFeedbackTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            return false;
        }
    };

    public void setCurrent(TextView tv) {
        tv.setText(getResources().getString(R.string.current) + ": " + appTitle);
        if (icon != null) {
            addNew.setImageDrawable(icon);
        } else {
            addNew.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_add));
        }

        writeToSharedPrefs();
    }

    public void writeToSharedPrefs() {

        SharedPreferences.Editor e = sharedPrefs.edit();

        e.putString("launcher_title_" + position, appTitle);
        e.putString("launcher_package_name_" + position, packageName);
        e.putString("launcher_class_path_" + position, classPath);

        e.commit();

        SettingsActivity.prefChanged = true;
    }
}
