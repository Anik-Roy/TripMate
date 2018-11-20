package com.example.anik.tripmate;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SingleUserInfo extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;


    String userId = "", userEmail = "";
    TextView details, aboutText, speakText, interestText, travelingText, countryText, genderText;
    TextView birthdayText, hometownText, occupationText, websiteText;
    TextView about, speaks, interest, traveling, country, gender, birthday, hometown, occupation;
    TextView website;

    Button mProfileSendReqButton, messageButton, trackFriend, mProfileDeclineReqButton;

    List<PhotoAlbum> photoList;
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    ImageView leftArrow, rightArrow;
    TextView id;

    String visitedCountry = "";
    int pos = 0;
    private String mCurrent_state;
    String _Location;
    String uid;

    Location locations;
    private FusedLocationProviderClient mFusedLocationClient;
    ProgressDialog mProgressDialog;

    UserInfo userInfo;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user_info);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final Intent data = getIntent();
        uid = data.getStringExtra("uid");

        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");

        mCurrent_state = "not_friends";

        mProfileSendReqButton = (Button) findViewById(R.id.sendReqButton);
        messageButton = (Button) findViewById(R.id.messageButton);
        trackFriend = (Button) findViewById(R.id.trackFriendLocation);
        mProfileDeclineReqButton = (Button) findViewById(R.id.declineReq);

        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
        mProfileDeclineReqButton.setEnabled(false);

        mProfileSendReqButton.setOnClickListener(this);
        messageButton.setOnClickListener(this);
        trackFriend.setOnClickListener(this);
        mProfileDeclineReqButton.setOnClickListener(this);

        linearLayout = (LinearLayout) findViewById(R.id.infoLinearLayout);

        leftArrow = (ImageView) findViewById(R.id.slideLeft);
        rightArrow = (ImageView) findViewById(R.id.slideRight);

        photoList = new ArrayList<>();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this, photoList);
        viewPager.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    userId = firebaseUser.getUid();
                    userEmail = firebaseUser.getEmail();
                } else {
                    Log.i("Email", "No User");
                }
            }
        };

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.arrowScroll(View.FOCUS_LEFT);
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.arrowScroll(View.FOCUS_RIGHT);
            }
        });

//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setTitle("Loading User Data...");
//        mProgressDialog.setMessage("Please wait...User data is loading.");
//        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.show();

        mCurrentUser = auth.getCurrentUser();

        if(mCurrentUser != null && mCurrentUser.getUid().equals(uid)) {
            String f = "Followers";
            String ff = "Following";
            mProfileSendReqButton.setText(f);
            messageButton.setText(ff);
            trackFriend.setText("Track Me");
        }

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                userInfo = snapshot.getValue(UserInfo.class);

                try {
                    if (userInfo.getName() != null) {
                        toolbar.setTitle(userInfo.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(userInfo.getProfilephoto() != null) {
                    Map profilePhoto = userInfo.getProfilephoto();

                    Iterator it = profilePhoto.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        Uri uri = Uri.parse(pair.getValue().toString());

                        if (uri != null) {
                            PhotoAlbum album = new PhotoAlbum();
                            album.setThumbnail(uri);
                            photoList.add(album);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                else {
                    Uri uri = Uri.parse("android.resource://com.example.anik.tripmate/drawable/noimage");
                    PhotoAlbum album = new PhotoAlbum();
                    album.setThumbnail(uri);
                    photoList.add(album);
                    adapter.notifyDataSetChanged();
                }

                setUserInfo();

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(uid)) {
                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if(req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileSendReqButton.setText("Accept Follow Request.");
                                mProfileDeclineReqButton.setVisibility(View.VISIBLE);
                                mProfileDeclineReqButton.setEnabled(true);
                            }

                            else if(req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqButton.setText("Cancel Follow Request");
                            }

                            //mProgressDialog.dismiss();
                        }

                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqButton.setText("UNFOLLOW");

                                        //mProgressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //mProgressDialog.dismiss();
                                }
                            });
                            //mProgressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //mProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });
    }

    private void setUserInfo() {
        linearLayout.removeAllViews();

        if (userInfo.getAbout() != null) {
            TextView dtt = new TextView(this);
            dtt.setText("Details:\n");
            dtt.setId(0);
            dtt.setTextColor(Color.BLACK);
            dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(dtt);
            flag = true;

            TextView tt = new TextView(this);
            tt.setText("About:\n");
            tt.setId(11);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(userInfo.getAbout()+"\n");
            tv.setId(1);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getLanguage() != null) {

            TextView dtt = null;
            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Speaks:\n");
            tt.setId(22);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(userInfo.getLanguage()+"\n");
            tv.setId(2);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getInterest() != null) {

            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Interest:\n");
            tt.setId(33);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(userInfo.getInterest()+"\n");
            tv.setId(3);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getTravel() != null) {
            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Why I'm on Trip Mate Finder:\n");
            tt.setId(44);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(userInfo.getTravel()+"\n");
            tv.setId(4);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getVisitedCountry() != null) {
            String d = userInfo.getVisitedCountry();
            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Country I'v visited:\n");
            tt.setId(55);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(5);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getGender() != null) {
            String d = userInfo.getGender();

            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Gender:\n");
            tt.setId(66);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(6);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getBirthday() != null) {
            String d = userInfo.getBirthday();
            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Birthday:\n");
            tt.setId(77);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(7);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getHometown() != null) {
            String d = userInfo.getHometown();
            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Hometown:\n");
            tt.setId(88);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(8);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getOccupation() != null) {
            String d = userInfo.getOccupation();
            TextView dtt = null;
            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Occupation:\n");
            tt.setId(99);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(9);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
            setTextSize(dtt, tt, tv);
        }

        if (userInfo.getWebsite() != null) {
            String d = userInfo.getWebsite();
            TextView dtt = null;

            if(!flag) {
                dtt = new TextView(this);
                dtt.setText("Details:\n");
                dtt.setId(0);
                dtt.setTextColor(Color.BLACK);
                dtt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(dtt);
                flag = true;
            }

            TextView tt = new TextView(this);
            tt.setText("Website:\n");
            tt.setId(1010);
            tt.setTextColor(Color.BLACK);
            tt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tt);

            TextView tv = new TextView(this);
            tv.setText(d+"\n");
            tv.setId(10);
            tv.setTextColor(Color.parseColor("#37474F"));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
            setTextSize(dtt, tt, tv);
            final String url = tv.getText().toString();

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (URLUtil.isValidUrl(url)) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else {
                        Toast.makeText(SingleUserInfo.this, "Invalid website address", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setTextSize(TextView dtt, TextView tt, TextView tv) {
        if(dtt != null && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            dtt.setTextSize(34);
            tt.setTextSize(32);
            tv.setTextSize(30);
        }
        else if(dtt != null && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            dtt.setTextSize(30);
            tt.setTextSize(28);
            tv.setTextSize(26);
        }

        else if(dtt != null && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            dtt.setTextSize(18);
            tt.setTextSize(14);
            tv.setTextSize(16);
        }

        else if(dtt != null && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            dtt.setTextSize(8);
            tt.setTextSize(6);
            tv.setTextSize(4);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    Intent mapIntent;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.declineReq) {
            mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(SingleUserInfo.this, "Request deleted successfully", Toast.LENGTH_SHORT).show();
                                            mCurrent_state = "not_friends";
                                            mProfileSendReqButton.setText("FOLLOW");
                                            mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                            mProfileDeclineReqButton.setEnabled(false);
                                        }
                                    });
                            mProfileSendReqButton.setEnabled(true);
                        }
                    });
        }

        if(v.getId() == R.id.sendReqButton) {
            mProfileSendReqButton.setEnabled(false);

            // ------------ Not Friends ----------------
            if(mCurrent_state.equals("not_friends")) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference newDatabaseReference = rootRef.child("notification").child(uid).push();

                final String newNotificationId = newDatabaseReference.getKey();

                //HashMap<String, String> notificationData = new HashMap<>();
                Map notificationData = new HashMap();
                notificationData.put("from", mCurrentUser.getUid());
                notificationData.put("type", "request");
                notificationData.put("timestamp", ServerValue.TIMESTAMP);

                Map<String, Object> requestMap = new HashMap<String, Object>();
                requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uid + "/request_type", "sent");
                requestMap.put("Friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type", "received");
                requestMap.put("notification/" + uid + "/" + newNotificationId, notificationData);

                //rootRef.child("notification").child(uid).child(newNotificationId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(SingleUserInfo.this, "There was an error in sending request", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SingleUserInfo.this, "Request Sent Successflly.", Toast.LENGTH_SHORT).show();
                            //FirebaseDatabase.getInstance().getReference().child("notification").child(uid).child(newNotificationId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            mCurrent_state = "req_sent";
                            mProfileSendReqButton.setText("Cancel Follow Request.");
                            mProfileSendReqButton.setEnabled(true);
                        }
                    }
                });
            }

            // ------------ Cancel Friend Request ----------------
            if(mCurrent_state.equals("req_sent")) {
                mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(SingleUserInfo.this, "Request canceled successfully", Toast.LENGTH_SHORT).show();
                                                mCurrent_state = "not_friends";
                                                mProfileSendReqButton.setText("FOLLOW");
                                            }
                                        });
                                mProfileSendReqButton.setEnabled(true);
                            }
                        });
            }

            // ------------ Accept Friend Request --------------

            if(mCurrent_state.equals("req_received")) {
                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

                DatabaseReference newDatabaseReference = rootRef.child("notification").child(uid).push();

                final String newNotificationId = newDatabaseReference.getKey();

                //HashMap<String, String> notificationData = new HashMap<>();
                Map notificationData = new HashMap();

                notificationData.put("from", mCurrentUser.getUid());
                notificationData.put("type", "accepted");
                notificationData.put("timestamp", ServerValue.TIMESTAMP);

                Map friendsMap = new HashMap();
                friendsMap.put("Friends/"+mCurrentUser.getUid()+"/"+uid+"/date", currentDate);
                friendsMap.put("Friends/"+uid+"/"+mCurrentUser.getUid()+"/date", currentDate);

                friendsMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+uid, null);
                friendsMap.put("Friend_req/"+uid+"/"+mCurrentUser.getUid(), null);
                friendsMap.put("notification/" + uid + "/" + newNotificationId, notificationData);


                //rootRef.child("notification").child(uid).child(newNotificationId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            Toast.makeText(SingleUserInfo.this, "Request accepted.", Toast.LENGTH_SHORT).show();
                            //FirebaseDatabase.getInstance().getReference().child("notification").child(uid).child(newNotificationId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            mCurrent_state = "friends";
                            mProfileSendReqButton.setText("UNFOLLOW");
                            mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                            mProfileDeclineReqButton.setEnabled(false);
                            mProfileSendReqButton.setEnabled(true);
                        }
                        else {
                            String errorMessage = databaseError.getMessage();
                            Toast.makeText(SingleUserInfo.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if(mCurrent_state.equals("friends")) {

                Map unfriendMap = new HashMap();
                unfriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+uid, null);
                unfriendMap.put("Friends/"+uid+"/"+mCurrentUser.getUid(), null);

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            Toast.makeText(SingleUserInfo.this, "Successfully Unfriend !", Toast.LENGTH_SHORT).show();
                            mCurrent_state = "not_friends";
                            mProfileSendReqButton.setText("Follow Request.");
                            mProfileSendReqButton.setEnabled(true);
                        }
                        else {
                            String errorMessage = databaseError.getMessage();
                            Toast.makeText(SingleUserInfo.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        else if(v.getId() == R.id.messageButton) {
            Intent intent = new Intent(SingleUserInfo.this, ChatActivity.class);
            intent.putExtra("uid", uid);

            if(userInfo.getName() != null) {
                intent.putExtra("name", userInfo.getName());
            }

            else {
                intent.putExtra("name", userInfo.getEmail());
            }

            startActivity(intent);

        }

        else if(v.getId() == R.id.trackFriendLocation) {

            if(mCurrentUser.getUid().equals(uid)) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }

                if(checkGPSenabled()) {
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        locations = location;
                                        getUserLocation();
                                    }
                                }
                            });
                }
            }
            else if(mCurrent_state.equals("friends")){
                DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                        assert userInfo != null;

                        if(userInfo.getLocationShare() != null && userInfo.getLocationShare().equals("true") && userInfo.getLatitude() != null && userInfo.getLongitude() != null) {
                            Double longitude = Double.parseDouble(userInfo.getLongitude());
                            Double latitude = Double.parseDouble(userInfo.getLatitude());
                            mapIntent = new Intent(SingleUserInfo.this, MapsActivity.class);
                            mapIntent.putExtra("longitude", longitude);
                            mapIntent.putExtra("latitude", latitude);
                            mapIntent.putExtra("uid", uid);

                            if(userInfo.getName() == null)
                                mapIntent.putExtra("name", userInfo.getEmail());
                            else
                                mapIntent.putExtra("name", userInfo.getName());

                            startActivity(mapIntent);
                        }

                        else {
                            Toast.makeText(SingleUserInfo.this, "User not sharing his location.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            else {
                Toast.makeText(this, "You should be friend with him/her to track friend", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public boolean checkGPSenabled() {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled || !network_enabled) {
            // notify user
            final AlertDialog diag = new AlertDialog.Builder(this)
                    .setTitle("Enable GPS Services...")
                    .setView(R.layout.alart_dialog_gps_enable)
                    .create();

            diag.show();

            Button cancelButton = (Button) diag.findViewById(R.id.cencelButton);
            Button enableGps = (Button) diag.findViewById(R.id.enableButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SingleUserInfo.this, "GPS service not enabled", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

            enableGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
        }

        else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }

    boolean flag = false;

    public void getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        assert locationManager != null;
        List<String>  providerList = locationManager.getAllProviders();
        if(null!=locations && null!=providerList && providerList.size()>0){
            final double longitude = locations.getLongitude();
            final double latitude = locations.getLatitude();

            //Toast.makeText(this, String.valueOf(locations.getLongitude()), Toast.LENGTH_LONG).show();

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if(null!=listAddresses&&listAddresses.size()>0){
                    _Location = listAddresses.get(0).getAddressLine(0);
                    //trackFriend.setText(_Location);
                    Toast.makeText(SingleUserInfo.this, "Location Tracked", Toast.LENGTH_SHORT).show();
                    newStudent.child("location").setValue(_Location).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent mapIntent = new Intent(SingleUserInfo.this, MapsActivity.class);
                            mapIntent.putExtra("longitude", longitude);
                            mapIntent.putExtra("latitude", latitude);
                            mapIntent.putExtra("uid", uid);

                            mapIntent.putExtra("name", _Location);
                            startActivity(mapIntent);
                        }
                    });
                }
                else {
                    _Location = "Your Location";
                    //trackFriend.setText(_Location);
                    Toast.makeText(SingleUserInfo.this, "Location Tracked", Toast.LENGTH_SHORT).show();
                    newStudent.child("location").setValue(_Location).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent mapIntent = new Intent(SingleUserInfo.this, MapsActivity.class);
                            mapIntent.putExtra("longitude", longitude);
                            mapIntent.putExtra("latitude", latitude);
                            mapIntent.putExtra("uid", uid);

                            mapIntent.putExtra("name", _Location);
                            startActivity(mapIntent);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                return null;
            }

            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
