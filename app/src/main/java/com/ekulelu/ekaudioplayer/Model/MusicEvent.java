package com.ekulelu.ekaudioplayer.Model;

/**
 * Created by Ekulelu on 16/8/14.
 */
public class MusicEvent {
    public static final int NEXT = 0;
    public static final int PREVIOUS = 1;
    public static final int MODE_CHANGE = 2;
    public static final int MUSIC_COMPLETED = 3;

    private int action;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
