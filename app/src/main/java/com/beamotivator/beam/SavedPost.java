package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.beamotivator.beam.adapters.AdapterPosts;
import com.beamotivator.beam.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedPost extends AppCompatActivity {

    RecyclerView savedPostsRv;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    FirebaseAuth firebaseAuth;
    String myId ;
    String ig = "";
    int count  = 0;

    //Toolbar savedPosts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            Drawable background = this.getResources().getDrawable(R.drawable.main_gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(this.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);

        }
        setContentView(R.layout.activity_saved_post);

        //set firebase
        firebaseAuth = FirebaseAuth.getInstance();
        myId = firebaseAuth.getCurrentUser().getUid();

        Toolbar savedTlbr = (Toolbar) findViewById(R.id.savedPostTlbr);
        setSupportActionBar(savedTlbr);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        savedPostsRv = findViewById(R.id.savedRecyclerView);

        postList = new ArrayList<>();

        loadSavedPosts();




    }

    private void loadSavedPosts() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(SavedPost.this);

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        savedPostsRv.setLayoutManager(layoutManager);

        myId = firebaseAuth.getCurrentUser().getUid();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Extras").child(myId).child("Saved");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){

                    String postId = ""+ds.getKey();

                    //now check for the post details
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Posts");
                    ref1.orderByChild("pId")
                            .equalTo(postId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                                    for(DataSnapshot ds1:datasnapshot.getChildren()){


                                        ModelPost modelPost = ds1.getValue(ModelPost.class);


                                        //add post
                                        postList.add(modelPost);



                                        //adapter
                                        adapterPosts = new AdapterPosts(SavedPost.this,postList);

                                        //set adapter to recycler view
                                        savedPostsRv.setAdapter(adapterPosts);
                                    }




                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}