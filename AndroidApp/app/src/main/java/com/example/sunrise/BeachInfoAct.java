package com.example.sunrise;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sunrise.DocObjs.BeachInfo;
import com.example.sunrise.DocObjs.ExperienceComment;
import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BeachInfoAct extends BaseActivity {

    public final static String ACTIVITY_TAG = "BeachInfoAct";
    public final static double COORD_NOT_RECEIVED_CODE = 404;
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public final static long WEEK_MILISECONDS = 518400000;
    public final static long ONE_MILI = 1000;
    public final static int INFO_MAX_DAYS = 7;
    public final static int[] VALID_HOURS = new int[]{6, 9, 12, 15, 18, 21};
    private final static CollectionReference COMMENTS_COLLECTION = FirebaseFirestore.getInstance()
            .collection(FirebaseCom.COMMENTS_COLLECTION);
    private final static CollectionReference BEACH_INFO_REF = FirebaseFirestore.getInstance()
            .collection(FirebaseCom.BEACH_INFO_COLLECTION);

    // query beach info
    private String beachName;
    private GeoPoint geoCoords;
    private String selectedDate;
    private int selectedHour;

    // share comment attbs
    private Spinner spinnerContext;
    private RatingBar ratingBarEvaluation;
    private EditText editTextComment;
    private FirebaseUser loginUser;
    private Query queryPreviousComment;

    // beach information views
    private TextView textViewTemperature;
    private TextView textViewWind;
    private TextView textViewPrecipitation;
    private TextView textViewWaveSize;
    private TextView textViewSwell;
    private TextView textViewWaterTemp;
    private TextView textViewSunrise;
    private TextView textViewSunset;
    private TextView textViewLowTide1;
    private TextView textViewLowTide2;
    private TextView textViewHighTide1;
    private TextView textViewHighTide2;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beach_info);

        // inicialize beach
        beachName = getIntent().getStringExtra("beachName");
        double latitude = getIntent().getDoubleExtra("latitude", COORD_NOT_RECEIVED_CODE);
        double longitude = getIntent().getDoubleExtra("longitude", COORD_NOT_RECEIVED_CODE);
        geoCoords = new GeoPoint(latitude, longitude);
        setNameGeoCoords();

        // set date and time pickers
        setDatePicker();
        setTimePicker();

        // set date and hour saved
        if (savedInstanceState != null){
            selectedDate = savedInstanceState.getString("selectedDate");
            selectedHour = savedInstanceState.getInt("selectedHour");
        }

        // initialize views
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewWind = findViewById(R.id.textViewWind);
        textViewPrecipitation = findViewById(R.id.textViewPrecipitation);
        textViewWaveSize = findViewById(R.id.textViewWaveSize);
        textViewSwell = findViewById(R.id.textViewSwell);
        textViewWaterTemp = findViewById(R.id.textViewWaterTemp);
        textViewSunrise = findViewById(R.id.textViewSunrise);
        textViewSunset = findViewById(R.id.textViewSunset);
        textViewLowTide1 = findViewById(R.id.textViewTide1);
        textViewLowTide2 = findViewById(R.id.textViewTide2);
        textViewHighTide1 = findViewById(R.id.textViewTide3);
        textViewHighTide2 = findViewById(R.id.textViewTide4);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // initialize comment card
        loginUser = FirebaseAuth.getInstance().getCurrentUser();

        // initialize beach information
        updateInfo();

        if (loginUser != null && !beachName.equals(getString(R.string.advancedSearch))) {
            // create query to get previous comments
            queryPreviousComment = COMMENTS_COLLECTION.whereEqualTo("location", geoCoords)
                    .whereEqualTo("email", loginUser.getEmail());

            // initialize views and set card visible
            initializeCommentCard();
            findViewById(R.id.cardViewShareExperience).setVisibility(View.VISIBLE);

            // on submit click event
            ImageButton commentSubmitBtn = findViewById(R.id.imageButtonSubmitComment);
            commentSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCommentToDB();
                }
            });
        }
    }

    private void setNameGeoCoords(){
        // get views
        TextView textViewBeachName = findViewById(R.id.textViewBeachName);
        TextView textViewLong = findViewById(R.id.textViewLongitude);
        TextView textViewLat = findViewById(R.id.textViewLatitude);

        // set values
        String beachNameToDisplay = (beachName == null) ? getString(R.string.advancedSearch) : beachName;
        textViewBeachName.setText(beachNameToDisplay);
        textViewLong.setText(String.format("%.4f", geoCoords.getLongitude()));
        textViewLat.setText(String.format("%.4f", geoCoords.getLatitude()));
    }

    private void setDatePicker(){
        final Calendar calendar = Calendar.getInstance();
        final EditText editTextDate = findViewById(R.id.editTextDate);
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.FRENCH);

        // initial value
        selectedDate = dateFormat.format(calendar.getTime());
        editTextDate.setText(selectedDate);

        // create on date set event
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);

                // update edit text
                selectedDate = dateFormat.format(calendar.getTime());
                editTextDate.setText(selectedDate);

                // update views info
                updateInfo();
            }
        };

        // on click event
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(BeachInfoAct.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));

                // 1 week limits
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - ONE_MILI);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + WEEK_MILISECONDS);

                // show date picker dialog
                datePickerDialog.show();
            }
        });
    }

    private void setTimePicker(){
        final Calendar calendar = Calendar.getInstance();
        final EditText editTextHour = findViewById(R.id.editTextHour);

        //initial value
        selectedHour = getNearestHour(calendar.get(Calendar.HOUR_OF_DAY));
        editTextHour.setText(selectedHour + "h");

        // on click event
        editTextHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create time picker dialog
                new TimePickerDialog(BeachInfoAct.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = getNearestHour(hourOfDay);
                        editTextHour.setText(selectedHour + "h");
                        updateInfo();
                    }
                }, selectedHour, 0, true).show();
            }
        });

    }

    private int getNearestHour(int hour){
        // initialize nearest hour to the first valid hour
        int nearestHour = VALID_HOURS[0];
        int minDiference = Math.abs(hour - nearestHour);

        // search valid hour with min diff
        for (int i = 1; i < VALID_HOURS.length; i++){
            int validHour = VALID_HOURS[i];
            int diff = Math.abs(hour - validHour);

            if (diff <= minDiference){
                nearestHour = validHour;
                minDiference = diff;
            }
        }

        return nearestHour;
    }

    private void initializeCommentCard(){
        // get views
        spinnerContext = findViewById(R.id.spinnerExperienceContext);
        ratingBarEvaluation = findViewById(R.id.ratingBarExperience);
        editTextComment = findViewById(R.id.edit_textComment);

        // create and set adapter
        final String[] contexts = getResources().getStringArray(R.array.beachContext);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.beachContext,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerContext.setAdapter(adapter);

        // on item selected event
        spinnerContext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCommentCard(contexts[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // initialize values
        updateCommentCard(contexts[0]);
    }

    private void updateCommentCard(String context){
        Log.w("context", context);
        // add context field verification to the query
        Query query = queryPreviousComment.whereEqualTo("context", context);

        // execute query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<DocumentSnapshot> docsResult = task.getResult().getDocuments();

                    if (docsResult.size() != 0){
                        // transform result doc to an experience comment obj
                        ExperienceComment comment = getLastComment(docsResult);

                        // get rating and comment
                        float evaluation = comment.getEvaluation();
                        String commentStr = comment.getComment();

                        // update views
                        ratingBarEvaluation.setRating(evaluation);
                        editTextComment.setText(commentStr);
                    }

                    else {
                        ratingBarEvaluation.setRating(0);
                        editTextComment.setText("");
                    }
                }

                else{
                    Log.w(ACTIVITY_TAG, task.getException());
                }
            }
        });
    }

    private void addCommentToDB(){
        String commentStr = editTextComment.getText().toString();

        if (commentStr.trim().length() > 0) {
            // get needed values
            String commentContext = spinnerContext.getSelectedItem().toString();
            float ratingValue = ratingBarEvaluation.getRating();

            // add comment
            FirebaseCom.addComment(geoCoords, commentContext, ratingValue, commentStr,
                    loginUser.getEmail(), getApplicationContext());
        }

        else{
            Toast.makeText(getApplicationContext(), getString(R.string.emptyComment),
                    Toast.LENGTH_LONG).show();
        }
    }

    private ExperienceComment getLastComment(List<DocumentSnapshot> docs){
        // initialize the return value with first doc of the list
        ExperienceComment returnComment = docs.get(0).toObject(ExperienceComment.class);

        // search more recent comment
        for (DocumentSnapshot doc : docs){
            ExperienceComment commentObj = doc.toObject(ExperienceComment.class);

            if (commentObj.getTime().compareToIgnoreCase(returnComment.getTime()) > 0)
                returnComment = commentObj;
        }

        return returnComment;
    }

    private void updateInfo(){
        // create query
        Query query = BEACH_INFO_REF.whereEqualTo("date", selectedDate).
                whereEqualTo("hour", selectedHour + "00").whereEqualTo("location", geoCoords);

        // execute query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() != 0){

                    // parse document to BeachInfo object
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    BeachInfo beachInfoObj = doc.toObject(BeachInfo.class);

                    // set information to the views
                    textViewTemperature.setText(beachInfoObj.getTemperature() + "ºC");
                    textViewWind.setText(beachInfoObj.getWind());
                    textViewPrecipitation.setText(beachInfoObj.getPrecipitation() + "mm");
                    textViewWaveSize.setText(beachInfoObj.getWaveSize() + "m");
                    textViewSwell.setText(beachInfoObj.getSwell());
                    textViewWaterTemp.setText(beachInfoObj.getWaterTemperature() + "ºC");
                    textViewSunrise.setText(beachInfoObj.getSunrise());
                    textViewSunset.setText(beachInfoObj.getSunset());

                    // set tides info
                    setTides(beachInfoObj);
                }
            }
        });
    }

    private void setTides(BeachInfo beachInfo){
        List<String> lowTides = beachInfo.getLowTides();
        List<String> highTides = beachInfo.getHighTides();

        // set low tides
        textViewLowTide1.setText(lowTides.get(0));

        if (lowTides.size() > 1)
            textViewLowTide2.setText(lowTides.get(1));

        else
            textViewLowTide2.setText(getString(R.string.notDefined));

        // set high tides
        textViewHighTide1.setText(highTides.get(0));

        if (highTides.size() > 1)
            textViewHighTide2.setText(highTides.get(1));

        else
            textViewHighTide2.setText(getString(R.string.notDefined));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedDate", selectedDate);
        outState.putInt("selectedHour", selectedHour);
    }
}
