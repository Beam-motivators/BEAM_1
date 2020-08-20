package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beamotivator.beam.R;
import com.beamotivator.beam.adapters.AdapterSuggestions;
import com.beamotivator.beam.models.ModelSuggestions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SuggestionsActivity extends AppCompatActivity {

    //views
    Button send;
    EditText suggestions;
    String suggestion;
    RecyclerView suggestionsRv;


    String myUid,name,dp = "";
    FirebaseAuth firebaseAuth;

    RelativeLayout userLayout;
    RelativeLayout adminLayout;
    private ArrayList<ModelSuggestions> suggestionsList;

    private AdapterSuggestions adapterSuggestions;

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
        setContentView(R.layout.activity_suggestions);

        Toolbar suggTlbr = (Toolbar) findViewById(R.id.suggestionTlbr);

        setSupportActionBar(suggTlbr);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();

        //init views
        send = findViewById(R.id.suggestionBtn);
        suggestions = findViewById(R.id.suggestionET);
        suggestionsRv = findViewById(R.id.suggestionsRecyclerView);
        adminLayout = findViewById(R.id.adminSuggestionLayout);
        userLayout = findViewById(R.id.user_suggestion_layout);

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin");
        adminRef.orderByChild("uid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    if(myUid.equals(ds.child("uid").getValue())){
                        adminLayout.setVisibility(View.VISIBLE);
                        userLayout.setVisibility(View.GONE);
                        break;
                    }
                    else{
                        adminLayout.setVisibility(View.GONE);
                        userLayout.setVisibility(View.VISIBLE);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //user info to be included in post
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDb.orderByChild("uid").equalTo(myUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    dp = "" + ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestion = suggestions.getText().toString();
                if(TextUtils.isEmpty(suggestion)){

                    Toast.makeText(SuggestionsActivity.this, "Input field blank", Toast.LENGTH_SHORT).show();
                }
                else{
                    saveSuggestion(myUid,suggestion);
                }
            }
        });
        suggestionsList = new ArrayList<>();

        loadSuggestions();

    }

    private void loadSuggestions() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);



        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        suggestionsRv.setLayoutManager(layoutManager);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Suggestions");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                suggestionsList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelSuggestions model = ds.getValue(ModelSuggestions.class);

                    suggestionsList.add(model);

                    //adapter posts
                    adapterSuggestions = new AdapterSuggestions(getApplicationContext(),suggestionsList);

                    //set adapter to recyclerview
                    suggestionsRv.setAdapter(adapterSuggestions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
            }
        });

    }

    private void saveSuggestion(String myUid,String suggestion) {

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String sugId = timestamp+name;
        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("uid",myUid);
        hashMap.put("name",name);
        hashMap.put("uDp",dp);
        hashMap.put("sugId",sugId);
        hashMap.put("suggestion",suggestion);


        DatabaseReference sugRef = FirebaseDatabase.getInstance().getReference("Suggestions");
        sugRef.child(sugId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SuggestionsActivity.this, "Message has been sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SuggestionsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}