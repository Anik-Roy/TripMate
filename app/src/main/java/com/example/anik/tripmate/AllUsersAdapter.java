package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anik on 3/10/18.
 */

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.MyViewHolder> {

    private Context mContext;
    List<UserModelClass> albumList;
    private View rootView;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    List<String> keyListFirebase = new ArrayList<>();
    Spannable buttonLabel;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView name, distance;
        public Button deleteButton;

        public MyViewHolder(View view) {
            super(view);
            rootView = view;

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            name = (TextView) view.findViewById(R.id.name);
            distance = (TextView) view.findViewById(R.id.distance);

            //deleteButton = (Button) view.findViewById(R.id.deletePhoto);

            //buttonLabel = new SpannableString(" GOING");
            //buttonLabel.setSpan(new ImageSpan(mContext, R.drawable.touch,
            //       ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //deleteButton.setText(buttonLabel);

        }
    }

    public AllUsersAdapter(Context mContext, List<UserModelClass> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public AllUsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card_layout, parent, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        return new AllUsersAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AllUsersAdapter.MyViewHolder holder, int position) {
        final UserModelClass album = albumList.get(position);

        if(album.getName() != null) {
            holder.name.setText(album.getName());
        }

        if(album.getDistance() != null) {
            holder.distance.setText(album.getDistance());
        }
        // loading album cover using Glide library
        if(album.getProfilePhoto() != null && !album.getProfilePhoto().equals("default"))
            Glide.with(mContext).load(Uri.parse(album.getProfilePhoto())).into(holder.thumbnail);

        if(album.getProfilePhoto() != null && album.getProfilePhoto().equals("default"))
            Glide.with(mContext).load(Uri.parse("android.resource://com.example.anik.tripmate/drawable/userimage")).into(holder.thumbnail);

//        Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);
//
//                        Glide
//                        .with(mContext)
//                        .load(album.getThumbnail())
//                        .asBitmap()
//                        .into(new SimpleTarget<Bitmap>(100,100) {
//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                                holder.thumbnail.setImageBitmap(resource); // Possibly runOnUiThread()
//                            }
//                        });
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = album.getUid();
                Intent intent = new Intent(mContext, SingleUserInfo.class);
                intent.putExtra("uid", uid);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
