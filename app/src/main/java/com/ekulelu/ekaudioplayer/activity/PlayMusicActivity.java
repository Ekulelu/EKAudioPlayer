package com.ekulelu.ekaudioplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;

import com.ekulelu.ekaudioplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/** Music play main activity. Create a service to play music which can run in background.
 * Created by aahu on 2016/8/11 0011.
 */
public class PlayMusicActivity extends AppCompatActivity{


    @BindView(R.id.img_btn_cover)
    ImageButton mImgBtnCover;

    @BindView(R.id.img_btn_fast_reverse)
    Button mBtnFastReverse;

    @BindView(R.id.img_btn_previous)
    Button mBtnPrevious;

    @BindView(R.id.img_btn_play_stop)
    Button mBtnPlay;

    @BindView(R.id.img_btn_next)
    Button mBtnNext;

    @BindView(R.id.img_btn_fast_forward)
    Button mBtnFastForward;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.bind(this);
    }


}
