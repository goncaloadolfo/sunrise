package com.example.sunrise.SuggestionsActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunrise.R;
import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CommentsActivity extends BaseActivity {

    private RecyclerView recyclerViewComments;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);

        recyclerViewComments = findViewById(R.id.recyclerComments);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get information from intent
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", -1);
        double longitude = intent.getDoubleExtra("longitude", -1);
        String context = intent.getStringExtra("context");

        if (context != null) {
            // get comments
            GeoPoint location = new GeoPoint(latitude, longitude);
            Query query = db.collection(FirebaseCom.COMMENTS_COLLECTION)
                    .whereEqualTo("location", location)
                    .whereEqualTo("context", context);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        setupRecycler(task.getResult().getDocuments());
                    }
                }
            });
        }
    }

    private void setupRecycler(List<DocumentSnapshot> comments){

        // sort comments by date
        Collections.sort(comments, new Comparator<DocumentSnapshot>() {
            @Override
            public int compare(DocumentSnapshot o1, DocumentSnapshot o2) {
                String date1 = (String) o1.get("time");
                String date2 = (String) o2.get("time");
                return -date1.compareToIgnoreCase(date2);
            }
        });

        // create manager
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerViewComments.setLayoutManager(layoutManager);
        }

        else{
            GridLayoutManager manager = new GridLayoutManager(this, 2);
            recyclerViewComments.setLayoutManager(manager);
        }

        // specify an adapter (see also next example)
        CommentsAdapter adapter = new CommentsAdapter(comments);
        recyclerViewComments.setAdapter(adapter);
    }
}
