package com.klinker.android.launcher.info_page.cards.next_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.klinker.android.launcher.api.BaseCard;
import com.klinker.android.launcher.api.CardsLayout;
import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.launcher.info_page.Utils;

import java.util.List;

/**
 * Created by luke on 5/15/14.
 */
public class NextAlarmCard extends BaseCard {

    private static final int DEFAULT_HOURS_LOOKAHEAD = 12;

    // the helper to facilitate getting resources for the card
    private ResourceHelper helper;

    // helps to set the times on the card
    public java.text.DateFormat timeFormatter;

    // display the data for the event
    private TextView time;

    /**
     * Used to initialize and create the card.
     * @param context
     * @param layout
     * @return
     */
    public BaseCard getCard(Context context) {
        return new NextAlarmCard(context);
    }

    /**
     * Default constructor
     * @param context context of the fragment
     * @param parent parent of this card, ie the CardsLayout
     */
    public NextAlarmCard(Context context) {
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
        View root = helper.getLayout("card_alarm");

        // the children to display the event info
        time = (TextView) root.findViewById(helper.getId("time"));

        addView(root);

        // settings will let them choose what app to open
        showSettings(true);

        onRefresh();
        refreshCardLayout();
    }

    /**
     * Called after a refresh has been completed
     * use this to set any refreshed data to the cards layout
     */
    @Override
    public void refreshCardLayout() {
        if (!TextUtils.isEmpty(alarm)) {
            time.setText(alarm);
            shouldShow(true);
        } else {
            shouldShow(false);
        }
    }


    // information on installed apps
    private List<ResolveInfo> mInstalledApps;
    private ApplicationsDialog.AppAdapter mAppAdapter;

    /**
     * Called when the user presses the 3 dot settings menu in the corner of a card
     */
    @Override
    protected void settingsClicked() {

        // set up intent used to search for all apps that show in the launcher
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mInstalledApps = getContext().getPackageManager().queryIntentActivities(mainIntent, 0);

        // creates applications dialog that lists all installed apps
        ApplicationsDialog appDialog = new ApplicationsDialog();
        mAppAdapter = appDialog.createAppAdapter(getContext(), mInstalledApps);
        mAppAdapter.update();

        final Context mContext = getContext();

        // Create listview with all launcher apps listed
        final ListView list = new ListView(mContext);
        list.setAdapter(mAppAdapter);

        // create new dialog box with applications list
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(helper.getString("choose_app"));
        builder.setView(list);
        final Dialog dialog = builder.create();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {

                // if clicked item already on the list, do nothing and return
                final ApplicationsDialog.AppItem info = (ApplicationsDialog.AppItem) arg0.getItemAtPosition(arg2);
                final String packageName = info.packageName;

                Log.v("next_alarm_card", packageName);

                // get the default launcher activity from that package
                PackageManager pm = mContext.getPackageManager();
                Intent i = new Intent("android.intent.action.MAIN");
                i.addCategory("android.intent.category.LAUNCHER");
                List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);

                if (lst != null) {
                    for (ResolveInfo resolveInfo : lst) {
                        if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                            PreferenceManager.getDefaultSharedPreferences(mContext)
                                    .edit()
                                    .putString("next_alarm_app_to_open", resolveInfo.activityInfo.name)
                                    .putString("next_alarm_app_to_open_package", resolveInfo.activityInfo.packageName)
                                    .commit();
                            break;
                        }
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * The user clicked the card
     * @param view view that was pressed
     */
    @Override
    protected void onCardPressed(View view) {
        String className = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("next_alarm_app_to_open", "");
        String packageName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("next_alarm_app_to_open_package", "");

        if (className.equals("")) {
            // there isn't an activity set, so let the user pick one
            settingsClicked();
        } else {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }

    private String alarm;

    /**
     * User has pulled to refresh the card.
     * This method is run on a background thread, not the ui thread.
     * There is then a callback to the ui thread when it is completed.
     */
    @Override
    public void onRefresh() {
        alarm = Settings.System.getString(getContext().getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED);
    }
}
