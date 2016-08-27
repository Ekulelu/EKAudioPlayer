package com.ekulelu.ekaudioplayer.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.R;

import java.util.ArrayList;


/** RecyclerView封装完了，用MusicRecyclerView代替，这个类不用了。
 * Created by aahu on 2016/8/11 0011.
 */

@Deprecated
public class MusicList extends RecyclerView{


    public ArrayList<MusicModel> getmData() {
        return mData;
    }

    public void setmData(ArrayList<MusicModel> mData) {
        this.mData = mData;
    }

    ArrayList<MusicModel> mData = new ArrayList<MusicModel>();

    public MusicList(Context context) {
        super(context);
        init();
    }




    public MusicList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setLayoutManager(new LinearLayoutManager(ContextUtil.getInstance()));
        setAdapter(new MusicListAdapter());
        addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        setItemAnimator( new DefaultItemAnimator());
    }



    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view , int position);
    }

    public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder>{

        private OnItemClickLitener mOnItemClickLitener;

        public void setmOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ContextUtil.getInstance());
            View view = inflater.inflate(R.layout.item_music_list,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.mTvTitle.setText(mData.get(position).getTitle());
            holder.mTvAlbum.setText(mData.get(position).getAlbum());
            holder.mTvArtist.setText(mData.get(position).getArtist());

            if(mOnItemClickLitener != null) {
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(view, pos);
                    }
                });

                holder.itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemLongClick(view, pos);
                        return false;
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
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
}
