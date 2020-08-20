package com.beamotivator.beam;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beamotivator.beam.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomProfileActivity extends AppCompatActivity {

    String hisId;

    //views
    CircleImageView bottomProfileImage;
    TextView bottomProfileName;
    LinearLayout bottomSheet;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_bottom_sheet);

        Intent intent = getIntent();
        hisId = intent.getStringExtra("hisId");

        bottomSheet = findViewById(R.id.bottomSheetContainer);

        //setting up the bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.layout_bottom_sheet,bottomSheet);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


        bottomProfileImage = bottomSheetView.findViewById(R.id.bottomProfileImage);
        bottomProfileName = bottomSheetView.findViewById(R.id.bottomProfileName);

        //set views for bottom profile
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid")
                .equalTo(hisId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String image = ""+snapshot.child("image").getValue();
                        String name = ""+snapshot.child("name").getValue();

                        bottomProfileName.setText(name);

                        try{
                            Picasso.get()
                                    .load(image)
                                    .placeholder(R.drawable.ic_image)
                                    .into(bottomProfileImage);
                        }
                        catch (Exception e){
                            bottomProfileImage.setImageResource(R.drawable.ic_image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }
}
