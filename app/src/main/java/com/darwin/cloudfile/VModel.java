package com.darwin.cloudfile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VModel extends ViewModel {
    private UserRepository rep;
    private MutableLiveData<TransferrableData> data;
    public VModel(UserRepository repos, String token){
        rep=repos;
        data=(MutableLiveData<TransferrableData>) rep.getData(token);
    }

    public LiveData<TransferrableData> getData() {
        return data;
    }
}