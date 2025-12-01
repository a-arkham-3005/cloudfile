package com.darwin.cloudfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ErrorDialog {
    public AlertDialog errorDialogCreate(Context act, int errorMessageId){
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.error_dialogbox_title);
        builder.setMessage(errorMessageId);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        return builder.create();
    }
    public AlertDialog errorDialogCreate(Context act, String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.error_dialogbox_title);
        builder.setMessage(errorMessage);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        return builder.create();
    }
}
