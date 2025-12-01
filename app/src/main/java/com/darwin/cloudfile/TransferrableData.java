package com.darwin.cloudfile;

public class TransferrableData {
    private String[] credentials_for_ftp;
    private String current_dir;
    private String error_msg;
    public TransferrableData(String[] creds, String dir, String error){
        credentials_for_ftp=creds;
        current_dir=dir;
        error_msg=error;
    }

    public String getCurrent_dir() {
        return current_dir;
    }

    public String getError_msg() {
        return error_msg;
    }

    public String[] getCredentials_for_ftp() {
        return credentials_for_ftp;
    }

    public void setCurrent_dir(String current_dir) {
        this.current_dir = current_dir;
    }
}
