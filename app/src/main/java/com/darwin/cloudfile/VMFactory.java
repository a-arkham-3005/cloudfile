package com.darwin.cloudfile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class VMFactory implements ViewModelProvider.Factory {
    private final UserRepository rep;
    private final String token;
    public VMFactory(UserRepository ur, String t){
        rep=ur;
        token=t;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
        if (modelClass.isAssignableFrom(VModel.class)) {
            // Instantiate the ViewModel using the collected arguments
            return (T) new VModel(rep, token);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
