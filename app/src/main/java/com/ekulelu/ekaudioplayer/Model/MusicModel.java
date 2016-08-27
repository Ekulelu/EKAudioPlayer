package com.ekulelu.ekaudioplayer.Model;

import java.io.Serializable;

/** 保存一个音乐文件的信息
 * Created by aahu on 2016/8/11 0011.
 */
public class MusicModel implements Serializable {
    private long mId;

    /**
     * 标题，不是文件名，可能为unknown
     */
    private String mTitle;
    /**
     * 总时间
     */
    private int mDuration;
    /**
     * 文件路径
     */
    private String mPath;
    /**
     * 歌手，可能为unknown
     */
    private String mArtist;
    /**
     * 专辑，可能为unknown
     */
    private String mAlbum;

    /**
     * 专辑ID，可能为unknown
     */
    private long mAlbumId;

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long mAlbumId) {
        this.mAlbumId = mAlbumId;
    }
}
