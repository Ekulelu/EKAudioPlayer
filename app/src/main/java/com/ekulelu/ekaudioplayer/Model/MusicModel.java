package com.ekulelu.ekaudioplayer.Model;

import java.io.Serializable;

/** 保存一个音乐文件的信息
 * Created by aahu on 2016/8/11 0011.
 */
public class MusicModel implements Serializable {
    private int id;

    /**
     * 标题，不是文件名，可能为unknown
     */
    private String title;
    /**
     * 总时间
     */
    private int duration;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 歌手，可能为unknown
     */
    private String artist;
    /**
     * 专辑，可能为unknown
     */
    private String album;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

}
