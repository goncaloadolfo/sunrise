package com.example.sunrise.SuggestionsActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunrise.DocObjs.ExperienceComment;
import com.example.sunrise.DocObjs.UserObj;
import com.example.sunrise.Profile;
import com.example.sunrise.R;
import com.example.sunrise.Utils.FirebaseCom;
import com.example.sunrise.Utils.GlideApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Holder> {

    private static final String FIREBASE_STORAGE_URL = "gs://finalprojectdam.appspot.com/";
    private List<DocumentSnapshot> commentsDocs;
    private FirebaseFirestore db;

    public class Holder extends RecyclerView.ViewHolder{

        public Holder(View v){
            super(v);
        }

        public void setCommentValues(DocumentSnapshot commentDoc){
            // get views
            final ImageView profilePic = itemView.findViewById(R.id.imageViewCommentProfilePic);
            final TextView author = itemView.findViewById(R.id.textViewCommentAuthor);
            TextView comment = itemView.findViewById(R.id.textViewCommentText);
            TextView commentDate = itemView.findViewById(R.id.textViewCommentDate);
            RatingBar rating = itemView.findViewById(R.id.ratingBarEvaluation);

            // get and set author name and profile pic
            final ExperienceComment commentObj = commentDoc.toObject(ExperienceComment.class);
            Query query = db.collection(FirebaseCom.USERS_COLLECTION).whereEqualTo("email", commentObj.getEmail());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        UserObj userObj = task.getResult().getDocuments().get(0).toObject(UserObj.class);
                        author.setText(userObj.getFirstName() + " " + userObj.getLastName());

                        StorageReference ref = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(FIREBASE_STORAGE_URL + userObj.getProfilePicName());

                        GlideApp.with(profilePic.getContext())
                                .load(ref)
                                .into(profilePic);
                    }
                }
            });

            // set comment and date, rating
            comment.setText(commentObj.getComment());
            commentDate.setText(commentObj.getTime());
            rating.setRating(commentObj.getEvaluation());

            // profile intent
            author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), Profile.class);
                    intent.putExtra("email", commentObj.getEmail());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }


    public CommentsAdapter(List<DocumentSnapshot> commentsDocs){
        this.commentsDocs = commentsDocs;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new Holder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setCommentValues(commentsDocs.get(position));
    }

    @Override
    public int getItemCount() {
        return commentsDocs.size();
    }
}
