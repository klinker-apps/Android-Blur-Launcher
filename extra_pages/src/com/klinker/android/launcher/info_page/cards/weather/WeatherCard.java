/*
 * Copyright 2014 Klinker Apps Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.launcher.info_page.cards.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.info_page.Utils;
import com.klinker.android.launcher.api.BaseCard;
import com.klinker.android.launcher.api.CardsLayout;

public class WeatherCard extends BaseCard {

    private static final String LOGTAG = "WeatherCard";

    public BaseCard getCard(Context context) {
        return new WeatherCard(context);
    }

    /**
     * Default constructor
     * @param context context of the fragment
     */
    public WeatherCard(Context context) {
        super(context);
    }

    /**
     * Get the weather data for the cards
     */
    @Override
    public void onRefresh() {
        // we want to mark it as not set so that it will update
        weather = null;

        // get the shared prefs to use for the woeid and units
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        YahooWeatherApiClient.setWeatherUnits(sharedPrefs.getString("klink_launcher_weather_units", "f"));
        String woeid = sharedPrefs.getString("klink_launcher_weather_woeid", "-1");

        if (woeid.equals("-1")) {
            // If they have location set to automatic
            getWeatherFromPlayServices();
        } else {
            // if they have manually set the location
            try {
                weather = YahooWeatherApiClient.getWeatherForWoeid(woeid, "");
            } catch (CantGetWeatherException e) {
                e.printStackTrace();
                weather = null;
            }

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshCardLayout();
                }
            });
        }
    }

    // current conditions
    private TextView location;
    private TextView temperature;
    private TextView todayHigh;
    private TextView todayLow;
    private ImageView weatherDepiction;

    // future conditions
    private TextView[] futureHigh;
    private TextView[] futureLow;
    private TextView[] futureDay;
    private ImageView[] futurePic;

    // references for the settings
    public LinearLayout weatherLayout;
    public LinearLayout settingsLayout;
    public TextView question;
    public TextView ans1;
    public TextView ans2;

    // helper with the resources since they can't be used normally
    private ResourceHelper helper;

    // shared prefs to write the settings into
    public SharedPreferences sharedPrefs;

    /**
     * Creates a view for the card, inflated from the card_weather xml file
     */
    @Override
    public void setUpCardLayout() {
        // remember that we need the resource helper since this will be running from a different package
        helper = new ResourceHelper(getContext(), Utils.PACKAGE_NAME);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // get the root view layout
        View root = helper.getLayout("card_weather");

        // views for the settings on the card
        weatherLayout = (LinearLayout) root.findViewById(helper.getId("weather_layout"));
        settingsLayout = (LinearLayout) root.findViewById(helper.getId("settings_layout"));
        question = (TextView) root.findViewById(helper.getId("question"));
        ans1 = (TextView) root.findViewById(helper.getId("answer_1"));
        ans2 = (TextView) root.findViewById(helper.getId("answer_2"));

        // get the text views for the weather data
        location = (TextView) root.findViewById(helper.getId("location"));
        temperature = (TextView) root.findViewById(helper.getId("temp"));
        todayHigh = (TextView) root.findViewById(helper.getId("today_high"));
        todayLow = (TextView) root.findViewById(helper.getId("today_low"));
        weatherDepiction = (ImageView) root.findViewById(helper.getId("image"));

        futureHigh = new TextView[4];
        futureLow = new TextView[4];
        futureDay = new TextView[4];
        futurePic = new ImageView[4];

        // get the views for the future forecast
        for (int i = 0; i < 4; i++) {
            futureHigh[i] = (TextView) root.findViewById(helper.getId("forecast_" + (i+1) + "_high"));
            futureLow[i] = (TextView) root.findViewById(helper.getId("forecast_" + (i+1) + "_low"));
            futurePic[i] = (ImageView) root.findViewById(helper.getId("forecast_" + (i+1) + "_pic"));
            futureDay[i] = (TextView) root.findViewById(helper.getId("day_" + (i+1)));
        }

        // add this view to the linear layout to display
        addView(root);

        showSettings(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        }).start();
    }

    /**
     * Called when the user presses the 3 dot settings menu in the corner of a card
     */
    @Override
    protected void settingsClicked() {

        // if the settings aren't already showing
        if (weatherLayout.getVisibility() == View.VISIBLE) {
            // we will display them, animation is done with animateLayoutChanges attribute in XML
            weatherLayout.setVisibility(View.INVISIBLE);
            settingsLayout.setVisibility(View.VISIBLE);

            // first we will ask if they want fahrenheit or celsius
            askFahOrCel();
        } else {
            returnToWeather();
        }
    }

    /**
     * Sets the text views correctly then adds
     * on click listeners to continue to next event
     */
    private void askFahOrCel() {
        question.setText(helper.getString("temp_units") + ":");
        ans1.setText("°F");
        ans2.setText("°C");

        // will commit the units, then set up the next question
        ans1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefs.edit().putString("klink_launcher_weather_units", "f").commit();

                setAutoOrManualQuestion();
            }
        });

        ans2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefs.edit().putString("klink_launcher_weather_units", "c").commit();

                setAutoOrManualQuestion();
            }
        });
    }

    /**
     * fades the view out, switches the text, then fades it back in
     */
    private void setAutoOrManualQuestion() {
        // fade them to invisible
        question.setVisibility(View.INVISIBLE);
        ans1.setVisibility(View.INVISIBLE);
        ans2.setVisibility(View.INVISIBLE);

        // set the next question
        askAutoOrManualLocation();

        // fade them back to visible
        question.setVisibility(View.VISIBLE);
        ans1.setVisibility(View.VISIBLE);
        ans2.setVisibility(View.VISIBLE);
    }

    /**
     * sets up the question then either commits the auto location
     * or brings up the dialog to find the location
     */
    private void askAutoOrManualLocation() {
        question.setText(helper.getString("choose_location") + ":");
        ans1.setText(helper.getString("auto"));
        ans2.setText(helper.getString("find"));

        ans1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefs.edit().putString("klink_launcher_weather_woeid", "-1").commit();
                sharedPrefs.edit().putString("klink_launcher_weather_display", "").commit();

                returnToWeather();
            }
        });

        ans2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // we just open up the dialog fragment on this one to get location
                LocationChooserDialogFragment fragment = LocationChooserDialogFragment.newInstance();

                // open the dialog fragment
                Activity activity = (Activity) getContext();
                activity.getFragmentManager().beginTransaction()
                        .add(fragment, "location_dialog")
                        .commit();


                returnToWeather();
            }
        });
    }

    private void returnToWeather() {
        settingsLayout.setVisibility(View.INVISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    // holds the weather data for the location
    private WeatherData weather;

    /**
     * Called to set the data to the layout
     */
    @Override
    public void refreshCardLayout() {
        // now we want to actually write the values to the text views
        if (weather != null) {
            location.setText(weather.location);
            temperature.setText(weather.temperature + "°");
            todayHigh.setText(weather.future[0].high  + "°");
            todayLow.setText(weather.future[0].low  + "°");
            weatherDepiction.setImageDrawable(WeatherData.getConditionIconId(weather.conditionCode, helper));


            try {
                for (int i = 0; i < 4; i++) {
                    futureLow[i].setText(weather.future[i+1].low + "°");
                    futureHigh[i].setText(weather.future[i+1].high + "°");
                    futureDay[i].setText(weather.future[i+1].day);
                    futurePic[i].setImageDrawable(WeatherData.getConditionIconId(weather.future[i+1].conditionCode, helper));
                }
            } catch (NullPointerException e) {

            }

        } else {
            location.setText(helper.getString("location_not_found") + ".");
        }
    }

    /**
     * Callback for when the view was pressed
     * @param view view that was pressed
     */
    @Override
    public void onCardPressed(View view) {

        // if they are in settings, dont do anything here
        if (weatherLayout.getVisibility() == View.VISIBLE && weather != null) {
            if (weather.webLink != null) {
                // I will try it this way, a lot of the time, the web link seems to be null for
                // some reason and this doesn't work
                getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse(weather.webLink))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
            } else {
                // we will just redirect to google's if yahoo doesn't work
                // since they get the location and manage it for us
                getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.google.com/#q=weather"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
            }
        } else {
            returnToWeather();
        }
    }

    private LocationClient mPlayServicesLocationClient;

    /**
     * Called when the user has their location set to automatic.
     * This will connect to google play services to get the last location
     * from their device.
     */
    private void getWeatherFromPlayServices() {
        mPlayServicesLocationClient = new LocationClient(getContext(), new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // client has connected, so lets get the location
                final Location lastLocation = mPlayServicesLocationClient.getLastLocation();

                // disconnect the client to end networking
                mPlayServicesLocationClient.disconnect();
                mPlayServicesLocationClient = null;

                // set the weather data from that location
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setWeaterDataFromGeo(lastLocation);
                    }
                }).start();
            }

            @Override
            public void onDisconnected() {
                // client has disconnected
                mPlayServicesLocationClient = null;
            }

        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                // something went wrong, may expand here later
                mPlayServicesLocationClient = null;
            }
        });

        // get the connection
        mPlayServicesLocationClient.connect();
    }

    /**
     * Uses the users last geolocation to get the weather data
     * @param location last geolocation of the user
     */
    private void setWeaterDataFromGeo(final Location location) {
        // try to get the weather data. It is networked, so things could go wrong
        try {
            weather = YahooWeatherApiClient.getWeatherForLocationInfo(YahooWeatherApiClient.getLocationInfo(location));
        } catch (CantGetWeatherException e) {
            weather = null;
        }

        // once it is done, return to the UI thread and use that data to refresh the layout
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshCardLayout();
            }
        });
    }
}
