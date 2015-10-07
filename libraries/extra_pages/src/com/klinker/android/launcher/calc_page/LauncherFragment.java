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

package com.klinker.android.launcher.calc_page;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.klinker.android.launcher.api.BaseLauncherPage;
import com.klinker.android.launcher.api.ResourceHelper;
import org.javia.arity.SyntaxException;

/**
 * Fragment to display all of the info cards that are created and implemented by library
 */
public class LauncherFragment extends BaseLauncherPage {

    public Context context;
    private ResourceHelper resHelper;

    private String syntaxError;

    // root view of the fragment
    private RelativeLayout rootView;
    private LinearLayout content;
    protected TextView equation;

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
        return new View[] {rootView.findViewById(resHelper.getId("background"))};
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

        syntaxError = resHelper.getString("syntax_error");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate our view to be displayed with the helper
        rootView = (RelativeLayout) resHelper.getLayout("calc_page_layout");
        content = (LinearLayout) rootView.findViewById(resHelper.getId("content"));
        equation = (TextView) rootView.findViewById(resHelper.getId("calculations"));

        // Add padding at the bottom of the list if the navigation bar is showing and translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Utils.hasNavBar(context)) {
            LinearLayout navBarSpace = new LinearLayout(context);
            navBarSpace.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    Utils.getNavBarHeight(context)
            ));

            content.addView(navBarSpace);
        }

        setUpLayout(content);

        return rootView;
    }

    private boolean shouldClear = false;

    /**
     * Sets up the buttons to click and display text in the calculations box
     */
    public void setUpLayout(View content) {

        String[] names = new String[] {
                "button1", "button2", "button3",
                "button4", "button5", "button6",
                "button7", "button8", "button9",
                "button0", "buttonDecimal", "buttonAdd",
                "buttonSubtract", "buttonMultiply", "buttonDivide"
        };

        // Equals button
        Button solve = (Button) content.findViewById(resHelper.getId("buttonEquals"));
        solve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // solve logic here
                try {
                    equation.setText(Logic.evaluate(equation.getText().toString()));
                } catch (SyntaxException e) {
                    equation.setText(syntaxError);
                }

                shouldClear = true;
            }
        });

        // buttons that insert text
        for (int i = 0; i < names.length; i++) {
            Button btn = (Button) content.findViewById(resHelper.getId(names[i]));
            if (btn != null) {
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String insert = ((Button) view).getText().toString();

                        if (shouldClear && Logic.isNumber(insert)) {
                            equation.setText(insert);
                        } else {
                            equation.append(insert);
                        }

                        shouldClear = false;
                    }
                });
            }
        }

        // clear button
        Button clear = (Button) content.findViewById(resHelper.getId("clear"));
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                equation.setText("");
            }
        });

        // delete button
        ImageButton delete = (ImageButton) content.findViewById(resHelper.getId("delete"));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shouldClear) {
                    equation.setText("");
                } else {
                    String current = equation.getText().toString();
                    if (current.length() > 0) {
                        if (current.equals(syntaxError)) {
                            equation.setText("");
                        } else {
                            equation.setText(current.substring(0, current.length() - 1));
                        }
                    }
                }

                shouldClear = false;
            }
        });
    }
}
