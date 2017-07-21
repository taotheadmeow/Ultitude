package com.teamsmokeweed.ultitude;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.DecimalFormat;

public class Activity_calibrate extends AppCompatActivity {
    double sealvlpress = 1013.25;
    double sensorFineTune;
    double currentPressure;
    EditText currentPressIn;
    EditText knowAltIn;
    EditText knowSlpIn;
    EditText compIo;
    Button calBtn;
    Button okbtn;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        Toolbar calibrateTitlebar = (Toolbar) findViewById(R.id.toolbar_calibrate_page);
        calibrateTitlebar.setTitle("Calibrate Sensor");

        setSupportActionBar(calibrateTitlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setSupportActionBar(calibrateTitlebar);
        calibrateTitlebar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
            }
        });

        sp = getSharedPreferences("ultitude_preference", Context.MODE_PRIVATE);
        sealvlpress = (double)sp.getFloat("sealvlpress", 1013.25f);
        sensorFineTune = (double)sp.getFloat("sensorFineTune", 0.00f);
        currentPressure = (double)sp.getFloat("currentPressure", 0.00f);

        currentPressIn = (EditText) findViewById(R.id.rawPressureIn);
        knowAltIn = (EditText) findViewById(R.id.knownAltIn);
        knowSlpIn = (EditText) findViewById(R.id.slpIn);
        compIo = (EditText) findViewById(R.id.compIo);
        calBtn = (Button) findViewById(R.id.calculateBtn);
        okbtn = (Button) findViewById(R.id.okBtn);

        currentPressIn.setText(new DecimalFormat("0.00").format(currentPressure));
        knowSlpIn.setText(new DecimalFormat("0.00").format(sealvlpress));

        calBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    double calculatedPressure;
                    double alt = Double.parseDouble(knowAltIn.getText().toString());
                    double slp = Double.parseDouble(knowSlpIn.getText().toString());
                    if (alt / (145366.45 * 0.348) - 1.0 < 0) {
                        calculatedPressure = -Math.pow(-(alt / (145366.45 * 0.348) - 1.0), 1 / 0.190284) * slp;
                    } else {
                        calculatedPressure = Math.pow(alt / (145366.45 * 0.348) - 1.0, 1 / 0.190284) * slp;
                    }

                    sensorFineTune = Math.abs(calculatedPressure) - Double.parseDouble(currentPressIn.getText().toString());
                    compIo.setText(new DecimalFormat("0.00").format(sensorFineTune));
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error: can't calculate!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sp.edit().putFloat("sensorFineTune", Float.parseFloat(compIo.getText().toString())).apply();
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Oh! Can't apply! Please check compensation field!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    boolean isEmptyNum(EditText editText){
        return !editText.getText().toString().matches(".*\\\\d+.*");
    }



}
