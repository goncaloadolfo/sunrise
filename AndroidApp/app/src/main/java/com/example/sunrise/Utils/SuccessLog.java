package com.example.sunrise.Utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

public class SuccessLog implements OnSuccessListener {

    private String toastMsg;
    private Context context;


    public SuccessLog(String toastMsg, Context context){
        this.toastMsg = toastMsg;
        this.context = context;
    }

    @Override
    public void onSuccess(Object o) {
        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
    }
}
