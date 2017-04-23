package com.ushaswini.tripplanner;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Vinnakota Venkata Ratna Ushaswini
 * TabTrips
 * 18/04/2017
 */

public class TabTrips extends Fragment {

    User currentUser;

    FloatingActionButton addTrip;

    ListView lv_your_trips;
    ListView lv_friend_trips;

    AdapterCustomTrip your_adapter;
    AdapterCustomTrip friend_adapter;

    private TripListner mListener;

    ArrayList<TripDetails> your_trips;
    ArrayList<TripDetails> friends_trips;
    ArrayList<TripDetails> listOfTrips;

    ArrayList<String> friendsUids;
    ArrayList<String> tripUids;



    DatabaseReference tripsDatabaseReference;
    String uid = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_trips, container, false);
        currentUser = (User) getArguments().get("currentUser");

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        your_trips = new ArrayList<>();
        friends_trips = new ArrayList<>();
        listOfTrips = new ArrayList<>();

        friendsUids = new ArrayList<>();
        tripUids = new ArrayList<>();

        addTrip = (FloatingActionButton) getView().findViewById(R.id.fab);
        lv_your_trips = (ListView) getView().findViewById(R.id.your_trips);
        lv_friend_trips = (ListView) getView().findViewById(R.id.friends_trips);

        if(currentUser != null){
            uid = currentUser.getUid();
        }

        your_adapter = new AdapterCustomTrip(getContext(),R.layout.custom_trip_row,your_trips,false);
        lv_your_trips.setAdapter(your_adapter);
        your_adapter.setNotifyOnChange(true);

        friend_adapter = new AdapterCustomTrip(getContext(),R.layout.custom_trip_row,friends_trips,true);
        lv_friend_trips.setAdapter(friend_adapter);
        friend_adapter.setNotifyOnChange(true);

        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mListener.addTrip();
            }
        });

        tripsDatabaseReference = FirebaseDatabase.getInstance().getReference();



        tripsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("users").child(uid).exists()){

                    friendsUids.clear();
                    tripUids.clear();
                    friends_trips.clear();
                    your_trips.clear();

                    String friendUid;
                    for(DataSnapshot data : dataSnapshot.child("users").child(uid).child("friendsUids").getChildren()){
                        friendUid = (String) data.getValue();
                        friendsUids.add(friendUid);
                    }
                }




                if(dataSnapshot.child("users").child(uid).child("tripUids").exists()){

                    TripDetails trip;
                    String tripId;
                    for(DataSnapshot data : dataSnapshot.child("users").child(uid).child("tripUids").getChildren() ){
                        tripId = (String) data.getValue();
                        tripUids.add(tripId);
                        trip = dataSnapshot.child("trips").child(tripId).getValue(TripDetails.class);
                        if(trip != null){
                            your_trips.add(trip);
                        }



                        //friends_trips.remove(trip);
                    }
                }

                for(String friendUid : friendsUids){
                    if(dataSnapshot.child("users").child(friendUid).child("tripUids").exists()){
                        String tripUid;
                        TripDetails trip;

                        for(DataSnapshot data : dataSnapshot.child("users").child(friendUid).child("tripUids").getChildren()){
                            tripUid  = (String) data.getValue();

                            if(!tripUids.contains(tripUid)){
                                trip = dataSnapshot.child("trips").child(tripUid).getValue(TripDetails.class);
                                friends_trips.add(trip);
                            }


                        }
                    }

                }





                your_adapter.notifyDataSetChanged();
                friend_adapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*tripsDatabaseReference = FirebaseDatabase.getInstance().getReference("trips");

        tripsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                your_trips.clear();
                friends_trips.clear();
                listOfTrips.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    TripDetails trip = data.getValue(TripDetails.class);
                    listOfTrips.add(trip);

                }
                separateListOfTrips();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }



    private void separateListOfTrips(){
        for(TripDetails trip : listOfTrips){
            if(trip.getFriendsUids().contains(uid)){
                your_trips.add(trip);
            }else{
                friends_trips.add(trip);
            }
        }

        your_adapter.notifyDataSetChanged();
        friend_adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TabSettings.handleSaveChanges) {
            mListener = (TripListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void postYourTrips(ArrayList<TripDetails> trips){
        this.your_trips = trips;
        your_adapter.notifyDataSetChanged();
    }

    interface TripListner{
        void addTrip();
    }
}
