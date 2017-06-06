package com.texton.texton;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
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
import android.widget.TextView;
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
    private Switch heatAlertSwitch;
    private SeekBar heatAlertSlider;

    private SharedPreferences sp;
    private String phoneNumberSP;
    private boolean boostToggleSP;
    private String boostTimeSP;

    private int[] boostValues = new int[] {30,60,120,240,480,720};
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
        heatAlertSwitch = (Switch) findViewById(R.id.heatAlertSwitch);
        heatAlertSlider = (SeekBar) findViewById(R.id.heatAlertSlider);

        //changed SP file name
        //sp = this.getSharedPreferences("com.texton.texton", Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        phoneNumberSP = sp.getString("phoneNumber", "Enter your unit's phone number");
        phoneNumber.setText(phoneNumberSP);

        rememberSettings();
        showTermsAndConditions();

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
                    Log.i("SMS", "#12#0#" + boostValues[boostSelection] + "#");
                    sendSmsByManager("#12#0#" + boostValues[boostSelection] + "#");
                    sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                    sp.edit().putBoolean("boostToggle", true).apply();
                    startService(new Intent(getBaseContext(), TimerService.class));
                    heatSwitch.setChecked(true);
                    boostSlider.setEnabled(false);
                } else {
                    stopService(new Intent(getBaseContext(), TimerService.class));
                    sp.edit().putBoolean("boostToggle", false).apply();
                    heatSwitch.setChecked(false);
                    boostSlider.setEnabled(true);
                }
            }
        });

        boostSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                boostSelection = progress;
                int t = boostValues[progress];
                int hours = t / 60; //since both are ints, you get an int
                int minutes = t % 60;
                String time = String.format("%d:%02d", hours, minutes);
                boostSwitch.setText("Activate Boost for " + time + "hr(s)");
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

        heatAlertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendSmsByManager("#21#0#1#");
                    sendSmsByManager("#22#0#" + heatAlertSlider.getProgress() + "#40#");
                    sp.edit().putBoolean("heatAlertSwitch", true).apply();
                    heatAlertSlider.setEnabled(false);
                } else {
                    sendSmsByManager("#21#0#0#");
                    sp.edit().putBoolean("heatAlertSwitch", false).apply();
                    heatAlertSlider.setEnabled(true);
                }
            }
        });

        heatAlertSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sp.edit().putInt("heatAlertSlider", progress).apply();
                heatAlertSwitch.setText("Alert me when house is below " + progress + (char) 0x00B0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

    private void showTermsAndConditions() {
        boolean agreed = sp.getBoolean("agreed",false);
        if (!agreed) {
            String url = "<a href=\"https://drive.google.com/drive/folders/0BwYraaqEyWO3UHJQa1p6dEk1M1U\">Terms and Conditions</a>";
            Spanned message = Html.fromHtml(url);
            AlertDialog d = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                d = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle(R.string.termsQuestionAlertTitle)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton(R.string.AlertYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disableEnableUI(true);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean("agreed", true);
                                editor.apply();
                            }
                        })
                        .setNegativeButton(R.string.AlertNo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disableEnableUI(false);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean("agreed", false);
                                editor.apply();
                            }
                        })
                        .setMessage(message)
                        .show();

                TextView msgTxt = (TextView) d.findViewById(android.R.id.message);
                msgTxt.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private void disableEnableUI(Boolean enabled) {
        phoneNumber.setEnabled(enabled);
        status.setEnabled(enabled);
        heatSwitch.setEnabled(enabled);
        boostSwitch.setEnabled(enabled);
        boostSlider.setEnabled(enabled);
        heatAlertSwitch.setEnabled(enabled);
        heatAlertSlider.setEnabled(enabled);
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
        sp.edit().putBoolean("heatAlertSwitch", heatAlertSwitch.isChecked()).apply();
        sp.edit().putInt("heatAlertSlider", heatAlertSlider.getProgress()).apply();
    }

    private void rememberSettings() {
        phoneNumber.setText(sp.getString("phoneNumber", ""));
        heatSwitch.setChecked(sp.getBoolean("heatToggle", false));
        boostSwitch.setChecked(sp.getBoolean("boostToggle", false));
        boostSlider.setProgress(sp.getInt("boostSelection", 2));
        //boostSwitch.setText("Activate Boost for " + String.valueOf(sp.getInt("boostValue", 45) + " minutes"));
        heatAlertSwitch.setChecked(sp.getBoolean("heatAlertSwitch", false));
        heatAlertSlider.setProgress(sp.getInt("heatAlertSlider", 10));
        heatAlertSwitch.setText("Alert me when house is below " + sp.getInt("heatAlertSlider", 10) + (char) 0x00B0);


        int t = boostValues[sp.getInt("boostSelection", 2)];
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;
        String time = String.format(getString(R.string.timeFormat), hours, minutes);
        boostSwitch.setText("Activate Boost for " + time + "hr(s)");
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
            case R.id.terms:
                openWebURL(getString(R.string.terms_url));
                return true;
            default:
                return false;
        }
    }
}
