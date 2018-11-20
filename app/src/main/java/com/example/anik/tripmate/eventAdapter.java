package com.example.anik.tripmate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

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
 * Created by anik on 2/7/18.
 */

public class eventAdapter extends RecyclerView.Adapter<eventAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private List<eventAlbum> albumList;
    private List<eventAlbum> copyAlbumList = new ArrayList<>();

    private View rootView;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference newStudent;
    FirebaseUser mCurrentUser;

    List<String> keyListFirebase = new ArrayList<>();

    boolean notifyDatasetChanged = false;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if(constraint == null || constraint.length() == 0) {
                    albumList = copyAlbumList;
                    results.values = albumList;
                    results.count = albumList.size();
                }

                else {
                    ArrayList<eventAlbum> filteredName = new ArrayList<>();
                    albumList = copyAlbumList;

                    for(eventAlbum album: albumList) {
                        if (album.getPlace().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                            // if `contains` == true then add it
                            // to our filtered list
                            filteredName.add(album);
                        }
                    }

                    results.values = filteredName;
                    results.count = filteredName.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                albumList = (ArrayList<eventAlbum>) results.values;
                notifyDataSetChanged();

                notifyDatasetChanged = true;
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView placeName;
        public TextView userGoing;

        public MyViewHolder(View view) {
            super(view);
            rootView = view;

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            placeName = (TextView) view.findViewById(R.id.placeName);
            userGoing = (TextView) view.findViewById(R.id.userGoing);
        }
    }

    public eventAdapter(Context mContext, List<eventAlbum> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;

        if(!notifyDatasetChanged)
            copyAlbumList = albumList;
    }

    @Override
    public eventAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_event, parent, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mCurrentUser = auth.getCurrentUser();

        newStudent = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        return new eventAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final eventAdapter.MyViewHolder holder, int position) {
        final eventAlbum album = albumList.get(position);

        // loading album cover using Glide library
        Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);

//        Glide
//                .with(mContext)
//                .load(album.getThumbnail())
//                .asBitmap()
//                .into(new SimpleTarget<Bitmap>(100, 100) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                        holder.thumbnail.setImageBitmap(resource); // Possibly runOnUiThread()
//                    }
//                });

        holder.placeName.setText(album.getPlace());
        holder.userGoing.setText("Going: "+album.getGoing());

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, holder.placeName.getText(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(mContext, album.getFrom(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, EventFullInfo.class);
                intent.putExtra("eventRoot", album.getEventRoot());
                intent.putExtra("place", album.getPlace());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
