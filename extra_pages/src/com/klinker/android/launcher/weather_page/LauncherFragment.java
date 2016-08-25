package com.klinker.android.launcher.weather_page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.klinker.android.launcher.api.BaseLauncherPage;

public class LauncherFragment extends BaseLauncherPage {
    @Override
    public BaseLauncherPage getFragment(int i) {
        return new LauncherFragment();
    }

    @Override
    public View[] getBackground() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new View(getActivity());
    }
}
