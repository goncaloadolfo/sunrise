package com.example.sunrise.SuggestionsActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunrise.DocObjs.Beach;
import com.example.sunrise.DocObjs.ExperienceComment;
import com.example.sunrise.R;
import com.example.sunrise.Utils.FirebaseCom;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class BeachSuggestionsAdapter extends FirestoreRecyclerAdapter<Beach, BeachSuggestionsAdapter.Holder> {

    private String context;

    public class Holder extends RecyclerView.ViewHolder{

        public Holder(View v){
            super(v);
        }

        public void setCardValues(Beach beach){

            // get views
            TextView beachName = itemView.findViewById(R.id.textViewSuggestionsBeachName);
            final TextView nrVotes = itemView.findViewById(R.id.textViewNrVotes);
            final RatingBar rating = itemView.findViewById(R.id.ratingBarSuggestion);
            final Button detailsBtn = itemView.findViewById(R.id.buttonDetails);

            // set beach name
            beachName.setText(beach.getName());

            // set rating
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collection(FirebaseCom.COMMENTS_COLLECTION)
                    .whereEqualTo("location", beach.getLocation())
                    .whereEqualTo("context", context);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        final ArrayList<DocumentSnapshot> docs = (ArrayList<DocumentSnapshot>) task.getResult().getDocuments();
                        float avrRating = calcAverageRating(docs);
                        int nrVotesInt = docs.size();

                        nrVotes.setText(String.valueOf(nrVotesInt));
                        rating.setRating(avrRating);

                        // set details event click
                        detailsBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);

                                if (docs.size() > 0) {
                                    ExperienceComment comment = docs.get(0).toObject(ExperienceComment.class);
                                    intent.putExtra("latitude", comment.getLocation().getLatitude());
                                    intent.putExtra("longitude", comment.getLocation().getLongitude());
                                    intent.putExtra("context", comment.getContext());
                                }

                                itemView.getContext().startActivity(intent);
                            }
                        });
                    }
                }
            });
        }

        private float calcAverageRating(List<DocumentSnapshot> commentDocs){
            float ratingSum = 0.0f;
            int nrComments = commentDocs.size();

            for (DocumentSnapshot doc : commentDocs){
                ExperienceComment commentObj = doc.toObject(ExperienceComment.class);
                ratingSum += commentObj.getEvaluation();
            }

            return (nrComments == 0) ? 0 : ratingSum / nrComments;
        }
    }


    public BeachSuggestionsAdapter(FirestoreRecyclerOptions<Beach> options, String context){
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.beach_suggestion, parent, false);
        return new Holder(rootView);
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, int i, @NonNull Beach beach) {
        holder.setCardValues(beach);
    }

}
