package com.klinker.android.launcher.addons.settings.page_picker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.AppSettings;

public class ExtraPageFragment extends Fragment {

    protected Context context;
    protected SharedPreferences sharedPrefs;

    private Button none;
    private Button info;
    private Button verticalDrawer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        context = activity;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.page_picker_extra, null);

        none = (Button) rootView.findViewById(R.id.no_extra_page);
        info = (Button) rootView.findViewById(R.id.blur_info);
        verticalDrawer = (Button) rootView.findViewById(R.id.vertical_drawer);

        setUpButtons();

        return rootView;
    }

    public void setUpButtons() {

        final Drawable selectedDrawable = getResources().getDrawable(R.drawable.rect_border_pressed);
        final Drawable normalDrawable = getResources().getDrawable(R.drawable.rect_border_filled);

        none.setOnTouchListener(mHapticFeedbackTouchListener);
        info.setOnTouchListener(mHapticFeedbackTouchListener);
        verticalDrawer.setOnTouchListener(mHapticFeedbackTouchListener);

        switch (AppSettings.getInstance(context).extraPage) {
            case AppSettings.NO_EXTRA_PAGE:
                none.setBackgroundDrawable(selectedDrawable);
                info.setBackgroundDrawable(normalDrawable);
                verticalDrawer.setBackgroundDrawable(normalDrawable);
                break;
            case AppSettings.BLUR_INFO:
                none.setBackgroundDrawable(normalDrawable);
                info.setBackgroundDrawable(selectedDrawable);
                verticalDrawer.setBackgroundDrawable(normalDrawable);
                break;
            case AppSettings.VERTICAL_DRAWER:
                none.setBackgroundDrawable(normalDrawable);
                info.setBackgroundDrawable(normalDrawable);
                verticalDrawer.setBackgroundDrawable(selectedDrawable);
                break;
        }

        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("blur_extra", "none clicked");

                none.setBackgroundDrawable(selectedDrawable);
                info.setBackgroundDrawable(normalDrawable);
                verticalDrawer.setBackgroundDrawable(normalDrawable);

                sharedPrefs.edit().putInt("extra_page", 0).commit();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("blur_extra", "info clicked");

                none.setBackgroundDrawable(normalDrawable);
                info.setBackgroundDrawable(selectedDrawable);
                verticalDrawer.setBackgroundDrawable(normalDrawable);

                sharedPrefs.edit().putInt("extra_page", 1).commit();
            }
        });

        verticalDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("blur_extra", "vertical drawer clicked");

                none.setBackgroundDrawable(normalDrawable);
                info.setBackgroundDrawable(normalDrawable);
                verticalDrawer.setBackgroundDrawable(selectedDrawable);

                sharedPrefs.edit().putInt("extra_page", 2).commit();
            }
        });
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
}

