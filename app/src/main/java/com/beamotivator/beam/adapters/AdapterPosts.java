package com.beamotivator.beam.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.beamotivator.beam.AboutActivity;
import com.beamotivator.beam.Imagepopup.PhotoFullPopupWindow;
import com.beamotivator.beam.PostDetailActivity;
import com.beamotivator.beam.PostLikedByActivity;
import com.beamotivator.beam.R;
import com.beamotivator.beam.ThierProfile;
import com.beamotivator.beam.models.ModelPost;
import com.beamotivator.beam.models.ModelUser;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;
    FirebaseAuth firebaseAuth;
    LinearLayout bottomSheet;
    CircleImageView bottomProfileImage;
    TextView bottomSheetName;


    String myUid;

    private  DatabaseReference likesRef; //for likes database node
    private  DatabaseReference postsRef; //for posts database node
    private  DatabaseReference totalLikesRef;

    boolean mProcessLike = false;
    boolean mSaved = false;
    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        totalLikesRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);

        bottomSheet = view.findViewById(R.id.bottomSheetContainer);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        final String uid = postList.get(i).getUid();
        String uName = postList.get(i).getuName();
        String uDp = postList.get(i).getuDp();
        final String pId = postList.get(i).getpId();
        final String pDescription = postList.get(i).getpDescr();
        final String pImage = postList.get(i).getpImage();
        String pTimeStamp = postList.get(i).getpTime();
        String pComments = postList.get(i).getpComments(); //total number of comments for a post
        String timestamp = postList.get(i).getStamp();

        //to convert it date time
        String dateTime = null;

        //convert timestamp to DD/MM/YY hh:mm am/pm
        if(pTimeStamp != null){

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
            dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
            myHolder.pTimeTv.setText(dateTime);
        }

        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.pDescriptionTv.setText(pDescription);

        Query query = likesRef.orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String pLikes = ""+ Objects.requireNonNull(ds.child("pLikes").getValue()).toString();
                    myHolder.pLikesTv.setText(pLikes + " Helpful");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        myHolder.pCommentsTv.setText(pComments+ " Comments");

        //set likes for each post
        setLikes(myHolder, pId);

        //saved activity for each post
        setSaved(myHolder,pId);

        //set user dp
        try {
            Glide.with(context)
                    .load(uDp)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .into(myHolder.uPictureIv);
        } catch (Exception e) {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //set post image
        //if no image then hide imageview
        if(pImage.equals("noImage"))
        {
            myHolder.pImageIv.setVisibility(View.GONE);
        }
        else {
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Glide.with(context)
                        .load(pImage)
                        .centerInside()
                        .into(myHolder.pImageIv);
            }
            catch (Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();//get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(new Date(Long.parseLong(timestamp )* 1000));
        Date date = new Date();
        try {
            date = sdf.parse(localTime);//get local date
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) {
            //  return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            //  return null;
        }

        float timeDIM = getTimeDistanceInMinutes(time);

        String timeAgo = null;

        if (timeDIM == 0) {
            timeAgo = "just now";
        } else if (timeDIM == 1) {
            //  return  "1 minute";
            timeAgo="1 minute ago";
        } else if (timeDIM >= 2 && timeDIM <= 44) {
            timeAgo = (Math.round(timeDIM)) + " minutes ago";
        } else if (timeDIM >= 45 && timeDIM <= 89) {
            timeAgo = " 1 hour ago";
        } else if (timeDIM >= 90 && timeDIM <= 1439) {
            timeAgo = "about " + (Math.round(timeDIM / 60)) + " hours";
        } else if (timeDIM >= 1440 && timeDIM <= 2519) {
            timeAgo = "1 day ago";
        } else if (timeDIM >= 2520 && timeDIM <= 43199) {
            timeAgo = (Math.round(timeDIM / 1440)) + " days";
        } else if (timeDIM >= 43200 && timeDIM <= 86399) {
            timeAgo = "about a month ago";
        } else if (timeDIM >= 86400 && timeDIM <= 525599) {
            timeAgo = (Math.round(timeDIM / 43200)) + " months";
        } else if (timeDIM >= 525600 && timeDIM <= 655199) {
            timeAgo = "about a year ago";
        } else if (timeDIM >= 655200 && timeDIM <= 914399) {
            timeAgo = "over a year ago";
        } else if (timeDIM >= 914400 && timeDIM <= 1051199) {
            timeAgo = "almost 2 years ago";
        } else {
            timeAgo = "about " + (Math.round(timeDIM / 525600)) + " years";
        }
        // return timeAgo + " ago";
        myHolder.stamp.setText(timeAgo);





        myHolder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoFullPopupWindow(context, R.layout.popup_photo_full, myHolder.view1,pImage, null);

            }
        });



        //handle button clicks
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                showMoreOptions(myHolder.moreBtn, uid ,myUid, pId, pImage);
            }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get total number of likes for the post, who like button clicked
                //if currently signed in user has not liked it before


                final int pLikes = 0;

                mProcessLike = true;
                Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();


                //get id of the post clicked
                final String postIde = postList.get(i).getpId();
                //get user id of the one who posted
                final String pUid = postList.get(i).getUid();




             likesRef.child(postIde).child("Likes").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mProcessLike){
                            if(dataSnapshot.child(postIde).hasChild(myUid)){
                                //already like so remove like

                                Query query = likesRef.orderByChild("pId").equalTo(postIde);

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds:snapshot.getChildren())
                                        {
                                            String mplikes = ""+ds.child("pLikes").getValue().toString();
                                            int pLikes = Integer.valueOf(mplikes);
                                            likesRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                likesRef.child(postIde).child("Likes").child(postIde).child(myUid).removeValue();
                                decrementTotalLikes(pUid);
                                Toast.makeText(context, "YO", Toast.LENGTH_SHORT).show();
                                mProcessLike = false;

                            }
                            else
                            {
                                Query query = likesRef.orderByChild("pId").equalTo(postIde);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds:snapshot.getChildren())
                                        {
                                            String mplikes = ""+ds.child("pLikes").getValue().toString();
                                            int pLikes = Integer.valueOf(mplikes);
                                            likesRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                likesRef.child(postIde).child("Likes").child(postIde).child(myUid).setValue("Liked");
                                //    likesRef.child(postIde).child(myUid).setValue("Helpful");
                                incrementTotalLikes(pUid);
                                if(!myUid.equals(uid)){
                                    addToHisNotifications(""+uid,""+pId, "liked your post");

                                }
                                mProcessLike = false;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //calculateLikePoints(uid);
            }


        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start Post detail activity
                Intent intent =  new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);//will get the post id with this, ie id of post clicked
                context.startActivity(intent);
            }
        });

        //save posts
        myHolder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost(myUid,pId);
            }
        });

        //show bottom sheet
        myHolder.uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBottomProfile(uid);

            }
        });
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //some posts contain only text and some contain images so we have to handle them both
                //get image from imageview
                BitmapDrawable bitmapDrawable = (BitmapDrawable)myHolder.pImageIv.getDrawable();
                if(bitmapDrawable == null)
                {
                    //post without image
                    shareTextOnly(pDescription);
                }
                else
                {
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pDescription,bitmap);



                }
            }
        });
        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click to go to TheirProfile with the uid, uid of clicked user
                //which will be used to show user specific data/posts
                Intent intent = new Intent(context, ThierProfile.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        //click like count to start post liked by activity and pass post id
        myHolder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });
    }

    private void savePost(final String myUid, final String pId) {
        mSaved = true;
        String timestamp = ""+System.currentTimeMillis();
        final DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("Extras");

        savedRef.child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mSaved){
                    if(snapshot.child("Saved").hasChild(pId)){
                        //already like so remove like
                        Toast.makeText(context, "Vallam", Toast.LENGTH_SHORT).show();
                        savedRef.child(myUid).child("Saved").child(pId).removeValue();
                        mSaved = false;

                    }
                    else
                    {
//                        //not liked , like it
//                        postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
//                        postsRef.child(postIde).child("Likes").child(postIde).child(myUid).setValue("Helpful");
//                        incrementTotalLikes(pUid);
//                        addToHisNotifications(""+uid,""+pId, "found your post helpful");
                        savedRef.child(myUid).child("Saved").child(pId).setValue("Saved");
                        mSaved = false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void setBottomProfile(String uid) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.Theme_Design_BottomSheetDialog);

        View bottomSheetView = LayoutInflater.from(context)
                .inflate(R.layout.layout_bottom_sheet,bottomSheet);

        bottomSheetName = bottomSheetView.findViewById(R.id.bottomProfileName);
        bottomProfileImage = bottomSheetView.findViewById(R.id.bottomProfileImage);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("name").getValue();
                        String image = ""+snapshot.child("image").getValue();

                        bottomSheetName.setText(name);
                        try{
                            Glide.with(context)
                                    .load(image)
                                    .placeholder(R.drawable.ic_image)
                                    .into(bottomProfileImage);
                        }
                        catch(Exception e){
                            bottomProfileImage.setImageResource(R.drawable.ic_image);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


    }

    private void decrementTotalLikes(final String hisId) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    int tLikes = Integer.parseInt(modelUser.getTotalLikes());
                    ref.getRef().child(hisId).child("totalLikes").setValue(""+(tLikes-1));
                //    Toast.makeText(context, ""+tLikes, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void incrementTotalLikes(final String hisId) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    int tLikes = Integer.parseInt(modelUser.getTotalLikes());
                    ref.getRef().child(hisId).child("totalLikes").setValue(""+(tLikes+1));
                    Toast.makeText(context, ""+tLikes, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }








//    private void calculateLikePoints(String uid) {
//
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
//        ref.orderByChild("uid")
//                .equalTo(uid)
//                .addValueEventListener(new ValueEventListener() {
//
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        for(DataSnapshot ds:dataSnapshot.getChildren()){
//                            ModelPost model = ds.getValue(ModelPost.class);
//
//                            int sum = Integer.parseInt(model.getpLikes());
//                            totalLikes = totalLikes + sum;
//                        }
//                        myPoints = totalLikes;
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//
//                });
//
////        if(totalLikes >= 25){
////            myPoints = totalLikes;
////        }
//
//
////        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
////        userRef.child(uid).child("likePoints").setValue("50");
//    }

    private void addToHisNotifications(String hisUid,String pId, String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Extras");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });


    }


    private void shareTextOnly(String pDescription) {
        //concatenate title and description to share
        String shareBody = "" + pDescription;

        //share Intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); //in case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));  //message to show in shared dialog

    }

    private void shareImageAndText( String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = "" + pDescription;

        //first we will save the image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));

        //copy same code in post detail activity

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(),"images");
        Uri uri = null;
        try
        {
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream  stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context,"com.example.bensonsocial",file);

        }
        catch (Exception e)
        {
            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.child(postKey).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    /*to indicate user has liked this post
                     * change the icon to another
                     * change text like to liked */
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumb_up_24px_fill, 0,0,0);

                }
                else {
                    //user not liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumb_up_24px, 0,0,0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setSaved(final MyHolder holder, final String postKey) {
        DatabaseReference uRef = FirebaseDatabase.getInstance().getReference("Extras");
        uRef.child(myUid).child("Saved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(postKey)){
                    //user has liked this post
                    /*to indicate user has liked this post
                     * change the icon to another
                     * change text save to saved */
                    holder.saveBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.turned_in_24px, 0,0,0);

                }
                else {
                    //user not saved this post
                    holder.saveBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bookmark_border_24px, 0,0,0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating pop up menu when moe button clicked
        Toast.makeText(context, "Endho", Toast.LENGTH_SHORT).show();
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show delete option in posts of only currently signed in user
        if(uid.equals(myUid))
        {
            //add items to menu
            popupMenu.getMenu().add(Menu.NONE, 0,0 , "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Edit");

        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"Details");



        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == 0){
                    //Delete item is clicked
                    beginDelete(pId,pImage);

                }
                else if(id==1){
                    //Edit is clicked
                    //Start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, AboutActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                else if(id == 2)
                {
                    //start post detail activity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        if(pImage.equals("noImage"))
        {
            //post is without image
            deleteWithoutImage(pId);
        }
        else{
            //post is with image
            deleteWithImage(pId, pImage);

        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        pd.show();

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //image deleted now delete from database
                        pd.dismiss();
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds:dataSnapshot.getChildren()){
                                    ds.getRef().removeValue(); //removes values from firebase where pid matches

                                }
                                //deleted
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");
        //image deleted now delete from database
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue(); //removes values from firebase where pid matches

                }
                //deleted
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    static class MyHolder extends RecyclerView.ViewHolder {

        //views from row_post.xml
        ImageView pImageIv;
        CircleImageView uPictureIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv,stamp;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, saveBtn, shareBtn;
        LinearLayout profileLayout;
CardView view1;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);

            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            saveBtn = itemView.findViewById(R.id.saveButton);
            shareBtn = itemView.findViewById(R.id.readMoreButton);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            stamp = itemView.findViewById(R.id.stamp);
            view1 = itemView.findViewById(R.id.postsCard);

        }
    }
    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    private static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }


}
