package com.beamotivator.beam;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private  static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout signIn;
    private FirebaseAuth mAuth;

    private int check = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        signIn = findViewById(R.id.SignIn);

        //Before mAuth
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();



        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });



    }

    private void firebaseAuthWithGoogle(String idToken) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            final String personName = Objects.requireNonNull(acct.getDisplayName()).trim();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            final String proPic = personPhoto.toString();
            final String name = personName.substring(0, personName.lastIndexOf(" "));

//            if(personEmail.endsWith("christuniverstiy.in")) {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                //Get user email and id from auth
                                String email = user.getEmail();
                                String uid = user.getUid();

                                //When user registers store data in firebase database using hashmap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put info to hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", name);
                                hashMap.put("phone", "");
                                hashMap.put("image", proPic);
                                hashMap.put("myPoints", "0");
                                hashMap.put("weeklyPoints", "0");
                                hashMap.put("totalLikes", "0");
                                hashMap.put("postCount", "0");

                                //Firebase database instance
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                                //Add path to store data "Users"
                                DatabaseReference reference = firebaseDatabase.getReference("Users");

                                //put data within hashmap in database
                                reference.child(uid).setValue(hashMap);

                                Toast.makeText(MainActivity.this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                //goto profile activity after logging in
                                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                                finish();

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void registerMe(String idToken, final String personEmail, final String personName, final String proPic) {
        //Toast.makeText(RegisterActivity.this, "Why is this happening? " + per, Toast.LENGTH_SHORT).show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Get user email and id from auth
                            String uid = user.getUid();

                            //When user registers store data in firebase database using hashmap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put info to hashmap
                            hashMap.put("email", personEmail);
                            hashMap.put("uid", uid);
                            hashMap.put("name", personName);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("image", proPic);
                            hashMap.put("myPoints", "0");
                            hashMap.put("weeklyPoints", "0");
                            hashMap.put("totalLikes", "0");
                            hashMap.put("postCount", "0");

                            //Firebase database instance
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                            //Add path to store data "Users"
                            DatabaseReference reference = firebaseDatabase.getReference("Users");

                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(MainActivity.this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            //goto profile activity after logging in
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(this, "Value "+ check, Toast.LENGTH_SHORT).show();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN ) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}