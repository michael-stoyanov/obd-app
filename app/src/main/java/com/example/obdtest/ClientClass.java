package com.example.obdtest;

import android.os.AsyncTask;

import com.example.obdtest.logger.LogWriter;
import com.example.obdtest.commands.ObdCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientClass extends AsyncTask<Void, Void, String> {

    public interface ObdCommandResponse {
        String getObdFormattedResponse(String response);
    }

    private ObdCommandResponse obdResponse = null;
    private ObdCommand command;

    public ClientClass(ObdCommand command, ObdCommandResponse obdCommandResponse) {
        this.obdResponse = obdCommandResponse;
        this.command = command;
    }

    @Override
    protected String doInBackground(Void... voids) {
        InetSocketAddress address = new InetSocketAddress("192.168.0.10", 35000);

        Socket socket = new Socket();

        try {
            socket.connect(address, 1000);

            OutputStream mBufferOut = socket.getOutputStream();
            InputStream mBufferIn = socket.getInputStream();

            command.run(mBufferIn, mBufferOut);
        } catch (Exception e) {
            LogWriter.appendError(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket.isConnected())
                    socket.close();
            } catch (Exception e) {
                LogWriter.appendError(e.getMessage());
                e.printStackTrace();
            }
        }

        return command.getFormattedResult();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(String commandResult) {
        obdResponse.getObdFormattedResponse(commandResult);
    }

}


