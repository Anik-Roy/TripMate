package com.example.anik.tripmate;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InterestedEvent extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";

    RecyclerView recyclerView;
    public static eventAdapter adapter;
    List<eventAlbum> eventList = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    int cnt = 0;
    String from = "", to = "", going = "";
    Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interested_event);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();
        //Toast.makeText(getContext(), mCurrentUser.getEmail(), Toast.LENGTH_LONG).show();

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

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("interestedEvent");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new eventAdapter(this, eventList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventList.clear();
                adapter.notifyDataSetChanged();

                for(DataSnapshot parent: snapshot.getChildren()) {

                    //Toast.makeText(getContext(), parent.getKey(), Toast.LENGTH_SHORT).show();

                    final DatabaseReference tempRef = newStudent.child(parent.getKey());

                    final String eventRoot = parent.getKey();

                    tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                final String place = data.getKey();

                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("event").child(eventRoot).child(place);

                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        EventInfo eventInfo = dataSnapshot.getValue(EventInfo.class);

                                        try {
                                            from = eventInfo.getFrom();
                                            to = eventInfo.getTo();
                                            uri = Uri.parse(eventInfo.getImage());

                                            String toDate[] = to.split("\\.");

                                            //System.out.println(toDate.length);

                                            int dateValue = Integer.parseInt(toDate[0]);
                                            int monthValue = Integer.parseInt(toDate[1]);
                                            int yearValue = Integer.parseInt(toDate[2]);

                                            Calendar c = Calendar.getInstance();
                                            int curYear = c.get(Calendar.YEAR);
                                            int curMonth = c.get(Calendar.MONTH) + 1;
                                            int curDate = c.get(Calendar.DATE);

                                            boolean over = false;

                                            if (curYear >= yearValue) {
                                                if (curYear > yearValue) {
                                                    over = true;
                                                } else {
                                                    if (curMonth >= monthValue) {
                                                        if (curMonth > monthValue) {
                                                            over = true;
                                                        } else {
                                                            if (curDate > dateValue) {
                                                                over = true;
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if(over) {
                                                return;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            Iterator it = eventInfo.getGoing().entrySet().iterator();

                                            cnt = 0;

                                            while (it.hasNext()) {
                                                Map.Entry pair = (Map.Entry) it.next();
                                                cnt++;
                                            }

                                            eventAlbum event = new eventAlbum(uri, place, from, to, eventRoot, String.valueOf(cnt));
                                            eventList.add(event);
                                            adapter.notifyDataSetChanged();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //Toast.makeText(getContext(), place+" "+from+" "+to+" "+uri, Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
            }
        });
    }
}
