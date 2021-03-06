package com.ushaswini.tripplanner;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TabbedActivity extends AppCompatActivity implements TabSettings.handleSaveChanges,
                                                                TabTrips.TripListner,
                                                                AdapterFriends.IHandleConnect,
                                                                AdapterNotifications.IShareData
{


    final int ACTIVITY_SELECT_IMAGE = 1234;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference postsDatabaseReference;
    StorageReference storageReference;
    FirebaseAuth.AuthStateListener mAuthListener;

    ValueEventListener databaseChangeListener;

    User user;
    FirebaseUser currentUser;
    TabLayout tabLayout;
    Uri imageUri;
    String fName,lName;
    TabFriends tabFriends;
    TabSettings tabSettings;
    TabTrips tabTrips;
    TabNotifications tabNotifications;
    ArrayList<TripDetails> trips;
    ArrayList<User> friends;
    String currentUserUid;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    public boolean isConnectedOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (null != ni){
            if(ni.isConnected()){
                return true;
            }else{
                return false;
            }

        } else {
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        setTitle("Trip Planner");

        try{


            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            tabLayout = (TabLayout) findViewById(R.id.tablayout);
            tabLayout.setupWithViewPager(mViewPager);


            tabLayout.getTabAt(1).setIcon(R.drawable.ic_trip);
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_friends);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_notifications);
            tabLayout.getTabAt(3).setIcon(R.drawable.ic_settings);

            mFirebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            storageReference = FirebaseStorage.getInstance().getReference();

            trips = new ArrayList<>();

            currentUser = mFirebaseAuth.getCurrentUser();

            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            if(currentUser != null){
                currentUserUid = currentUser.getUid();
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                try{
                    currentUser = firebaseAuth.getCurrentUser();

                    if(currentUser != null){
                        currentUserUid = currentUser.getUid();
                    }
                }catch (Exception e){
                    Toast.makeText(TabbedActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }


            }
        };

        databaseChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{
                    if(currentUserUid != null){
                        if(dataSnapshot.child("users").child(currentUserUid).exists()){
                            user = dataSnapshot.child("users").child(currentUserUid).getValue(User.class);
                            Log.d("user",user.toString());
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(TabbedActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        try{
            getMenuInflater().inflate(R.menu.menu_tabbed, menu);

            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.menu_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));


            return true;
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }
return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


try{
    FirebaseAuth.getInstance().signOut();
    Intent i=new Intent(TabbedActivity.this,LoginActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
    return true;
}catch (Exception e){
    Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
}
return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        databaseReference.addValueEventListener(databaseChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
        databaseReference.removeEventListener(databaseChangeListener);
    }

    @Override
    public void changeImage() {
        try{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),ACTIVITY_SELECT_IMAGE);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void saveChanges(final String fName, final String lName, final User.GENDER gender) {
        try{
            this.fName = fName;
            this.lName = lName;
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fName + ", " + lName)
                    .build();

            currentUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        user.setfName(fName);
                        user.setlName(lName);
                        user.setGender(gender);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + currentUserUid + "/fName" ,fName);
                        childUpdates.put("/users/" + currentUserUid + "/lName",lName);
                        childUpdates.put("/users/" + currentUserUid + "/gender", gender);

                        databaseReference.updateChildren(childUpdates);

                        Toast.makeText(TabbedActivity.this, "User Details updated", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void changePassword(String oldPassword, final String newPassword) {
        try{
            if(!newPassword.equals(oldPassword)){


                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentUser.getEmail(), oldPassword);

// Prompt the user to re-provide their sign-in credentials
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(TabbedActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();

                                                Intent intent = new Intent(TabbedActivity.this,LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);

                                                Log.d("demo", "Password updated");
                                            } else {
                                                Toast.makeText(TabbedActivity.this, "Error password not updated.", Toast.LENGTH_SHORT).show();
                                                Log.d("demo", "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(TabbedActivity.this, "Error. auth failed", Toast.LENGTH_SHORT).show();
                                    Log.d("demo", "Error auth failed");
                                }
                            }
                        });
            }else{
                Toast.makeText(this, "New Password cannot be same as old password.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }




    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if (requestCode == ACTIVITY_SELECT_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                            changeProfileImage(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    void changeProfileImage(Bitmap bitmap){


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] dataArray = baos.toByteArray();

        StorageReference reference = storageReference.child("profile_images/" + currentUserUid + ".png");

        UploadTask uploadTask = reference.putBytes(dataArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                imageUri = taskSnapshot.getDownloadUrl();

                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(imageUri)
                        .build();

                currentUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            tabSettings.postCurrentUserImage(imageUri.toString());
                            user.setImageUrl(taskSnapshot.getDownloadUrl().toString());

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + currentUserUid + "imageUrl",imageUri);
                            databaseReference.updateChildren(childUpdates);

                            Toast.makeText(TabbedActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });
    }

    @Override
    public void addTrip() {
        try{
            Intent intent = new Intent(TabbedActivity.this,AddTripActivity.class);
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void addFriend(final User friendUser, final View v) {

        try{
            new AlertDialog.Builder(this)
                    .setTitle("Add Friend")
                    .setMessage("Do you really want to add this friend")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            user.addToSentFriendRequestUid(friendUser.getUid());
                            friendUser.addToReceivedFriendRequestUid(user.getUid());

                            Map<String, Object> postCurrentUser = user.toMap();
                            Map<String, Object> postUser = friendUser.toMap();



                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
                            childUpdates.put("/users/" + friendUser.getUid(),postUser);

                            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    ((ImageButton)v).setImageResource(R.mipmap.ic_sent);

                                }
                            });

                            Toast.makeText(TabbedActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }






    }

    @Override
    public void displayReceivedMessage(User friend) {

        try{
            user.addToSentFriendRequestUid(friend.getUid());
            friend.addToReceivedFriendRequestUid(user.getUid());

            Map<String, Object> postCurrentUser = user.toMap();
            Map<String, Object> postUser = friend.toMap();



            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
            childUpdates.put("/users/" + friend.getUid(),postUser);

            databaseReference.updateChildren(childUpdates);

            Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void displaySentMessage() {
        Toast.makeText(this, "Friend Request sent already.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void removeFriend(final User friendUser) {
        try{
            new AlertDialog.Builder(this)
                    .setTitle("Remove Friend")
                    .setMessage("Do you really want to remove this friend")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            user.removeFriendUid(friendUser.getUid());
                            friendUser.removeFriendUid(user.getUid());

                            Map<String, Object> postCurrentUser = user.toMap();
                            Map<String, Object> postUser = friendUser.toMap();



                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
                            childUpdates.put("/users/" + friendUser.getUid(),postUser);

                            databaseReference.updateChildren(childUpdates);

                            //Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
                            Toast.makeText(TabbedActivity.this, "Removed from friend list.", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }




    }

    @Override
    public void selectFriend(int position, View v) {

    }

    @Override
    public void removeFriendFromTrip(int position) {

    }

    @Override
    public void handleFriendRequest(User friend, boolean accept) {

        try{
            if(accept){
                user.removeFromReceivedRequestUid(friend.getUid());
                user.addFriendUid(friend.getUid());

                friend.addFriendUid(currentUserUid);
                friend.removeFromSentFriendRequestUid(currentUserUid);

                Map<String, Object> postCurrentUserValues = user.toMap();
                Map<String, Object> postFriendValues = friend.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("users/" + currentUserUid ,postCurrentUserValues);
                childUpdates.put("users/" + friend.getUid(),postFriendValues);
                databaseReference.updateChildren(childUpdates);
            }else{
                user.removeFromReceivedRequestUid(friend.getUid());

                friend.removeFromSentFriendRequestUid(currentUserUid);

                Map<String, Object> postCurrentUserValues = user.toMap();
                Map<String, Object> postFriendValues = friend.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("users/" + currentUserUid ,postCurrentUserValues);
                childUpdates.put("users/" + friend.getUid(),postFriendValues);
                databaseReference.updateChildren(childUpdates);
            }

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }



    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            try{
                Bundle bundle = new Bundle();
                bundle.putSerializable("currentUser",currentUserUid);


                switch(position){
                    case 0: tabFriends = new TabFriends();
                        tabFriends.setArguments(bundle);
                        return tabFriends;

                    case 1: {
                        tabTrips = new TabTrips();
                        tabTrips.setArguments(bundle);
                        return tabTrips;

                    }
                    case 2: tabNotifications = new TabNotifications();
                        return tabNotifications;
                    case 3: {
                        tabSettings = new TabSettings();
                        tabSettings.setArguments(bundle);
                        return tabSettings;
                    }

                    default: return null;
                }
            }catch (Exception e){
                Toast.makeText(TabbedActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
            }
            return null;


        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }


    }
}
