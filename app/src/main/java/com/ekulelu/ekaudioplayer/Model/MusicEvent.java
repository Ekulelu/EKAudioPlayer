package com.ekulelu.ekaudioplayer.Model;

/**
 * Created by Ekulelu on 16/8/14.
 */
public class MusicEvent {
    public static final int NEXT = 0;
    public static final int PREVIOUS = 1;
    public static final int MODE_CHANGE = 2; // 循环模式改变
    public static final int MUSIC_COMPLETED = 3;
    public static final int CALL_STATE_RINGING = 4;
    public static final int SMS_RECEIVED = 5;


    private int action;

    public MusicEvent(){

    }

    public MusicEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
