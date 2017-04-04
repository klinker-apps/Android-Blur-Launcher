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

package xyz.klinker.blur.addons.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import xyz.klinker.blur.R;
import xyz.klinker.blur.addons.settings.bubble_tutorial.TutorialActivity;
import xyz.klinker.blur.addons.settings.page_picker.PagePickerActivity;
import xyz.klinker.blur.addons.utils.Utils;
import xyz.klinker.blur.launcher3.Utilities;


public class SettingsActivity extends Activity {

    public static final int REQUEST = 10203;
    public static final int PREF_CHANGED = 12002;

    public Context context;
    public static boolean prefChanged;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

        if (Build.VERSION.SDK_INT >= 21) {
            int color = getResources().getColor(R.color.black);
            int transparent = adjustAlpha(color, .65f);

            getWindow().setStatusBarColor(transparent);
            getWindow().setNavigationBarColor(transparent);
        }

        prefChanged = false;
        context = this;

        setContentView(R.layout.settings_activity);

        findViewById(R.id.background).setAlpha(55/100f);

        getViews();
        setFeedback();
        setClicks();
        //startTutorial();
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public TextView layout;
    public TextView visuals;
    public TextView dock;
    public TextView chooseFrag;
    public TextView help;
    public TextView donate;
    public TextView klinkerApps;
    public TextView restartLauncher;

    public ImageButton overflow;

    public void getViews() {
        layout = (TextView) findViewById(R.id.screen_layout_button);
        visuals = (TextView) findViewById(R.id.visuals_button);
        dock = (TextView) findViewById(R.id.dock_button);
        chooseFrag = (TextView) findViewById(R.id.choose_fragments_button);
        help = (TextView) findViewById(R.id.help_button);
        donate = (TextView) findViewById(R.id.donate_button);
        klinkerApps = (TextView) findViewById(R.id.our_apps_button);
        restartLauncher = (TextView) findViewById(R.id.restart_launcher_button);

        overflow = (ImageButton) findViewById(R.id.overflow_button);

        Utilities.applyTypeface(layout);
        Utilities.applyTypeface(visuals);
        Utilities.applyTypeface(dock);
        Utilities.applyTypeface(chooseFrag);
        Utilities.applyTypeface(help);
        Utilities.applyTypeface(donate);
        Utilities.applyTypeface(klinkerApps);
        Utilities.applyTypeface(restartLauncher);

        Utilities.applyTypeface((TextView) findViewById(R.id.fragment_settings));
        Utilities.applyTypeface((TextView) findViewById(R.id.other_information));
        Utilities.applyTypeface((TextView) findViewById(R.id.display_settings));

        // set the margin for the status bar if they have transparent
        if (Build.VERSION.SDK_INT >= 19) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) overflow.getLayoutParams();
            params.setMargins(0, Utils.getStatusBarHeight(this), 0, 0); //substitute parameters for left, top, right, bottom
            overflow.setLayoutParams(params);
        }
    }

    public void setFeedback() {
        View.OnTouchListener mHapticFeedbackTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
                return false;
            }
        };

        layout.setOnTouchListener(mHapticFeedbackTouchListener);
        visuals.setOnTouchListener(mHapticFeedbackTouchListener);
        dock.setOnTouchListener(mHapticFeedbackTouchListener);
        chooseFrag.setOnTouchListener(mHapticFeedbackTouchListener);
        help.setOnTouchListener(mHapticFeedbackTouchListener);
        donate.setOnTouchListener(mHapticFeedbackTouchListener);
        klinkerApps.setOnTouchListener(mHapticFeedbackTouchListener);
        restartLauncher.setOnTouchListener(mHapticFeedbackTouchListener);

        overflow.setOnTouchListener(mHapticFeedbackTouchListener);
    }

    public static final int PAGE_LAYOUT = 1;
    public static final int PAGE_VISUALS = 2;
    public static final int PAGE_DOCK = 3;
    public static final int PAGE_EXPERIMENTAL = 4;
    public static final int PAGE_BACKUP = 5;
    public static final int PAGE_GESTURES = 6;

    public void setClicks() {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent popupSetting = new Intent(context, SettingsPopupActivity.class);
                popupSetting.putExtra("page", PAGE_LAYOUT);
                startActivity(popupSetting);
            }
        });

        visuals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent popupSetting = new Intent(context, SettingsPopupActivity.class);
                popupSetting.putExtra("page", PAGE_VISUALS);
                startActivity(popupSetting);
            }
        });

        dock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent popupSetting = new Intent(context, SettingsPopupActivity.class);
                popupSetting.putExtra("page", PAGE_DOCK);
                startActivity(popupSetting);
            }
        });

        chooseFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picker = new Intent(context, PagePickerActivity.class);
                startActivity(picker);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picker = new Intent(context, GetHelpActivity.class);
                startActivity(picker);
            }
        });

        klinkerApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picker = new Intent(context, KlinkerAppsActivity.class);
                startActivity(picker);
            }
        });

        restartLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(PREF_CHANGED);
                finish();
            }
        });

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overflowClick(view);
            }
        });
    }

    private void startTutorial() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("initial_tutorial", true)) {
            // tried setting the states of the buttons with the receivers here, but it didn't work

            // have to delay starting the activity so that the animation coming into settings
            // the first time isn't cut off
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(context, TutorialActivity.class));
                    overridePendingTransition(0, 0);
                }
            }, 300);
        }
    }

    private static final int EXP_SETTINGS = 1;
    private static final int BACKUP = 2;
    private static final int FIND_PAGES = 3;
    private static final int FIND_CARDS = 4;
    private static final int ABOUT_US = 5;

    public void overflowClick(View v) {
        final PopupMenu menu = new PopupMenu(context, v);
        menu.getMenu().add(Menu.NONE, EXP_SETTINGS, Menu.NONE, context.getString(R.string.experimental_settings));
        menu.getMenu().add(Menu.NONE, BACKUP, Menu.NONE, context.getString(R.string.backup_and_restore));
        //menu.getMenu().add(Menu.NONE, FIND_PAGES, Menu.NONE, context.getString(R.string.find_pages));
        //menu.getMenu().add(Menu.NONE, FIND_CARDS, Menu.NONE, context.getString(R.string.find_cards));
        //menu.getMenu().add(Menu.NONE, ABOUT_US, Menu.NONE, context.getString(R.string.about));
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case EXP_SETTINGS:
                        Intent exp = new Intent(context, SettingsPopupActivity.class);
                        exp.putExtra("page", PAGE_EXPERIMENTAL);
                        startActivity(exp);
                        return true;
                    case BACKUP:
                        Intent backup = new Intent(context, SettingsPopupActivity.class);
                        backup.putExtra("page", PAGE_BACKUP);
                        startActivity(backup);
                        return true;
                    case FIND_PAGES:
                        new AlertDialog.Builder(context).setItems(R.array.find_pages, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0: // klinker apps pages
                                                showKlinkerAppsPages();
                                                break;
                                            case 1:
                                                Intent pages = new Intent(Intent.ACTION_VIEW);
                                                pages.setData(Uri.parse("market://search?q=" + Uri.encode("blur launcher page")));
                                                pages.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                startActivity(pages);
                                                break;
                                        }
                                    }
                                })
                                .create()
                                .show();

                        return true;
                    case FIND_CARDS:
                        Intent cards = new Intent(Intent.ACTION_VIEW);
                        cards.setData(Uri.parse("market://search?q=" + Uri.encode("blur launcher card")));
                        cards.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(cards);
                        return true;
                    case ABOUT_US:
                        return true;
                    default:
                        return true;
                }
            }
        });
        menu.show();
    }

    public void showKlinkerAppsPages() {
        new AlertDialog.Builder(context)
                .setItems(R.array.our_pages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        switch (which) {
                            case 0: // talon launcher page
                                browserIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=xyz.klinker.blur.twitter_page"));
                                break;
                            case 1: // evolvesms
                                browserIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.evolve_sms"));
                                break;
                        }
                        startActivity(browserIntent);
                    }
                })
                .create()
                .show();
    }

}