package xyz.klinker.blur.extra_pages;

import android.app.Fragment;
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
    private View background;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(getLayoutRes(), container, false);
        background = root.findViewById(LauncherPageLayout.BACKGROUND_ID);

        initLayout(root);
        return root;
    }

    /**
     * Creates a View array which will be faded in and out as the page
     * is opened and closed from the main launcher.
     *
     * Override this method if you want to do something other than R.id.background.
     *
     * @return an array of all the views to be faded in and out
     */
    public View[] getAlphaChangingViews() {
        // if background is null, that is ok, we handle null views in this array, as well
        // as a null array.
        return new View[] {
                background
        };
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