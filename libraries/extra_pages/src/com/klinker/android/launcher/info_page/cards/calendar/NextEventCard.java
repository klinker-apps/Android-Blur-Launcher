package com.klinker.android.launcher.info_page.cards.calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.klinker.android.launcher.api.BaseCard;
import com.klinker.android.launcher.api.CardsLayout;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.extra_pages.R;
import com.klinker.android.launcher.info_page.Utils;

import java.util.Date;

/**
 * Created by luke on 5/15/14.
 */
public class NextEventCard extends BaseCard {

    private static final int DEFAULT_HOURS_LOOKAHEAD = 12;

    // the helper to facilitate getting resources for the card
    private ResourceHelper helper;

    // helps to set the times on the card
    public java.text.DateFormat timeFormatter;

    // display the data for the event
    private TextView title;
    private TextView timestamp;
    private ImageView image;

    // the root layouts for the parts of the card
    private LinearLayout eventLayout;
    private LinearLayout eventSettingsLayout;

    /**
     * Used to initialize and create the card.
     * @param context
     * @return
     */
    public BaseCard getCard(Context context) {
        return new NextEventCard(context);
    }

    /**
     * Default constructor
     * @param context context of the fragment
     */
    public NextEventCard(Context context) {
        super(context);
    }

    /**
     * Creates the layout for the card and sets the components
     */
    @Override
    protected void setUpCardLayout() {
        // remember that we need the resource helper since this will be running from a different package
        helper = new ResourceHelper(getContext(), Utils.PACKAGE_NAME);

        // get the time formatter from the system
        timeFormatter = android.text.format.DateFormat.getTimeFormat(getContext());

        // get the root view layout
        View root = helper.getLayout("card_next_event");

        // the children to display the event info
        title = (TextView) root.findViewById(helper.getId("title"));
        timestamp = (TextView) root.findViewById(helper.getId("timestamp"));
        image = (ImageView) root.findViewById(helper.getId("cal_image"));

        // the layouts for the parts of the card
        eventLayout = (LinearLayout) root.findViewById(helper.getId("event_layout"));
        eventSettingsLayout = (LinearLayout) root.findViewById(helper.getId("event_settings_layout"));

        addView(root);

        showSettings(true);

        onRefresh();
        refreshCardLayout();
    }

    /**
     * Called when the user presses the 3 dot settings menu in the corner of a card
     */
    @Override
    protected void settingsClicked() {
        // show the settings and hide the event
        // animation is just handled by the xml attribute animateLayoutChanges="true"
        eventLayout.setVisibility(View.INVISIBLE);
        eventSettingsLayout.setVisibility(View.VISIBLE);

        // get the buttons
        TextView yesButton = (TextView) eventSettingsLayout.findViewById(helper.getId("yes_button"));
        TextView noButton = (TextView) eventSettingsLayout.findViewById(helper.getId("no_button"));

        yesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean("next_event_card_show_all_day", true)
                        .commit();

                eventSettingsLayout.setVisibility(View.INVISIBLE);
                eventLayout.setVisibility(View.VISIBLE);

                // probably shouldn't call this from the UI thread, but it is fine for now.
                onRefresh();
                refreshCardLayout();
            }
        });

         noButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean("next_event_card_show_all_day", false)
                        .commit();

                eventSettingsLayout.setVisibility(View.INVISIBLE);
                eventLayout.setVisibility(View.VISIBLE);

                // probably shouldn't call this from the UI thread, but it is fine for now.
                onRefresh();
                refreshCardLayout();
            }
        });
    }

    /**
     * Called after a refresh has been completed
     * use this to set any refreshed data to the cards layout
     */
    @Override
    public void refreshCardLayout() {
        if (id != 0) {
            title.setText(titleText);
            timestamp.setText(timeText);

            if (happeningNow) {
                image.setColorFilter(getContext().getResources().getColor(R.color.klinker_apps_orange));
            } else {
                image.clearColorFilter();
            }
            shouldShow(true);
        } else {
            shouldShow(false);
        }
    }

    /**
     * The user clicked the card
     * @param view view that was pressed
     */
    @Override
    protected void onCardPressed(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(id)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        getContext().startActivity(intent);
    }

    private String titleText;
    private String timeText;
    private long id;
    private boolean happeningNow = false;

    /**
     * User has pulled to refresh the card.
     * This method is run on a background thread, not the ui thread.
     * There is then a callback to the ui thread when it is completed.
     */
    @Override
    public void onRefresh() {
        Cursor cursor = CalendarUtils.tryOpenEventsCursor(getContext(), DEFAULT_HOURS_LOOKAHEAD);
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(CalendarUtils.EventsQuery.EVENT_ID);
                long eventBegin = cursor.getLong(CalendarUtils.EventsQuery.BEGIN);
                long eventEnd = cursor.getLong(CalendarUtils.EventsQuery.END);
                titleText = cursor.getString(CalendarUtils.EventsQuery.TITLE);

                String beginString = timeFormatter.format(new Date(eventBegin));
                String endString = timeFormatter.format(new Date(eventEnd));

                // the beginning and end for an all day even is 7pm to 7pm
                // so we check that here.
                if (beginString.equals(endString)) {
                    if (PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getBoolean("next_event_card_show_all_day", true)) {
                        timeText = helper.getString("all_day");
                    } else {
                        // this will force it to not be shown
                        id = 0;
                        titleText = "";
                        timeText = "";
                    }
                } else {
                    timeText = beginString + " - " + endString;
                }

                long currTime = CalendarUtils.getCurrentTimestamp();

                if (currTime < eventEnd && currTime > eventBegin) {
                    happeningNow = true;
                } else {
                    happeningNow = false;
                }
            } while (id == 0 && cursor.moveToNext());
        } else {
            id = 0;
            titleText = "";
            timeText = "";
        }
    }
}
