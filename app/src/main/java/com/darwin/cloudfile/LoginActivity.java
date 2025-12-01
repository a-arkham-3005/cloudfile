package com.darwin.cloudfile;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.database.sqlite.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.net.ftp.FTPFile;
import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText nickname;
    EditText pass;
    String nick;
    String password;
    Activity link;
    String token;
    SQLiteDatabase tokenBase;
    TokenDbHelper tokenHelper;
    VModel viewModel;
    String[] ftpLogin;
    String currentDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        link=this;
        nickname=findViewById(R.id.nickField);
        pass=findViewById(R.id.passField);
        tokenHelper=new TokenDbHelper(this);
        tokenBase=tokenHelper.getWritableDatabase();
    }

    public void loginClick(View v){
        nick=nickname.getText().toString();
        password=pass.getText().toString();
        String postData = "{\"nick\": \""+nick+"\",\"pass\": \""+password+"\"}";
        // send login data to backend, which will generate login token and send it back, or send back error message
        // trying to replace deprecated AsyncTask with a newer background task thing
        token=HttpHandler.executeTask(postData,"login");
        // if successful. write token down to SQLite and sign us in
        if(!Objects.equals(token, "debug_error") && !Objects.equals(token, "incorrect_data")){
            // do the job
            // Toast.makeText(this, token, Toast.LENGTH_LONG).show();
            ContentValues cv=new ContentValues();
            cv.put(TokenDbHelper.COLUMN_NAME,token);
            tokenBase.insert(TokenDbHelper.TABLE_NAME,null,cv);
            // restart activity to drop the user to files activity, or get an error if not yet approved
            recreate();
        } else if (token.equals("incorrect_data")) {
            // otherwise throw a login error in dialog box
            new ErrorDialog().errorDialogCreate(link,R.string.login_err).show();
        } else {
            new ErrorDialog().errorDialogCreate(link,token).show();
        }
    }

    public void regClick(View v){
        // redirect us to register form on top of login screen
        Intent reg=new Intent(this, RegActivity.class);
        startActivity(reg);
    }

    public void forgotClick(View v){
        // redirect to forgot password form
        Intent forgot=new Intent(this, ForgotActivity.class);
        startActivity(forgot);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String token=FtpLoginHandler.getToken(this);
        if(!Objects.equals(token, "unauthorized")){
            UserRepository repository=new UserRepository();
            VMFactory factory=new VMFactory(repository,token);
            viewModel=new ViewModelProvider(this,factory).get(VModel.class);
            viewModel.getData().observe(this, transferrableData -> {
                String error=transferrableData.getError_msg();
                if(!error.isEmpty()){
                    switch(error){
                        case "registration_not_yet_approved":{
                            new ErrorDialog().errorDialogCreate(this,R.string.not_confirmed_err).show();
                            break;
                        }
                        case "user_removed":{
                            new ErrorDialog().errorDialogCreate(this,R.string.profile_deleted).show();
                            FtpLoginHandler.localLogout();
                            break;
                        }
                        case "token_expired":{
                            new ErrorDialog().errorDialogCreate(this,R.string.expired).show();
                            FtpLoginHandler.localLogout();
                            break;
                        }
                        default:{
                            new ErrorDialog().errorDialogCreate(this,error).show();
                        }
                    }
                }else{
                    ftpLogin=transferrableData.getCredentials_for_ftp();
                    currentDir=transferrableData.getCurrent_dir();
                    ArrayList<FTPFile> tmp=FtpHandler.executeFetch("fetch_dir",currentDir,ftpLogin);
                    if(!tmp.isEmpty()&&Objects.equals(tmp.get(0).getName(), "<ERROR>")){
                        new ErrorDialog().errorDialogCreate(this,"Сбой FTP-сервера, повторите попытку позже").show();
                    }else{
                        Intent main=new Intent(this,MainFileActivity.class);
                        startActivity(main);
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tokenHelper.close();
    }
}
