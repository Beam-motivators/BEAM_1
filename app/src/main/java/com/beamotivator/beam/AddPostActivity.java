package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beamotivator.beam.adapters.AdapterGroupNames;
import com.beamotivator.beam.models.ModelGroupNames;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddPostActivity extends AppCompatActivity  {
    private static ProgressDialog progressDialog;


    //ap - add post
    private FirebaseAuth firebaseAuth;
    private Button apPostBtn;
    private EditText  apDescription;
    private TextView inputChoice;
    private ImageView apImage;
    private ImageButton dropArrow;
    DatabaseReference userDb;
    ProgressDialog pd;

    //intent message from broadcast
    String bGroupName=null;

    private RecyclerView groupNamesRv;

    //List to get group names
    ArrayList<ModelGroupNames> groupNames;
    AdapterGroupNames adapterGroups;



    //permissions
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;




    //Image picked will be same in this
    Uri image_rui = null;

    //user info
    String name, uid, email, dp;


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
        setContentView(R.layout.activity_add_post);

       // Toolbar addPostTlbr = findViewById(R.id.apTlbr);

        //setSupportActionBar(addPostTlbr);

//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        email = user.getEmail();
        uid = user.getUid();

        apPostBtn = findViewById(R.id.postBtn);
        apDescription = findViewById(R.id.pDescEt);
        apImage = findViewById(R.id.pImage);
        pd = new ProgressDialog(this);
        //postChoice = findViewById(R.id.postChoice);
        inputChoice = findViewById(R.id.inputChoiceTv);
        groupNamesRv = findViewById(R.id.groupNameRv);
        dropArrow = findViewById(R.id.dropIcon);
        //selection = findViewById(R.id.choiceMade);
        //groupNamesList = findViewById(R.id.groupNamesLV);

        groupNamesRv.setVisibility(View.INVISIBLE);

        dropArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupNamesRv.setVisibility(View.VISIBLE);
            }
        });






        //init group title list
        groupNames = new ArrayList<>();

        loadGroupNames();

        //get data through intent from previous activities adapter
        Intent intent = getIntent();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever,new IntentFilter("custom-message"));

        //get data and its type from intent
        String action = intent.getAction();
        String type = intent.getType();
        if(Intent.ACTION_SEND.equals(action) && type != null){

            if("text/plain".equals(type)){
                //text type data
                handleSendText(intent);
            }
            else if(type.startsWith("image")){
                //image type data
                handleSendImage(intent);
            }
        }

        Intent intentPostType = getIntent();
        String postType = intentPostType.getStringExtra("type");
        Toast.makeText(this, ""+postType, Toast.LENGTH_SHORT).show();
        if(postType.equalsIgnoreCase("text")){
            apImage.setVisibility(View.GONE);
        }
        else if(postType.equalsIgnoreCase("image")){
            //set image from galleryIntent
            Toast.makeText(this, ""+intentPostType, Toast.LENGTH_SHORT).show();
            image_rui = Uri.parse(intent.getStringExtra("imageUri"));
            apImage.setImageURI(image_rui);
        }





        //user info to be included in post
        userDb = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDb.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //init permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //get image from gallery on click
        apImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });




        //post button click listener
        apPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = apDescription.getText().toString().trim();
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();

                    return;
                }
                if(inputChoice.getText().toString().trim().equalsIgnoreCase("Choose group")){
                    Toast.makeText(AddPostActivity.this, "Choose a group", Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(AddPostActivity.this, ""+inputChoice.getText().toString(), Toast.LENGTH_SHORT).show();
                    if (image_rui == null) {
                        uploadData(description, "noImage");
                    } else {
                        String out = image_rui.toString();
                        uploadData(description, out);
                    }

                }


            }

        });
    }

    private void loadGroupNames() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        groupNamesRv.setLayoutManager(layoutManager);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        ref.orderByChild("groupTitle").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupNames.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelGroupNames model = ds.getValue(ModelGroupNames.class);

                    //String name =""+model.getGroupTitle();
                    groupNames.add(model);

                    //init adapter
                    adapterGroups = new AdapterGroupNames(getApplicationContext(),groupNames);

                    //set adapter to recycler view
                    groupNamesRv.setAdapter(adapterGroups);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void handleSendImage(Intent intent) {
        //handle the received image(uri)
        Uri imageURI = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageURI != null){
            image_rui = imageURI;
            //set to imageview

            apImage.setImageURI(image_rui);
        }
    }

    private void handleSendText(Intent intent) {
        //handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(sharedText!=null)
        {
            //set to description edit text
            apDescription.setText(sharedText);
        }
    }

    private void uploadData( final String description, String uri) {
//        pd.setMessage("Uploading");
//        pd.show();
        progressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(" Uploading Plese Wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        String userid = firebaseAuth.getCurrentUser().getUid();
        //for post-image name ,post-id and post-publish-time
        final String timestamp = String.valueOf(System.currentTimeMillis());
        final String pId = timestamp + name;

        String filePathAndName = "Posts/" + "post_" + timestamp;

        if (!uri.equals("noImage")) {
            //post with pic
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded to firebase now get the url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUrl = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()) {
                                //url is received upload to firebase

                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("group",bGroupName);
                                String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                                hashMap.put("stamp", timeStamp);
                                //hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pTime", timestamp);
                                hashMap.put("pImage", downloadUrl);
                                hashMap.put("pId", pId);
                                hashMap.put("pComments", "0");
                                hashMap.put("pLikes", "0");




                                //path to store data in firebase database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                                //put data in this ref
                                ref.child(pId).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                progressDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                //apTitle.setText("");
                                                apDescription.setText("");
                                                apImage.setImageURI(null);
                                                image_rui = null;

                                                HashMap<Object,String> likesHash = new HashMap<>();
                                                likesHash.put("pLikes","0");
                                                likesHash.put("pId",pId);

                                                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");
                                                likesRef.child(pId).setValue(likesHash)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(AddPostActivity.this, "We did it", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(AddPostActivity.this, "Couldn't do", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });




                                                startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));




                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed adding post nin database
                                        progressDialog.dismiss();
                                        Toast.makeText(AddPostActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed uploading image
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //displaying the upload progress
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            });
        } else {
            //post without pic
            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("group",bGroupName);
            hashMap.put("pDescr", description);
            hashMap.put("pTime", timestamp);
            hashMap.put("pImage", uri);
            String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            hashMap.put("stamp", timeStamp);
            hashMap.put("pId", pId);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");


            //path to store data in firebase database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

            //put data in this ref
            ref.child(pId).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in database
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
//                            apTitle.setText("");
                            apDescription.setText("");
                            apImage.setImageURI(null);
                            image_rui = null;

                            HashMap<Object,String> likesHash = new HashMap<>();
                            likesHash.put("pLikes","0");
                            likesHash.put("pId",pId);

                            DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");
                            likesRef.child(pId).setValue(likesHash)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddPostActivity.this, "We did it", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddPostActivity.this, "Couldn't do", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post nin database
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void  showImagePickDialog() {
        if(!checkStoragePermissions())
            requestStoragePermissions();
        else{
            pickFromGallery();
        }
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }



    private boolean checkStoragePermissions(){
        //check if storage permissions is enabled or not
        // if enabled then return true
        // if not enabled then return false

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissions(){
        //request run time storage permissions
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//goto previous activity
        return super.onSupportNavigateUp();
    }




    //to handle the requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when allow or denied is clicked on dialog
        //here we will allow permission cases(allowed and denied)

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Enable storage permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //handle activity permissions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this image will be called after picking image from camera or gallery
        if(resultCode == RESULT_OK)
        {
            if(requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                //get uri of the image
                image_rui = data.getData();

                //set image to imageview
                apImage.setImageURI(image_rui);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public BroadcastReceiver mMessageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bGroupName = intent.getStringExtra("groupName");
            Toast.makeText(context, ""+bGroupName, Toast.LENGTH_SHORT).show();
            inputChoice.setText(bGroupName);
            groupNamesRv.setVisibility(View.INVISIBLE);
        }
    };




}