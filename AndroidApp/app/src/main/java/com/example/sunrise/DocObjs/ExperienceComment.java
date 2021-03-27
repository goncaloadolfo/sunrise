package com.example.sunrise.DocObjs;

import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ExperienceComment {

    public static final String COMMENT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private GeoPoint location;
    private String context;
    private float evaluation;
    private String comment;
    private String email;
    private String time;


    public ExperienceComment(){}

    public ExperienceComment(GeoPoint location, String context, float evaluation, String comment,
                             String email){
        this.location = location;
        this.context = context;
        this.evaluation = evaluation;
        this.comment = comment;
        this.email = email;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(COMMENT_DATE_FORMAT, Locale.FRENCH);
        this.time = dateFormat.format(calendar.getTime());
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getContext() {
        return context;
    }

    public float getEvaluation() {
        return evaluation;
    }

    public String getComment() {
        return comment;
    }

    public String getEmail() {
        return email;
    }

    public String getTime() {
        return time;
    }
}
