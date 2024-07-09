package com.example.obdtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obdtest.broadcastReceivers.CallReceiver;
import com.example.obdtest.broadcastReceivers.SmsListener;
import com.example.obdtest.logger.LogSender;
import com.example.obdtest.logger.LogWriter;
import com.example.obdtest.services.OBDCollectingService;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    Button wifiManager_enable;
//    Button wifiManager_scan;
//    SensorEventListener sensorEventListener;
//    LocationListener locationListener;
//    List<ScanResult> scanResultList;
//    float mLastX, mLastY, mLastZ;
//    boolean mInitialized;
//    SensorManager mSensorManager;
//    Sensor mAccelerometer;
//    final float NOISE = (float) 2.0;
    Button test;
//    Button accelerometerCommand;
//    Button locationCommand;

    WifiManager wifiManager;
//    WifiConfiguration wifiConfig;

    public TextView result_text1;
    public TextView result_text2;
//    TextView result_text3;

    //    Button vinNoCommand;
    Button service;
    Button sendLogs;

    static final int REQUEST_WRITE_STORAGE = 112;
    boolean isButtonPressed;
    boolean isServiceRunning = false;

    String stopServiceText = "Stop service";
    String startServiceText = "Start service";

    LocationManager locationManager;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;

            Location previousLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (previousLocation != location) {
                LogWriter.appendLog(String.format("GPS: %s,%s", location.getLatitude(), location.getLongitude()));
                result_text1.setText(String.format("Longitude: %s", location.getLongitude()));
                result_text2.setText(String.format("Latitude: %s", location.getLatitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
        }
    };

//    TelephonyManager telephonyManager;

    SmsListener smsListener;
    CallReceiver callReceiver;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.smsListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                             Manifest.permission.RECEIVE_SMS,
                             Manifest.permission.READ_PHONE_STATE,
                             Manifest.permission.CALL_PHONE},
                REQUEST_WRITE_STORAGE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//        Intent backgroundService = new Intent(getApplicationContext(), DispIndepBackgroundService.class);
//        startService(backgroundService);

        smsListener = new SmsListener();
        callReceiver = new CallReceiver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            registerReceiver(smsListener, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

        registerReceiver(callReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isButtonPressed = true;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        result_text1 = (TextView) findViewById(R.id.result_text1);
        result_text2 = (TextView) findViewById(R.id.result_text2);
//        result_text3 = (TextView) findViewById(R.id.result_text3);

        sendLogs = (Button) findViewById(R.id.sendLog);
        service = (Button) findViewById(R.id.service);
        test = (Button) findViewById(R.id.test);

        service.setText(startServiceText);

        setOnClickListeners();
    }
//            sensorEventListener = new SensorEventListener() {
//                @Override
//                public void onSensorChanged(SensorEvent event) {
//                    if (!isButtonPressed) {
//                        float x = event.values[0];
//                        float y = event.values[1];
//                        float z = event.values[2];
//                        if (!mInitialized) {
//                            mLastX = x;
//                            mLastY = y;
//                            mLastZ = z;
//                            result_text1.setText("0.0");
//                            result_text2.setText("0.0");
//                            result_text3.setText("0.0");
//                            mInitialized = true;
//                        } else {
//                            float deltaX = Math.abs(mLastX - x);
//                            float deltaY = Math.abs(mLastY - y);
//                            float deltaZ = Math.abs(mLastZ - z);
//                            if (deltaX < NOISE) deltaX = (float) 0.0;
//                            if (deltaY < NOISE) deltaY = (float) 0.0;
//                            if (deltaZ < NOISE) deltaZ = (float) 0.0;
//                            mLastX = x;
//                            mLastY = y;
//                            mLastZ = z;
//                            result_text1.setText(Float.toString(deltaX));
//                            result_text2.setText(Float.toString(deltaY));
//                            result_text3.setText(Float.toString(deltaZ));
//                            LogWriter.appendLog(String.format("x: %f, y: %f, z: %f", x, y, z));
//                        }
//                    }
//                }
//
//                @Override
//                public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//                }
//            };
//            mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//
//            BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            scanResultList = wifiManager.getScanResults();
//            String deviceArray[] = new String[scanResultList.size()];
//
//            for (int i = 0; i < scanResultList.size(); i++) {
//                deviceArray[i] = scanResultList.get(i).SSID;
//            }
//
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceArray);
//
//            networks.setAdapter(adapter);
//            networks.setVisibility(View.VISIBLE);
//
//            unregisterReceiver(this);
//        }
//    };

    public void setOnClickListeners() {
        service.setOnClickListener(this);
        sendLogs.setOnClickListener(this);
        test.setOnClickListener(this);
    }

//                        networks.setOnItemClickListener(new AdapterView.OnItemClickListener()
//        @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ScanResult device = scanResultList.get(position);
//
//                wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", device.SSID);
//
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//
//                int inetId = wifiManager.addNetwork(wifiConfig);
//
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(inetId, true);
//                wifiManager.reconnect();
//
//                networks.setVisibility(View.INVISIBLE);
//            }
//        });
//
//    private void VLinkConnect() {
//
//        wifiConfig = new WifiConfiguration();
//
//        wifiConfig.SSID = "V-Link";
//        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//
//        int inetId = wifiManager.addNetwork(wifiConfig);
//
//        wifiManager.enableNetwork(inetId, true);
//        wifiManager.reconnect();
//
//        if (wifiManager.getConnectionInfo().getSSID().equals("V-Link"))
//            Toast.makeText(this, "Connected to: " + wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
//    }
//
//        @SuppressWarnings("deprecation")
//    public void scanWifi() {
//        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//
//        wifiManager.startScan();
//        Toast.makeText(this, "Scanning .....", Toast.LENGTH_LONG).show();
//        IPaddress = intToInetAddress(wifiManager.getDhcpInfo().serverAddress).getHostAddress();
//    }
//    GPSClass gpsClass = new GPSClass();

    String response = "";

    @Override
    public void onClick(View btn) {
        try {
            switch (btn.getId()) {
                case R.id.sendLog:
                    if (response.isEmpty()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    LogSender sender = new LogSender("http://10.0.0.247:8092/api/ObdLog");
                                    File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + LogWriter.logFileName);

                                    sender.addFilePart("", logFile);
                                    response = sender.finish();

                                    if (response != null && response.contains("\"reasonPhrase\":")) {
                                        String status = "\"reasonPhrase\":\"";

                                        int startIndex = response.lastIndexOf(status) + status.length();
                                        response = response.substring(startIndex);
                                        int endIndex = response.indexOf("\"");
                                        response = response.substring(0, endIndex);

//                                    if (statusCode.equals("200")) {
//                                        logFile.delete();
//                                        response = "File sent successfully";
//                                    }
                                    }
                                } catch (IOException e) {
                                    LogWriter.appendError(e.getMessage());
                                    Toast.makeText(getApplicationContext(), "Something went wrong with the request.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).start();
                    } else {
                        result_text1.setText(response);
                        response = "";
                    }
                    break;
                case R.id.service:
                    Intent serviceIntent = new Intent(this, OBDCollectingService.class);
                    GPSClass gpsClass = new GPSClass();

                    if (!isServiceRunning) {
                        gpsClass.enable();
                        service.setText(stopServiceText);
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            return;

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, locationListener);

                        startService(serviceIntent);
                        isServiceRunning = true;
                    } else {
                        gpsClass.disable();
                        service.setText(startServiceText);
                        locationManager.removeUpdates(locationListener);
                        stopService(serviceIntent);
                        isServiceRunning = false;
                    }
                    break;
                case R.id.test:
                    Intent intent = new Intent(MainActivity.this, QRCodeScanner.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LogWriter.appendError(e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (response != null && !response.isEmpty())
            result_text1.setText(response);
    }

//        public InetAddress intToInetAddress(int hostAddress) {
//        byte[] addressBytes = {(byte) (0xff & hostAddress),
//                (byte) (0xff & (hostAddress >> 8)),
//                (byte) (0xff & (hostAddress >> 16)),
//                (byte) (0xff & (hostAddress >> 24))};
//
//        try {
//            return InetAddress.getByAddress(addressBytes);
//        } catch (UnknownHostException e) {
//            throw new AssertionError();
//        }
//    }
//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            prepareObdCommunication();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    //    @SuppressLint({"MissingPermission"})
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 10:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
//                return;
//        }
//    }
//
    public class GPSClass implements LocationListener {
        LocationManager locManager;

        public void enable() {
            locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        }

        public void disable() {
            locManager.removeUpdates(this);
        }

        public void onLocationChanged(Location location) {
            @SuppressLint("MissingPermission") Location previousLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (previousLocation != location) {
                DecimalFormat formatter = new DecimalFormat("%,3d%n");
                LogWriter.appendLog(String.format("GPSclass: %s,%s", formatter.format(location.getLatitude()), formatter.format(location.getLongitude())));
                result_text1.setText(String.format("Latitude %s", location.getLatitude()));
                result_text2.setText(String.format("Longitude %s", location.getLongitude()));
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }
}