package com.example.obdtest;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.obdtest.broadcastReceivers.DispIndepReceiver;

public class DispIndepActivity extends AppCompatActivity {

    private DispIndepReceiver screenOnOffReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("dev2qa.com - Keep BroadcastReceiver Running After App Exit.");

        // Create an IntentFilter instance.
        IntentFilter intentFilter = new IntentFilter();

        // Add network connectivity change action.
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");

        // Set broadcast receiver priority.
        intentFilter.setPriority(100);

        // Create a network change broadcast receiver.
        screenOnOffReceiver = new DispIndepReceiver();

        // Register the broadcast receiver with the intent filter object.
        registerReceiver(screenOnOffReceiver, intentFilter);

        Log.d(DispIndepReceiver.SCREEN_TOGGLE_TAG, "onCreate: screenOnOffReceiver is registered.");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister screenOnOffReceiver when destroy.
        if (screenOnOffReceiver != null) {
            unregisterReceiver(screenOnOffReceiver);
            Log.d(DispIndepReceiver.SCREEN_TOGGLE_TAG, "onDestroy: screenOnOffReceiver is unregistered.");
        }
    }
}