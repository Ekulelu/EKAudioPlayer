package com.ekulelu.ekaudioplayer.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.R;


/**
 * Created by aahu on 2016/8/11 0011.
 */


public class MusicList extends RecyclerView{

    String[] mData = {"qqqq","wwww","eeee"};

    public MusicList(Context context) {
        super(context);
        setLayoutManager(new LinearLayoutManager(ContextUtil.getInstance()));
        setAdapter(new MusicListAdapter());

    }

    public MusicList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayoutManager(new LinearLayoutManager(ContextUtil.getInstance()));
        setAdapter(new MusicListAdapter());
        addItemDecoration(new DividerItemDecoration(DividerItemDecoration.VERTICAL_LIST));
    }

    class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ContextUtil.getInstance());
            View view = inflater.inflate(R.layout.view_music_list_item,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.mTv.setText(mData[position%3]);
        }

        @Override
        public int getItemCount() {
            return mData.length * 13;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mTv;
            public MyViewHolder(View view) {
                super(view);
                mTv = (TextView) view.findViewById(R.id.text_view_music_title);

            }
        }
    }
}
