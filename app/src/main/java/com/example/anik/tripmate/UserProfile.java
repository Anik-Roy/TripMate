package com.example.anik.tripmate;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";
    Button mapLocation, shareLocation, messageButton;
    TextView details, aboutText, speakText, interestText, travelingText, countryText, genderText;
    TextView birthdayText, hometownText, occupationText, websiteText;
    TextView about, speaks, interest, traveling, country, gender, birthday, hometown, occupation;
    TextView website;
    List<PhotoAlbum> photoList;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    ImageView editProfile, pinpost, leftArrow, rightArrow;

    int pos = 0;
    TextView id;
    String locationSharingState = "false";
    Location locations;

    UserInfo userInfo;
    ProgressDialog dialog;

    private FusedLocationProviderClient mFusedLocationClient;

    LinearLayout linearLayout;
    boolean flag = false;
    DisplayMetrics displayMetrics;
    int height, width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        setContentView(R.layout.activity_user_profile);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapLocation = (Button) findViewById(R.id.mapLocation);
        shareLocation = (Button) findViewById(R.id.shareLocation);
        messageButton = (Button) findViewById(R.id.messageButton);

        pinpost = (ImageView) findViewById(R.id.pinpost);
        leftArrow = (ImageView) findViewById(R.id.slideLeft);
        rightArrow = (ImageView) findViewById(R.id.slideRight);

        linearLayout = (LinearLayout) findViewById(R.id.infoLinearLayout);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editProfile = (ImageView) toolbar.findViewById(R.id.editProfile);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, ExistingEvent.class);
                startActivity(intent);
                finish();
            }
        });

        pinpost.setVisibility(View.INVISIBLE);
        photoList = new ArrayList<>();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this, photoList);
        viewPager.setAdapter(adapter);

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

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        storageReference = storage.getReference();

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

        mCurrentUser = auth.getCurrentUser();

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        newStudent.child("email").setValue(mCurrentUser.getEmail());

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userInfo = snapshot.getValue(UserInfo.class);

                if (userInfo.getName() != null) {
                    toolbar.setTitle(userInfo.getName());
                } else {
                    toolbar.setTitle("Profile");
                }

                setUserInfo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("profilephoto").exists()) {
                    final DatabaseReference tempRef = newStudent.child("profilephoto");

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            photoList.clear();
                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                Uri uri = Uri.parse(data.getValue(String.class));
                                //Toast.makeText(UserProfile.this, uri.toString(), Toast.LENGTH_SHORT).show();
                                PhotoAlbum album = new PhotoAlbum();
                                album.setThumbnail(uri);
                                photoList.add(album);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("photolink", "onCancelled", databaseError.toException());
                        }
                    });
                } else {
                    Uri uri = Uri.parse("android.resource://com.example.anik.tripmate/drawable/noimage");
                    PhotoAlbum album = new PhotoAlbum();
                    album.setThumbnail(uri);
                    photoList.add(album);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });

        //TextView t = (TextView) id;

        editProfile.setOnClickListener(this);
        mapLocation.setOnClickListener(this);
        shareLocation.setOnClickListener(this);
        messageButton.setOnClickListener(this);
    }

    private void setUserInfo() {

        if(userInfo.getLocation() != null) {
            mapLocation.setText(userInfo.getLocation());
        }

        if(userInfo.getLocationShare() == null) {
            newStudent.child("locationShare").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    locationSharingState = "false";
                }
            });
        }


        else {
            locationSharingState = userInfo.getLocationShare();

            if(locationSharingState.equals("true")) {
                shareLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked, 0, 0, 0);
            }
        }

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
            tt.setText("Wny I'm on Trip Mate Finder:\n");
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
                        Toast.makeText(UserProfile.this, "Invalid website address", Toast.LENGTH_LONG).show();
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
    public void onClick(View v) {
        if (v.getId() == R.id.editProfile) {

            Intent intent = new Intent(UserProfile.this, EditProfile.class);
            startActivity(intent);
            finish();
        }

        else if (v.getId() == R.id.mapLocation) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);

            dialog = new ProgressDialog(this);
            dialog.show();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                dialog.dismiss();
                return;
            }

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                locations = location;
                                //Toast.makeText(UserProfile.this, String.valueOf(locations.getLongitude()), Toast.LENGTH_SHORT).show();
                                getUserLocation();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                        }
                    });
            //Intent intent = new Intent(UserProfile.this, MapsActivity.class);
            //startActivity(intent);
            //getUserLocation();
        }

        else if(v.getId() == R.id.shareLocation) {
            if(locationSharingState.equals("false")) {
                shareLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked, 0, 0, 0);
                newStudent.child("locationShare").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        locationSharingState = "true";
                    }
                });
            }

            else if(locationSharingState.equals("true")) {
                shareLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.notchecked, 0, 0, 0);
                newStudent.child("locationShare").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        locationSharingState = "false";
                    }
                });
            }
        }

        else if(v.getId() == R.id.messageButton) {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ChatClass.class);
            startActivity(intent);
        }

        else {
            //Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
            String url = id.getText().toString();

            if (URLUtil.isValidUrl(url)) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } else {
                Toast.makeText(UserProfile.this, "Invalid website address", Toast.LENGTH_LONG).show();
            }
        }
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

    public void getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        assert locationManager != null;
        List<String>  providerList = locationManager.getAllProviders();
        if(null!=locations && null!=providerList && providerList.size()>0){

            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if(null!=listAddresses&&listAddresses.size()>0){
                    String _Location = listAddresses.get(0).getAddressLine(0);
                    mapLocation.setText(_Location);

                    Map map = new HashMap();
                    map.put("location", _Location);
                    map.put("latitude", String.valueOf(latitude));
                    map.put("longitude", String.valueOf(longitude));

                    newStudent.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(UserProfile.this, "Location Tracked", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfile.this, "Cann't track your location. Check your internet connection and GPS setting", Toast.LENGTH_LONG).show();
                            dialog.hide();
                        }
                    });
                }
                else {
                    dialog.dismiss();
                }
            } catch (IOException e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        }
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuInflater blowup = getMenuInflater();
//        blowup.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.settings:
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                break;
//
//            case R.id.exit:
//                finish();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UserProfile.this, ExistingEvent.class);
        startActivity(intent);
        finish();
    }
}
