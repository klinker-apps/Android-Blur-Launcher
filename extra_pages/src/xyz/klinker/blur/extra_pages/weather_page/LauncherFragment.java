package xyz.klinker.blur.extra_pages.weather_page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.klinker.blur.extra_pages.BaseLauncherPage;
import xyz.klinker.blur.extra_pages.R;

public class LauncherFragment extends BaseLauncherPage {

    @Override
    public BaseLauncherPage getNewInstance() {
        return new LauncherFragment();
    }

    @Override
    public View[] getBackground() {
        return new View[] {
                root.findViewById(R.id.background)
        };
    }

    @Override
    public int getLayoutRes() {
        return R.layout.weather_page;
    }

    @Override
    public void initLayout(View inflated) {

    }
}
