package com.example.sunrise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sunrise.DocObjs.UserObj;
import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.example.sunrise.Utils.GlideApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;


public class Profile extends BaseActivity {

    public static final int EDIT_PHOTO = 1;
    private static final String FIREBASE_STORAGE_URL = "gs://finalprojectdam.appspot.com/";
    private final static CollectionReference USERS_REFERENCE = FirebaseFirestore.getInstance()
            .collection(FirebaseCom.USERS_COLLECTION);
    private ImageView profilePicView;
    private TextView nameView;
    private TextView hometownView;
    private TextView emailView;
    private ImageView editPhoto;


    public void updateUserInfo(){
        // set user information
        final String email = getIntent().getStringExtra("email");

        Query query = USERS_REFERENCE.whereEqualTo("email", email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    UserObj userObj = task.getResult().getDocuments().get(0).toObject(UserObj.class);
                    nameView.setText(userObj.getFirstName() + " " + userObj.getLastName());
                    hometownView.setText(userObj.getHomeTown());
                    emailView.setText(email);

                    // set profile pic
                    StorageReference storageReference = FirebaseStorage.getInstance().
                            getReferenceFromUrl(FIREBASE_STORAGE_URL + userObj.getProfilePicName());
                    GlideApp.with(profilePicView.getContext()).load(storageReference).into(profilePicView);
                }
            }
        });

        // dont allow profile image change if it isnt the current user
        if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(email)){
            editPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // get views
        profilePicView = findViewById(R.id.imageViewProfilePic);
        nameView = findViewById(R.id.textViewName);
        hometownView = findViewById(R.id.textViewHomeTown);
        emailView = findViewById(R.id.textViewEmail);

        // set listener to edit photo btn
        editPhoto = ((ImageView) findViewById(R.id.imageViewEditPhoto));
        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create intent to user image gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, EDIT_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // back from image gallery
        if (requestCode == EDIT_PHOTO && resultCode == RESULT_OK){
            Uri uri = data.getData();
            String email = emailView.getText().toString();
            String newProfilePicName = generateProfilePicName();

            // upload img to firestorage
            StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(FIREBASE_STORAGE_URL)
                    .child(newProfilePicName);
            fileRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        updateUserInfo();
                    }
                }
            });

            // update att in database
            FirebaseCom.changeProfilePic(email, newProfilePicName);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserInfo();
    }

    private String generateProfilePicName(){
        return "ProfilePic" + new Random().nextInt(999999999);
    }

}
