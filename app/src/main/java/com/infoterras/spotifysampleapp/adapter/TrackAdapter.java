package com.infoterras.spotifysampleapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infoterras.spotifysampleapp.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by gustavo.filho on 23/09/17.
 */

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder> {

    private OnClick onClick;

    public interface OnClick{
        void click(Track track);
    }

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    private List<Track> trackList;

    public TrackAdapter(List<Track> trackList) {
        this.trackList = trackList;
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrackHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_line, parent, false));
    }

    @Override
    public void onBindViewHolder(TrackHolder holder, final int position) {
        holder.track.setText(trackList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.click(trackList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public void setTrackList(List<Track> trackList){
        this.trackList = trackList;
        notifyDataSetChanged();
    }

    class TrackHolder extends RecyclerView.ViewHolder {

        TextView track;

        TrackHolder(View itemView) {
            super(itemView);
            track = itemView.findViewById(R.id.tv_track_name);
        }
    }
}
