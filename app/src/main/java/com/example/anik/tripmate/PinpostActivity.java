package com.example.anik.tripmate;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PinpostActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mUserEventDatabase;
    FirebaseUser mCurrentUser;

    Toolbar toolbar;
    FloatingActionButton fab;
    RecyclerView pinpostRecyclerView;
    PinpostAdapter adapter;
    List<PinPostAlbum> pinpostList = new ArrayList<>();
    String eventRoot, placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpost);

        Intent intent = getIntent();

        eventRoot = intent.getStringExtra("uid");
        placeName = intent.getStringExtra("name");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mUserEventDatabase = FirebaseDatabase.getInstance().getReference().child("event").child(eventRoot).child(placeName);

        toolbar = (Toolbar) findViewById(R.id.pinpostToolbar);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        pinpostRecyclerView = (RecyclerView) findViewById(R.id.pinpostRecyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        adapter = new PinpostAdapter(this, pinpostList);

        pinpostRecyclerView.setLayoutManager(mLayoutManager);
        pinpostRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pinpostRecyclerView.setNestedScrollingEnabled(false);
        pinpostRecyclerView.setAdapter(adapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(mCurrentUser.getUid().equals(eventRoot)) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PinpostActivity.this, AddPinPost.class);
                    intent.putExtra("eventRoot", eventRoot);
                    intent.putExtra("place", placeName);

                    startActivity(intent);
                }
            });
        } else {
            fab.setVisibility(View.INVISIBLE);
            fab.setClickable(false);
        }

        mUserEventDatabase.child("PinPost").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()) {

                    try {
                        pinpostList.clear();
                        adapter.notifyDataSetChanged();

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            PinPostAlbum album = data.getValue(PinPostAlbum.class);
                            pinpostList.add(album);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
