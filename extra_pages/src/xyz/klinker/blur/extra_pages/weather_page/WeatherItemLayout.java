package xyz.klinker.blur.extra_pages.weather_page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import xyz.klinker.blur.extra_pages.R;

public class WeatherItemLayout extends FrameLayout {
    public WeatherItemLayout(Context context) {
        this(context, null);
    }

    public WeatherItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View root = LayoutInflater.from(context).inflate(R.layout.weather_item, this, false);
        addView(root);
    }
}
