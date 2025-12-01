package com.darwin.cloudfile;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

public class ChangeDataFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private EditText fullname;
    private Activity act;

    public ChangeDataFragment() {
        // Required empty public constructor
    }

    public static ChangeDataFragment newInstance(String param1) {
        ChangeDataFragment fragment = new ChangeDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
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
        return inflater.inflate(R.layout.fragment_change_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fullname=getView().findViewById(R.id.fullname2);
        Button savebtn=getView().findViewById(R.id.save);
        act=getActivity();
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_fn=fullname.getText().toString();
                if(new_fn.isEmpty()){
                    AlertDialog.Builder builder=showDialog("Ошибка",R.string.fill_all_fields);
                    builder.create().show();
                }else{
                    String request="{\"nick\":\""+mParam1+"\",\"fullname\":\""+new_fn+"\"}";
                    HttpHandler.executeTask(request,"update_data");
                    AlertDialog.Builder builder=new AlertDialog.Builder(act);
                    builder.setTitle("Примечание");
                    builder.setMessage(R.string.success_save);
                    builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getParentFragmentManager().popBackStack();
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private AlertDialog.Builder showDialog(String title, int message) {
        AlertDialog.Builder builder=new AlertDialog.Builder(act);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder;
    }
}