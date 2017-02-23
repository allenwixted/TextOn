package com.allenwixted.texton;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumber;
    private Button smsManagerBtn;
    private Switch heatSwitch;
    private Switch boostSwitch;
    private SeekBar boostSlider;
    private SharedPreferences sp;
    private String phoneNumberSP;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        smsManagerBtn = (Button) findViewById(R.id.smsManager);
        heatSwitch = (Switch) findViewById(R.id.heatSwitch);
        boostSwitch = (Switch) findViewById(R.id.boostSwitch);
        boostSlider = (SeekBar) findViewById(R.id.boostSlider);

        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    sp.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
                    Log.i("SP", sp.getString("phoneNumber", "ERROR READING"));

                }
            }
        });

        sp = this.getSharedPreferences("com.allenwixted.texton", Context.MODE_PRIVATE);
        //sp.edit().putString("phoneNumber", "Enter your unit's phone number").apply();
        phoneNumberSP = sp.getString("phoneNumber", "Enter your unit's phone number");

        phoneNumber.setText(phoneNumberSP);

        Log.i("SP", phoneNumberSP);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)){

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                Toast.makeText(getApplicationContext(),"We need SMS to operate your heating unit",
                        Toast.LENGTH_LONG).show();
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, 1);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, 1);
            }
        }

        smsManagerBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                sendSmsByManager();
            }
        });
    }

    public void sendSmsByManager() {
        try {
            // Get the default instance of the SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumberSP,
                    null,
                    "SMS CODES WILL GO HERE",
                    null,
                    null);
            Toast.makeText(getApplicationContext(), "Commands sent to heater successfully",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"SMS Failed, Check Credit and #",
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    smsManagerBtn.setEnabled(true);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    smsManagerBtn.setEnabled(false);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
