package com.darwin.cloudfile;

import android.os.Handler;
import android.os.Looper;
import org.apache.commons.net.ftp.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class FtpHandler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final FTPClient ftp=new FTPClient();
    private static boolean moveRenameInBackground(String action, String remoteFile1, String remoteFile2, String[] credentials) {
        boolean error=false;
        try {
            // do something for FTP
            ftp.connect(InetAddress.getByName("darwin66400.ddns.net"), 21);
            ftp.login(credentials[0],credentials[1]);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            if (action.equals("move_rename")) {
                error=!ftp.rename(remoteFile1, remoteFile2);
            }
            ftp.logout();
            ftp.disconnect();
            return !error;
        } catch (Exception e) {
            return false;
        }
    }
    private static boolean doInBackground(String action, File localFile, String remoteFile, String[] credentials) {
        boolean error=false;
        try {
            // do something for FTP
            ftp.connect(InetAddress.getByName("darwin66400.ddns.net"), 21);
            ftp.login(credentials[0],credentials[1]);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            switch(action){
                case "download":{
                    OutputStream oS=new FileOutputStream(localFile);
                    boolean success=ftp.retrieveFile(remoteFile,oS);
                    oS.close();
                    if(!success){
                        error=true;
                    }
                    break;
                }
                case "upload":{
                    InputStream iS=new FileInputStream(localFile);
                    boolean success=ftp.storeFile(remoteFile,iS);
                    iS.close();
                    if(!success){
                        error=true;
                    }
                    break;
                }
                case "delete":{
                    boolean success=ftp.deleteFile(remoteFile);
                    if(!success){
                        error=true;
                    }
                    break;
                }
                case "mkdir":{
                    boolean success=ftp.makeDirectory(remoteFile);
                    if(!success){
                        error=true;
                    }
                    break;
                }
                default:{}
            }
            ftp.logout();
            ftp.disconnect();
            return !error;
        } catch (Exception e) {
            return false;
        }
    }
    private static ArrayList<FTPFile> fetchInBackground(String action, String remoteDir, String[] credentials) {
        ArrayList<FTPFile> data=new ArrayList<>();
        try {
            // do something for FTP
            ftp.connect(InetAddress.getByName("darwin66400.ddns.net"), 21);
            ftp.login(credentials[0],credentials[1]);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            if (action.equals("fetch_dir")) {
                ftp.changeWorkingDirectory(remoteDir);
                FTPFile[] files = ftp.listFiles(remoteDir);
                data.addAll(Arrays.asList(files));
            }
            ftp.logout();
            ftp.disconnect();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            FTPFile dummy=new FTPFile();
            dummy.setName("<ERROR>");
            data.add(dummy);
            return data;
        }
    }
    public static boolean executeTask(String action, File localFile, String remoteFile, String[] credentials) {
        CountDownLatch latch=new CountDownLatch(1);
        // Pre-execution (on UI thread)
        handler.post(() -> {
            // Update UI, e.g., show a loading circle
            System.out.println("Starting download/upload task...");
        });

        AtomicBoolean error= new AtomicBoolean(false);
        // Background execution
        executor.execute(() -> {
            // Perform long-running operation
            boolean result = doInBackground(action,localFile,remoteFile,credentials);
            // Post-execution (on UI thread)
            handler.post(() -> {
                // Update UI with the result
            });
            error.set(!result);
            latch.countDown();
        });
        try{
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted! Use the program on your fear and risk.");
        }
        return(!error.get());
    }

    public static boolean executeMoveRename(String action, String remoteFile1, String remoteFile2, String[] credentials) {
        CountDownLatch latch=new CountDownLatch(1);
        // Pre-execution (on UI thread)
        handler.post(() -> {
            // Update UI, e.g., show a loading circle
            System.out.println("Starting moving/renaming task...");
        });

        AtomicBoolean error= new AtomicBoolean(false);
        // Background execution
        executor.execute(() -> {
            // Perform long-running operation
            boolean result = moveRenameInBackground(action,remoteFile1,remoteFile2,credentials);
            // Post-execution (on UI thread)
            handler.post(() -> {
                // Update UI with the result
            });
            if (!result) error.set(true);
            latch.countDown();
        });
        try{
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted! Use the program on your fear and risk.");
        }
        return !error.get();
    }

    public static ArrayList<FTPFile> executeFetch(String action, String remoteDir, String[] credentials) {
        CountDownLatch latch=new CountDownLatch(1);
        // Pre-execution (on UI thread)
        handler.post(() -> {
            // Update UI, e.g., show a loading circle
            System.out.println("Starting fetching task...");
        });
        AtomicReference<ArrayList<FTPFile>> data=new AtomicReference<>(new ArrayList<>());
        // Background execution
        executor.execute(() -> {
            // Perform long-running operation
            ArrayList<FTPFile> result = fetchInBackground(action,remoteDir,credentials);
            // Post-execution (on UI thread)
            handler.post(() -> {
                // Update UI with the result
            });
            data.set(result);
            latch.countDown();
        });
        try{
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted! Use the program on your fear and risk.");
        }
        return data.get();
    }
}
