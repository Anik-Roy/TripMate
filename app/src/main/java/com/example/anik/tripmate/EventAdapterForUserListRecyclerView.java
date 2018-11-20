package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by anik on 2/12/18.
 */

public class EventAdapterForUserListRecyclerView extends RecyclerView.Adapter<EventAdapterForUserListRecyclerView.MyViewHolder> {

    private Context mContext;
    private List<userListAlbum> albumList;

    private View rootView;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView thumbnail;
        public TextView userName;
        public LinearLayout linearLayout;

        public MyViewHolder(View view) {
            super(view);
            rootView = view;

            linearLayout = view.findViewById(R.id.linearLayout);
            thumbnail = (CircleImageView) view.findViewById(R.id.userProfile);
            userName = (TextView) view.findViewById(R.id.userName);
        }
    }

    public EventAdapterForUserListRecyclerView(Context mContext, List<userListAlbum> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public EventAdapterForUserListRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_event_user_list, parent, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        return new EventAdapterForUserListRecyclerView.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventAdapterForUserListRecyclerView.MyViewHolder holder, int position) {
        final userListAlbum album = albumList.get(position);

        try {
            if (album.getUri().equals("no uri")) {
                holder.thumbnail.setImageResource(R.drawable.userimage);
            } else {
                // loading album cover using Glide library
                //Glide.with(mContext).load(album.getUri()).into(holder.thumbnail);

                Glide
                        .with(mContext)
                        .load(album.getUri())
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                holder.thumbnail.setImageBitmap(resource); // Possibly runOnUiThread()
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.userName.setText(album.getUserName());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SingleUserInfo.class);
                intent.putExtra("uid", album.getUid());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

}
