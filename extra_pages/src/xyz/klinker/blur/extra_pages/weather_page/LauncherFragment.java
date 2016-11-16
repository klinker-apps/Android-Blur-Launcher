package xyz.klinker.blur.extra_pages.weather_page;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.klinker.blur.extra_pages.BaseLauncherPage;
import xyz.klinker.blur.extra_pages.R;
import xyz.klinker.blur.extra_pages.calc_page.Utils;

public class LauncherFragment extends BaseLauncherPage {

    @Override
    public BaseLauncherPage getNewInstance() {
        return new LauncherFragment();
    }

    private SwipeRefreshLayout swipeRefreshLayout;

    private WeatherItemLayout dayOne;
    private WeatherItemLayout dayTwo;
    private WeatherItemLayout dayThree;
    private WeatherItemLayout dayFour;


    @Override
    public int getLayoutRes() {
        return R.layout.weather_page;
    }

    @Override
    public void initLayout(View inflated) {
        swipeRefreshLayout = (SwipeRefreshLayout) inflated.findViewById(R.id.swipe_refresh_layout);

        dayOne = (WeatherItemLayout) inflated.findViewById(R.id.day_one);
        dayTwo = (WeatherItemLayout) inflated.findViewById(R.id.day_two);
        dayThree = (WeatherItemLayout) inflated.findViewById(R.id.day_three);
        dayFour = (WeatherItemLayout) inflated.findViewById(R.id.day_four);

        /*swipeRefreshLayout.setProgressViewOffset(false, Utils.toDP(getActivity(), 52) * -1, Utils.toDP(getActivity(), 32));
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });*/
    }
}
