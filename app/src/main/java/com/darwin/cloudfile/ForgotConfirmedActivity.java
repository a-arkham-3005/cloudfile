package com.darwin.cloudfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class ForgotConfirmedActivity extends AppCompatActivity {
    EditText pass;
    EditText conf;
    String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_confirmed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pass=findViewById(R.id.newpass_f);
        conf=findViewById(R.id.confirm_f);
        Intent intent=getIntent();
        nickname=intent.getStringExtra("nickname");
    }
    public void clickConfirm(View v){
        String password=pass.getText().toString();
        String confirmation=conf.getText().toString();
        if(password.isEmpty() || confirmation.isEmpty()){
            new ErrorDialog().errorDialogCreate(this,R.string.fill_all_fields).show();
        }else if(!password.equals(confirmation)){
            new ErrorDialog().errorDialogCreate(this,R.string.pass_not_match).show();
        }else{
            String postData="{\"nick\":\""+nickname+"\",\"pass\":\""+password+"\"}";
            String response=HttpHandler.executeTask(postData,"resetpass");
            if(Objects.equals(response, "OK")){
                AlertDialog.Builder bld=new AlertDialog.Builder(this);
                bld.setCancelable(true).setTitle(R.string.info_dialogbox_title).setMessage(R.string.pass_success_reset);
                bld.setPositiveButton("OK", this::dialogFinish);
                bld.create().show();
            }else{
                new ErrorDialog().errorDialogCreate(this,response).show();
            }
        }
    }

    private void dialogFinish(DialogInterface dialogInterface, int i) {
        finish();
    }
}