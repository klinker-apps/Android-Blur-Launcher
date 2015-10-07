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

package com.klinker.android.launcher.addons.pages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.SettingsActivity;
import com.klinker.android.launcher.addons.utils.AnimationUtils;
import com.klinker.android.launcher.api.BaseLauncherPage;

public class HolderPage extends BaseLauncherPage {

    private int position;
    private View background;

    @Override
    public BaseLauncherPage getFragment(int position) {
        HolderPage fragment = new HolderPage();
        Bundle args = new Bundle(1);
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(BaseLauncherPage.POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.holder_fragment, container, false);

        background = rootView.findViewById(R.id.holder_background);
        TextView fragmentLabel = (TextView) rootView.findViewById(R.id.page_number);
        TextView settingsButton = (TextView) rootView.findViewById(R.id.settings_button);
        TextView restartButton = (TextView) rootView.findViewById(R.id.restart_launcher_button);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());

        int count = 0;

        for (int i = 0; i < 5; i++) {
            String path =  sharedPrefs.getString("launcher_class_path_" + i, "");

            if (!path.isEmpty()) {
                count++;
            }
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

        if (count == 0) {
            // there are no fragments. Tell them that and give them a settings button
            fragmentLabel.setText(getString(R.string.no_frags_disclaimer));
            settingsButton.setVisibility(View.VISIBLE);
            settingsButton.setOnTouchListener(mHapticFeedbackTouchListener);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            });
            showAnimation(settingsButton);
        } else {
            // the fragment couldn't be found, probably because it was just updated
            fragmentLabel.setText(getString(R.string.reset_frags_disclaimer));
            restartButton.setVisibility(View.VISIBLE);
            restartButton.setOnTouchListener(mHapticFeedbackTouchListener);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Manually kill this process
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            showAnimation(restartButton);
        }

        return rootView;
    }

    private void showAnimation(final View v) {
        AnimationUtils.tada(v).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAnimation(v);
            }
        }, 4000);
    }

    @Override
    public View[] getBackground() {
        return new View[] {this.background};
    }

}
