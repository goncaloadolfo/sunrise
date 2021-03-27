package com.example.sunrise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.example.sunrise.Utils.WWOCom;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class AdvancedSearch extends BaseActivity implements OnMapReadyCallback {

    private MapView mapView;
    private LatLng selectedPoint;
    private GoogleMap googleMap;
    private float zoomLevelState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_search);

        // get map view
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // get selected location
        if (savedInstanceState != null) {
            selectedPoint = new LatLng(savedInstanceState.getDouble("latitude", 400), savedInstanceState.getDouble("longitude", 400));
            selectedPoint = (selectedPoint.latitude == 400) ? null : selectedPoint;
            zoomLevelState = savedInstanceState.getFloat("zoomLevel");
        }

        // set on click listener to search btn
        final Button searchButton = ((Button) findViewById(R.id.buttonSearch));
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedPoint != null) {
                    // event detection feedback
                    Toast.makeText(getActivityInstance(), getString(R.string.processing), Toast.LENGTH_LONG).show();

                    // get beach info available for the selected location
                    final GeoPoint geoPoint = new GeoPoint(selectedPoint.latitude, selectedPoint.longitude);
                    Query query = FirebaseFirestore.getInstance().collection(FirebaseCom.BEACH_INFO_COLLECTION)
                            .whereEqualTo("location", geoPoint);
                    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ArrayList<String> daysMissing = MainActivity.getDaysMissingInfo(queryDocumentSnapshots);
                            if (daysMissing.size() != 0)
                                new WWOCom(geoPoint, getActivityInstance(), daysMissing).execute();

                            else {
                                Intent intent = new Intent(getActivityInstance(), BeachInfoAct.class);
                                intent.putExtra("beachName", getString(R.string.advancedSearch));
                                intent.putExtra("longitude", geoPoint.getLongitude());
                                intent.putExtra("latitude", geoPoint.getLatitude());
                                startActivity(intent);
                            }
                        }
                    });
                }

                else{
                    Toast.makeText(getActivityInstance(), getString(R.string.selectLocation), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        // set last marker
        if (selectedPoint != null){
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(selectedPoint));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPoint, zoomLevelState));
        }

        // set initial coords and move camera
        else{
            LatLng initialLocation = new LatLng(0, 0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation));
        }

        // set on map click listener to add marker
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedPoint = latLng;
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(selectedPoint));
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(selectedPoint));
            }
        });
    }

    private AdvancedSearch getActivityInstance(){
        return this;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedPoint != null) {
            outState.putDouble("latitude", selectedPoint.latitude);
            outState.putDouble("longitude", selectedPoint.longitude);
        }

        outState.putFloat("zoomLevel", googleMap.getCameraPosition().zoom);
    }
}
