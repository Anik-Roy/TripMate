package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageGroupAdapter extends RecyclerView.Adapter<MessageGroupAdapter.MessageViewHolder> {

    Context mContext;
    List<Messages> mMessageList;

    public MessageGroupAdapter(Context mContext, List<Messages> mMessageList) {
        this.mContext = mContext;
        this.mMessageList = mMessageList;
        //Toast.makeText(mContext, "hello", Toast.LENGTH_LONG).show();
        this.notifyDataSetChanged();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.msg_group_single_layout, parent, false);

            return new MessageViewHolder(view);
        }

        else if(viewType == 2){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.msg_group_my_layout, parent, false);

            return new MessageViewHolder(view);
        }

        else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = mMessageList.get(position);

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messages.getFrom().equals(mCurrentUser.getUid())) {
            return 2;
        }
        else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        final Messages c = mMessageList.get(position);

        ConvertTimeStamp convertTimeStamp = new ConvertTimeStamp();
        Long lastTime = c.getTime();
        String ls = convertTimeStamp.getTimeAgo(lastTime, mContext);

        holder.sentTime.setText(ls);

        holder.nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SingleUserInfo.class);

                intent.putExtra("uid", c.getFrom());
                mContext.startActivity(intent);
            }
        });
        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(c.getFrom());
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                String name;

                try {

                    if (userInfo.getName() != null) {
                        name = userInfo.getName();
                        holder.nameText.setText(name);
                    }
                    else if(userInfo.getEmail() != null) {
                        holder.nameText.setText(userInfo.getEmail());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(c.getType().equals("text")) {

            if(c.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.linearLayout.setBackgroundResource(R.drawable.message_my_text_background);
                holder.nameText.setVisibility(View.INVISIBLE);
            }
            else
                holder.linearLayout.setBackgroundResource(R.drawable.message_text_background);

            holder.messageText.setText(c.getMessage());
            holder.messageText.setVisibility(View.VISIBLE);
            holder.imageView.setImageDrawable(null);
            holder.imageView.setVisibility(View.INVISIBLE);

            if(c.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                DatabaseReference mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");

                if(c.getSeen()) {
                    int color = Color.parseColor("#D50E8F"); //The color u want
                    holder.isSeen.setColorFilter(color);
                    holder.isSeen.setBackgroundResource(R.drawable.doubler);
                }
                else {
                    int color = Color.parseColor("#FFFFFF"); //The color u want
                    holder.isSeen.setColorFilter(color);
                    holder.isSeen.setBackgroundResource(R.drawable.doubler);
                }

//                mMessageDatabase.child(c.getFrom()).child(c.getTo()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for(DataSnapshot data: dataSnapshot.getChildren()) {
//                            if(data.getKey().equals("seen")) {
//                                boolean seen = (boolean)data.getValue();
//                                if(seen) {
//                                    holder.isSeen.setText("seen");
//                                }
//                                else {
//                                    holder.isSeen.setText("not seen");
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
            }
        }

        else if(c.getType().equals("image")){
            holder.linearLayout.setBackground(null);
            holder.messageText.setVisibility(View.INVISIBLE);
            Glide.with(mContext).load(Uri.parse(c.getMessage())).placeholder(R.drawable.userimage).into(holder.imageView);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, FullScreenImageClass.class);
                    intent.putExtra("image", c.getMessage());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout relativeLayout;
        public LinearLayout linearLayout;
        public TextView messageText;
        public TextView sentTime, nameText;
        public ImageView isSeen, imageView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageText);
            sentTime = (TextView) itemView.findViewById(R.id.timeTextLayout);
            nameText = (TextView) itemView.findViewById(R.id.nameText);
            isSeen = (ImageView) itemView.findViewById(R.id.isSeen);
            imageView = (ImageView) itemView.findViewById(R.id.imageSend);
            //relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        }
    }
}
