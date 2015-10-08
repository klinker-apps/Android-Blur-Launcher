package com.klinker.android.launcher.info_page.cards.weather;

/**
 * Created by luke on 5/7/14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.info_page.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment that pops up when touching the preference.
 */
public class LocationChooserDialogFragment extends DialogFragment implements
        TextWatcher,
        LoaderManager.LoaderCallbacks<List<YahooWeatherApiClient.LocationSearchResult>> {
    /**
     * Time between search queries while typing.
     */
    private static final int QUERY_DELAY_MILLIS = 500;

    private SearchResultsListAdapter mSearchResultsAdapter;
    private ListView mSearchResultsList;

    public LocationChooserDialogFragment() {
    }

    public static LocationChooserDialogFragment newInstance() {
        return new LocationChooserDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        tryBindList();
    }

    ResourceHelper helper;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        helper = new ResourceHelper(getActivity(), Utils.PACKAGE_NAME);

        // Force Holo Light since ?android:actionBarXX would use dark action bar
        final Context layoutContext = new ContextThemeWrapper(getActivity(),
                helper.getIdentifier("Theme_LocationDialog", "style"));

        View rootView = helper.getLayout("dialog_weather_location_chooser");
        TextView searchView = (TextView) rootView.findViewById(helper.getId("location_query"));
        searchView.addTextChangedListener(this);

        /*Button units = (Button) rootView.findViewById(helper.getId("units"));
        units.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] fItems = new String[] {helper.getString("degree_f"), helper.getString("degree_c")};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(fItems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

                        if (item == 0) {
                            e.putString("klink_launcher_weather_units", "f");
                        } else {
                            e.putString("klink_launcher_weather_units", "c");
                        }

                        e.commit();

                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });*/

        ImageView poweredByYahoo = (ImageView) rootView.findViewById(helper.getId("powered_by_yahoo"));
        poweredByYahoo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutContext.startActivity(
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.yahoo.com/?ilc=401"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
            }
        });

        // set up the result list
        mSearchResultsList = (ListView) rootView.findViewById(helper.getId("location_list"));
        mSearchResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long itemId) {
                String value = mSearchResultsAdapter.getPrefValueAt(position);

                // Put them into the launcher's shared prefs
                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                String woeid = getWoeidFromValue(value);
                if (woeid == null) {
                    // automatic weather
                    e.putString("klink_launcher_weather_woeid", "-1");
                    e.putString("klink_launcher_weather_display", getDisplayValue(value));
                } else {
                    // they chose a location
                    e.putString("klink_launcher_weather_woeid", getWoeidFromValue(value));
                    e.putString("klink_launcher_weather_display", getDisplayValue(value));
                }
                e.commit();

                dismiss();
            }
        });

        tryBindList();

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    public String getDisplayValue(String value) {
        if (TextUtils.isEmpty(value) || value.indexOf(',') < 0) {
            try {
                return helper.getString("location_automatic");
            } catch (Exception e) {
                return "Automatic";
            }
        }

        String[] woeidAndDisplayName = value.split(",", 2);
        return woeidAndDisplayName[1];
    }

    public String getWoeidFromValue(String value) {
        if (TextUtils.isEmpty(value) || value.indexOf(',') < 0) {
            // automatic
            return null;
        }

        String[] woeidAndDisplayName = value.split(",", 2);
        return woeidAndDisplayName[0];
    }

    private void tryBindList() {
        if (isAdded() && mSearchResultsAdapter == null) {
            mSearchResultsAdapter = new SearchResultsListAdapter();
        }

        if (mSearchResultsAdapter != null && mSearchResultsList != null) {
            mSearchResultsList.setAdapter(mSearchResultsAdapter);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        mQuery = charSequence.toString();
        if (mRestartLoaderHandler.hasMessages(0)) {
            return;
        }

        mRestartLoaderHandler.sendMessageDelayed(
                mRestartLoaderHandler.obtainMessage(0),
                QUERY_DELAY_MILLIS);
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private String mQuery;

    private Handler mRestartLoaderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle args = new Bundle();
            args.putString("query", mQuery);
            getLoaderManager().restartLoader(0, args, LocationChooserDialogFragment.this);
        }
    };

    @Override
    public Loader<List<YahooWeatherApiClient.LocationSearchResult>> onCreateLoader(int id, Bundle args) {
        final String query = args.getString("query");
        return new ResultsLoader(query, getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<YahooWeatherApiClient.LocationSearchResult>> loader,
                               List<YahooWeatherApiClient.LocationSearchResult> results) {
        mSearchResultsAdapter.changeArray(results);
    }

    @Override
    public void onLoaderReset(Loader<List<YahooWeatherApiClient.LocationSearchResult>> loader) {
        mSearchResultsAdapter.changeArray(null);
    }

    private class SearchResultsListAdapter extends BaseAdapter {
        private List<YahooWeatherApiClient.LocationSearchResult> mResults;

        private SearchResultsListAdapter() {
            mResults = new ArrayList<YahooWeatherApiClient.LocationSearchResult>();
        }

        public void changeArray(List<YahooWeatherApiClient.LocationSearchResult> results) {
            if (results == null) {
                results = new ArrayList<YahooWeatherApiClient.LocationSearchResult>();
            }

            mResults = results;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // we will always put one item in there, just for a little bit of spacing
            return Math.max(1,mResults.size());
        }

        @Override
        public Object getItem(int position) {
            if (position == 0 && mResults.size() == 0) {
                return null;
            }

            return mResults.get(position);
        }

        public String getPrefValueAt(int position) {
            if (position == 0 && mResults.size() == 0) {
                return "";
            }

            YahooWeatherApiClient.LocationSearchResult result = mResults.get(position);
            return result.woeid + "," + result.displayName;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0 && mResults.size() == 0) {
                return -1;
            }

            return mResults.get(position).woeid.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = helper.getLayout("list_item_weather_location_result");
            }

            if (mResults.size() > 0) {
                YahooWeatherApiClient.LocationSearchResult result = mResults.get(position);
                ((TextView) convertView.findViewById(helper.getId("text_view_1")))
                        .setText(result.displayName);
                ((TextView) convertView.findViewById(helper.getId("text_view_2")))
                        .setText(result.country);
            }

            return convertView;
        }
    }

    /**
     * Loader that fetches location search results from {@link YahooWeatherApiClient}.
     */
    private static class ResultsLoader extends AsyncTaskLoader<List<YahooWeatherApiClient.LocationSearchResult>> {
        private String mQuery;
        private List<YahooWeatherApiClient.LocationSearchResult> mResults;

        public ResultsLoader(String query, Context context) {
            super(context);
            mQuery = query;
        }

        @Override
        public List<YahooWeatherApiClient.LocationSearchResult> loadInBackground() {
            return YahooWeatherApiClient.findLocationsAutocomplete(mQuery);
        }

        @Override
        public void deliverResult(List<YahooWeatherApiClient.LocationSearchResult> apps) {
            mResults = apps;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }
        }

        @Override
        protected void onStartLoading() {
            if (mResults != null) {
                deliverResult(mResults);
            }

            if (takeContentChanged() || mResults == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();
            onStopLoading();
        }
    }
}