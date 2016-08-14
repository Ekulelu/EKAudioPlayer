package com.ekulelu.ekaudioplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ekulelu.ekaudioplayer.Model.MusicEvent;
import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.R;
import com.ekulelu.ekaudioplayer.service.MusicService;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Music play main activity. Create a service to play music which can run in background.
 * Created by aahu on 2016/8/11 0011.
 */
public class PlayMusicActivity extends AppCompatActivity{

    private final int FAST_TIME = 3000;

    @BindView(R.id.text_view_music_title)
    TextView mTvMusicTitle;

    @BindView(R.id.img_btn_cover)
    ImageButton mImgBtnCover;

    @BindView(R.id.img_btn_fast_reverse)
    ImageButton mImgBtnFastReverse;

    @BindView(R.id.img_btn_previous)
    ImageButton mImgBtnPrevious;

    @BindView(R.id.img_btn_play_pause)
    ImageButton mImgBtnPlay;

    @BindView(R.id.img_btn_next)
    ImageButton mImgBtnNext;

    @BindView(R.id.img_btn_fast_forward)
    ImageButton mImgBtnFastForward;

    private ServiceConnection conn;

    private MusicService mMusicService;


    private MusicModel mMusicModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.bind(this);
        MyLog.e("----MusicActivity create");

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
        //一定要先赋值musicmodell模型再绑定模型
        bindMusicService();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
        startMusic();
    }

    private void initWidgets() {
        mTvMusicTitle.setText(mMusicModel.getTitle());

    }

    private void startMusic() {
        initWidgets();
        mMusicService.startPlay(mMusicModel.getPath());
    }

    private void bindMusicService() {
        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMusicService = ((MusicService.LocalBinder)service).getService();
                startMusic();
            }
        };

        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        MyLog.e("---MusicActivity stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MyLog.e("---MusicActivity Destroy");
        super.onDestroy();
    }




//    @Subscribe
//    public void OnMusicEvent(MusicEvent event) {
//        switch (event.getAction()) {
//            case MusicEvent.MUSIC_COMPLETED:
//                EventBus.getDefault().post();
//        }
//    }


    /****************************************
     * 以下的控件响应方法
     */


    @OnClick(R.id.img_btn_play_pause) void onClickPlayOrPause() {
        if(mMusicService.isPause()) {
            mMusicService.startPlay();
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mMusicService.pausePlay();
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @OnClick(R.id.img_btn_fast_forward) void onClickFastForward() {
        if (!mMusicService.isPlaying()) {
            MyLog.e("--not playing ");
        }
        int seekTime = mMusicService.getCurrentPosition() + FAST_TIME;
        mMusicService.seekToTime(seekTime);
        MyLog.e("--seek to time + " + seekTime);
    }

    @OnClick(R.id.img_btn_fast_reverse) void onClickFastReverse() {
        int seekTime = mMusicService.getCurrentPosition() - FAST_TIME;
        mMusicService.seekToTime(seekTime);
        MyLog.e("--seek to time + " + seekTime);
    }

    @OnClick(R.id.img_btn_next) void onClickNext() {
        // TODO call server...
        MyLog.e("--next music");
        MusicEvent event = new MusicEvent();
        event.setAction(MusicEvent.NEXT);
        EventBus.getDefault().post(event);
    }

    @OnClick(R.id.img_btn_previous) void onClickPrevious() {
        // TODO call server...
        MyLog.e("--next music");
        MusicEvent event = new MusicEvent();
        event.setAction(MusicEvent.PREVIOUS);
        EventBus.getDefault().post(event);
    }

    @OnClick(R.id.img_btn_back_to_list) void onClickBackToList() {
        MyLog.e("back to list");
        this.finish();
    }

    @OnClick(R.id.img_btn_cover) void onClickCover() {
        // TODO call server...
        MyLog.e("" + mMusicService.isPlaying());
    }















}
