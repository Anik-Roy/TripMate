package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
 * Created by Ravi Tamada on 18/05/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyViewHolder> {

    private Context mContext;
    static List<PhotoAlbum> albumList;
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
        public Button deleteButton;

        public MyViewHolder(View view) {
            super(view);
            rootView = view;

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            deleteButton = (Button) view.findViewById(R.id.deletePhoto);

            //buttonLabel = new SpannableString(" GOING");
            //buttonLabel.setSpan(new ImageSpan(mContext, R.drawable.touch,
             //       ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //deleteButton.setText(buttonLabel);

        }
    }

    public AlbumsAdapter(Context mContext, List<PhotoAlbum> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        PhotoAlbum album = albumList.get(position);

        // loading album cover using Glide library
        Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);

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
                //Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show();
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(mContext, FullScreenViewActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        buttonLabel = new SpannableString(" ");
        buttonLabel.setSpan(new ImageSpan(mContext, R.drawable.delete,
                ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.deleteButton.setText(buttonLabel);


        holder.deleteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show();
                int position = holder.getAdapterPosition();

                //deleteFunction(position);

                albumList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, albumList.size());

                newStudent.child("profilephoto").child(OneFragment.keyIdForFirebaseDatabaseImages.get(position)).removeValue();
                OneFragment.keyIdForFirebaseDatabaseImages.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

//    public  void deleteFunction(int position) {
//        albumList.remove(position);
//        notifyItemRemoved(position);
//        notifyItemRangeChanged(position, albumList.size());
//
//        newStudent.child("profilephoto").child(OneFragment.keyIdForFirebaseDatabaseImages.get(position)).removeValue();
//        OneFragment.keyIdForFirebaseDatabaseImages.remove(position);
//
//    }
}
