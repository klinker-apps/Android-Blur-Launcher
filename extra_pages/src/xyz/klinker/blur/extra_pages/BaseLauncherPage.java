package xyz.klinker.blur.extra_pages;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

public abstract class BaseLauncherPage extends Fragment {

    public static final String POSITION = "position";
    public static final String DRAWER_OPENED = "com.klinker.android.launcher.FRAGMENTS_OPENED";
    public static final String DRAWER_CLOSED = "com.klinker.android.launcher.FRAGMENTS_CLOSED";

    public abstract BaseLauncherPage getFragment(int position);
    public abstract View[] getBackground();

    private BroadcastReceiver drawerOpened = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onFragmentsOpened();
        }
    };

    private BroadcastReceiver drawerClosed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onFragmentsClosed();
        }
    };

    /**
     * This is something that can be overridden on the fragments side,
     * we will do nothing with it here
     *
     * No call to super.onFragmentsOpened() is necessary.
     */
    public void onFragmentsOpened() {

    }

    /**
     * This is something that can be overridden on the fragments side,
     * we will do nothing with it here.
     *
     * No call to super.onFragmentsClosed() is necessary.
     */
    public void onFragmentsClosed() {

    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();

        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(DRAWER_OPENED);
            getActivity().registerReceiver(drawerOpened, filter);

            filter = new IntentFilter();
            filter.addAction(DRAWER_CLOSED);
            getActivity().registerReceiver(drawerClosed, filter);
        }
    }

    @Override
    public void onPause() {
        Context context = getActivity();

        if (context != null) {
            getActivity().unregisterReceiver(drawerOpened);
            getActivity().unregisterReceiver(drawerClosed);
        }

        super.onPause();
    }
}