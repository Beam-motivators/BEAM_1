package com.beamotivator.beam.fragments;


import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beamotivator.beam.GroupCreateActivity;
import com.beamotivator.beam.R;
import com.beamotivator.beam.adapters.AdapterMyGroups;
import com.beamotivator.beam.models.ModelMyGroups;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MyGroupFragment extends Fragment {

    RecyclerView groupsRv;
    List<ModelMyGroups> groupsList;

    AdapterMyGroups adapterGroups;
    //firebase
    FirebaseAuth firebaseAuth;

    Button createBtn;
    TextView groupEmpty;

    public MyGroupFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_my_group, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        createBtn = view.findViewById(R.id.createBtn);

        groupsRv = view.findViewById(R.id.myGroupsRv);
        groupEmpty = view.findViewById(R.id.groupsMessage);
        groupsList = new ArrayList<>();

        loadMyGroups();

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin");

        adminRef.orderByChild("uid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String myUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                    if(myUid.equals(Objects.requireNonNull(ds.child("uid").getValue()).toString())){
                        createBtn.setVisibility(View.VISIBLE);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), GroupCreateActivity.class));
            }
        });
        return view ;
    }

    private void loadMyGroups() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
         groupsRv.setLayoutManager(layoutManager);



        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Groups");

        ref.orderByChild("groupTitle").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupsList.clear();
                String myUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("Participants").hasChild(myUid)) {
                        ModelMyGroups mGroups = ds.getValue(ModelMyGroups.class);

                        //add group
                        groupsList.add(mGroups);


                        //init adapter
                        adapterGroups = new AdapterMyGroups(getActivity(), groupsList);

                        //set adapter to recycler view
                        groupsRv.setAdapter(adapterGroups);

                    }
                    if (groupsList.size() == 0) {
                        groupsRv.setVisibility(View.GONE);
                        groupEmpty.setVisibility(View.VISIBLE);

                    }
                }
                if (groupsList.size() == 0) {
                    groupsRv.setVisibility(View.GONE);
                    groupEmpty.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}