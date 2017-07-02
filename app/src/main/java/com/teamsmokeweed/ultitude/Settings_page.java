package com.teamsmokeweed.ultitude;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class Settings_page extends AppCompatActivity {
    double sealvlpress = 1013.25;
    boolean manualslp;
    boolean autoLocation;
    double[] location = {13.87, 100.54};
    double sensorFineTune;
    SharedPreferences sp;
    Switch autoSlpSw;
    Switch autoLocationSw;
    EditText latInput;
    EditText lonInput;
    EditText slpInput;
    TextView sensorOffset;
    TextView coordview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Toolbar settingTitlebar = (Toolbar) findViewById(R.id.setting_pg_action);
        settingTitlebar.setTitle("Settings");

        setSupportActionBar(settingTitlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //get sp
        sp = getSharedPreferences("ultitude_preference", Context.MODE_PRIVATE);
        manualslp = sp.getBoolean("manualslp", false);
        sealvlpress = (double)sp.getFloat("sealvlpress", 1013.25f);
        autoLocation = sp.getBoolean("autoLocation", true);
        location[0] = (double)sp.getFloat("lat", 13.75f);
        location[1] = (double)sp.getFloat("lon", 100.51f);
        sensorFineTune = (double)sp.getFloat("sensorFineTune", 0.00f);

        //set view
        autoSlpSw = (Switch) findViewById(R.id.autoSlpSwitch);
        autoLocationSw = (Switch) findViewById(R.id.autoLocationSw);
        latInput = (EditText) findViewById(R.id.latInput);
        lonInput = (EditText) findViewById(R.id.lonInput);
        slpInput = (EditText) findViewById(R.id.slpEdit);
        sensorOffset = (TextView) findViewById(R.id.sensorOffset);
        //sensorFineTuneInput = (EditText) findViewById(R.id.fineTuneEdit);

        //setting from pref
        autoSlpSw.setChecked(!manualslp);
        autoLocationSw.setChecked(autoLocation);
        slpInput.setText(new DecimalFormat("0.00").format(sealvlpress));
        latInput.setText(new DecimalFormat("0.00").format(location[0]));
        lonInput.setText(new DecimalFormat("0.00").format(location[1]));
        if(sensorFineTune >= 0){
            sensorOffset.setText("+"+new DecimalFormat("0.00").format(sensorFineTune));
        }
        else{
            sensorOffset.setText(new DecimalFormat("0.00").format(sensorFineTune));
        }
        //sensorFineTuneInput.setText(new DecimalFormat("#.00").format(sensorFineTune));


        setSupportActionBar(settingTitlebar);
        settingTitlebar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
            }
        });

        RelativeLayout calibMenu = (RelativeLayout) findViewById(R.id.calib);
        calibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(Settings_page.this, Activity_calibrate.class);
                startActivityForResult(k, 1);
            }
        });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apply_settings:
                // Put Everything xD
                try {
                    sp.edit().putBoolean("manualslp", !(autoSlpSw.isChecked())).apply();
                    sp.edit().putBoolean("autoLocation", autoLocationSw.isChecked()).apply();
                    sp.edit().putFloat("sealvlpress", Float.parseFloat(slpInput.getText().toString())).apply();
                    sp.edit().putFloat("lat", Float.parseFloat(latInput.getText().toString())).apply();
                    sp.edit().putFloat("lon", Float.parseFloat(lonInput.getText().toString())).apply();
                    //sp.edit().putFloat("sensorFineTune", ).apply();
                    Toast.makeText(getApplicationContext(), "Settings applied!", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "You can't leave field blank! Go, check it out", Toast.LENGTH_SHORT).show();
                }
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                sensorFineTune = (double)sp.getFloat("sensorFineTune", 0.00f);
                if(sensorFineTune >= 0){
                    sensorOffset.setText("+"+new DecimalFormat("0.00").format(sensorFineTune));
                }
                else {
                    sensorOffset.setText(new DecimalFormat("0.00").format(sensorFineTune));
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}
