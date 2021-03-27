package com.example.sunrise.BeachListActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunrise.DocObjs.Beach;
import com.example.sunrise.BeachInfoAct;
import com.example.sunrise.Utils.GlideApp;
import com.example.sunrise.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class BeachZoneAdapter extends FirestoreRecyclerAdapter<Beach, BeachZoneAdapter.CardViewHolder> {

    private Context activityContext;

    public static class CardViewHolder extends RecyclerView.ViewHolder{

        private static final String FIREBASE_STORAGE_URL = "gs://finalprojectdam.appspot.com/";

        public CardViewHolder(View v) {
            super(v);
        }

        public void setBeach(Beach beach, final Context activityContext){
            // beach name
            ((TextView)itemView.findViewById(R.id.textViewBeachTitle)).setText(beach.getName());

            // beach image
            String imageName = beach.getImgName();
            StorageReference storageReference = FirebaseStorage.getInstance().
                    getReferenceFromUrl(FIREBASE_STORAGE_URL + imageName);

            ImageView imageView = ((ImageView) itemView.findViewById(R.id.imageViewBeach));
            GlideApp.with(imageView.getContext()).load(storageReference).into(imageView);

            // beach information event
            ImageButton beachInfoButton = ((ImageButton) itemView.findViewById(R.id.imageButtonBeachInfo));

            // intent extra info
            GeoPoint geoCoords = beach.getLocation();
            final double latitude = geoCoords.getLatitude();
            final double longitude = geoCoords.getLongitude();
            final String beachName = beach.getName();

            beachInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // intent to beach info activity
                    Intent intent = new Intent(activityContext, BeachInfoAct.class);
                    intent.putExtra("beachName", beachName);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("latitude", latitude);
                    activityContext.startActivity(intent);
                }
            });
        }

    }

    public BeachZoneAdapter(FirestoreRecyclerOptions<Beach> options, Context activityContext){
        super(options);
        this.activityContext = activityContext;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.beach_item, parent, false);
        CardViewHolder holder = new CardViewHolder(rootView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position, Beach beach) {
        holder.setBeach(beach, activityContext);
    }

}
