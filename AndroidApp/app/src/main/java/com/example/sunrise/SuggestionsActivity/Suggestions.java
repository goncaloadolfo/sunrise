package com.example.sunrise.SuggestionsActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunrise.DocObjs.Beach;
import com.example.sunrise.R;
import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class Suggestions extends BaseActivity {

    private String selectedContext;
    private BeachSuggestionsAdapter suggestionsAdapter;
    private RecyclerView beachesRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestions);

        setupSpinner();
        setupRecyclerView(savedInstanceState);
    }

    private void setupSpinner(){

        // get spinner and contexts
        Spinner spinner = ((Spinner) findViewById(R.id.spinnerSuggestions));
        final String[] contexts = getResources().getStringArray(R.array.beachContext);
        selectedContext = contexts[0];

        // create and set adapter
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.beachContext,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // on item selected event
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedContext = contexts[position];
                setupRecyclerView(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupRecyclerView(Bundle savedInstanceState){

        // stop older adapter if exists
        if (suggestionsAdapter != null)
            suggestionsAdapter.stopListening();

        // get recycler view
        beachesRecyclerView = findViewById(R.id.recyclerViewSuggestions);

        // create manager
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
            beachesRecyclerView.setLayoutManager(manager);
        }

        else{
            GridLayoutManager manager = new GridLayoutManager(this, 2);
            beachesRecyclerView.setLayoutManager(manager);
        }

        // create query options
        Query query = FirebaseFirestore.getInstance().collection(FirebaseCom.BEACH_COLLECTION);
        FirestoreRecyclerOptions<Beach> options = new FirestoreRecyclerOptions.Builder<Beach>().
                setQuery(query, Beach.class).build();

        // set content adapter
        suggestionsAdapter = new BeachSuggestionsAdapter(options, selectedContext);
        beachesRecyclerView.setAdapter(suggestionsAdapter);

        // start listening
        suggestionsAdapter.startListening();

        // set saved position
        if (savedInstanceState != null) {
            final int lastPos = savedInstanceState.getInt("scrollPos");

            if (lastPos != 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.w("position", lastPos + "");
                        beachesRecyclerView.scrollToPosition(lastPos + 1);
                    }
                }, 200);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (suggestionsAdapter != null)
            suggestionsAdapter.stopListening();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // get first visible item position
        int pos;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayoutManager manager = (LinearLayoutManager) beachesRecyclerView.getLayoutManager();
            pos = manager.findFirstVisibleItemPosition();
        }

        else{
            GridLayoutManager manager = (GridLayoutManager) beachesRecyclerView.getLayoutManager();
            pos = manager.findFirstVisibleItemPosition();
        }

        outState.putInt("scrollPos", pos);
    }
}
