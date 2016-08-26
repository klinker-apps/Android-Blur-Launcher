package xyz.klinker.blur.extra_pages.weather_page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.klinker.blur.extra_pages.BaseLauncherPage;
import xyz.klinker.blur.extra_pages.R;

public class LauncherFragment extends BaseLauncherPage {

    private View root;

    @Override
    public BaseLauncherPage getFragment(int i) {
        return new LauncherFragment();
    }

    @Override
    public View[] getBackground() {
        return new View[] {
                root.findViewById(R.id.background)
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.weather_page, container, false);
        return root;
    }
}
