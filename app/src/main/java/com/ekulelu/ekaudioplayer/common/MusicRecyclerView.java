package com.ekulelu.ekaudioplayer.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.R;

import java.util.ArrayList;

/**
 * Created by aahu on 2016/8/16 0016.
 */
public class MusicRecyclerView extends EKRecyclerView {

    ArrayList<MusicModel> mData = new ArrayList<MusicModel>();

    public ArrayList<MusicModel> getmData() {
        return mData;
    }

    public void setmData(ArrayList<MusicModel> mData) {
        this.mData = mData;
    }

//    public MusicRecyclerView(Context context) {
//        super(context);
//    }

    public MusicRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public LayoutManager receiveLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void bindDataToView(ViewHolder viewHolder, int position) {
        MyViewHolder holder = (MyViewHolder) viewHolder;
        holder.mTvTitle.setText(mData.get(position).getTitle());
        holder.mTvAlbum.setText(mData.get(position).getAlbum());
        holder.mTvArtist.setText(mData.get(position).getArtist());
    }

    @Override
    public int getItemViewResourceId() {
        return R.layout.item_music_list;
    }

    @Override
    public Class receiveViewHolderClass() {
        return MyViewHolder.class;
    }

    public static class  MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvTitle;
        TextView mTvAlbum;
        TextView mTvArtist;
        public MyViewHolder(View view) {
            super(view);
            mTvTitle = (TextView) view.findViewById(R.id.text_view_music_title);
            mTvAlbum = (TextView) view.findViewById(R.id.text_view_album);
            mTvArtist = (TextView) view.findViewById(R.id.text_view_artist);

        }
    }



}
