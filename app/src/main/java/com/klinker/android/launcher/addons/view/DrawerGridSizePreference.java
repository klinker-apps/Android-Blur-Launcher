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

/**
 * Created by luke on 8/10/14.
 */
public class DrawerGridSizePreference extends DialogPreference {

    private NumberPicker widthPicker;
    private NumberPicker heightPicker;

    public DrawerGridSizePreference(Context context, AttributeSet attrs) {
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
        widthPicker.setMaxValue(9);
        widthPicker.setMinValue(3);
        widthPicker.setValue(Integer.parseInt(
                        sharedPrefs.getString("col_count_all_apps",
                                getContext().getResources().getInteger(R.integer.default_col_count) + ""))
        );
        widthPicker.setWrapSelectorWheel(false);

        heightPicker.setMaxValue(9);
        heightPicker.setMinValue(3);
        heightPicker.setValue(Integer.parseInt(
                        sharedPrefs.getString("row_count_all_apps",
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
                    .putString("col_count_all_apps", widthPicker.getValue() + "")
                    .putString("row_count_all_apps", heightPicker.getValue() + "")
                    .commit();
        }
    }
}
