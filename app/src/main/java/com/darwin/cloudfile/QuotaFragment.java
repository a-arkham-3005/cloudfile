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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

public class QuotaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1; // nickname
    private Activity act;

    public QuotaFragment() {
        // Required empty public constructor
    }

    public static QuotaFragment newInstance(String param1) {
        QuotaFragment fragment = new QuotaFragment();
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
        return inflater.inflate(R.layout.fragment_quota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        act=getActivity();
        String request="{\"nick\":\""+mParam1+"\",\"pars\":\"quota\"}";
        String response=HttpHandler.executeTask(request,"get_data");
        JsonQuota data=new Gson().fromJson(response, JsonQuota.class);
        long quota=data.getQuota();
        assert getView() != null;
        if(quota==4294967296L){
            RadioButton toDisable=getView().findViewById(R.id.plan1);
            toDisable.setEnabled(false);
            RadioButton toCheck=getView().findViewById(R.id.plan2);
            toCheck.setChecked(true);
        }else if(quota==8589934592L){
            RadioButton toDisable=getView().findViewById(R.id.plan1);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan2);
            toDisable.setEnabled(false);
            RadioButton toCheck=getView().findViewById(R.id.plan3);
            toCheck.setChecked(true);
        }else if(quota==17179869184L){
            RadioButton toDisable=getView().findViewById(R.id.plan1);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan2);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan3);
            toDisable.setEnabled(false);
            RadioButton toCheck=getView().findViewById(R.id.plan4);
            toCheck.setChecked(true);
        }else if(quota==34359738368L){
            RadioButton toDisable=getView().findViewById(R.id.plan1);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan2);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan3);
            toDisable.setEnabled(false);
            toDisable=getView().findViewById(R.id.plan4);
            toDisable.setEnabled(false);
        }else{
            RadioButton toCheck=getView().findViewById(R.id.plan1);
            toCheck.setChecked(true);
        }
        Button buybtn=getView().findViewById(R.id.buybtn);
        buybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getView() != null;
                RadioGroup rg=getView().findViewById(R.id.radioGroup);
                AlertDialog.Builder builder=new AlertDialog.Builder(act);
                builder.setTitle("Примечание");
                builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                if(rg.getCheckedRadioButtonId()!=-1){
                    if(rg.getCheckedRadioButtonId()==R.id.plan1){
                        builder.setMessage("Чтобы купить подписку, переведите 200 руб. администратору сервера. За реквизитами обращайтесь по e-mail. В переводе укажите никнейм. После продления плана мы вам сообщим по почте. Не забывайте платить каждый месяц, в противном случае квота будет сброшена до 2 Гб, а если будет зафиксировано превышение, мы вам объявим о 7-дневном сроке до удаления больших файлов с хранилища.");
                    }else if(rg.getCheckedRadioButtonId()==R.id.plan2){
                        builder.setMessage("Чтобы купить подписку, переведите 500 руб. администратору сервера. За реквизитами обращайтесь по e-mail. В переводе укажите никнейм. После продления плана мы вам сообщим по почте. Не забывайте платить каждый месяц, в противном случае квота будет сброшена до 2 Гб, а если будет зафиксировано превышение, мы вам объявим о 7-дневном сроке до удаления больших файлов с хранилища.");
                    }else if(rg.getCheckedRadioButtonId()==R.id.plan3){
                        builder.setMessage("Чтобы купить подписку, переведите 1300 руб. администратору сервера. За реквизитами обращайтесь по e-mail. В переводе укажите никнейм. После продления плана мы вам сообщим по почте. Не забывайте платить каждый месяц, в противном случае квота будет сброшена до 2 Гб, а если будет зафиксировано превышение, мы вам объявим о 7-дневном сроке до удаления больших файлов с хранилища.");
                    }else{
                        builder.setMessage("Чтобы купить подписку, переведите 3000 руб. администратору сервера. За реквизитами обращайтесь по e-mail. В переводе укажите никнейм. После продления плана мы вам сообщим по почте. Не забывайте платить каждый месяц, в противном случае квота будет сброшена до 2 Гб, а если будет зафиксировано превышение, мы вам объявим о 7-дневном сроке до удаления больших файлов с хранилища.");
                    }
                }else{
                    builder.setMessage("У вас уже активирован план на максимальную квоту.");
                }
                builder.create().show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    static class JsonQuota{
        long quota;

        public long getQuota() {
            return quota;
        }
    }
}