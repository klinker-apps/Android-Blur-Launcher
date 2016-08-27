package xyz.klinker.blur.extra_pages;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A Fragment that provides callbacks for when the drawer is opened and closed.
 */
public abstract class BaseLauncherPage extends Fragment {

    public static final String ARG_POSITION = "arg_position";

    /**
     * Get a new instance of the fragment for the pages adapter. Accessed via reflection.
     *
     * @param position the position in the adapter
     * @return the fragment to display.
     */
    public final BaseLauncherPage getFragment(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);

        BaseLauncherPage page = getNewInstance();
        page.setArguments(args);
        return page;
    }

    /**
     * New-up an instance of the subclass.
     */
    public abstract BaseLauncherPage getNewInstance();

    /**
     * Creates a View array which will be faded in and out as the page
     * is opened and closed from the main launcher
     *
     * @return an array of all the views to be faded in and out
     */
    public abstract View[] getBackground();

    /**
     * Get the layout to inflate.
     *
     * @return layout resource (R.layout.calculator_page)
     */
    public abstract int getLayoutRes();

    /**
     * Initialize the layout's views.
     *
     * @param inflated
     */
    public abstract void initLayout(View inflated);

    public View root;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(getLayoutRes(), container, false);
        initLayout(root);
        return root;
    }

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

    /**
     * Method to get the position of this page on the adapter, in case it is ever needed.
     *
     * @return page number in the adapter. 0 - ...
     */
    public int getPagePosition() {
        return getArguments().getInt(ARG_POSITION);
    }
}