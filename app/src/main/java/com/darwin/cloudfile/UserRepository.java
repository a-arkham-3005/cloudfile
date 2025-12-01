package com.darwin.cloudfile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

public class UserRepository{
    public LiveData<TransferrableData> getData(String token){
        MutableLiveData<TransferrableData> liveData=new MutableLiveData<>();
        String resp=HttpHandler.executeTask("{\"token\":\""+token+"\"}","check_token");
        if(resp.charAt(0) != '{'){ // if an error has occurred while fetching credentials
            liveData.setValue(new TransferrableData(null,"",resp));
        }else{
            UserRepository.JsonTokenResponse fullResponse=new Gson().fromJson(resp,UserRepository.JsonTokenResponse.class);
            String[] logins=new String[2];
            logins[0]=fullResponse.getNick();
            logins[1]=fullResponse.getPass();
            liveData.setValue(new TransferrableData(logins,"/",""));
        }
        return liveData;
    }
    static class JsonTokenResponse{
        String nick;
        String pass;

        public String getNick() {
            return nick;
        }
        public String getPass() {
            return pass;
        }
    }
}
