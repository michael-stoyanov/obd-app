package com.example.obdtest.logger;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

    static String logFilePath;
    public static String logFileName = "log.txt";
    public static String errorsFileName = "errors.txt";

    public static void appendLog(String text) {
        appendMessage(text, logFileName, true);
    }

    public static void appendLog(String text, boolean putNewLine) {
        appendMessage(text, logFileName, putNewLine);
    }

    public static void appendError(String text) {
        appendMessage(text, errorsFileName, true);
    }

    private static void appendMessage(String text, String fileName, boolean putNewLine) {
        BufferedWriter buf = null;
        logFilePath = Environment.getExternalStorageDirectory().getPath();
        File logFile = new File(logFilePath + "/" + fileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                appendError(e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);

            if (putNewLine)
                buf.newLine();
            else
                buf.append(",");
        } catch (IOException e) {
            try {
                if (buf != null)
                    buf.newLine();
            } catch (IOException e1) {
                e.printStackTrace();
            }
            appendError(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (buf != null)
                    buf.close();
            } catch (IOException e) {
                appendLog(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
