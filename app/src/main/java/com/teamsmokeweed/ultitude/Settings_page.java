package com.teamsmokeweed.ultitude;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class Settings_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Toolbar settingTitlebar = (Toolbar) findViewById(R.id.setting_pg_action);
        settingTitlebar.setTitle("Settings");
        setSupportActionBar(settingTitlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
