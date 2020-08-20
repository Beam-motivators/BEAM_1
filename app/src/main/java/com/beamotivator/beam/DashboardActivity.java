package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.beamotivator.beam.No_Internet.Internet_off;
import com.beamotivator.beam.fragments.HomeFragment;
import com.beamotivator.beam.fragments.MyGroupFragment;
import com.beamotivator.beam.fragments.NotificationFragment;
import com.beamotivator.beam.fragments.SearchFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.security.Permission;

public class    DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    private ActionBar actionBar;
    private BottomNavigationView navigationView;
    String mUID;
    Boolean isRotate=false;
    boolean isClicked = false;
    TextView addText,addImage;
    FloatingActionButton fabAddPost;
    LinearLayout addPostLayout;
    LinearLayout bottomSheet;

    GoogleSignInClient mGoogleSignInClient;
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private final String SAMPLE_CROPPED_IMG_NAME="SampleCropImg";

    //permissions array
    String[] storagePermissions;
    String[] cameraPermissions;

    //set the type of posttype
    Intent intentPostType = null;

    Uri image_rui = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_dashboard);


        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork=cm.getActiveNetworkInfo();

        boolean isConnected=activeNetwork !=null && activeNetwork.isConnected();
        if (isConnected){


        }
        else{


            Intent c = new Intent(getApplicationContext(), Internet_off.class);
            startActivity(c);


        }

        //init permissions
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        checkuserstatus();
        //bottom navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //set add post fab
        fabAddPost = findViewById(R.id.add_post_fab);

        Animation animation = AnimationUtils.loadAnimation(DashboardActivity.this, R.anim.scale);
        fabAddPost.startAnimation(animation);
//        addPostLayout = findViewById(R.id.add_post_layout);
//        addPostLayout.setVisibility(View.GONE);
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBottomPost();
            }
        });




        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.frame_content,fragment1);
        ft1.commit();

    }

    private void setBottomPost() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog);

        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_add_post,bottomSheet);

        addText = bottomSheetView.findViewById(R.id.add_post_text);
        addImage = bottomSheetView.findViewById(R.id.add_post_image);

        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentPostType = new Intent(DashboardActivity.this,AddPostActivity.class);
                intentPostType.putExtra("type","text");
                startActivity(intentPostType);
                bottomSheetDialog.dismiss();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.show();

    }

    //to choose between camera and gallery
    private void showImagePickDialog() {

        if (!checkStoragePermissions()){
            //no permission, send request
            requestStoragePermissions();

        }
        else {
            pickFromGallery();
        }


    }

    private void pickFromGallery() {
        Intent intentImage = new Intent(Intent.ACTION_PICK);
        intentImage.setType("image/*");
        startActivityForResult(intentImage,IMAGE_PICK_GALLERY_CODE);
    }


    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }


    @Override
    protected void onResume() {
        checkuserstatus();
        super.onResume();
    }


    //managing bottom navigation listener
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            //handle item clicks
            switch (menuItem.getItemId()){
                case R.id.nav_home:


                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.frame_content,fragment1);
                    ft1.commit();
                    return true;

                case R.id.nav_search:
                    //users fragment
                    //actionBar.setTitle("Users");
                    SearchFragment fragment3 = new SearchFragment();
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.frame_content,fragment3);
                    ft3.commit();
                    return true;
                case R.id.nav_saved:
                    //chat fragment
                    //actionBar.setTitle("Chat List");
                    MyGroupFragment fragment4 = new MyGroupFragment();
                    FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.frame_content,fragment4);
                    ft4.commit();
                    return true;
                case R.id.nav_notifications:
                    //notification fragment
                    //showMoreOptions();
                    NotificationFragment fragment5 = new NotificationFragment();
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.frame_content,fragment5);
                    ft5.commit();
                    return true;
            }

            return false;
        }
    };



    //Check if user logged in or not
    public void checkuserstatus()
    {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //stay signed in
            //set email of user
            //protxt.setText(user.getEmail());
            mUID = user.getUid();




        }
        else{

            mGoogleSignInClient.signOut();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        checkuserstatus();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_rui = data.getData();
                File tempCropped = new File(getCacheDir(),"tempImgCropped.png");
                Uri destinationUri = Uri.fromFile(tempCropped);
                UCrop.of(image_rui,destinationUri)
                        .start(this);
//
                //openCropActivity(image_rui,image_rui);
                // startCrop(image_rui,image_rui);
            }
            else if(requestCode == UCrop.REQUEST_CROP){
                Uri imageCropped = UCrop.getOutput(data);
                intentPostType = new Intent(this,AddPostActivity.class);
                intentPostType.putExtra("type","image");
                intentPostType.putExtra("imageUri",imageCropped.toString());
                startActivity(intentPostType);


            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


}