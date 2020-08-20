package com.beamotivator.beam.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.R;
import com.beamotivator.beam.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;

    private ProgressDialog pd;


    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
        ProgressDialog pd = new ProgressDialog(context);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, viewGroup, false );

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get the data
        final String uid = commentList.get(i).getUid();
        final String cid = commentList.get(i).getcId();
        String comment = commentList.get(i).getComment();
        String timestamp = commentList.get(i).getTimeStamp();
        String name = commentList.get(i).getuName();
        String image = commentList.get(i).getuDp();
        String email = commentList.get(i).getuEmail();

        String datetime = null;

        if(timestamp != null){
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(Long.parseLong(timestamp));
            datetime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
            myHolder.timeTv.setText(datetime);
        }

        //set the data
        myHolder.nameTv.setText(name);
        myHolder.commentTv.setText(comment);

        try
        {
            Picasso.get()
            .load(image)
            .placeholder(R.drawable.ic_image)
                    .into(myHolder.avatarIv);
        }
        catch(Exception e)
        {

        }

        //comment click listener
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if comment is by currently signed in user or not
                if(myUid.equals(uid))
                {
                    //my comment
                    //delete the comment

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete comment
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss dialog
                            dialog.dismiss();

                        }
                    });
                    builder.create().show();
                }
                else {
                    //not my comment
                    Toast.makeText(context, "Can't delete other's comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        pd.setMessage("Deleting the comment");
        pd.show();

        ref.child("Comments").child(cid).removeValue();

        //now update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = "" + dataSnapshot.child("pComments").getValue();
                int newCommentsBal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue("" + newCommentsBal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    static class MyHolder extends RecyclerView.ViewHolder{

        //declare views from row_comments.xml
        CircleImageView avatarIv;
        TextView timeTv, commentTv, nameTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv =  itemView.findViewById(R.id.avatarIv);
            timeTv = itemView.findViewById(R.id.timeTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            nameTv = itemView.findViewById(R.id.nameTv);
        }
    }
}
