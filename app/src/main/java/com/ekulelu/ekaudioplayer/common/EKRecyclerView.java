package com.ekulelu.ekaudioplayer.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.lang.reflect.Constructor;

/** A little bit more delightful RecyclerView. You should extend the EKRecyclerView.
 * Created by aahu on 2016/8/16 0016.
 */



public abstract class EKRecyclerView extends RecyclerView{

    /**
     * An interface for listening the item click event;
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    private Class mViewHolderClass;


//    public EKRecyclerView(Context context) {
//        super(context);
//        init();
//    }

    public EKRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * do some initialization work
     */
    private void init(){
        mViewHolderClass = this.receiveViewHolderClass();
        setLayoutManager(receiveLayoutManager());
        setAdapter(new EKAdapter());
        addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        setItemAnimator( new DefaultItemAnimator());

    }

    /**
     * LayoutManager of RecyclerView
     * @return LayoutManager
     */
    public abstract LayoutManager receiveLayoutManager();

    /**
     * Return the recyclerView items' count
     * @return items' count
     */
    public abstract int getItemCount();

    /**
     * You can bind data to your widgets in this method, and can set widgets listener in this method.
     * @param viewHolder
     * @param position
     */
    public abstract void bindDataToView(ViewHolder viewHolder, int position);

    /**
     *
     * @return the resource id of view of item
     */
    public abstract int getItemViewResourceId();

    /**
     *
     * @return the class of your viewHolder, and this viewHolder should extend from EKViewHolder.
     */
    public abstract Class receiveViewHolderClass();

    /**
     * Set the itemClickLitener
     * @param mOnItemClickLitener
     */
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }


    /**
     * An Adapter for EKRecyclerView, and this adapter should be used with the EKViewHolder or
     * its subclass.
     */
    public class EKAdapter extends RecyclerView.Adapter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(EKRecyclerView.this.getContext());
            View view = inflater.inflate(EKRecyclerView.this.getItemViewResourceId(),parent,false);
            ViewHolder viewHolder = null;
            try {
                Constructor constructor = mViewHolderClass.getConstructor(View.class);
                viewHolder =(ViewHolder) constructor.newInstance(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if(mOnItemClickLitener != null) {  //set the item listener
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(view, pos);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemLongClick(view, pos);
                        return false;
                    }
                });
            }
            EKRecyclerView.this.bindDataToView(holder,position);
        }

        /**
         * get items count from recyclerView
         * @return
         */
        @Override
        public int getItemCount() {
            return EKRecyclerView.this.getItemCount();
        }
    }
}
