package com.example.cai;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private Button button_send, button_get_gps;
    private EditText editText_phone, editText_message, editText_lat, editText_long;
    private String phoneNo, message, lat, longi;

    private LocationManager locationManager;
    private LocationListener locationListener;

    public static final int PERMISSION_REQUEST_SMS = 1;
    public static final int PERMISSION_REQUEST_GPS = 10;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get buttons
        button_get_gps  = findViewById(R.id.get_gps);
        button_send     = findViewById(R.id.button_send);

        // Get texts
        editText_phone      = findViewById(R.id.editText_phone);
        editText_message    = findViewById(R.id.editText_message);
        editText_lat        = findViewById(R.id.editText_lat);
        editText_long       = findViewById(R.id.editText_long);

        // Config Locations
        locationManager     = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener    = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                editText_lat.setText(""+location.getLatitude());
                editText_long.setText(""+location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                },  PERMISSION_REQUEST_GPS);
            }

        }

        button_get_gps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                getGPS();
            }
        });

        button_send.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                send();
            }
        });
    }

    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }

    public void getGPS()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                    },  PERMISSION_REQUEST_GPS);
                }
                else {
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                }
            }
        }
        else
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
    }

    // Check permission and send SMS
    public void send()
    {
        phoneNo = editText_phone.getText().toString();
        message = editText_message.getText().toString();
        lat     = editText_lat.getText().toString();
        longi   = editText_long.getText().toString();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_SMS);
            }
            else
                sendSMS(phoneNo, message);
        }
        else
            sendSMS(phoneNo, message);
    }

    public void sendSMS(String phoneNo, String msg)
    {
        if(lat.trim().length() > 0 && longi.trim().length() > 0)
        {
            msg = String.format("%s\nLatitude : %s\nLongitude : %s", msg, lat, longi);
        }
        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_SMS : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permission granted to send SMS", Toast.LENGTH_SHORT).show();
                    sendSMS(phoneNo, message);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case PERMISSION_REQUEST_GPS:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getGPS();
                else
                    closeNow();
                return;
        }
    }
}

