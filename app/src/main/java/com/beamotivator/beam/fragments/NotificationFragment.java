package com.beamotivator.beam.fragments;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.beamotivator.beam.R;
import com.beamotivator.beam.adapters.AdapterNotifications;
import com.beamotivator.beam.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class NotificationFragment extends Fragment {

    //views
    RecyclerView notificationRv;
    private FirebaseAuth firebaseAuth;

    ArrayList<ModelNotification> notificationList;

    AdapterNotifications adapterNotifications;
    TextView emptyMesg;

    String myId = "";
    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            Drawable background = getActivity().getResources().getDrawable(R.drawable.main_gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getActivity().getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(getActivity().getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);

        }
        View view = inflater.inflate(R.layout.fragment_notification,container,false);


        //init views
        notificationRv = view.findViewById(R.id.notificationsRv);
        firebaseAuth = FirebaseAuth.getInstance();
        myId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        emptyMesg = view.findViewById(R.id.notificationsEmpty);
        notificationList = new ArrayList<>();

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {//linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newest posts, load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set this layout to recycler view
        notificationRv.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Extras");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationList.clear();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            //get data
                            ModelNotification model = ds.getValue(ModelNotification.class);


                            //add to list
                            notificationList.add(model);

                            //set adapter
                            adapterNotifications = new AdapterNotifications(getActivity(),notificationList);

                            //set to recycler view
                            notificationRv.setAdapter(adapterNotifications);

                        }
                        if(notificationList.size() == 0)
                        {
                            emptyMesg.setVisibility(View.VISIBLE);
                            notificationRv.setVisibility(View.GONE);
                        }
                        else{
                            emptyMesg.setVisibility(View.GONE);
                            notificationRv.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}