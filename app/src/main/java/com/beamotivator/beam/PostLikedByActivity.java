package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.beamotivator.beam.adapters.AdapterUsers;
import com.beamotivator.beam.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {

    String postId;

    //views
    RecyclerView recyclerView;

    FirebaseAuth firebaseAuth;


    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar likedToolbar = findViewById(R.id.likedTlbr);

        setSupportActionBar(likedToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //get the post Id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init views
        recyclerView = findViewById(R.id.recyclerView);


        userList = new ArrayList<>();


        //get list of uid of users who liked the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    String hisUid = ""+ ds.getRef().getKey();

                    //get user info from each id
                    getUsers(hisUid);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUsers(String hisUid) {
        //get infromation of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            userList.add(modelUser);
                        }
                        //setup adapter
                        adapterUsers = new AdapterUsers(PostLikedByActivity.this,userList);

                        //setup adapter to recyclerview
                        recyclerView.setAdapter(adapterUsers);


                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}