package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference notificationDatabase;
    FirebaseUser mCurrentUser;

    RecyclerView notificationRecyclerView;
    FirebaseRecyclerAdapter adapter;

    static Context context;
    TextView noNewNotification;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        mCurrentUser = mAuth.getCurrentUser();

        noNewNotification = (TextView) findViewById(R.id.noNewNotification);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        notificationRecyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        //notificationRecyclerView.setHasFixedSize(true);
        notificationRecyclerView.setNestedScrollingEnabled(false);
        notificationRecyclerView.setLayoutManager(linearLayoutManager);

        query = notificationDatabase.child(mCurrentUser.getUid()).orderByChild("timestamp");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    noNewNotification.setVisibility(View.VISIBLE);
                }

                else {
                    //Toast.makeText(NotificationActivity.this, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                    noNewNotification.setVisibility(View.INVISIBLE);

//                    for(DataSnapshot data: dataSnapshot.getChildren()) {
//                      Toast.makeText(ChatClass.this, data.getValue().toString(), Toast.LENGTH_LONG).show();
//                        ChatSetUp c = data.getValue(ChatSetUp.class);
//                        Toast.makeText(ChatClass.this, String.valueOf(data.getKey()), Toast.LENGTH_LONG).show();
//                    }

                    FirebaseRecyclerOptions<NotificationSetUp> options = new FirebaseRecyclerOptions.Builder<NotificationSetUp>()
                            .setQuery(query, NotificationSetUp.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<NotificationSetUp, NotificationViewHolder>(options) {

                        @Override
                        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            //context = parent.getContext();
                            //Toast.makeText(ChatClass.this, "on create", Toast.LENGTH_LONG).show();

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.notification_single_layout, parent, false);
                            return new NotificationViewHolder(view);
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
                        protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position, @NonNull final NotificationSetUp model) {
                            final String user_id = getRef(position).getKey();

                            final String from = model.getFrom();
                            final String type = model.getType();
                            final Long timestamp = (Long) model.getTimestamp();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(from)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                                            try {
                                                if (userInfo.getName() != null) {
                                                    if (type.equals("request")) {
                                                        String text = userInfo.getName() + " sent you a friend request.";

                                                        //Toast.makeText(NotificationActivity.this, text, Toast.LENGTH_SHORT).show();
                                                        holder.setText(text);
                                                    } else if (type.equals("accepted")) {
                                                        String text = userInfo.getName() + " accepted your friend request.";

                                                        //Toast.makeText(NotificationActivity.this, text, Toast.LENGTH_SHORT).show();
                                                        holder.setText(text);
                                                    }
                                                } else {
                                                    if (type.equals("request")) {
                                                        String text = userInfo.getEmail() + " sent you a friend request.";
                                                        //Toast.makeText(NotificationActivity.this, text, Toast.LENGTH_SHORT).show();
                                                        holder.setText(text);
                                                    } else if (type.equals("accepted")) {
                                                        String text = userInfo.getEmail() + " accepted your friend request.";
                                                        //Toast.makeText(NotificationActivity.this, text, Toast.LENGTH_SHORT).show();
                                                        holder.setText(text);
                                                    }
                                                }

                                                holder.setImg(userInfo.getUserImage());
                                            }catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                            holder.setTime(timestamp);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chat_intent = new Intent(NotificationActivity.this, SingleUserInfo.class);
                                    chat_intent.putExtra("uid", from);
                                    startActivity(chat_intent);
                                }
                            });
                        }
                    };
                    adapter.startListening();
                    notificationRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public NotificationViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = mView.getContext();
        }

        public void setImg(String uri) {
            CircleImageView img = (CircleImageView) mView.findViewById(R.id.userProfileImage);
            if(uri != null)
                Glide.with(context).load(Uri.parse(uri)).into(img);
        }

        public void setText(String text) {
            TextView notifiationText = (TextView) mView.findViewById(R.id.notificationText);
            notifiationText.setText(text);
        }

        public void setTime(Long time) {
            TextView notificationTime = (TextView) mView.findViewById(R.id.notificationTime);
            ConvertTimeStamp cv = new ConvertTimeStamp();

            if(time != null) {
                String t = cv.getTimeAgo(time, context);
                notificationTime.setText(t);
            }
        }
    }
}
