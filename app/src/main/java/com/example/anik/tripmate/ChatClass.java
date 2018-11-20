package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter_LifecycleAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by anik on 3/16/18.
 */

public class ChatClass extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mConvDatabase;
    DatabaseReference mMessageDatabase;
    DatabaseReference mUserDatabase;
    FirebaseUser mCurrentUser;

    TextView noChatList;

    Toolbar toolbar;
    RecyclerView chatRecyclerView;
    FirebaseRecyclerAdapter chatAdapter;

    //static Context context;
    Query query;
    String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_chat);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUser.getUid());
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUser.getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chat");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatClass.this, ExistingEvent.class);
                startActivity(intent);
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        //chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        noChatList = (TextView) findViewById(R.id.noChatList);

        query = mConvDatabase.orderByChild("timestamp");
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    noChatList.setVisibility(View.VISIBLE);
                }

                else {
                    noChatList.setVisibility(View.INVISIBLE);

//                    for(DataSnapshot data: dataSnapshot.getChildren()) {
//                      Toast.makeText(ChatClass.this, data.getValue().toString(), Toast.LENGTH_LONG).show();
//                        ChatSetUp c = data.getValue(ChatSetUp.class);
//                        Toast.makeText(ChatClass.this, String.valueOf(data.getKey()), Toast.LENGTH_LONG).show();
//                    }

                    FirebaseRecyclerOptions<ChatSetUp> options = new FirebaseRecyclerOptions.Builder<ChatSetUp>()
                            .setQuery(query, ChatSetUp.class)
                            .build();

                    chatAdapter = new FirebaseRecyclerAdapter<ChatSetUp, ChatViewHolder>(options) {

                        @Override
                        public ChatViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
                            //context = parent.getContext();
                            //Toast.makeText(ChatClass.this, "on create", Toast.LENGTH_LONG).show();

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.chat_list_layout, parent, false);
                            return new ChatViewHolder(view);
                        }

                        @Override
                        public void onDataChanged() {
                            super.onDataChanged();
                            //int a = chatAdapter.getItemCount();
                            //ChatSetUp cc = (ChatSetUp) chatAdapter.getItem(0);

                            //if(chatAdapter.getItemCount() == 0)
                            //    Toast.makeText(ChatClass.this, "Zero", Toast.LENGTH_LONG).show();
                            //else
                            //    Toast.makeText(ChatClass.this, String.valueOf(a), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public int getItemCount() {
                            //Toast.makeText(ChatClass.this, String.valueOf(super.getItemCount()), Toast.LENGTH_SHORT).show();
                            return super.getItemCount();
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final ChatSetUp model) {
                            final String user_id = getRef(position).getKey();

                            Query lastMessageQuery = mMessageDatabase.child(user_id).limitToLast(1);

                            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if(dataSnapshot.child("message").getValue() != null) {
                                        String data = dataSnapshot.child("message").getValue().toString();
                                        String from = dataSnapshot.child("from").getValue().toString();
                                        holder.setMessage(data, from, model.isSeen());
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mUserDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                                    name = userInfo.getName();

                                    if(userInfo.getAvailable() != null) {
                                        holder.setOfflineUser(userInfo.getAvailable());
                                    }

                                    if(userInfo.getName() != null) {
                                        holder.setName(userInfo.getName());
                                    }

                                    else {
                                        holder.setName(userInfo.getEmail());
                                    }

                                    holder.setUserImage(userInfo.getUserImage(), ChatClass.this);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chat_intent = new Intent(ChatClass.this, ChatActivity.class);
                                    chat_intent.putExtra("uid", user_id);
                                    chat_intent.putExtra("name", name);
                                    startActivity(chat_intent);
                                }
                            });
                        }
                    };
                    chatAdapter.startListening();
                    chatRecyclerView.setAdapter(chatAdapter);
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
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            //context = mView.getContext();
        }

        public void setMessage(String message, String from, boolean seen) {
            TextView userMessage = (TextView) mView.findViewById(R.id.userMessage);
            //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            userMessage.setText(message);

            if(!seen && !from.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.BOLD);
            }

            else {
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.userName);
            textView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userProfile);
            //Glide.with(ctx).load(Uri.parse(thumb_image)).into(userImageView);

//            Glide.with(ctx)
//                    .load(Uri.parse(thumb_image))
//                    .placeholder(R.drawable.userimage)
//                    .crossFade()
//                    .into(userImageView);

            try {
                Picasso.with(ctx).load(Uri.parse(thumb_image)).into(userImageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setOfflineUser(String status) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.online);

            if(status.equals("true"))
                imageView.setVisibility(View.VISIBLE);
            else
                imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatClass.this, ExistingEvent.class);
        startActivity(intent);
        finish();
    }
}
