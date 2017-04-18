package com.texton.texton;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.TimeUnit;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

/*
 * Copyright (C) 2017 Allen Wixted
 */

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumber;
    private Button status;
    private Switch heatSwitch;
    private Switch boostSwitch;
    private SeekBar boostSlider;
    private Button help;

    private SharedPreferences sp;
    private String phoneNumberSP;
    //private boolean heatToggleSP;
    private boolean boostToggleSP;
    private String boostTimeSP;

//    private String on = "on";
//    private String off = "off";
//    private String boost = "boost";
    private int[] boostValues = new int[] {15,30,45,60,120};
    private static int boostSelection = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        status = (Button) findViewById(R.id.smsManager);
        heatSwitch = (Switch) findViewById(R.id.heatSwitch);
        boostSwitch = (Switch) findViewById(R.id.boostSwitch);
        boostSlider = (SeekBar) findViewById(R.id.boostSlider);

        //changed SP file name
        //sp = this.getSharedPreferences("com.texton.texton", Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        phoneNumberSP = sp.getString("phoneNumber", "Enter your unit's phone number");
        phoneNumber.setText(phoneNumberSP);

        rememberSettings();

        if(phoneNumberSP == ""){
            phoneNumber.setHint("Enter your unit's phone number");
        }


        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                    Log.i("SP", sp.getString("phoneNumber", "ERROR READING"));
                } else {
                    sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                }
            }
        });

        heatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                Log.i("SP", sp.getString("phoneNumber", "Error Reading Number"));

                if(isChecked){
                    if(boostSwitch.isChecked()){

                    } else {
                        sendSmsByManager("#01#");
                    }
                    sp.edit().putBoolean("heatToggle", true).apply();

                } else {
                    sendSmsByManager("#02#");
                    sp.edit().putBoolean("heatToggle", false).apply();
                    boostSwitch.setChecked(false);
                }
            }
        });

        boostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sp.edit().putInt("boostSelection", boostSelection).apply();
                    Log.i("SMS", "#138#0#" + boostValues[boostSelection] + "#");
                    sendSmsByManager("#138#0#" + boostValues[boostSelection] + "#");
                    sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                    sp.edit().putBoolean("boostToggle", true).apply();
                    startService(new Intent(getBaseContext(), TimerService.class));
                    heatSwitch.setChecked(true);
                    boostSwitch.setEnabled(false);
                } else {
                    stopService(new Intent(getBaseContext(), TimerService.class));
                    sp.edit().putBoolean("boostToggle", false).apply();
                    heatSwitch.setChecked(false);
                    boostSwitch.setEnabled(true);
                }
            }
        });

        boostSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                boostSelection = progress;
                boostSwitch.setText("Activate Boost for " + String.valueOf(boostValues[progress]) + " minutes");
                sp.edit().putInt("boostSelection", boostSelection).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sp.edit().putInt("boostSelection", boostSelection).apply();
            }
        });

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)){
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                Toast.makeText(getApplicationContext(),"We need SMS to operate your heating unit",
//                        Toast.LENGTH_LONG).show();
//                // sees the explanation, try again to request the permission.
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, 1);
//
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, 1);
//            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, 1);
        }

        status.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                sendSmsByManager("#07#");
            }
        });
    }

    public void sendSmsByManager(String code) {
        try {
            sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
            // Get the default instance of the SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber.getText().toString(), null, code, null, null);
            Log.i("TEST", phoneNumber.getText().toString());
            Toast.makeText(getApplicationContext(), phoneNumber.getText().toString() + ": " + code, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"SMS Failed, Check Credit and #", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    heatSwitch.setEnabled(true);
                    boostSwitch.setEnabled(true);
                    boostSlider.setEnabled(true);
                    status.setEnabled(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    heatSwitch.setEnabled(false);
                    boostSwitch.setEnabled(false);
                    boostSlider.setEnabled(false);
                    status.setEnabled(false);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    private void saveData() {
        sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
        sp.edit().putBoolean("heatToggle", heatSwitch.isChecked()).apply();
        sp.edit().putBoolean("boostToggle", boostSwitch.isChecked()).apply();
        sp.edit().putInt("boostValue", boostValues[boostSelection]).apply();
        sp.edit().putInt("boostSelection", boostSelection).apply();
    }

    private void rememberSettings() {
        phoneNumber.setText(sp.getString("phoneNumber", ""));
        heatSwitch.setChecked(sp.getBoolean("heatToggle", false));
        boostSwitch.setChecked(sp.getBoolean("boostToggle", false));
        boostSlider.setProgress(sp.getInt("boostSelection", 2));
        boostSwitch.setText("Activate Boost for " + String.valueOf(sp.getInt("boostValue", 45) + " minutes"));
    }

    public void openWebURL( String mURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( mURL ) );
        startActivity( browse );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.settings:
                openWebURL(getString(R.string.url));
                return true;
            default:
                return false;
        }
    }
}
