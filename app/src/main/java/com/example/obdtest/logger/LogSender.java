package com.example.obdtest.logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogSender {

    private HttpURLConnection httpConn;
    private DataOutputStream request;
    private final String boundary = "*****";
    private final String crlf = "\r\n";
    private final String twoHyphens = "--";

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL url to send info to
     * @throws IOException
     */
    public LogSender(String requestURL)
            throws IOException {

        // creates a unique boundary based on time stamp
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);

        httpConn.setChunkedStreamingMode(0);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestProperty("Cache-Control", "no-cache");
        httpConn.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + this.boundary);

        request = new DataOutputStream(httpConn.getOutputStream());
    }


    /**
     * Adds a upload file section to the request
     *
     * @param fieldName     name attribute in <input type="file" name="..." />
     * @param fileForUpload a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File fileForUpload)
            throws IOException {
        String fileName = fileForUpload.getName();
        request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\";filename=\"" +
                fileName + "\"" + this.crlf);
        request.writeBytes(this.crlf);


        try {

            FileInputStream fileStream = new FileInputStream(fileForUpload);
            int bytesAvailable = fileStream.available();
            int maxBufferSize = 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] bytes = new byte[bufferSize];
//            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(fileForUpload));


            int bytesRead = fileStream.read(bytes, 0, bufferSize);

            while (bytesRead > 0) {
                request.write(bytes, 0, bufferSize);
                bytesAvailable = fileStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileStream.read(bytes, 0, bufferSize);
            }

            fileStream.close();

        } catch (FileNotFoundException e) {
            LogWriter.appendLog("From addFilePart method: " + e.getMessage());
        } catch (Exception e) {
            LogWriter.appendLog(e.getMessage());
        }

    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
        String response = "";

        request.writeBytes(this.crlf);
        request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

        request.flush();
        request.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            InputStream responseStream = new
                    BufferedInputStream(httpConn.getInputStream());

            BufferedReader responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");

            responseStreamReader.close();

            response = stringBuilder.toString();
            httpConn.disconnect();
        } else {
            throw new IOException("Bad request: " + status);
        }

        return response;
    }
}


