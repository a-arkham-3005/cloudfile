package com.darwin.cloudfile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import android.os.Handler;
import android.os.Looper;

public class HttpHandler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    public static String sendPostRequest(String urlString, String postData) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configure connection for POST request
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // Allows sending data
        connection.setRequestProperty("Content-Type", "application/json"); // Example: sending JSON
        connection.setRequestProperty("Content-Length", String.valueOf(postData.getBytes(StandardCharsets.UTF_8).length));

        // Send POST data
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postData.getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        // Get response
        int responseCode = connection.getResponseCode();
        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) { // Success codes
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else { // Error codes
            in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
    private static String doInBackground(String postData, String action) {
        try {
            return HttpHandler.sendPostRequest("http://darwin66400.ddns.net/cf/"+action+".php", postData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String executeTask(String postData, String action) {
        CountDownLatch latch=new CountDownLatch(1);
        // Pre-execution (on UI thread)
        handler.post(() -> {
            // Update UI, e.g., show a loading circle
            System.out.println("Starting HTTP task...");
        });

        AtomicBoolean error= new AtomicBoolean(false);
        AtomicReference<String> resultAll=new AtomicReference<>("nothing_received_error");
        // Background execution
        executor.execute(() -> {
            // Perform long-running operation
            String result = doInBackground(postData,action);
            // Post-execution (on UI thread)
            handler.post(() -> {
                // Update UI with the result
            });
            if (result == null) error.set(true);
            else resultAll.set(result);
            latch.countDown();
        });
        try{
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted! Use the program on your fear and risk.");
        }
        if(error.get()) return "debug_error";
        else return resultAll.get();
    }
}