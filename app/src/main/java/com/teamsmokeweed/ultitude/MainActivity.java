package com.teamsmokeweed.ultitude;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    double sealvlpress = 1013.25;
    double currentPressure;
    String sensorName;
    TextView statusView;
    boolean manualslp;
    boolean autoLocation;
    boolean useMetric;
    SharedPreferences sp;
    double[] location = {0,0};
    Thread t;
    ImageView indicatorView;
    double sensorFineTune;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //read setting
        sp = getSharedPreferences("ultitude_preference", Context.MODE_PRIVATE);
        manualslp = sp.getBoolean("manualslp", false);
        sealvlpress = (double) sp.getFloat("sealvlpress", 1013.25f);
        autoLocation = sp.getBoolean("autoLocation", true);
        location[0] = (double) sp.getFloat("lat", 13.75f);
        location[1] = (double) sp.getFloat("lon", 100.51f);
        sensorFineTune = (double) sp.getFloat("sensorFineTune", 0.00f);
        useMetric = sp.getBoolean("useMetric", true);
        sp.getFloat("currentPressure", 0.00f);

        //Look for barometer sensor
        SensorManager snsMgr = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        Sensor pS = snsMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorName = pS.getName();
        snsMgr.registerListener(this, pS, SensorManager.SENSOR_DELAY_UI);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        statusView = (TextView) findViewById(R.id.statusView);
        indicatorView = (ImageView) findViewById(R.id.indicator);
        final TextView meterUnitView = (TextView) findViewById(R.id.meterUnitView);
        final TextView feetUnitView = (TextView) findViewById(R.id.feetUnitView);

        meterUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meterUnitView.setAlpha(1.0f);
                feetUnitView.setAlpha(0.3f);
                useMetric = true;
                sp.edit().putBoolean("useMetric", true).apply();
            }
        });

        feetUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meterUnitView.setAlpha(0.3f);
                feetUnitView.setAlpha(1.0f);
                useMetric = false;
                sp.edit().putBoolean("useMetric", false).apply();
            }
        });

        t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLocation(autoLocation);
                                getApi();
                                if (isFinishing() || manualslp) {
                                    t.interrupt();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {

                }
            }
        };
        if (manualslp) {
            setManualSLP();
        }
        else {
            getLocation(autoLocation);
            getApi();
            if(t.isInterrupted() || !t.isAlive()){
                t.start();
            }
        }

    }
    void setManualSLP(){
        if(t.isAlive()){
            t.interrupt();
        }
        statusView.setText("Using custom SLP.");
        indicatorView.setImageResource(R.drawable.ic_dot_24dp);
        sealvlpress = (double) sp.getFloat("sealvlpress", 1013.25f);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        TextView pressView = (TextView) findViewById(R.id.press);
        TextView altitudeView = (TextView) findViewById(R.id.altitude);
        TextView sealvlView = (TextView) findViewById(R.id.SeaLvPress);
        //calibrate
        currentPressure = values[0];
        if(!manualslp && (t.isInterrupted() || !t.isAlive())){
            t.start();
        }

        double pressure = currentPressure + (double) sp.getFloat("sensorFineTune", 0.00f);
        pressView.setText((new DecimalFormat("##.0").format(pressure)));
        sealvlView.setText((new DecimalFormat("##.0").format(sealvlpress)));
        altitudeView.setText(new DecimalFormat("##,###").format(pressureConvert(pressure, sealvlpress)));
    }

    double pressureConvert(double currentPressure, double SeaLvPressure) {
        if(useMetric) {
            return (1 - Math.pow(currentPressure / SeaLvPressure, 0.190284)) * 145366.45 * 0.3048;
        }
        else{
            return (1 - Math.pow(currentPressure / SeaLvPressure, 0.190284)) * 145366.45;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                sp.edit().putFloat("currentPressure", (float)currentPressure).apply();
                Intent k = new Intent(this, Settings_page.class);
                startActivityForResult(k, 1);

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
                manualslp = sp.getBoolean("manualslp", false);
                sealvlpress = (double) sp.getFloat("sealvlpress", 1013.25f);
                autoLocation = sp.getBoolean("autoLocation", true);
                location[0] = (double) sp.getFloat("lat", 13.75f);
                location[1] = (double) sp.getFloat("lon", 100.51f);
                if(!manualslp) {
                    getLocation(autoLocation);
                    getApi();
                    if (t.isInterrupted() || !t.isAlive()) {
                        t.start();
                    }
                }
                else {
                    setManualSLP();
                }
                }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                if(!manualslp) {
                    getLocation(autoLocation);
                    getApi();
                    if (!t.isAlive()) {
                        t.start();
                    }
                }
                else {
                    setManualSLP();
                }
            }
        }
    }


    public void getLocation(boolean isAutoLocation) {
        double[] coord = {0,0};
        if(isAutoLocation) {
            GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
            location[0] = gpsTracker.getLatitude();
            location[1] = gpsTracker.getLongitude();
        }
        else{
            location[0] = (double) sp.getFloat("lat", 13.75f);
            location[1] = (double) sp.getFloat("lon", 100.51f);
        }
    }


    public void getApi(){
        String baseUrl = "http://api.openweathermap.org/data/2.5/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Interface service = retrofit.create(Interface.class);

        Map<String, String> param = new HashMap<>();
        param.put("lat", ""+location[0]);
        param.put("lon", ""+location[1]);
        param.put("appid", "f1711bf6fbee00fd83c6de3a48dcbcc5");
        service.getApi(param).enqueue(new Callback<WeatherApi>() {
            @Override
            public void onResponse(Call<WeatherApi> call, Response<WeatherApi> response) {
                try {
                    try {
                        sealvlpress = response.body().getMain().getSeaLevel();
                    } catch (NullPointerException e){
                        sealvlpress = response.body().getMain().getPressure();
                    }
                    statusView.setText("Connected. Using SLP at: "+ new DecimalFormat("#.##").format(location[0])+", "+new DecimalFormat("#.##").format(location[1]));
                    indicatorView.setImageResource(R.drawable.ic_green_dot_24dp);
                }catch (NullPointerException e){
                    sealvlpress = (double) sp.getFloat("sealvlpress", 1013.25f);
                    statusView.setText("Offline. Error: Can't acquire location! Using custom SLP.");
                    indicatorView.setImageResource(R.drawable.ic_red_dot_24dp);
                }



            }

            @Override
            public void onFailure(Call<WeatherApi> call, Throwable t) {
               // Toast.makeText(MainActivity.this, "Fail: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                // Log.e("TAG", "onFailure: "+ t.getMessage().toString());
                sealvlpress = (double) sp.getFloat("sealvlpress", 1013.25f);
                statusView.setText("Oh we can't obtain SLP from internet. Using custom SLP. ");
                indicatorView.setImageResource(R.drawable.ic_dot_24dp);
            }
        });
    }

    @Override
    public void onBackPressed() {
        t.interrupt();
        finish(); // finish activity
        }





}

