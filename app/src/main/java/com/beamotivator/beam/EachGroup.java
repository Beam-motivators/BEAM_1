package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.beamotivator.beam.adapters.AdapterPosts;
import com.beamotivator.beam.models.ModelPost;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EachGroup extends AppCompatActivity {

    ActionBar actionBar;
    RecyclerView groupPosts;

    //Adapter
    AdapterPosts adapterPosts;
    List<ModelPost> postList;
    String groupName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_group);

        actionBar = getSupportActionBar();

        Intent openIntent = getIntent();
        groupName = openIntent.getStringExtra("groupTitle");
        actionBar.setTitle(groupName);

        Toast.makeText(this, ""+groupName, Toast.LENGTH_SHORT).show();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //group posts recycler view
        groupPosts = findViewById(R.id.groupPostsRv);


        postList = new ArrayList<>();

        loadPosts();

    }

    private void loadPosts() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        groupPosts.setLayoutManager(layoutManager);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //Query query = ref.getRef();
        //get all data from this ref
        ref.orderByChild("group").equalTo(groupName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter posts
                    adapterPosts = new AdapterPosts(getApplicationContext(),postList);

                    //set adapter to recyclerview
                    groupPosts.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}