package com.example.sunrise;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sunrise.BeachListActivity.BeachList;
import com.example.sunrise.DocObjs.Beach;
import com.example.sunrise.DocObjs.BeachInfo;
import com.example.sunrise.Utils.FailureLog;
import com.example.sunrise.Utils.FirebaseCom;
import com.example.sunrise.Utils.WWOCom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public final static String MAIN_TAG = "main activity";
    private AnimationDrawable animation;
    private ProgressBar progressBar;


    public static ArrayList<String> getDaysMissingInfo(QuerySnapshot queryDocumentSnapshot){
        // create 1 week date list
        ArrayList<String> datesToCheck = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(BeachInfoAct.DATE_FORMAT, Locale.FRENCH);
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++){
            String date = dateFormat.format(calendar.getTime());
            datesToCheck.add(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // get days which information is available
        List<DocumentSnapshot> beachInfoDocs = queryDocumentSnapshot.getDocuments();
        ArrayList<String> datesAvailable = new ArrayList<>();

        for (DocumentSnapshot doc : beachInfoDocs){
            BeachInfo beachInfoObj = doc.toObject(BeachInfo.class);
            datesAvailable.add(beachInfoObj.getDate());
        }

        // check if any daily information is missing (by 1 week)
        ArrayList<String> daysMissing = new ArrayList<>();
        for (String dateNeeded : datesToCheck){
            if (!datesAvailable.contains(dateNeeded)){
                daysMissing.add(dateNeeded);
            }
        }

        return daysMissing;
    }

    public ProgressBar getProgressBar(){
        return progressBar;
    }

    public void checkProgressStatus(){
        if (progressBar.getMax() == progressBar.getProgress()){
            Intent intent = new Intent(this, BeachList.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // start animation
        ImageView imageView = findViewById(R.id.imageViewAnim);
        animation = (AnimationDrawable) imageView.getBackground();
        animation.start();

        // get progress bar
        progressBar = findViewById(R.id.progressBar);

        // get data
        getBeachesInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        animation.stop();
    }

    private void getBeachesInfo(){
        // query to get all beach docs
        Query query = FirebaseFirestore.getInstance().collection(FirebaseCom.BEACH_COLLECTION);

        // execute query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<DocumentSnapshot> beaches = task.getResult().getDocuments();
                    progressBar.setMax(beaches.size());
                    wwoComIfNeeded(beaches);
                }

                else{
                    Toast.makeText(getApplicationContext(), getString(R.string.fbProblem),
                            Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new FailureLog(MAIN_TAG, getString(R.string.fbProblem),
                getString(R.string.fbProblem), getApplicationContext()));
    }

    private void wwoComIfNeeded(List<DocumentSnapshot> beaches){
        // for each document
        for (DocumentSnapshot beachDoc : beaches){

            // convert to a beach object
            Beach beachObj = beachDoc.toObject(Beach.class);
            final GeoPoint geoCoords = beachObj.getLocation();

            // verify if already exist necessary information about the beach
            Query query = FirebaseFirestore.getInstance().collection(FirebaseCom.BEACH_INFO_COLLECTION)
                    .whereEqualTo("location", geoCoords);

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    // doesnt have enough information stored
                    ArrayList<String> missingDates = getDaysMissingInfo(queryDocumentSnapshots);
                    if (missingDates.size() != 0)
                        new WWOCom(geoCoords, getActivityInstance(), missingDates).execute();

                    else {
                        progressBar.incrementProgressBy(1);
                        checkProgressStatus();
                    }
                }
            });
        }
    }

    private MainActivity getActivityInstance(){
        return this;
    }

}
