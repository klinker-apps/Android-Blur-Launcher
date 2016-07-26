package com.klinker.android.launcher.addons.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.adapter.ChangelogAdapter;
import com.klinker.android.launcher.addons.adapter.FaqAdapter;
import com.klinker.android.launcher.addons.utils.XmlChangelogUtils;
import com.klinker.android.launcher.addons.utils.XmlCreditsUtils;
import com.klinker.android.launcher.addons.utils.XmlFaqUtils;

public class GetHelpActivity extends SettingsPopupActivity {

    private static final int CHANGELOG = 0;
    private static final int FAQ = 1;
    private static final int CONTACT_US = 2;
    private static final int BETA_COMMUNITY = 3;
    private static final int WEBSITE = 4;
    private static final int YOUTUBE = 5;
    private static final int CREDITS = 6;
    private static final int PRIVACY_POLICY = 7;

    private static final int TWITTER = 0;
    private static final int GOOGLE_PLUS = 1;
    private static final int EMAIL = 2;

    private static final String TWITTER_LINK = "https://twitter.com/lukeklinker";
    private static final String[] EMAIL_ADDRESS = new String[] {"luke@klinkerapps.com"};
    private static final String WEBSITE_LINK = "http://klinkerapps.com";
    private static final String YOUTUBE_LINK = "http://youtu.be/HD66-8a3J1w";
    private static final String GOOGLE_PLUS_LINK = "https://plus.google.com/communities/111855545153586177337";
    private static final String PRIVACY_POLICY_LINK = "http://privacy.klinkerapps.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.get_help_options, android.R.layout.simple_list_item_1);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case CHANGELOG:
                        showChangelog();
                        break;
                    case FAQ:
                        showFAQDialog();
                        break;
                    case CONTACT_US:
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.faq)
                                .setMessage(R.string.faq_first)
                                .setPositiveButton(R.string.view_faq, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showFAQDialog();
                                    }
                                })
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showContactUsDialog();
                                    }
                                })
                                .create()
                                .show();
                        break;
                    case BETA_COMMUNITY:
                        Intent beta = new Intent(Intent.ACTION_VIEW);
                        beta.setData(Uri.parse(GOOGLE_PLUS_LINK));
                        startActivity(beta);
                        break;
                    case WEBSITE:
                        Intent web = new Intent(Intent.ACTION_VIEW);
                        web.setData(Uri.parse(WEBSITE_LINK));
                        startActivity(web);
                        break;
                    case YOUTUBE:
                        Intent youtube = new Intent(Intent.ACTION_VIEW);
                        youtube.setData(Uri.parse(YOUTUBE_LINK));
                        startActivity(youtube);
                        break;
                    case CREDITS:
                        showCreditsDialog();
                        break;
                    case PRIVACY_POLICY:
                        Intent privacy = new Intent(Intent.ACTION_VIEW);
                        privacy.setData(Uri.parse(PRIVACY_POLICY_LINK));
                        startActivity(privacy);
                        break;
                }
            }
        });
    }

    @Override
    public void setXML() {
        // do nothing here, we don't want an xml
    }

    private void showFAQDialog() {
        final ListView list = new ListView(context);
        list.setDividerHeight(0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - 200);
        list.setLayoutParams(params);


        new AsyncTask<Spanned[], Void, XmlFaqUtils.FAQ[]>() {
            @Override
            public XmlFaqUtils.FAQ[] doInBackground(Spanned[]... params) {
                return XmlFaqUtils.parse(context);
            }

            @Override
            public void onPostExecute(XmlFaqUtils.FAQ[] result) {
                list.setAdapter(new FaqAdapter(context, result));
            }
        }.execute();

        new AlertDialog.Builder(context)
                .setTitle(R.string.faq)
                .setView(list)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showCreditsDialog() {
        final ListView list = new ListView(context);
        list.setDividerHeight(0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - 200);
        list.setLayoutParams(params);

        new AsyncTask<Spanned[], Void, Spanned[]>() {
            @Override
            public Spanned[] doInBackground(Spanned[]... params) {
                return XmlCreditsUtils.parse(context);
            }

            @Override
            public void onPostExecute(Spanned[] result) {
                list.setAdapter(new ChangelogAdapter(context, result));
            }
        }.execute();

        new AlertDialog.Builder(context)
                .setTitle(R.string.credits)
                .setView(list)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showChangelog() {
        final ListView list = new ListView(context);
        list.setDividerHeight(0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - 200);
        list.setLayoutParams(params);

        new AsyncTask<Spanned[], Void, Spanned[]>() {
            @Override
            public Spanned[] doInBackground(Spanned[]... params) {
                return XmlChangelogUtils.parse(context);
            }

            @Override
            public void onPostExecute(Spanned[] result) {
                list.setAdapter(new ChangelogAdapter(context, result));
            }
        }.execute();

        new AlertDialog.Builder(context)
                .setTitle(R.string.changelog)
                .setView(list)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showContactUsDialog() {
        new AlertDialog.Builder(context)
                .setItems(R.array.contact_us_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent();

                        switch(which) {
                            case TWITTER:
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(TWITTER_LINK));
                                break;
                            case GOOGLE_PLUS:
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(GOOGLE_PLUS_LINK));
                                break;
                            case EMAIL:
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("text/html");
                                intent.putExtra(Intent.EXTRA_EMAIL, EMAIL_ADDRESS);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Blur - A Launcher Replacement");
                                break;
                        }

                        if (intent != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create()
                .show();
    }
}
