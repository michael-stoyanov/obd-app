package com.example.obdtest.broadcastReceivers;

import android.content.Context;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //LogWriter.appendLog("Received incoming call from: " + number);

    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        //
    }
}