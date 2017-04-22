package com.ushaswini.tripplanner;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Vinnakota Venkata Ratna Ushaswini
 * TabNotifications
 * 18/04/2017
 */

public class TabNotifications extends Fragment {

    User currentUser;

    ListView lt_notifications;

    ArrayAdapter<User> adapter;

    ArrayList<String> notifications;

    ArrayList<String> uids;
    ArrayList<User> users;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    String currentUserUid;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_notifications, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lt_notifications = (ListView)getView().findViewById(R.id.lt_notifications);
        notifications = new ArrayList<>();
        uids = new ArrayList<>();
        users = new ArrayList<>();

        adapter = new ArrayAdapter<User>(getContext(),android.R.layout.simple_list_item_1,users);
        lt_notifications.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserUid = firebaseAuth.getCurrentUser().getUid();

        lt_notifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Do you want to add to your friend's list")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                currentUser.removeFromReceivedRequestUid(uids.get(position));
                                currentUser.addFriendUid(uids.get(position));

                                User friend = users.get(position);
                                friend.addFriendUid(currentUserUid);
                                friend.removeFromSentFriendRequestUid(currentUserUid);

                                Map<String, Object> postCurrentUserValues = currentUser.toMap();
                                Map<String, Object> postFriendValues = friend.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("users/" + currentUserUid ,postCurrentUserValues);
                                childUpdates.put("users/" + friend.getUid(),postFriendValues);
                                databaseReference.updateChildren(childUpdates);



                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uids.clear();
                notifications.clear();
                users.clear();

                if(currentUserUid != null){
                    if(dataSnapshot.child("users").child(currentUserUid).exists()){
                        currentUser = dataSnapshot.child("users").child(currentUserUid).getValue(User.class);
                        Log.d("user",currentUser.toString());
                    }

                    if(dataSnapshot.child("users").child(currentUserUid).child("receivedFriendRequestUids").exists()){
                        String uid;
                        for(DataSnapshot data : dataSnapshot.child("users").child(currentUserUid).child("receivedFriendRequestUids").getChildren()){
                            uid = (String) data.getValue();
                            uids.add(uid);
                        }
                    }

                    User user;
                    for(String uid : uids){

                        user = dataSnapshot.child("users").child(uid).getValue(User.class);
                        users.add(user);
                        /*String fName = (String) dataSnapshot.child("users").child(uid).child("fName").getValue();
                        String lName = (String) dataSnapshot.child("users").child(uid).child("lName").getValue();

                        String notification = "Received friend request from " + fName + "," + lName;
                        notifications.add(notification);*/
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    void parseNotifications(){

        for(String uid : uids){

        }
    }
}
