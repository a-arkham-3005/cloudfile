package com.darwin.cloudfile;

import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;

public class FtpFileItem implements Serializable {
    private FTPFile ftpFile;
    private String name;
    private boolean isDirectory;
    private long size;
    private String timestamp;

    public FtpFileItem(FTPFile ftpFile) {
        this.ftpFile = ftpFile;
        this.name = ftpFile.getName();
        this.isDirectory = ftpFile.isDirectory();
        this.size = ftpFile.getSize();
        this.timestamp = ftpFile.getTimestamp() != null ?
                ftpFile.getTimestamp().getTime().toString() : "неизв.";
    }

    // Getters
    public FTPFile getFtpFile() { return ftpFile; }
    public String getName() { return name; }
    public boolean isDirectory() { return isDirectory; }
    public long getSize() { return size; }
    public String getTimestamp() { return timestamp; }
}