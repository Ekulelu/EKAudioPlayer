package com.ekulelu.ekaudioplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;
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

/** Music play main activity. The activity will finish when it go to background.
 * Created by aahu on 2016/8/11 0011.
 */
public class PlayMusicActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    /**
     * 点击快进按钮后的快进时间。
     */
    private final int FAST_TIME = 5000;

    private final int FLAG_FRESH_WIDGETS = 0x1000;

    private final int FLAG_FRESH_TIME = 0x1001;

    /**
     * 接收到信息后的停顿时间
     */
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
    TextView mTvTime;

    @BindView(R.id.pro_bar)
    android.support.v7.widget.AppCompatSeekBar mSeekBar;

    private MusicService mMusicService;

    private MusicModel mMusicModel;

    private ServiceConnection mConn;


    /**
     * 负责后台调用更新时间
     */
    private Timer mTimer;

    private TimerTask mTimerTask;

    /**
     * 当前播放进度，单位是秒
     */
    private static int mCurrentPosInSecond;

    private PlayMusicActivityReceiver mReceiver;

//    private Runnable myRunnable= new Runnable() {
//        public void run() {
//            if (mRun) {
//                mHandlerTime.postDelayed(this, 1000);
//                int time = (int) (mMusicService.getCurrentPosition() * 0.001);
//                int minute = (time / 60);
//                int second = (time % 60);
//                mTvTime.setText( minute + " : " + second);
//                mSeekBar.setProgress(mMusicService.getCurrentPosition());
//                MyLog.e("----handler thread" +Thread.currentThread() );
//            }
//        }
//    };

    /**
     * 接收从Timer来的信息，更新控件
     */
    private Handler mHandleMainThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            MyLog.e("--handler");
            if (msg.what == FLAG_FRESH_WIDGETS) {//更新标题
                updateWidgets();
            } else if(msg.what == FLAG_FRESH_TIME){ //只更新时间
                mSeekBar.setProgress(mCurrentPosInSecond);
                setTvTime();
            }
        }
    };

    /**
     * 更新文字时间，不能把setProgress()放进来了，因为在它的回调函数里面调用了setTvTime()
     */
    private void setTvTime() {
        int durationInSecond = (int)(mMusicModel.getDuration() * 0.001);
        if (mCurrentPosInSecond > durationInSecond ){
            mCurrentPosInSecond = durationInSecond; //处理播放完一首的时候，下一首没来得及开始，但是时间仍在跑的情况。
        }
        int minute = (mCurrentPosInSecond / 60);
        int second = (mCurrentPosInSecond % 60);

        int minuteAll = (durationInSecond / 60);
        int secondAll = (durationInSecond % 60);
        String str = minute + " : " + second + " / " + minuteAll + " : " + secondAll;
        mTvTime.setText(str);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.bind(this);
        MyLog.e("----MusicActivity create");
//        MyLog.e("----MusicActivity" +Thread.currentThread() );

        mSeekBar.setOnSeekBarChangeListener(this);

        Intent intent=getIntent();
        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
        //一定要先赋值musicModel模型再绑定模型
        bindMusicService(); // 在它的返回函数里面会开始音乐

        EventBus.getDefault().register(this);  //注册EventBus

        //注册接收MainActivity信息的receiver
        mReceiver = new PlayMusicActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.PLAY_NEW_MUSIC);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMusicService != null && mMusicService.isPlaying()) {
            startTimer();  //处理从stop状态到running的情况。
        }

//        if (mTimerTask != null) {
//            mTimerTask.cancel();
//        }
//        mTimerTask = new MyTimerTask();
//        if (mTimer == null) {
//            mTimer = new Timer();
//        } else {
//            mTimer.purge(); //清空timer里面已经取消的timerTask
//        }
//        mTimer.schedule(mTimerTask,0,1000); //开启计时器计时
    }

    /**
     * 新的歌曲来的时候会调用，出现在播放完一首歌，或者点了上一首下一首的情况，这个时候这个activity可能没有显示
     */
    private class PlayMusicActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.PLAY_NEW_MUSIC.equals(action)) {
                mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
                mCurrentPosInSecond = 0;
                mSeekBar.setProgress(0);
                startPlayMusic();
            }
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = FLAG_FRESH_TIME;
            mCurrentPosInSecond+=1;
            mHandleMainThread.sendMessage(msg);
//            MyLog.e("----timer thread" +Thread.currentThread() );
        }
    }

    /**
     * 当有其他类重新发了Intent的时候会进入这个方法，现在不用了
     * @param intent
     */
//    @Override
//    protected void onNewIntent(Intent intent) {
//        MyLog.e("----new intent");
//        mMusicModel = (MusicModel) intent.getSerializableExtra(MainActivity.MUSIC_MODEL);
//        startPlayMusic();
//    }

    /**
     * 更新播放界面按钮和标题
     */
    private void updateWidgets() {
        mTvMusicTitle.setText(mMusicModel.getTitle());
        if (mMusicService.isPlaying()) {
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    /**
     * 这个方法在几个地方调用：一个是service连接后，这种情况是这个activity被新建了。
     * 第二是暂停后启动，这种状态下，这个activity仍在界面上。
     * 这个方法会调用service的方法，开始播放音乐。
     */
    private void startPlayMusic() {
        updateWidgets();
//        //timer不计时的话，只需取消TimerTask即可，和iOS不一样。
//        if (mTimerTask != null) {
//            mTimerTask.cancel();
//        }
//        mTimerTask = new MyTimerTask();
//        if (mTimer == null) {
//            mTimer = new Timer();
//        } else {
//            mTimer.purge(); //清空timer里面已经取消的timerTask
//        }
//        mTimer.schedule(mTimerTask,0,1000); //开启计时器计时
        startTimer();
        mSeekBar.setMax((int)(mMusicModel.getDuration() * 0.001));
        mMusicService.startPlay(mMusicModel.getPath());
        mCurrentPosInSecond = (int) (mMusicService.getCurrentPosition() * 0.001);

        mImgBtnPlay.setImageResource(android.R.drawable.ic_media_pause);
    }

    /**
     * 暂停播放，停止Timer。
     */
    private void pauseMusic() {
        mMusicService.pausePlay();
        mTimerTask.cancel();
        mTimer.purge();
        mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
    }


    /**
     *
     */
    private void bindMusicService() {
        mConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMusicService = ((MusicService.LocalBinder)service).getService();
                startPlayMusic();
            }
        };
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void startTimer(){
        //timer不计时的话，只需取消TimerTask即可，和iOS不一样。
        if (mTimerTask == null) {
            mTimerTask = new MyTimerTask();
            if (mTimer == null) {
                mTimer = new Timer();
            } else {
                mTimer.purge(); //清空timer里面已经取消的timerTask
            }
            mTimer.schedule(mTimerTask,0,1000); //开启计时器计时
        }
    }

    private void stopTimer() {
        mTimerTask.cancel(); //Android的是要timerTask cancel掉后就可以了，timer不用cancel
        mTimerTask = null;  //这里约定：timerTask为null说明不没有启动timer
        mTimer.purge();
    }


    /**
     * 用来接收来自Service的电话和短信事件，更新面板控件,在主线程执行
     */
    @Subscribe
    public void MusicEvent(MusicEvent event) {
        int action = event.getAction();
//        MyLog.e("----event thread" +Thread.currentThread() );
        MyLog.e("---event from tel or sms");
        if (MusicEvent.CALL_STATE_RINGING == action || MusicEvent.ACTION_NEW_OUTGOING_CALL == action) { //有电话来了或者打电话
            updateWidgets();
            MyLog.e("--playActivity receive pause");
//            mTimerTask.cancel(); //Android的是要timerTask cancel掉后就可以了，timer不用cancel
//            mTimerTask = null;  //为null说明不启动
//            mTimer.purge();
            stopTimer();
            mImgBtnPlay.setImageResource(android.R.drawable.ic_media_play);
        } else if (MusicEvent.SMS_RECEIVED == action) {
            updateWidgets();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = FLAG_FRESH_WIDGETS;
                    mHandleMainThread.sendMessage(msg);
//                    MyLog.e("----timer thread" +Thread.currentThread() );
                }
            };  //开一个timer，在一定延时之后重新播放音乐。
            new Timer().schedule(timerTask,MSM_REPLAY_TIME);
        }
    }

    @Override
    protected void onStop() {
        MyLog.e("---MusicActivity stop");
//        mTimerTask.cancel();
//        mTimer.purge();
        stopTimer();
        mTimer.cancel();
        mTimer = null;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MyLog.e("---MusicActivity Destroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unbindService(mConn);  //6.0版本要自己unbind一下，否则会报错
        super.onDestroy();
    }

    /******************************************************************
     *  SeekBar Listener methods
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
//            MyLog.e("---progress" + progress *1000);
            mMusicService.seekToTime(progress * 1000);
            mCurrentPosInSecond = progress;
            setTvTime(); //要先改了mCurrentPosInSecond再调用
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
//        mTimerTask.cancel();
//        mTimer.purge();
        stopTimer();
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        mTimerTask = new MyTimerTask();
//        mTimer.schedule(mTimerTask,0,1000);
        startTimer();
    }


    /****************************************
     * 以下的控件响应方法
     */


    @OnClick(R.id.img_btn_play_pause) void onClickPlayOrPause() {
        if(mMusicService.isPause()) {
            startPlayMusic();
        } else {
            pauseMusic();
        }
    }

    @OnClick(R.id.img_btn_fast_forward) void onClickFastForward() {
        if (!mMusicService.isPlaying()) {
            MyLog.e("--not playing ");
        }
        int seekTime = mMusicService.getCurrentPosition() + FAST_TIME;
        mCurrentPosInSecond = (int)(seekTime * 0.001);
        mMusicService.seekToTime(seekTime);
        MyLog.e("--seek to time + " + seekTime);
    }

    @OnClick(R.id.img_btn_fast_reverse) void onClickFastReverse() {
        int seekTime = mMusicService.getCurrentPosition() - FAST_TIME;
        mCurrentPosInSecond = (int)(seekTime * 0.001);
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
