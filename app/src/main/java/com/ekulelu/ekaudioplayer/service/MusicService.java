package com.ekulelu.ekaudioplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.ekulelu.ekaudioplayer.Model.MusicEvent;
import com.ekulelu.ekaudioplayer.util.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/** 控制音乐播放的类
 * Created by aahu on 2016/8/12 0012.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    public static final String MUSIC_COMPLETED = "com.ekulelu.ekaudioplayer.action.MusicComplete";

    private final IBinder binder = new LocalBinder();

    private MediaPlayer mMediaPlayer;
    private String mLastFilePath;
    private boolean mIsPause = true;
    private boolean mIsPlayCompleted = false;

    private MyPhoneStateListener mMyPhoneStateListener;
    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mLastFilePath = "";
        MyLog.e("----service create");

        mMyPhoneStateListener = new MyPhoneStateListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mMyPhoneStateListener, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //TODO 发送歌曲播放完毕的广播
        mIsPlayCompleted = true;
        Intent intent = new Intent();
        intent.setAction(MUSIC_COMPLETED);
        //sendBroadcast(intent);
        //发送应用内广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();  //release the MediaPlayer
        unregisterReceiver(mMyPhoneStateListener);
        MyLog.e("---music service stops");
        super.onDestroy();
    }



    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.d("unbind ======");
        return super.onUnbind(intent);
    }

    /***************************************************************
     * 以下的方法供外部调用
     *
     */
    public boolean isPause() {
        return mIsPause;
    }

    public boolean isPlaying () {
        return mMediaPlayer.isPlaying();
    }

    public boolean ismIsPlayCompleted() {
        return mIsPlayCompleted;
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void startPlay() {
        startPlay(mLastFilePath);
    }


    /**
     *  start or resume music
     * @param path music path
     */
    public void startPlay(String path) {
        startPlay(path, false);
    }

    /**
     * start or resume music, if you set isRestart = true,
     * you will restart to music whenever it is playing or pause
     * @param filePath music path
     * @param isRestart should restart to play
     */
    public void startPlay(String filePath, boolean isRestart) {
//        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
//        intent.putExtra(MainActivity.MEDIA_FILE_PATH, path);
//        intent.putExtra(MusicService.IS_RESTART, isRestart);
//        intent.putExtra(MusicService.ACTION_MODE,MusicService.ACTION_PLAY);
//        ContextUtil.getInstance().startService(intent);

        if (!mLastFilePath.equals(filePath)) {  //说明换了歌曲
            try {
                mMediaPlayer.stop();
                if (!mLastFilePath.equals(filePath)) {
                    mMediaPlayer.reset();
                }
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                MyLog.e("an new music start");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isRestart) { //和上次的歌曲一样,并且需要从头开始播放
            mMediaPlayer.seekTo(0);
            MyLog.e("music restart");

        } else if (!mMediaPlayer.isPlaying()){ //和上次的歌曲一样,没有正在播放(出声音),并且不需要从头开始播放
            mMediaPlayer.start();
            MyLog.e("music pause-start");
        }
        mIsPause = false;
        mIsPlayCompleted = false;
        mLastFilePath = filePath;
    }

    /**
     * pause the music which is playing
     */
    public void pausePlay() {
        mMediaPlayer.pause();
        mIsPause = true;
        MyLog.e("-- pause");
    }

    /**
     * stop the music, you should
     */
    public void stopPlay() {
        mMediaPlayer.stop();
        mIsPause = true;
        mIsPlayCompleted = false;
        MyLog.e("-- music stops");
    }

    public void seekToTime(int seekTime) {  // seekTime is in microsecond
        if (!isPlaying()) {
            return;
        }
        if (seekTime < 0 ) {
            seekTime = 0;  //will seek to time
        } else if (seekTime  > mMediaPlayer.getDuration()) {
            return;
        }

        mMediaPlayer.seekTo(seekTime);
        mIsPause = false;
    }



    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }



    //这种内部类必须要动态注册，否则会新建不了类
    public class MyPhoneStateListener extends BroadcastReceiver {
        public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){ //打电话
                pausePlay();
            } else if (SMS_RECEIVED_ACTION.equals(intent.getAction())) { //收到短信
                pausePlay();
                EventBus.getDefault().post(new MusicEvent(MusicEvent.SMS_RECEIVED));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startPlay();
            } else{
                //如果是来电
                TelephonyManager tm =
                        (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);

                switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING://标识当前是来电
                        pausePlay();
                        EventBus.getDefault().post(new MusicEvent(MusicEvent.CALL_STATE_RINGING));
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://接通

                        break;

                    case TelephonyManager.CALL_STATE_IDLE://空闲

                        break;
                    default:

                }
            }
        }
    }
}
