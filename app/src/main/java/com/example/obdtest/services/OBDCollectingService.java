package com.example.obdtest.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.obdtest.ClientClass;
import com.example.obdtest.logger.LogWriter;
import com.example.obdtest.commands.ObdCommand;
import com.example.obdtest.commands.SpeedCommand;
import com.example.obdtest.commands.control.VinCommand;
import com.example.obdtest.commands.engine.AbsoluteLoadCommand;
import com.example.obdtest.commands.engine.LoadCommand;
import com.example.obdtest.commands.engine.RPMCommand;
import com.example.obdtest.commands.engine.ThrottlePositionCommand;
import com.example.obdtest.commands.protocol.EchoOffCommand;
import com.example.obdtest.commands.protocol.HeadersOffCommand;
import com.example.obdtest.commands.protocol.LineFeedOffCommand;
import com.example.obdtest.commands.protocol.ObdResetCommand;
import com.example.obdtest.commands.protocol.SelectProtocolCommand;
import com.example.obdtest.commands.protocol.SpacesOffCommand;
import com.example.obdtest.commands.protocol.TimeoutCommand;
import com.example.obdtest.commands.temperature.EngineCoolantTemperatureCommand;
import com.example.obdtest.enums.ObdProtocols;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class OBDCollectingService extends Service {
    Socket socket;
    String vinNo;
    String serial;

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String prepareResp = prepareObdCommunication();

                while (!prepareResp.contains("OK"))
                        prepareResp = prepareObdCommunication();
            }
        }).run();

        vinNo = executeCommand(new VinCommand());
        serial = null;

        if (vinNo.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                    return;

                serial = Build.getSerial();
            } else {
                serial = Build.SERIAL;
            }
        }

        LogWriter.appendLog(serial != null ? "Serial " + serial : vinNo);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                String speed = executeCommand(new SpeedCommand());
                String rpm = executeCommand(new RPMCommand());
                String throttle = executeCommand(new ThrottlePositionCommand());
                String coolantTemp = executeCommand(new EngineCoolantTemperatureCommand());
                String load = executeCommand(new LoadCommand());
                String absLoad = executeCommand(new AbsoluteLoadCommand());

                LogWriter.appendLog(String.format("%s,%s,%s,%s,%s,%s", speed, rpm, throttle, coolantTemp, load, absLoad));

                handler.postDelayed(this, 10000);
            }
        };

        handler.post(runnable);

        return START_NOT_STICKY;
    }

    private String prepareObdCommunication() {
        // ATZ resp "ELM327v13aOK"
        if (executeCommand(new ObdResetCommand()) == null)
            return "Obd2 reset command failed";

        // AT E0 resp "OK"
        executeCommand(new EchoOffCommand());
        // AT L0 resp "OK"
        executeCommand(new LineFeedOffCommand());
        // AT ST resp "OK"
        executeCommand(new TimeoutCommand(60));
        // AT S0 resp "OK"
        executeCommand(new SpacesOffCommand());
        // AT H0 resp "OK"
        executeCommand(new HeadersOffCommand());
        // AT SP 0
        executeCommand(new SelectProtocolCommand(ObdProtocols.AUTO));

        return "OK";
    }

    private String executeCommand(final ObdCommand obdCommand) {
        String resp = "";
        try {

            resp = new ClientClass(obdCommand, new ClientClass.ObdCommandResponse() {
                @Override
                public String getObdFormattedResponse(String response) {
                    if (response == null || response.isEmpty() || response.trim().length() == 0
                            || response.equals("Not Connected") || response.equals("Connection timed out after 5000ms")) {
                        LogWriter.appendError(response);
                        return null;
                    }

                    return response;
                }
            }).execute().get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            LogWriter.appendError(e.getMessage());
            e.printStackTrace();
        }

        return resp;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (this.socket != null && this.socket.isConnected())
                this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket = null;
    }
}
