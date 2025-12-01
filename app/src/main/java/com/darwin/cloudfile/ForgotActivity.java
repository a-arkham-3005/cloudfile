package com.darwin.cloudfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotActivity extends AppCompatActivity {

    EditText nickname;
    EditText answer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        nickname=findViewById(R.id.nickname_f);
        answer=findViewById(R.id.answer_f);
    }
    public void click(View v){
        String nick=nickname.getText().toString();
        String anw=answer.getText().toString();
        if(nick.isEmpty() || anw.isEmpty()){
            new ErrorDialog().errorDialogCreate(this,R.string.fill_all_fields).show();
        }else{
            String postData="{\"nick\":\""+nick+"\",\"answer\":\""+anw+"\"}";
            String confirmed=HttpHandler.executeTask(postData,"forgot");
            switch(confirmed){
                case "incorrect_data":{
                    new ErrorDialog().errorDialogCreate(this,R.string.forgot_err).show();
                    break;
                }
                case "OK":{
                    Intent nextPage=new Intent(this, ForgotConfirmedActivity.class);
                    nextPage.putExtra("nickname",nick);
                    startActivity(nextPage);
                    finish();
                    break;
                }
                default:{
                    new ErrorDialog().errorDialogCreate(this,confirmed).show();
                }
            }
        }
    }
}