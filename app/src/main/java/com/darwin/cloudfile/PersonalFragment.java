package com.darwin.cloudfile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Objects;

public class PersonalFragment extends Fragment {
    private VModel model;
    private String nickname;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainFileActivity mfa=(MainFileActivity) getActivity();
        assert mfa != null;
        mfa.callback.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String token=FtpLoginHandler.getToken(getActivity());
        UserRepository repository=new UserRepository();
        VMFactory factory=new VMFactory(repository,token);
        assert getActivity() != null;
        model=new ViewModelProvider(getActivity(),factory).get(VModel.class);
        model.getData().observe(getActivity(),data->{
            nickname=data.getCredentials_for_ftp()[0];
        });
        String request="{\"nick\":\""+nickname+"\",\"pars\":\"fullname,used,quota\"}";
        String response=HttpHandler.executeTask(request,"get_data");
        JsonData data=new Gson().fromJson(response, JsonData.class);
        TextView tvFull=getView().findViewById(R.id.full_name_label);
        tvFull.setText(String.format(getResources().getString(R.string.first_last_name_label),data.getFullname()));
        TextView tvNick=getView().findViewById(R.id.nickname_label);
        tvNick.setText(String.format(getResources().getString(R.string.nickname_label),nickname));
        TextView tvQuota=getView().findViewById(R.id.quota_label);
        tvQuota.setText(String.format(getResources().getString(R.string.quota_label),formatFileSize(data.getQuota())));
        TextView tvUsed=getView().findViewById(R.id.used_label);
        tvUsed.setText(String.format(getResources().getString(R.string.used_label),formatFileSize(data.getUsed())));

        Button btn1=getView().findViewById(R.id.quota_button);
        Button btn2=getView().findViewById(R.id.change_data_btn);
        Button btn3=getView().findViewById(R.id.logout);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction=getParentFragmentManager().beginTransaction();
                QuotaFragment qf=QuotaFragment.newInstance(nickname);
                transaction.replace(R.id.fragCont,qf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction=getParentFragmentManager().beginTransaction();
                ChangeDataFragment cdf=ChangeDataFragment.newInstance(nickname);
                transaction.replace(R.id.fragCont,cdf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FtpLoginHandler.logout();
                assert getActivity() != null;
                getActivity().finish();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 байт";
        final String[] units = new String[]{"байт", "Кб", "Мб", "Гб", "Тб"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    static class JsonData{
        String fullname;
        long quota;
        long used;

        public String getFullname() {
            return fullname;
        }

        public long getUsed() {
            return used;
        }

        public long getQuota() {
            return quota;
        }
    }
}