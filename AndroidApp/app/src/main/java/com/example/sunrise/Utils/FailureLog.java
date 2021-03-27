package com.example.sunrise.Utils;

import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;

public class FailureLog implements OnFailureListener {

    // debug
    private String tag;
    private String msg;

    // user interaction
    private String toastMsg;
    private Context context;


    public FailureLog(String tag, String msg, String toastMsg, Context context){
        this.tag = tag;
        this.msg = msg;
        this.toastMsg = toastMsg;
        this.context = context;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.w(tag, msg);

        if (toastMsg != null)
            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
    }
}
