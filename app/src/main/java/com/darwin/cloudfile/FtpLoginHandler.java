package com.darwin.cloudfile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FtpLoginHandler {
    public static SQLiteDatabase tokenBase;
    public static TokenDbHelper tokenHelper;
    public static String token;
    public static String getToken(Context context){
        tokenHelper=new TokenDbHelper(context);
        tokenBase=tokenHelper.getWritableDatabase();
        Cursor crs=null;
        try{
            crs=tokenBase.rawQuery("SELECT * FROM tokens",null);
            if(crs.moveToFirst()){ // if we're authorized
                token=crs.getString(crs.getColumnIndexOrThrow("token"));
                return token;
            } else return "unauthorized";
        }finally{
            if (crs!=null) crs.close();
        }
    }
    public static void logout(){
        Cursor crs=null;
        try{
            crs=tokenBase.rawQuery("SELECT * FROM tokens",null);
            if(crs.moveToFirst()){
                token=crs.getString(crs.getColumnIndexOrThrow("token"));
                HttpHandler.executeTask("{\"token\":\""+token+"\"}","logout");
            }
        }finally{
            if (crs!=null) crs.close();
        }
        tokenBase.execSQL("DELETE FROM tokens");
        token="";
    }

    public static void localLogout(){
        tokenBase.execSQL("DELETE FROM tokens");
        token="";
    }
}