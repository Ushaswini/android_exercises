package com.ushaswini.tripplanner;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;

/**
 * Vinnakota Venkata Ratna Ushaswini
 * TabFriends
 * 18/04/2017
 */

public class TabFriends extends Fragment {

    ListView lt_friends;
    ListView lt_add_friends;

    User currentUser;

    AdapterFriends friendsAdapter;
    AdapterFriends addFriendsAdapter;

    ArrayList<User> friends;
    ArrayList<User> addFriends;
    ArrayList<String> friendUids;
    ArrayList<User> allUsers;



    DatabaseReference friendsDatabaseReference;
    String uid = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_friends, container, false);
        currentUser = (User) getArguments().get("currentUser");

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        friends = new ArrayList<>();
        addFriends = new ArrayList<>();
        friendUids = new ArrayList<>();
        allUsers = new ArrayList<>();

        lt_friends = (ListView) getView().findViewById(R.id.lt_your_friends);
        lt_add_friends = (ListView) getView().findViewById(R.id.lt_add_friends);

        friendsAdapter = new AdapterFriends(getContext(),R.layout.custom_friend_row,friends,true);
        lt_friends.setAdapter(friendsAdapter);
        friendsAdapter.setNotifyOnChange(true);

        addFriendsAdapter = new AdapterFriends(getContext(),R.layout.custom_friend_row,addFriends,false);
        lt_add_friends.setAdapter(addFriendsAdapter);
        addFriendsAdapter.setNotifyOnChange(true);

        if(currentUser != null){
            uid = currentUser.getUid();
        }

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference();

        friendsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String friendUid;
                if(dataSnapshot.child("users/" + uid  + "/friends").exists()){
                    for(DataSnapshot data : dataSnapshot.child("friends").getChildren()){
                        friendUid = (String) data.getValue();
                        friendUids.add(friendUid);
                    }
                }

                User user;

                for(DataSnapshot data : dataSnapshot.child("users").getChildren()){
                    user = data.getValue(User.class);
                    allUsers.add(user);
                }


                separateFriends();



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void separateFriends(){
        for(User user : allUsers){
            if(!friendUids.contains(user.getUid()) && !user.getUid().equals(uid)){
                addFriends.add(user);
                //allUsers.remove(user);
            }
        }

        friendsAdapter.notifyDataSetChanged();
        addFriendsAdapter.notifyDataSetChanged();
    }
}
