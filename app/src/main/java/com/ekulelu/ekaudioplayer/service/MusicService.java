package com.ekulelu.ekaudioplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ekulelu.ekaudioplayer.activity.MainActivity;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyLog;

import java.io.IOException;

/** 控制音乐播放的类
 * Created by aahu on 2016/8/12 0012.
 */
public class MusicService extends Service {

    public static String ACTION_MODE = "com.ekulelu.ekaudioplayer.action.ACTION_MODE";
    public static String ACTION_PLAY = "com.ekulelu.ekaudioplayer.action.PLAY";
    public static String ACTION_PAUSE = "com.ekulelu.ekaudioplayer.action.PAUSE";
    public static String ACTION_STOP = "com.ekulelu.ekaudioplayer.action.PLAY";
    public static String ACTION_SEEK_TO_TIME = "com.ekulelu.ekaudioplayer.action.SEEK_TO_TIME";
    public static String IS_RESTART = "com.ekulelu.ekaudioplayer.action.IS_RESTART";

    //TODO 增加状态常量，写供外界获得的函数
    public static int STATE_PLAY = 0;


    private MediaPlayer mMediaPlayer;
    private String mLastFilePath;
    private boolean mIsPause = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mLastFilePath = "";
        MyLog.e("----service create");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String actionMode = intent.getStringExtra(MusicService.ACTION_MODE);
        boolean isRestart = intent.getBooleanExtra(MusicService.IS_RESTART, false);
        if (ACTION_PLAY.equals(actionMode)) {
            String filePath = intent.getStringExtra(MainActivity.MEDIA_FILE_PATH);
            if (!mLastFilePath.equals(filePath)) {
                try {
                    mMediaPlayer.stop();
                    if (!mLastFilePath.equals(filePath)) {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(filePath);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    MyLog.e("start");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mIsPause && !isRestart) {
                mMediaPlayer.start();
                MyLog.e("pause - start");
            } else {
                mMediaPlayer.start();
                MyLog.e("restart");
            }
            mIsPause = false;
            mLastFilePath = filePath;
        } else if (ACTION_PAUSE.equals(actionMode)) {
            mMediaPlayer.pause();
            mIsPause = true;
            MyLog.e("pause");
        } else if (ACTION_STOP.equals(actionMode)) {
            mMediaPlayer.stop();
            mIsPause = true;
        } else if (ACTION_SEEK_TO_TIME.equals(actionMode)) {
            int seekTime = intent.getIntExtra(MainActivity.MEDIA_SEEK_TIME, -1);
            if (seekTime == -1) {
                return super.onStartCommand(intent, flags, startId);
            }
            mMediaPlayer.seekTo(seekTime * 1000);
            mIsPause = false;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();  //release the MediaPlayer
        super.onDestroy();
    }

    public boolean ismIsPause() {
        return mIsPause;
    }

    /***************************************************************
     * 以下的方法供外部调用
     *
    / * start or resume music
     * @param path music path
     */
    public static void startPlay(String path) {
        MusicService.startPlay(path, false);
    }

    /**
     * start or resume music, if you set isRestart = true,
     * you will restart to music whenever it is playing or pause
     * @param path
     * @param isRestart
     */
    public static void startPlay(String path, boolean isRestart) {
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        intent.putExtra(MainActivity.MEDIA_FILE_PATH, path);
        intent.putExtra(MusicService.IS_RESTART, isRestart);
        intent.putExtra(MusicService.ACTION_MODE,MusicService.ACTION_PLAY);
        ContextUtil.getInstance().startService(intent);
    }

    /**
     * pause the music which is playing
     */
    public static void pausePlay() {
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        intent.putExtra(MusicService.ACTION_MODE,MusicService.ACTION_PAUSE);
        ContextUtil.getInstance().startService(intent);
    }

    /**
     * stop the music, you should
     */
    public static void stopPlay() {
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        intent.putExtra(MusicService.ACTION_MODE,MusicService.ACTION_STOP);
        ContextUtil.getInstance().startService(intent);
    }

    public static void seekToTime(int seekTime) {
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        intent.putExtra(MusicService.ACTION_MODE,MusicService.ACTION_SEEK_TO_TIME);
        intent.putExtra(MusicService.ACTION_SEEK_TO_TIME, seekTime);
        ContextUtil.getInstance().startService(intent);
    }
}
