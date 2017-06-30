package com.teamsmokeweed.ultitude;

import android.app.Service;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    double sealvlpress = 1013.25;
    String sensorName;
    TextView statusView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Look for barometer sensor
//        SensorManager snsMgr = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
//        Sensor pS = snsMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
//        sensorName = pS.getName();
//        snsMgr.registerListener(this, pS, SensorManager.SENSOR_DELAY_UI);
//        sealvlpress= 1007;
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(myToolbar);
//        getOnlineSlpPressure( 1, 1);
//        statusView = (TextView) findViewById(R.id.statusView);
        getApi();
    }

    public double getSealvlpress() {
        return sealvlpress;
    }

    public void setSealvlpress(double sealvlpress) {
        this.sealvlpress = sealvlpress;
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
        if(sensorName.contains("HSPPAD038")){
            values[0] = values[0]-2;
        }
        pressView.setText((new DecimalFormat("##.0").format(values[0])));
        double pressure =  Double.parseDouble(pressView.getText().toString());
        altitudeView.setText(new DecimalFormat("##,###").format(pressureConvert(pressure, sealvlpress)));
        sealvlView.setText("" + sealvlpress);

    }

    double pressureConvert(double currentPressure, double SeaLvPressure){
        return (1-Math.pow(currentPressure/SeaLvPressure, 0.190284))*145366.45*0.3048;
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

                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public double getOnlineSlpPressure(double latitude, double longtitude){
        double slp = 1013.25;

        return slp;
    }

    public void getApi(){
        String baseUrl = "http://api.openweathermap.org/data/2.5/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Interface service = retrofit.create(Interface.class);

        Map<String, String> param = new HashMap<>();
        param.put("lat", "13.848");
        param.put("lon", "100.44");
        param.put("appid", "f1711bf6fbee00fd83c6de3a48dcbcc5");
        service.getApi(param).enqueue(new Callback<WeatherApi>() {
            @Override
            public void onResponse(Call<WeatherApi> call, Response<WeatherApi> response) {
                Toast.makeText(MainActivity.this, "Pressure: "+response.body().getMain().getPressure(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<WeatherApi> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fail: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onFailure: "+ t.getMessage().toString());
            }
        });
    }



}

