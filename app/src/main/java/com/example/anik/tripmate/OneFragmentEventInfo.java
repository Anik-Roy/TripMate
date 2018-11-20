package com.example.anik.tripmate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by anik on 2/13/18.
 */

public class OneFragmentEventInfo extends Fragment implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent, tempStudent;
    FirebaseUser mCurrentUser;

    ImageView eventImage;
    TextView placeNameText, eventPlace, eventTime, going;
    Button goingButton, interestedButton, maybeButton;
    LinearLayout linearLayout;
    RecyclerView userGoingRecyclerView;
    List<userListAlbum> userList;
    EventAdapterForUserListRecyclerView adapter;

    boolean isGoing = false, isInterested = false, isMaybe = false;
    int cnt = 0;
    String id = "";

    static String eventRoot;
    static String placeName;

    Spannable buttonLabel;

    public OneFragmentEventInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_event_info, container, false);

        eventRoot = EventFullInfo.eventRoot;
        placeName = EventFullInfo.placeName;

        userList = new ArrayList<>();
        adapter = new EventAdapterForUserListRecyclerView(getContext(), userList);

        eventImage = (ImageView)view.findViewById(R.id.eventImage);
        placeNameText = (TextView)view.findViewById(R.id.placeName);
        eventPlace = (TextView) view.findViewById(R.id.eventPlace);
        eventTime = (TextView) view.findViewById(R.id.eventTime);
        going = (TextView) view.findViewById(R.id.going);
        goingButton = (Button) view.findViewById(R.id.goingButton);
        linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
        //interestedButton = (Button) view.findViewById(R.id.interestedButton);
        //maybeButton = (Button) view.findViewById(R.id.maybeButton);

        buttonLabel = new SpannableString(" GOING");
        buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
                ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        goingButton.setText(buttonLabel);

//        buttonLabel = new SpannableString(" Interested");
//        buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
//                ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        interestedButton.setText(buttonLabel);
//
//        buttonLabel = new SpannableString(" Maybe");
//        buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
//                ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        maybeButton.setText(buttonLabel);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());

        userGoingRecyclerView = (RecyclerView) view.findViewById(R.id.userGoingRecyclerview);
        userGoingRecyclerView.setLayoutManager(mLayoutManager);
        userGoingRecyclerView.setItemAnimator(new DefaultItemAnimator());
        userGoingRecyclerView.setNestedScrollingEnabled(false);
        userGoingRecyclerView.setAdapter(adapter);

        goingButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        final Context context = getContext();

        if(mCurrentUser.getUid().equals(eventRoot)) {
            buttonLabel = new SpannableString(" DELETE EVENT");
            buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
                   ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            goingButton.setText(buttonLabel);
            //interestedButton.setVisibility(View.INVISIBLE);
            //maybeButton.setVisibility(View.INVISIBLE);
        }

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setTitle("Please wait!");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        newStudent = FirebaseDatabase.getInstance().getReference().child("event");

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(eventRoot).exists()) {

                    final DatabaseReference tempRef = newStudent.child(eventRoot);

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                String place = data.getKey();
                                String from = "", to = "";
                                Uri uri = null;

                                if(place.equals(placeName)) {
                                    userList.clear();
                                    adapter.notifyDataSetChanged();

                                    EventInfo eventInfo = data.getValue(EventInfo.class);

                                    from = eventInfo.getFrom();
                                    to = eventInfo.getTo();

                                    uri = Uri.parse(eventInfo.getImage());


                                    Iterator it = eventInfo.getGoing().entrySet().iterator();

                                    cnt = 0;

                                    while (it.hasNext()) {
                                        Map.Entry pair = (Map.Entry)it.next();
                                        cnt++;

                                        if(pair.getValue().equals(mCurrentUser.getUid()) && !mCurrentUser.getUid().equals(eventRoot)) {
                                            //goingButton.setText(" GOING");
                                            Spannable button = new SpannableString(" NOT GOING");
                                            button.setSpan(new ImageSpan(context, R.drawable.touch,
                                                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            goingButton.setText(button);

//                                            buttonLabel = new SpannableString(" NOT GOING");
//                                            buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
//                                                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                            goingButton.setText(buttonLabel);
                                            id = pair.getKey().toString();
                                        }

                                        final String userLink = (String)pair.getValue();
                                        tempStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(userLink);

                                        tempStudent.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                                                //Toast.makeText(getApplicationContext(), userInfo.getName(), Toast.LENGTH_SHORT).show();
                                                userListAlbum album = new userListAlbum();

                                                try {
                                                    if (userInfo.getName() == null || userInfo.getName().equals("")) {
                                                        album.setUserName(userInfo.getEmail());
                                                    } else {
                                                        album.setUserName(userInfo.getName());
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                album.setUid(userLink);

                                                try {
                                                    if (userInfo.getUserImage() != null) {
                                                        album.setUri(userInfo.getUserImage());
                                                    } else {
                                                        album.setUri("no uri");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                userList.add(album);
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled (DatabaseError databaseError2){

                                            }
                                        });
                                    }

                                    eventPlace.setText("Place Name: "+place);
                                    eventTime.setText("Event Time: "+from+"  to  "+to);
                                    placeNameText.setText(place);
                                    going.setText("Going: "+String.valueOf(cnt));

                                    if(getActivity() != null) {
                                        Glide.with(getActivity()).load(uri).into(eventImage);

                                        Glide
                                                .with(getActivity())
                                                .load(uri)
                                                .asBitmap()
                                                .into(new SimpleTarget<Bitmap>(100, 100) {
                                                    @Override
                                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                                        eventImage.setImageBitmap(resource); // Possibly runOnUiThread()
                                                    }
                                                });
                                    }
                                }

                            }
                            progressDialog.dismiss();
                            //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("photolink", "onCancelled", databaseError.toException());
                            progressDialog.dismiss();
                            //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.goingButton) {
            //Toast.makeText(getContext(), goingButton.getText().toString(), Toast.LENGTH_SHORT).show();

            if(goingButton.getText().toString().equals(" DELETE EVENT")) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this Event?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                if(eventRoot.equals(mCurrentUser.getUid())) {

                                    DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("event");

                                    tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (snapshot.child(eventRoot).exists()) {

                                                final DatabaseReference tempRef2 = newStudent.child(eventRoot);

                                                tempRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.child(placeName).exists()) {

                                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                                //Toast.makeText(getContext(), data.getKey(), Toast.LENGTH_SHORT).show();

                                                                if (data.getKey().equals(placeName)) {
                                                                    data.getRef().removeValue();
                                                                    Toast.makeText(getContext(), "Event Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(getContext(), ExistingEvent.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                                    startActivity(intent);
                                                                    getActivity().finish();
                                                                }
                                                            }

                                                            //dataSnapshot.child(placeName).getRef().removeValue();
                                                            //Toast.makeText(getContext(), "Event Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                                            //getActivity().finish();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {
                                                //Toast.makeText(getContext(), "aaa", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.i("Error", "Database Error");
                                        }
                                    });
                                }
                                else {
                                    return;
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            else {
                if (goingButton.getText().toString().equals(" NOT GOING") && !mCurrentUser.getUid().equals(eventRoot)) {

                    buttonLabel = new SpannableString(" GOING");
                    buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
                            ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    goingButton.setText(buttonLabel);

                    newStudent.child(eventRoot).child(placeName).child("going").child(id).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("interestedEvent").child(eventRoot).child(placeName).removeValue();

                } else if (goingButton.getText().toString().equals(" GOING") && !mCurrentUser.getUid().equals(eventRoot)) {
                    newStudent.child(EventFullInfo.eventRoot).child(EventFullInfo.placeName).child("going").push().setValue(mCurrentUser.getUid());

                    buttonLabel = new SpannableString(" NOT GOING");
                    buttonLabel.setSpan(new ImageSpan(getContext(), R.drawable.touch,
                            ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    goingButton.setText(buttonLabel);

                    tempStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("interestedEvent").child(eventRoot);

                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("interestedEvent").child(eventRoot).child(placeName).setValue("set");
                }
            }
        }

//        if(v.getId() == R.id.deleteEvent) {
//
//
//                //newStudent.child(eventRoot).child(placeName).setValue(null);
//
    }
}
