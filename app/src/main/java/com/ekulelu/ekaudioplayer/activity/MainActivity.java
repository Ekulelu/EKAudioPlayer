package com.ekulelu.ekaudioplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ekulelu.ekaudioplayer.common.MusicList;
import com.ekulelu.ekaudioplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_music_list)
    MusicList mRycvMusicList;

    String[] mData = {"qqqq","wwww","eeee"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


}
