package com.klinker.android.launcher.addons.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.klinker.android.launcher.R;

public class KlinkerAppsActivity extends SettingsPopupActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.klinker_apps, android.R.layout.simple_list_item_1);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] links = getResources().getStringArray(R.array.klinker_apps_links);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(links[i])));
            }
        });
    }

    @Override
    public void setXML() {
        // do nothing here, we don't want an xml
    }
}