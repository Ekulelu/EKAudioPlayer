package com.ekulelu.ekaudioplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ekulelu.ekaudioplayer.activity.MainActivity;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyLog;

import java.io.IOException;

/** 控制音乐播放的类
 * Created by aahu on 2016/8/12 0012.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

//    public static String ACTION_MODE = "com.ekulelu.ekaudioplayer.action.ACTION_MODE";
//    public static String ACTION_PLAY = "com.ekulelu.ekaudioplayer.action.PLAY";
//    public static String ACTION_PAUSE = "com.ekulelu.ekaudioplayer.action.PAUSE";
//    public static String ACTION_STOP = "com.ekulelu.ekaudioplayer.action.PLAY";
//    public static String ACTION_SEEK_TO_TIME = "com.ekulelu.ekaudioplayer.action.SEEK_TO_TIME";
//    public static String IS_RESTART = "com.ekulelu.ekaudioplayer.action.IS_RESTART";


    private final IBinder binder = new LocalBinder();

    private MediaPlayer mMediaPlayer;
    private String mLastFilePath;
    private boolean mIsPause = true;
    private boolean mIsPlayCompleted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mLastFilePath = "";
        MyLog.e("----service create");
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        String actionMode = intent.getStringExtra(MusicService.ACTION_MODE);
//        boolean isRestart = intent.getBooleanExtra(MusicService.IS_RESTART, false);
//        if (ACTION_PLAY.equals(actionMode)) {
//            String filePath = intent.getStringExtra(MainActivity.MEDIA_FILE_PATH);
//            if (!mLastFilePath.equals(filePath)) {  //说明换了歌曲
//                try {
//                    mMediaPlayer.stop();
//                    if (!mLastFilePath.equals(filePath)) {
//                        mMediaPlayer.reset();
//                    }
//                    mMediaPlayer.setDataSource(filePath);
//                    mMediaPlayer.prepare();
//                    mMediaPlayer.start();
//                    MyLog.e("start");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else if (mIsPause && !isRestart) { //pause - start
//                mMediaPlayer.start();
//                MyLog.e("pause - start");
//            } else {
//                mMediaPlayer.start();  //restart
//                MyLog.e("restart");
//            }
//            mIsPause = false;
//            mLastFilePath = filePath;
//        } else if (ACTION_PAUSE.equals(actionMode)) {
//            mMediaPlayer.pause();
//            mIsPause = true;
//            MyLog.e("pause");
//        } else if (ACTION_STOP.equals(actionMode)) {
//            mMediaPlayer.stop();
//            mIsPause = true;
//        } else if (ACTION_SEEK_TO_TIME.equals(actionMode)) {
//            int seekTime = intent.getIntExtra(MainActivity.MEDIA_SEEK_TIME, -1);
//            if (seekTime == -1) {
//                return super.onStartCommand(intent, flags, startId);
//            }
//            mMediaPlayer.seekTo(seekTime * 1000);
//            mIsPause = false;
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();  //release the MediaPlayer
        MyLog.e("music service stops");
        super.onDestroy();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.e("unbind ======");
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


}
