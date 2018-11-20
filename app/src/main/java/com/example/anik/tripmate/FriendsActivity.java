package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by anik on 3/6/18.
 */

public class FriendsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mFriendDatabase;
    FirebaseUser mCurrentUser;

    TextView ifFrindNotExist;
    RecyclerView friendRecyclerView;
    FirebaseRecyclerAdapter adapter;

    static Context context;
    boolean isPaused = false;
    Query query;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        ifFrindNotExist = (TextView) findViewById(R.id.ifFriendNotExist);
        friendRecyclerView = (RecyclerView) findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setHasFixedSize(true);
        friendRecyclerView.setNestedScrollingEnabled(false);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friend List");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendsActivity.this, ExistingEvent.class);
                startActivity(intent);
                finish();
            }
        });

        query = mFriendDatabase
                .child(mCurrentUser.getUid());

        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    ifFrindNotExist.setVisibility(View.VISIBLE);
                }

                else {
                    ifFrindNotExist.setVisibility(View.INVISIBLE);
                    FirebaseRecyclerOptions<FriendSetUp> options = new FirebaseRecyclerOptions.Builder<FriendSetUp>()
                            .setQuery(query, FriendSetUp.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<FriendSetUp, FriendViewHolder>(options) {

                        @Override
                        public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            //context = parent.getContext();
                            //Toast.makeText(FriendsActivity.this, "on create", Toast.LENGTH_LONG).show();

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.friend_list_layout, parent, false);

                            return new FriendViewHolder(view);
                        }

                        @Override
                        public void onDataChanged() {
                            super.onDataChanged();
                            //int a = adapter.getItemCount();
                            //FriendSetUp cc = (FriendSetUp) adapter.getItem(0);

                            //if(adapter.getItemCount() == 0)
                                //Toast.makeText(FriendsActivity.this, "Zero", Toast.LENGTH_LONG).show();
                            //else
                                //Toast.makeText(FriendsActivity.this, String.valueOf(cc.getDate()), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public int getItemCount() {
                            //Toast.makeText(FriendsActivity.this, String.valueOf(super.getItemCount()), Toast.LENGTH_SHORT).show();
                            return super.getItemCount();
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull FriendSetUp model) {
                            holder.setDate("Since: "+model.getDate());


                            final String user_id = getRef(position).getKey();

                            FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                                    if(userInfo.getName() != null) {
                                        holder.setName(userInfo.getName());
                                    }

                                    else {
                                        holder.setName(userInfo.getEmail());
                                    }

                                    if(userInfo.getUserImage() != null) {
                                        context = holder.itemView.getContext();
                                        if(!FriendsActivity.this.isFinishing() && !isPaused) {
                                            holder.setUserImage(userInfo.getUserImage());
                                        }
                                    }

                                    if(userInfo.getAvailable() != null && String.valueOf(userInfo.getAvailable()).equals("true")) {
                                        holder.setOfflineUser("true");
                                    }

                                    else {
                                        holder.setOfflineUser("false");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profile_intent = new Intent(FriendsActivity.this, SingleUserInfo.class);
                                    profile_intent.putExtra("uid", user_id);
                                    startActivity(profile_intent);
                                }
                            });
                        }
                    };
                    adapter.startListening();

                    friendRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = mView.getContext();
        }

        public void setDate(String date) {
            TextView mTextView = (TextView) mView.findViewById(R.id.sinceFriend);
            mTextView.setText(date);
        }

        public void setName(String name) {
            TextView mTextView = (TextView) mView.findViewById(R.id.userName);
            mTextView.setText(name);
        }

        public void setOfflineUser(String status) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.online);

            if(status.equals("true"))
                imageView.setVisibility(View.VISIBLE);
            else
                imageView.setVisibility(View.INVISIBLE);
        }

        public void setUserImage(String userImage) {
            final CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.userProfile);
            //Glide.with(context).load(Uri.parse(userImage)).placeholder(R.drawable.userimage).into(imageView);

            Glide
                    .with(context)
                    .load(Uri.parse(userImage))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(100, 100) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            imageView.setImageBitmap(resource); // Possibly runOnUiThread()
                        }
                    });

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FriendsActivity.this, ExistingEvent.class);
        startActivity(intent);
        finish();
    }
}
