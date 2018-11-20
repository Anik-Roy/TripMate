package com.example.anik.tripmate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class PinpostAdapter extends RecyclerView.Adapter<PinpostAdapter.MyViewHolder> {

    private Context mContext;
    private List<PinPostAlbum> albumList;

    private View rootView;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView pinpostText;
        public TextView pinpostDate;
        public TextView namePinpost;

        public MyViewHolder(View view) {
            super(view);
            rootView = view;

            pinpostText = (TextView) view.findViewById(R.id.loadPinpost);
            pinpostDate = (TextView) view.findViewById(R.id.dateofpinpost);
            namePinpost = (TextView) view.findViewById(R.id.namePinPost);
        }
    }

    public PinpostAdapter(Context mContext, List<PinPostAlbum> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pinpost_model, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final PinPostAlbum album = albumList.get(position);

        holder.pinpostText.setText(album.getPinpostText());
        holder.pinpostDate.setText(album.getPinpostDate());
        holder.namePinpost.setText(album.getNamePinPost());
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
