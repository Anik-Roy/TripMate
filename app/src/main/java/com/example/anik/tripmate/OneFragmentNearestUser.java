package com.example.anik.tripmate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.example.anik.tripmate.AllUsers.country;
import static com.example.anik.tripmate.AllUsers.female;
import static com.example.anik.tripmate.AllUsers.male;
import static com.example.anik.tripmate.AllUsers.max;
import static com.example.anik.tripmate.AllUsers.min;

/**
 * Created by anik on 4/24/18.
 */

public class OneFragmentNearestUser extends Fragment {

    FirebaseAuth mAuth;
    static DatabaseReference mUserDatabase;
    static FirebaseUser mCurrentUser;

    RecyclerView userRecyclerView;
    static AllUsersAdapter adapter;
    static List<UserModelClass> userList;
    static String latitude;
    static String longitude;
    static String distance;

    public OneFragmentNearestUser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //return inflater.inflate(R.layout.fragment_one, container, false);

        View view = inflater.inflate(R.layout.fragment_user_layout, container, false);

        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        setAllUser();

        userRecyclerView = (RecyclerView) view.findViewById(R.id.userRecyclerView);
        userList = new ArrayList<>();

        adapter = new AllUsersAdapter(getContext(), userList);

        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        userRecyclerView.setLayoutManager(mLayoutManager);
        userRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(0), true)); // dpToPx 10 chilo
        userRecyclerView.setNestedScrollingEnabled(false);
        userRecyclerView.setItemAnimator(new DefaultItemAnimator());
        userRecyclerView.setAdapter(adapter);

//        AllUsers.searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
//                setAllUser();
//            }
//        });

        return view;
    }

    public static void setAllUser() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                latitude = userInfo.getLatitude();
                longitude = userInfo.getLongitude();

                if(latitude == null) {
                    Toast.makeText(TripMate.getAppContext(), "Set Your Location First from Profile", Toast.LENGTH_LONG).show();
                    return;
                }

                Query query = mUserDatabase;

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {
                            Toast.makeText(TripMate.getAppContext(), "No user registered yet.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            userList.clear();
                            adapter.notifyDataSetChanged();

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                UserInfo userInfo = data.getValue(UserInfo.class);

                                UserModelClass modelClass = new UserModelClass();

                                String uid = data.getKey();

                                modelClass.setUid(uid);

                                if(userInfo.getLatitude() == null) {
                                    continue;
                                }

                                if((!male && !female && country.equals("") && min == 18 && max == 60) || (male && female && country.equals("") && min == 18 && max == 60)) {
                                    if (userInfo.getName() != null) {
                                        modelClass.setName(userInfo.getName());
                                    }
                                    else {
                                        modelClass.setName("default");
                                    }

                                    if (userInfo.getUserImage() != null) {
                                        modelClass.setProfilePhoto(userInfo.getUserImage());
                                    } else {
                                        modelClass.setProfilePhoto("default");
                                    }

                                    if (userInfo.getLatitude() != null) {
                                        modelClass.setLatitude(userInfo.getLatitude());
                                        modelClass.setLongitude(userInfo.getLongitude());
                                    }

                                    if (latitude != null && userInfo.getLatitude() != null) {

                                        Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                        dist /= 1000;
                                        distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";

                                        modelClass.setDistance(distance);
                                        modelClass.setDist(dist);

                                        userList.add(modelClass);

                                        try {
                                            Collections.sort(userList, new Comparator<UserModelClass>() {
                                                @Override
                                                public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                    //if(userModelClass.getDist() < t1.getDist())
                                                    return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());
                                                    //else
                                                    //  return 0;
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                else if(male && !female && country.equals("") && min == 18 && max == 60) {

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Male")) {


                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(female && !male && country.equals("") && min == 18 && max == 60) {

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Female")) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);

                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(male && !female && !country.equals("") && min == 18 && max == 60) {

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Male") &&
                                            userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(female && !male && !country.equals("") && min == 18 && max == 60) {

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Female") &&
                                            userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if((male && female && !country.equals("") && min == 18 && max == 60)
                                        || (!male && !female && !country.equals("") && min == 18 && max == 60)) {

                                    if(userInfo.getNationality() != null && userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(male && !female && country.equals("") && (min != 18 || max != 60)) {

                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Male")) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(female && !male && country.equals("") && (min != 18 || max != 60)) {

                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Female")) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                                else if(male && !female && !country.equals("") && (min != 18 || max != 60)) {

                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Male") &&
                                            userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if(female && !male && !country.equals("") && (min != 18 || max != 60)) {

                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;

                                    if(userInfo.getGender() != null && userInfo.getGender().equals("Female") &&
                                            userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if((male && female && !country.equals("") && (min != 18 || max != 60))
                                        || (!male && !female && !country.equals("") && (min != 18 || max != 60))) {

                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;

                                    if(userInfo.getNationality().equals(country)) {

                                        if (userInfo.getName() != null) {
                                            modelClass.setName(userInfo.getName());
                                        }
                                        else {
                                            modelClass.setName("default");
                                        }

                                        if (userInfo.getUserImage() != null) {
                                            modelClass.setProfilePhoto(userInfo.getUserImage());
                                        } else {
                                            modelClass.setProfilePhoto("default");
                                        }

                                        if (userInfo.getLatitude() != null) {
                                            modelClass.setLatitude(userInfo.getLatitude());
                                            modelClass.setLongitude(userInfo.getLongitude());
                                        }

                                        if (latitude != null && userInfo.getLatitude() != null) {

                                            Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                    Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                            dist /= 1000;
                                            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                            modelClass.setDistance(distance);
                                            //int pos = Collections.binarySearch(userList, modelClass, c);
                                            modelClass.setDist(dist);
                                            userList.add(modelClass);

                                            try {
                                                Collections.sort(userList, new Comparator<UserModelClass>() {
                                                    @Override
                                                    public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                        return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }

                                else if((!male && !female && country.equals("") && (min != 18 || max != 60))
                                        || (male && female && country.equals("") && (min != 18 || max != 60))) {


                                    if(userInfo.getYear() == null)
                                        continue;

                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    int age = year - Integer.parseInt(userInfo.getYear()) + 1;

                                    if(age < min || age > max)
                                        continue;


                                    if (userInfo.getName() != null) {
                                        modelClass.setName(userInfo.getName());
                                    }
                                    else {
                                        modelClass.setName("default");
                                    }

                                    if (userInfo.getUserImage() != null) {
                                        modelClass.setProfilePhoto(userInfo.getUserImage());
                                    } else {
                                        modelClass.setProfilePhoto("default");
                                    }

                                    if (userInfo.getLatitude() != null) {
                                        modelClass.setLatitude(userInfo.getLatitude());
                                        modelClass.setLongitude(userInfo.getLongitude());
                                    }

                                    if (latitude != null && userInfo.getLatitude() != null) {

                                        Double dist = meterDistanceBetweenPoints(Float.parseFloat(latitude), Float.parseFloat(longitude),
                                                Float.parseFloat(userInfo.getLatitude()), Float.parseFloat(userInfo.getLongitude()));

                                        dist /= 1000;
                                        distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", dist)) + "km";
                                        modelClass.setDistance(distance);
                                        //int pos = Collections.binarySearch(userList, modelClass, c);
                                        modelClass.setDist(dist);
                                        userList.add(modelClass);

                                        try {
                                            Collections.sort(userList, new Comparator<UserModelClass>() {
                                                @Override
                                                public int compare(UserModelClass userModelClass, UserModelClass t1) {
                                                    return (int) Math.round(userModelClass.getDist()) - (int) Math.round(t1.getDist());

                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

//                            int sz = userList.size();
//                            for(int i = 0; i < sz; i++) {
//                                Toast.makeText(getContext(), userList.get(i).getDistance(), Toast.LENGTH_SHORT).show();
//                                System.out.print(userList.get(i).dist);
//                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
