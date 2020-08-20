package com.beamotivator.beam.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.R;
import com.beamotivator.beam.ThierProfile;
import com.beamotivator.beam.models.ModelUser;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUser> userList;

    //For getting current users uid
    FirebaseAuth firebaseAuth;
    String myUid;


    //constructor
    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //get data
        final String hisUID = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        final String userEmail = userList.get(i).getEmail();
        final String uid = userList.get(i).getUid();

        //set data
        myHolder.mNameTV.setText(userName);
        myHolder.mEmailTV.setText(userEmail);
        try {
            Glide.with(context)
                    .load(userImage)
                    .into(myHolder.mAvatarIV);

        } catch (Exception e) {

        }



        //handle item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*click user from user list to start texting or messaging
                * start activity by putting uid of the reciever
                * we will use that UID to identify the user we are gonna chat*/

                //Toast.makeText(context, "You clicked", Toast.LENGTH_SHORT).show();
                //to go to the respective user during with his uid

                            Intent prof = new Intent(context, ThierProfile.class);
                            prof.putExtra("uid",uid);
                            context.startActivity(prof);

            }
        });



    }




    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view class holder
    static class MyHolder extends RecyclerView.ViewHolder {

        //views
        CircleImageView mAvatarIV;
        TextView mNameTV, mEmailTV;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mAvatarIV = itemView.findViewById(R.id.users_avatarIv);
            mNameTV = itemView.findViewById(R.id.users_name);
            mEmailTV = itemView.findViewById(R.id.users_email);
        }
    }

}
