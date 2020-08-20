package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.beamotivator.beam.adapters.AdapterPosts;
import com.beamotivator.beam.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    String gId = null;

    Button follow;
    DatabaseReference followRef;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String mUid = null;

    boolean mFollow = false;
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
        setContentView(R.layout.activity_each_group);

        // actionBar = getSupportActionBar();
        Toolbar groupTlbr = findViewById(R.id.groupsTlbr);

        setSupportActionBar(groupTlbr);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent openIntent = getIntent();
        groupName = openIntent.getStringExtra("groupTitle");
        gId = openIntent.getStringExtra("groupId");
        //actionBar.setTitle(groupName);

        getSupportActionBar().setTitle(groupName);

        Toast.makeText(this, ""+groupName, Toast.LENGTH_SHORT).show();

        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        //group posts recycler view
        followRef = FirebaseDatabase.getInstance().getReference("Groups");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mUid = user.getUid();

        groupPosts = findViewById(R.id.groupPostsRv);
        follow = findViewById(R.id.followBtn);

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followGroup();
            }
        });

        postList = new ArrayList<>();

        setFollow();

        loadPosts();

    }

    private void setFollow() {
        followRef.child(gId).child("Participants").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mUid)){
                    //user has liked this post
                    /*to indicate user has liked this post
                     * change the icon to another
                     * change text like to liked */
                    //holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumb_up_24px_fill, 0,0,0);
                    follow.setText("Following");
                    follow.setBackgroundResource(R.drawable.following);


                }
                else {
                    //user not liked this post
                    // holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumb_up_24px, 0,0,0);
                    follow.setText("Follow");
                    follow.setBackgroundResource(R.drawable.follow);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void followGroup() {
//        follow.setText("Following");
//
//        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Groups");
//
//        Query query = followRef.orderByChild("groupTitle").equalTo(groupName);
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                String mUid = user.getUid();
//                followRef.getRef().child(gId).child("Participants").child(mUid).setValue("Following");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        mFollow = true;

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mFollow){
                    if(snapshot.child(gId).child("Participants").hasChild(mUid)){
                        //already like so remove like
                        followRef.getRef().child(gId).child("Participants").child(mUid).removeValue();

                    }
                    else
                    {
                        followRef.getRef().child(gId).child("Participants").child(mUid).setValue("Following");

                    }
                    mFollow = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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