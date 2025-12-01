package com.darwin.cloudfile;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

public class FileInfoFragment extends Fragment {
    private static final String ARG_PARAM1 = "file";
    private static final String ARG_PARAM2 = "dir";
    private static final String ARG_PARAM3 = "creds";
    private FtpFileItem file;
    private String dir;
    private String[] creds;
    private Activity activity;

    public FileInfoFragment() {
        // Required empty public constructor
    }

    public static FileInfoFragment newInstance(FtpFileItem param1, String param2, String[] param3) {
        FileInfoFragment fragment = new FileInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putStringArray(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            file = (FtpFileItem) getArguments().getSerializable(ARG_PARAM1);
            dir=getArguments().getString(ARG_PARAM2);
            creds=getArguments().getStringArray(ARG_PARAM3);
        }
        OnBackPressedCallback callback=new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        MainFileActivity mfa=(MainFileActivity) getActivity();
        assert mfa != null;
        mfa.callback.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView file_name = getView().findViewById(R.id.filename);
        TextView size = getView().findViewById(R.id.size);
        TextView timestamp = getView().findViewById(R.id.timestamp);
        Button download = getView().findViewById(R.id.download_btn);
        Button move_rename = getView().findViewById(R.id.moveren_btn);
        Button delete = getView().findViewById(R.id.del_btn);
        activity=getActivity();

        file_name.setText(String.format(getResources().getString(R.string.file_name),file.getName()));
        size.setText(String.format(getResources().getString(R.string.file_size),formatFileSize(file.getSize())));
        timestamp.setText(String.format(getResources().getString(R.string.file_timestamp),file.getTimestamp()));
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File downloadDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File targetDl=new File(downloadDir,file.getName());
                Toast.makeText(getActivity(),"Загрузка начата",Toast.LENGTH_SHORT).show();
                boolean success=FtpHandler.executeTask("download",targetDl,dir+file.getName(),creds);
                if(success){
                    Toast.makeText(getActivity(),"Файл успешно загружен",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"Сбой загрузки, проверьте соединение",Toast.LENGTH_SHORT).show();
                }
            }
        });
        move_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle("Перемещение/переименование");
                builder.setMessage("Новый путь к файлу:");
                final EditText input = new EditText(getActivity());
                input.setText(dir+file.getName());
                builder.setView(input);
                builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success=FtpHandler.executeMoveRename("move_rename",dir+file.getName(),input.getText().toString(),creds);
                        if(success){
                            getParentFragmentManager().popBackStack();
                        }else{
                            Toast.makeText(getActivity(),"Ошибка. Проверьте правильность написания путей",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle("Удаление");
                builder.setMessage("Вы уверены?");
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success=FtpHandler.executeTask("delete",null,dir+file.getName(),creds);
                        if(success) {
                            long sizeInput=file.getSize();
                            String request="{\"nick\":\""+creds[0]+"\",\"pars\":\"used\"}";
                            String response=HttpHandler.executeTask(request,"get_data");
                            JsonUsed data=new Gson().fromJson(response,JsonUsed.class);
                            long newUsed=data.getUsed()-sizeInput;
                            request="{\"nick\":\""+creds[0]+"\",\"used\":"+newUsed+"}";
                            HttpHandler.executeTask(request,"update_data");
                            getParentFragmentManager().popBackStack();
                        }else{
                            Toast.makeText(getActivity(),"Ошибка удаления",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    static class JsonUsed{
        long used;

        public long getUsed() {
            return used;
        }
    }
    private String formatFileSize(long size) {
        if (size <= 0) return "0 байт";
        final String[] units = new String[]{"байт", "Кб", "Мб", "Гб", "Тб"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}