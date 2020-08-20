package com.beamotivator.beam.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beamotivator.beam.AboutActivity;
import com.beamotivator.beam.Attendance;
import com.beamotivator.beam.MainActivity;
import com.beamotivator.beam.PrefManager;
import com.beamotivator.beam.R;
import com.beamotivator.beam.SavedPost;
import com.beamotivator.beam.SuggestionsActivity;
import com.beamotivator.beam.ThierProfile;
import com.beamotivator.beam.TodoMain;
import com.beamotivator.beam.adapters.AdapterPosts;
import com.beamotivator.beam.models.ModelPost;
import com.beamotivator.beam.models.ModelUser;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    ShimmerFrameLayout shimmerFrameLayout;

    //init views
    CircleImageView wokImage,menuImage;
    TextView homeEmpty,wokPoints,menuName,wokname, menuEmail,greetName;

    DrawerLayout homeMenu;
    ImageView menuIv;
    ConstraintLayout wokDisplay;

    GoogleSignInClient mGoogleSignInClient;


    CardView wokCard;

    NavigationView homeNav;
    private PrefManager prefManager;

    //To get resources text
    Resources resources;
    ConstraintLayout constraintLayout;
    String myUid;
    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            Drawable background = getActivity().getResources().getDrawable(R.drawable.main_gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getActivity().getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(getActivity().getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);

        }


          constraintLayout = view.findViewById(R.id.greet_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

//        prefManager = new PrefManager(getActivity());
//        if (!prefManager.isFirstTimeLaunch()) {
//            launchHomeScreen();
//          getActivity().finish();
//        }
//        else {
//
//            constraintLayout.setVisibility(View.GONE);
//        }






        firebaseAuth = FirebaseAuth.getInstance();
        myUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        wokDisplay = view.findViewById(R.id.wokDisplay);

        wokCard = view.findViewById(R.id.wokCard);
        wokCard.setVisibility(View.GONE);

        wokCard = view.findViewById(R.id.wokCard);
        wokCard.setVisibility(View.GONE);

        wokImage = view.findViewById(R.id.wokImage);
        wokPoints = view.findViewById(R.id.wokPoints);
        wokname=view.findViewById(R.id.wokName);
        greetName=view.findViewById(R.id.home_username_greet);
        homeEmpty = view.findViewById(R.id.homeMessage);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);


        menuIv = view.findViewById(R.id.menuIv);

        //open drawer when menu is clicked
        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeEmpty.setVisibility(View.GONE);
                homeMenu.openDrawer(GravityCompat.START);
            }
        });

        //set home navigation
        homeNav = view.findViewById(R.id.menuExpand);
        homeNav.setItemIconTintList(null);
        View header = homeNav.getHeaderView(0);
        menuName = header.findViewById(R.id.menuNameTv);
        menuImage = header.findViewById(R.id.menuProfileIv);
        menuEmail = header.findViewById(R.id.menuEmailTv);



        //init the drawer layout
        homeMenu = view.findViewById(R.id.drawerMenu);

        //set navigation for drawer
        homeNav.setNavigationItemSelectedListener(drawerSelectedListener);

        //set values of menu drawer
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference("Users");
        menuRef.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String mName =""+snapshot.child("name").getValue();
                        String mImage = ""+snapshot.child("image").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        menuName.setText(mName);
                        greetName.setText(mName);
                        menuEmail.setText(email);

                        try{
                            Glide.with(container)
                                    .load(mImage)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_image)
                                    .into(menuImage);
                        }
                        catch (Exception e) {
                            menuImage.setImageResource(R.drawable.ic_image);

                            Toast.makeText(getActivity(), "" + mName, Toast.LENGTH_SHORT).show();
                        }

                    }




                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        resources = getResources();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newest post, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recycelr view
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();


        loadPosts();



        return view;
    }

    private NavigationView.OnNavigationItemSelectedListener drawerSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            //handle item clicks
            switch (menuItem.getItemId()){


                case R.id.setgoals:
                    Intent i = new Intent(getActivity(), TodoMain.class);
                    startActivity(i);
                    getActivity();
                    break;
                case R.id.bunkcheck:
                    Intent xi = new Intent(getActivity(), Attendance.class);
                    startActivity(xi);
                    getActivity();
                    break;

                case R.id.saved_Posts:
                    startActivity(new Intent(getActivity(), SavedPost.class));
                    break;

                case R.id.personalInfo:
                    Intent prof = new Intent(getActivity(), ThierProfile.class);
                    prof.putExtra("uid",myUid);
                    startActivity(prof);
                    break;
                case R.id.menuSuggestions:
                    startActivity(new Intent(getActivity(), SuggestionsActivity.class));
                    break;

                case R.id.menuLogout:
                    logout();
                    break;
                case R.id.menuAboutUs:
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
            }
            return false;
        }
    };

    private void logout() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to logout?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                mGoogleSignInClient.signOut();

                checkuserstatus();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


//    private void launchHomeScreen() {
//        prefManager.setFirstTimeLaunch(false);
//        constraintLayout.setVisibility(View.VISIBLE);
//
//    }





    private void loadPosts() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.getRef();
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter posts
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
                if(postList.size() == 0)
                {
                    recyclerView.setVisibility(View.GONE);
                    homeEmpty.setVisibility(View.VISIBLE);

                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    homeEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
            }
        });
    }



    @Override
    public void onStart() {

        super.onStart();
        checkuserstatus();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show options menu in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkuserstatus();
    }

    @Override
    public void onPause() {
        super.onPause();
    }





    public void checkuserstatus()
    {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //stay signed in
            //set email of user
            //protxt.setText(user.getEmail());
        }
        else
        {
            mGoogleSignInClient.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }




}