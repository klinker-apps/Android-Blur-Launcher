package xyz.klinker.blur.extra_pages.weather_page;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import xyz.klinker.blur.extra_pages.R;

/**
 * A res class representing weather data
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

    public static Drawable getConditionIcon(Resources res, int conditionCode) {
        // http://developer.yahoo.com/weather/
        switch (conditionCode) {
            case 0: // tornado
            case 1: // tropical storm
            case 2: // hurricane
            case 19: // dust or sand
            case 23: // blustery
            case 24: // windy
                return res.getDrawable(R.drawable.windy);
            case 20: // foggy
            case 21: // haze
            case 22: // smoky
            case 26: // cloudy
                return res.getDrawable(R.drawable.cloud);
            case 25: // cold
                return res.getDrawable(R.drawable.snowflake);
            case 27: // mostly cloudy (night)
            case 29: // partly cloudy (night)
                return res.getDrawable(R.drawable.cloudy_night);
            case 28: // mostly cloudy (day)
            case 30: // partly cloudy (day)
            case 44: // partly cloudy
                return res.getDrawable(R.drawable.cloudy_day);
            case 31: // clear (night)
            case 33: // fair (night)
                return res.getDrawable(R.drawable.moon);
            case 32: // sunny
            case 34: // fair (day)
                return res.getDrawable(R.drawable.sun);
            case 36: // hot
                return res.getDrawable(R.drawable.thermometer);
            case 3: // severe thunderstorms
            case 4: // thunderstorms
            case 37: // isolated thunderstorms
            case 38: // scattered thunderstorms
            case 39: // scattered thunderstorms
            case 45: // thundershowers
            case 47: // isolated thundershowers
                return res.getDrawable(R.drawable.storm);
            case 5: // mixed rain and snow
            case 7: // mixed snow and sleet
            case 16: // snow
            case 41: // heavy snow
            case 43: // heavy snow
                return res.getDrawable(R.drawable.snowing);
            case 13: // snow flurries
            case 14: // light snow showers
            case 15: // blowing snow
            case 42: // scattered snow showers
            case 46: // snow showers
                return res.getDrawable(R.drawable.snow_day);
            case 6: // mixed rain and sleet
            case 8: // freezing drizzle
            case 9: // drizzle
            case 10: // freezing rain
            case 17: // hail
            case 18: // sleet
            case 35: // mixed rain and hail
                return res.getDrawable(R.drawable.rain);
            case 11: // showers
            case 12: // showers
            case 40: // scattered showers
                return res.getDrawable(R.drawable.rain_day);
        }

        return null;
    }
}