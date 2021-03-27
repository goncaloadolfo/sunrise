package com.example.sunrise.BeachListActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sunrise.DocObjs.Beach;
import com.example.sunrise.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ZoneFragment extends Fragment {

    public static final String BEACH_COLLECTION = "BEACH";
    public static final String BEACH_ZONE_ATR = "locationZone";
    private String zone;
    private BeachZoneAdapter adapter;
    private RecyclerView beachesRecyclerView;

    public ZoneFragment(String zone){
        this.zone = zone;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beaches_zone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // get recycler view
        beachesRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // create manager
        RecyclerView.LayoutManager manager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        else
            manager = new LinearLayoutManager(getActivity());

        beachesRecyclerView.setLayoutManager(manager);

        // create query options
        Query query = FirebaseFirestore.getInstance().collection(BEACH_COLLECTION).
                whereEqualTo(BEACH_ZONE_ATR, zone);
        FirestoreRecyclerOptions<Beach> options = new FirestoreRecyclerOptions.Builder<Beach>().
                setQuery(query, Beach.class).build();

        // set content adapter
        adapter = new BeachZoneAdapter(options, getContext());
        beachesRecyclerView.setAdapter(adapter);

        // set saved position
        if (savedInstanceState != null) {
            final int lastPos = savedInstanceState.getInt("scrollPos");

            if (lastPos != 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        beachesRecyclerView.scrollToPosition(lastPos);
                    }
                }, 200);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // get first visible item position
        LinearLayoutManager manager = (LinearLayoutManager) beachesRecyclerView.getLayoutManager();
        int pos = manager.findFirstVisibleItemPosition();
        outState.putInt("scrollPos", pos);
    }
}
