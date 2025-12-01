package com.darwin.cloudfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class RegActivity extends AppCompatActivity {

    EditText name_surname, nickname_r, pass_r, confirm_r, answer_r;
    String fullname, nickname, password, confirm, answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        name_surname=findViewById(R.id.name_surname_r);
        nickname_r=findViewById(R.id.nickname_r);
        pass_r=findViewById(R.id.pass_r);
        confirm_r=findViewById(R.id.confirm_r);
        answer_r=findViewById(R.id.answer_r);
    }

    public void reg(View v){
        fullname=name_surname.getText().toString();
        nickname=nickname_r.getText().toString();
        password=pass_r.getText().toString();
        confirm=confirm_r.getText().toString();
        answer=answer_r.getText().toString();
        if(Objects.equals(fullname, "") || Objects.equals(nickname, "") || Objects.equals(password, "") || Objects.equals(confirm, "") || Objects.equals(answer, "")){
            // not all fields filled - show error
            new ErrorDialog().errorDialogCreate(this,R.string.fill_all_fields).show();
        } else if(!confirm.equals(password)){
            // passwords mismatch - show error
            new ErrorDialog().errorDialogCreate(this,R.string.pass_not_match).show();
        } else {
            // otherwise send data to backend and get response - OK or error
            String postData = "{\"fullname\": \""+fullname+"\",\"nick\": \""+nickname+"\",\"pass\":\""+password+"\",\"answer\":\""+answer+"\"}";
            String result=HttpHandler.executeTask(postData,"reg");
            if(Objects.equals(result, "nickname_taken")){
                new ErrorDialog().errorDialogCreate(this,R.string.nickname_taken).show();
            } else if(Objects.equals(result, "OK")){
                // new InfoDialog().errorDialogCreate(this,R.string.reg_success).show();
                AlertDialog.Builder bld=new AlertDialog.Builder(this);
                bld.setCancelable(true).setTitle(R.string.info_dialogbox_title).setMessage(R.string.reg_success);
                bld.setPositiveButton("OK", this::dialogFinish);
                bld.create().show();
            } else {
                new ErrorDialog().errorDialogCreate(this,result).show();
            }
        }
    }
    public void dialogFinish(DialogInterface dialog, int which){
        finish();
    }
}