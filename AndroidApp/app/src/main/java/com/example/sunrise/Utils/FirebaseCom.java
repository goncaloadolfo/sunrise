package com.example.sunrise.Utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sunrise.BeachInfoAct;
import com.example.sunrise.DocObjs.BeachInfo;
import com.example.sunrise.DocObjs.ExperienceComment;
import com.example.sunrise.MainActivity;
import com.example.sunrise.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public final class FirebaseCom {

    private static final String FIREBASE_STORAGE_URL = "gs://finalprojectdam.appspot.com/";

    // log tag
    public static final String FIRESTORE_TAG = "firestore";

    // collections
    public static final String USERS_COLLECTION = "USER";
    public static final String COMMENTS_COLLECTION = "COMMENTS";
    public static final String BEACH_COLLECTION = "BEACH";
    public static final String BEACH_INFO_COLLECTION = "BEACH_INFO";

    // log msg
    public static final String ADD_USER_FAILURE = "problems adding an user";
    public static final String ADD_COMMENT_FAILURE = "problems adding a comment";

    //others
    public static final String DEFAULT_PROFILEPIC_NAME = "DefaultProfilePic.png";


    public static void addUser(String email, String firstName, String lastName, String homeTown){
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("homeTown", homeTown);
        userMap.put("profilePicName", DEFAULT_PROFILEPIC_NAME);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION).add(userMap).
                addOnFailureListener( new FailureLog(FIRESTORE_TAG, ADD_USER_FAILURE, null, null));
    }

    public static void addComment(GeoPoint location, String context, float evaluation, String comment,
                                  String email, Context activityContext){
        ExperienceComment commentObj = new ExperienceComment(location, context, evaluation, comment,
                email);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COMMENTS_COLLECTION).add(commentObj).addOnFailureListener(
                new FailureLog(FIRESTORE_TAG, ADD_COMMENT_FAILURE,
                        activityContext.getString(R.string.commentProblem), activityContext))
                .addOnSuccessListener(new SuccessLog(activityContext.getString(R.string.commentSuccess)
                        , activityContext));
    }

    public static void addBeachInfo(BeachInfo[] info, final AppCompatActivity activity){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int i = 0; i < info.length - 1; i++){
            BeachInfo beachInfo = info[i];

            // get coord/date/hour of the beach information
            GeoPoint location = beachInfo.getLocation();
            String date = beachInfo.getDate();
            String hour = beachInfo.getHour();

            // add beach info
            db.collection(BEACH_INFO_COLLECTION).add(beachInfo);
        }

        // add last beach info with on complete event
        final BeachInfo lastBeachInfo = info[info.length - 1];
        db.collection(BEACH_INFO_COLLECTION).add(lastBeachInfo).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.getProgressBar().incrementProgressBy(1);
                    mainActivity.checkProgressStatus();
                }

                else{
                    // get geo coord
                    GeoPoint location = lastBeachInfo.getLocation();

                    // create and start intent
                    Intent intent = new Intent(activity, BeachInfoAct.class);
                    intent.putExtra("beachName", activity.getString(R.string.advancedSearch));
                    intent.putExtra("longitude", location.getLongitude());
                    intent.putExtra("latitude", location.getLatitude());
                    activity.startActivity(intent);
                }
            }
        });
    }

    public static void changeProfilePic(String email, final String newProfilePic) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get document of the user
        Query query = db.collection(USERS_COLLECTION).whereEqualTo("email", email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    String id = doc.getId();
                    String previousProfilePic = doc.getString("profilePicName");

                    // update profile pic att
                    db.collection(USERS_COLLECTION).document(id).update("profilePicName", newProfilePic);

                    // delete previous image if it isnt default pic
                    if (!previousProfilePic.equals("DefaultProfilePic.png")) {
                        StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(FIREBASE_STORAGE_URL)
                                .child(previousProfilePic);
                        fileRef.delete();
                    }
                }
            }
        });
    }

}
