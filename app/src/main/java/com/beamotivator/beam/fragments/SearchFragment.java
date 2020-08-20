package com.beamotivator.beam.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.beamotivator.beam.GroupCreateActivity;
import com.beamotivator.beam.MainActivity;
import com.beamotivator.beam.R;
import com.beamotivator.beam.SettingsActivity;
import com.beamotivator.beam.adapters.AdapterMyGroups;
import com.beamotivator.beam.adapters.AdapterUsers;
import com.beamotivator.beam.models.ModelMyGroups;
import com.beamotivator.beam.models.ModelUser;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {
    //Setting views
    RecyclerView recyclerView , groupsRv;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    FirebaseAuth firebaseAuth;

    //views for groups
    List<ModelMyGroups> groupsList;
    AdapterMyGroups adapterGroups;

    //Tab details
    EditText searchEt;
    TabLayout searchTabs;
    TabItem userTab, groupTab;
    ViewPager tabView;
    int limit = 2;


    public SearchFragment() {
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);


        //init user recyclerview
        recyclerView = view.findViewById(R.id.search_recyclerView);
        firebaseAuth = FirebaseAuth.getInstance();

        //init group recyclerview
        groupsRv = view.findViewById(R.id.myGroupsRv);
        firebaseAuth = FirebaseAuth.getInstance();

        //search bar
        searchEt = view.findViewById(R.id.searchEt);
        //init tab views
        searchTabs = view.findViewById(R.id.searchTabs);
        userTab = view.findViewById(R.id.userTab);
        groupTab = view.findViewById(R.id.groupTab);


        searchTabs.bringToFront();


        //init user list
        userList = new ArrayList<>();

        //init group list
        groupsList = new ArrayList<>();

        getAllUSers();

        searchTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                tabView.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.VISIBLE);
                         groupsRv.setVisibility(View.GONE);
                        //get all users
                        getAllUSers();
                        break;
                    case 1:
                        Toast.makeText(getContext(), "2", Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        groupsRv.setVisibility(View.VISIBLE);

                        //get all groups
                        loadMyGroups();
                        break;



                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });










        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
                searchGroup(s.toString());
                Toast.makeText(getContext(), "Changed", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;

    }
    private void getAllUSers() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);

         recyclerView.setLayoutManager(layoutManager);
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        //get all data  from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds:dataSnapshot .getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all users except currently signed in user
                    if(!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),userList);

                    //Set adapter to recycler view


                   recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void searchUser(final String query) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        //get all data  from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all searched users except currently signed in user
                    if (!modelUser.getUid().equals(fUser.getUid())) {

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(modelUser);
                        }
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);

                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();

                    //Set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


        private void searchGroup(final String query) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
            //layoutManager.setReverseLayout(true);
            //layoutManager.setStackFromEnd(true);

            groupsRv.setLayoutManager(layoutManager);
            //get current user
            //final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

            //get path of database named "Users" containing users info
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

            //get all data  from path
            ref.orderByChild("groupTitle").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    groupsList.clear();
                    for(DataSnapshot ds:dataSnapshot .getChildren()){
                        ModelMyGroups modelGroup = ds.getValue(ModelMyGroups.class);


                            if(modelGroup.getGroupTitle().toLowerCase().contains(query.toLowerCase()
                                    )){
                                groupsList.add(modelGroup);
                            }


                        //adapter
                        adapterGroups = new AdapterMyGroups(getActivity(),groupsList);

                        //refresh adapter
                        adapterGroups.notifyDataSetChanged();

                        //Set adapter to recycler view
                        groupsRv.setAdapter(adapterGroups);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



    }
    private void loadMyGroups() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);

          groupsRv.setLayoutManager(layoutManager);



        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Groups");

        ref.orderByChild("groupTitle").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupsList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){

                    ModelMyGroups mGroups = ds.getValue(ModelMyGroups.class);

                    //add group
                    groupsList.add(mGroups);


                    //init adapter
                    adapterGroups = new AdapterMyGroups(getActivity(),groupsList);

                    //set adapter to recycler view
                     groupsRv.setAdapter(adapterGroups);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(false); //to show options menu in fragment
        super.onCreate(savedInstanceState);
    }


    //inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main,menu);

        //Search View
        MenuItem item = menu.findItem(R.id.searchAction);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text
                    searchUser(s);

                }
                else if(!TextUtils.isEmpty(s.trim())) {
                    //search text contains group names
                    loadMyGroups();
                }
                else  {
                    //search text empty get all users
                    getAllUSers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user press any single letter

                //called when user press search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text
                    searchUser(s);

                }
                else if(!TextUtils.isEmpty(s.trim())) {
                    //search text contains group names
                    loadMyGroups();
                }
                else{
                    //load all users and groups
                    getAllUSers();

                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    //handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get items id
        int id = item.getItemId();
        if(id == R.id.actionLogout)
        {
            firebaseAuth.signOut();
            checkuserstatus();
        }
        else if(id == R.id.action_settings)
        {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if(id == R.id.action_create_group)
        {
            startActivity(new Intent(getActivity(),  GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkuserstatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null)
        {
            //stay here signed in
        }
        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
        }
    }
}