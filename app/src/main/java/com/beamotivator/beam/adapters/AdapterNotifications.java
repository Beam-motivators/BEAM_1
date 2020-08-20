package com.beamotivator.beam.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.PostDetailActivity;
import com.beamotivator.beam.R;
import com.beamotivator.beam.models.ModelNotification;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.HolderNotification> {

    private Context context;
    private ArrayList<ModelNotification> notificationList;

    private FirebaseAuth firebaseAuth;

    public AdapterNotifications(Context context, ArrayList<ModelNotification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate view row_notification
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications,parent,false);

        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotification holder, int position) {
        //get and set data to views

        //get data
        final ModelNotification model = notificationList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        final String timestamp = model.getTimestamp();
        String senderUid = model.getpUid();
        final String pId = model.getpId();

        //we will get name, email and image of user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            String name =""+ds.child("name").getValue();
                            String email =""+ds.child("email").getValue();
                            String image =""+ds.child("image").getValue();

                            //add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set views
                            holder.nameTv.setText(name);

                            try{
                                Glide.with(context)
                                        .load(image)
                                        .fitCenter()
                                        .placeholder(R.drawable.ic_image)
                                        .into(holder.avatarIv);
                            }
                            catch(Exception e)
                            {
                                holder.avatarIv.setImageResource(R.drawable.ic_image);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        holder.notificationTv.setText(notification);


        //click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start post detail activity
                //start post detail activity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);


            }
        });

        //long press to show delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                Toast.makeText(context, ""+model.getTimestamp(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification?");


                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Extras");
                        ref.child(Objects.requireNonNull(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()))
                                .child("Notifications")
                                .child(model.getTimestamp())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //deleted
                                        Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed
                                        Toast.makeText(context, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                return false;
            }
        });



    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    //holder class for views of row_notification
    static class HolderNotification extends RecyclerView.ViewHolder{

        //declare views
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.NavatarIv);
            nameTv = itemView.findViewById(R.id.NnameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);


        }
    }
}
