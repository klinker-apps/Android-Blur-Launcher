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

package com.klinker.android.launcher.addons.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.klinker.android.launcher.R;
import net.simonvt.numberpicker.NumberPicker;

public class GridSizePreference extends DialogPreference {

    private NumberPicker widthPicker;
    private NumberPicker heightPicker;

    public GridSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View preference = inflater.inflate(R.layout.preference_grid_size, null);
        widthPicker = (NumberPicker) preference.findViewById(R.id.width_picker);
        heightPicker = (NumberPicker) preference.findViewById(R.id.height_picker);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        widthPicker.setMaxValue(12);
        widthPicker.setMinValue(2);
        widthPicker.setValue(Integer.parseInt(
                sharedPrefs.getString("col_count",
                        getContext().getResources().getInteger(R.integer.default_col_count) + ""))
        );
        widthPicker.setWrapSelectorWheel(false);

        heightPicker.setMaxValue(12);
        heightPicker.setMinValue(3);
        heightPicker.setValue(Integer.parseInt(
                        sharedPrefs.getString("row_count",
                                getContext().getResources().getInteger(R.integer.default_row_count) + ""))
        );
        heightPicker.setWrapSelectorWheel(false);
        return preference;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.edit()
                    .putString("col_count", widthPicker.getValue() + "")
                    .putString("row_count", heightPicker.getValue() + "")
                    .commit();
        }
    }
}
