/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.launcher.info_page.cards.weather;

import android.graphics.drawable.Drawable;
import com.klinker.android.launcher.api.ResourceHelper;

/**
 * A helper class representing weather data
 */
public class WeatherData {
    public static final int INVALID_TEMPERATURE = Integer.MIN_VALUE;
    public static final int INVALID_CONDITION = -1;

    public int temperature = INVALID_TEMPERATURE;
    public int conditionCode = INVALID_CONDITION;
    public String conditionText;
    public String forecastText;
    public String location;
    public String webLink;

    public Forecast[] future;

    public static class Forecast {
        public int high;
        public int low;
        public int conditionCode;
        public String forecastText;
        public String day;

        public Forecast() {
            high = INVALID_TEMPERATURE;
            low = INVALID_TEMPERATURE;
            conditionCode = INVALID_CONDITION;
        }
    }

    public WeatherData() {
        // 0 will be today
        // 1-4 is the forecast
        future = new Forecast[5];
        for (int i = 0; i < 5; i++) {
            future[i] = new Forecast();
        }
    }

    public static Drawable getConditionIconId(int conditionCode, ResourceHelper helper) {
        // http://developer.yahoo.com/weather/
        switch (conditionCode) {
            case 19: // dust or sand
                return helper.getDrawable("dust_or_sand");
            case 20: // foggy
                return helper.getDrawable("foggy");
            case 21: // haze
                return helper.getDrawable("haze");
            case 22: // smoky
                return helper.getDrawable("smoky");
            case 23: // blustery
                return helper.getDrawable("blustery");
            case 24: // windy
                return helper.getDrawable("windy");
            case 25: // cold
                return helper.getDrawable("cold");
            case 26: // cloudy
                return helper.getDrawable("cloudy");
            case 27: // mostly cloudy (night)
                return helper.getDrawable("mostly_cloudy_night");
            case 28: // mostly cloudy (day)
                return helper.getDrawable("mostly_cloudy_day");
            case 29: // partly cloudy (night)
                return helper.getDrawable("partly_cloudy_night");
            case 30: // partly cloudy (day)
                return helper.getDrawable("partly_cloudy_day");
            case 44: // partly cloudy
                return helper.getDrawable("partly_cloudy");
            case 31: // clear (night)
                return helper.getDrawable("clear_night");
            case 33: // fair (night)
                return helper.getDrawable("fair_night");
            case 34: // fair (day)
                return helper.getDrawable("fair_day");
            case 32: // sunny
                return helper.getDrawable("sunny");
            case 36: // hot
                return helper.getDrawable("hot");
            case 0: // tornado
                return helper.getDrawable("tornado");
            case 1: // tropical storm
            case 2: // hurricane
                return helper.getDrawable("hurricane");
            case 3: // severe thunderstorms
                return helper.getDrawable("severe_thunderstorms");
            case 4: // thunderstorms
                return helper.getDrawable("thunderstorms");
            case 5: // mixed rain and snow
                return helper.getDrawable("mixed_rain_and_snow");
            case 6: // mixed rain and sleet
                return helper.getDrawable("mixed_rain_and_sleet");
            case 7: // mixed snow and sleet
                return helper.getDrawable("mixed_snow_and_sleet");
            case 8: // freezing drizzle
                return helper.getDrawable("freezing_drizzle");
            case 9: // drizzle
                return helper.getDrawable("drizzle");
            case 10: // freezing rain
                return helper.getDrawable("freezing_rain");
            case 11: // showers
            case 12: // showers
                return helper.getDrawable("showers");
            case 17: // hail
                return helper.getDrawable("hail");
            case 18: // sleet
                return helper.getDrawable("sleet");
            case 35: // mixed rain and hail
                return helper.getDrawable("mixed_rain_and_hail");
            case 37: // isolated thunderstorms
                return helper.getDrawable("isolated_thunderstorms");
            case 38: // scattered thunderstorms
            case 39: // scattered thunderstorms
                return helper.getDrawable("scattered_thunderstorms");
            case 40: // scattered showers
                return helper.getDrawable("scattered_showers");
            case 45: // thundershowers
                return helper.getDrawable("thundershowers");
            case 47: // isolated thundershowers
                return helper.getDrawable("isolated_thundershowers");
            case 13: // snow flurries
                return helper.getDrawable("snow_flurries");
            case 14: // light snow showers
                return helper.getDrawable("light_snow_showers");
            case 15: // blowing snow
                return helper.getDrawable("blowing_snow");
            case 16: // snow
                return helper.getDrawable("snow");
            case 41: // heavy snow
            case 43: // heavy snow
                return helper.getDrawable("isolated_thundershowers");
            case 42: // scattered snow showers
                return helper.getDrawable("scattered_snow_showers");
            case 46: // snow showers
                return helper.getDrawable("snow_showers");
        }

        return null;
    }
}
