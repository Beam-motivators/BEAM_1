package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.beamotivator.beam.adapters.AdapterPosts;
import com.beamotivator.beam.models.ModelPost;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThierProfile extends AppCompatActivity {

    FirebaseAuth  firebaseAuth;

    RecyclerView postsRecyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //views
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv, emptyProfile;

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
        setContentView(R.layout.activity_their_profile);

        Toolbar profileTlbr = findViewById(R.id.profileToolbar);

        setSupportActionBar(profileTlbr);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        postsRecyclerView = findViewById(R.id.recyclerview_posts);

        //init views
        emptyProfile = findViewById(R.id.emptyProfile);
        avatarIv = findViewById(R.id.avatarTV);
        nameTv = findViewById(R.id.nameTV);
        emailTv = findViewById(R.id.emailTV);
        phoneTv = findViewById(R.id.phoneTV);

        //get uid of clicked user to retrieve the data
        Intent prof = getIntent();
        uid = prof.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data is obtained
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();

                    //setData
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        //if image is recieved then set
                        Glide.with(getApplicationContext())
                                .load(image)
                                .into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception load the default image
                        Picasso.get().load(R.drawable.ic_image_white);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        postList = new ArrayList<>();

        loadHisPosts();
    }

    private void loadHisPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);

        //get all data by the user
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelPost myPost = ds.getValue(ModelPost.class);

                    //add post
                    postList.add(myPost);

                    //adapter
                    adapterPosts = new AdapterPosts(ThierProfile.this,postList);

                    //set this adapter to recycler view
                    postsRecyclerView.setAdapter(adapterPosts);
                }
                if(postList.size() == 0)
                {
                    postsRecyclerView.setVisibility(View.GONE);
                    emptyProfile.setVisibility(View.VISIBLE);
                }
                else
                {
                    postsRecyclerView.setVisibility(View.VISIBLE);
                    emptyProfile.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThierProfile.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    /*public void searchHisPosts(final String searchQuery){
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThierProfile.this);

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref  =FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);

        //get all data by the user
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelPost myPost = ds.getValue(ModelPost.class);

                    if(myPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            myPost.getpDescr().toLowerCase().contains((searchQuery.toLowerCase()))){
                        //add to list
                        postList.add(myPost);
                    }

                    //add post
                    postList.add(myPost);

                    //adapter
                    adapterPosts = new AdapterPosts(ThierProfile.this,postList);

                    //set this adapter to recycler view
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThierProfile.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.add_post).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);


        MenuItem item = menu.findItem(R.id.searchAction);
        //searchview of user specific post
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                //called when user presses search button
//                if(!TextUtils.isEmpty(s)){
//                    searchHisPosts(s);
//                }
//                else
//                {
//                    loadHisPosts();
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                //called when any letter is pressed on the search bar
//                if(!TextUtils.isEmpty(s))
//                {
//                    searchHisPosts(s);
//                }
//                else
//                {
//                    loadHisPosts();
//                }
//                return false;
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.actionLogout)
        {
            firebaseAuth.signOut();
            //checkuserstatus();
        }
        if(id == R.id.add_post)
        {
            startActivity(new Intent(ThierProfile.this,AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}