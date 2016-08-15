package com.ekulelu.ekaudioplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Music play main activity. Create a service to play music which can mRun in background.
 * Created by aahu on 2016/8/11 0011.
 */
public class PlayMusicActivity extends AppCompatActivity{

    private final int FAST_TIME = 5000;

    private final int FRESH_WEIGETS = 0x1000;

    private final int MSM_REPLAY_TIME = 3000;

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

    @BindView(R.id.text_view_time)
    TextView mTime;

    private MusicService mMusicService;

    private MusicModel mMusicModel;

    private Handler mHandlerTime = new Handler();

    private boolean mRun;

    private ServiceConnection conn;

    private Runnable myRunnable= new Runnable() {
        public void run() {

            if (mRun) {
                mHandlerTime.postDelayed(this, 1000);
                int time = (int) (mMusicService.getCurrentPosition() * 0.001);
                int minute = (time / 60);
                int second = (time % 60);
                mTime.setText( minute + " : " + second);

            }
        }
    };

    private Handler mHandleMainThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == FRESH_WEIGETS) {
                initWidgets();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.bind(this);
        MyLog.e("----MusicActivity create");

        Intent intent=getIntent();
        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
        //一定要先赋值musicModel模型再绑定模型
        bindMusicService();

        EventBus.getDefault().register(this);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        MyLog.e("----new intent");
        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
        startMusic();
    }

    private void initWidgets() {
        mTvMusicTitle.setText(mMusicModel.getTitle());
        if (mMusicService.isPlaying()) {
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void startMusic() {
        initWidgets();
        mMusicService.startPlay(mMusicModel.getPath());
        mRun = true;
        mHandlerTime.post(myRunnable);
        mImgBtnPlay.setImageResource(android.R.drawable.ic_media_pause);
    }
    private void pauseMusic() {
        mMusicService.pausePlay();
        mRun = false;
        mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
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


    @Subscribe
    public void MusicEvent(MusicEvent event) {
        int action = event.getAction();
        MyLog.e("---event");
        if (MusicEvent.CALL_STATE_RINGING == action) {
            pauseMusic();
            MyLog.e("--playActivity receive pause");
            mRun = false;
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
        } else if (MusicEvent.SMS_RECEIVED == action) {
            pauseMusic();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = FRESH_WEIGETS;
                    mHandleMainThread.sendMessage(msg);
                }
            };
            new Timer().schedule(timerTask,MSM_REPLAY_TIME);
        }
    }

    @Override
    protected void onStop() {
        MyLog.e("---MusicActivity stop");
        mRun = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MyLog.e("---MusicActivity Destroy");
        unbindService(conn);  //6.0版本要自己unbind一下，否则会报错
        super.onDestroy();
    }




    /****************************************
     * 以下的控件响应方法
     */


    @OnClick(R.id.img_btn_play_pause) void onClickPlayOrPause() {
        if(mMusicService.isPause()) {
            startMusic();
        } else {
            pauseMusic();
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
        MyLog.e("--next music");
        MusicEvent event = new MusicEvent();
        event.setAction(MusicEvent.NEXT);
        EventBus.getDefault().post(event);
    }

    @OnClick(R.id.img_btn_previous) void onClickPrevious() {
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
