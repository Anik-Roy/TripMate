package com.example.anik.tripmate;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by anik on 2/6/18.
 */

public class TwoFragmentForEvent extends Fragment {

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    String userId = "", userEmail = "";

    RecyclerView recyclerView;
    private eventAdapter adapter;
    List<eventAlbum> eventList = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    int cnt = 0;

    ProgressDialog progressDialog;

    public TwoFragmentForEvent() {
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

        newStudent = FirebaseDatabase.getInstance().getReference().child("event");

        View view = inflater.inflate(R.layout.fragment_two_my_event, container, false);
        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new eventAdapter(getActivity(), eventList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setTitle("Please wait!");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
          //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        newStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(mCurrentUser.getUid()).exists()) {

                    final DatabaseReference tempRef = newStudent.child(mCurrentUser.getUid());

                    final String eventRoot = mCurrentUser.getUid();

                    ValueEventListener valueEventListener = tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            eventList.clear();
                            adapter.notifyDataSetChanged();

                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                String place = data.getKey();
                                String from = "", to = "", going = "";
                                Uri uri = null;

                                EventInfo eventInfo = data.getValue(EventInfo.class);

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
                                int curMonth = c.get(Calendar.MONTH)+1;
                                int curDate = c.get(Calendar.DATE);

                                boolean over = false;

                                if(curYear >= yearValue) {
                                    if(curYear > yearValue) {
                                        over = true;
                                    }

                                    else {
                                        if(curMonth >= monthValue) {
                                            if(curMonth > monthValue) {
                                                over = true;
                                            }

                                            else {
                                                if(curDate > dateValue) {
                                                    over = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                if(over) {
                                    continue;
                                }

                                Iterator it = eventInfo.getGoing().entrySet().iterator();

                                cnt = 0;

                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry)it.next();
                                    cnt++;
                                }

                                eventAlbum event = new eventAlbum(uri, place, from, to, eventRoot, String.valueOf(cnt));
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
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
                    progressDialog.dismiss();
                    //Toast.makeText(getContext(), "aaa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Error", "Database Error");
                progressDialog.dismiss();
            }
        });

        return view;
    }
}
