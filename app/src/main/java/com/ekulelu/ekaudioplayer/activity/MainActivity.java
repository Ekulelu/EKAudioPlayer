package com.ekulelu.ekaudioplayer.activity;

import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.common.MusicList;
import com.ekulelu.ekaudioplayer.R;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyToast;

import java.util.ArrayList;
import android.net.Uri;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_music_list)
    MusicList mRycvMusicList;


    ArrayList<MusicModel> musicLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        grantUriPermission("com.ekulelu.ekaudioplayer", Uri.parse("content://media/external/audio/media"),0);
        MusicList.MusicListAdapter adapter = (MusicList.MusicListAdapter) mRycvMusicList.getAdapter();
        adapter.setmOnItemClickLitener(new MusicList.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                MyToast.showShortText("短按了  "  + position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                MyToast.showShortText("长按了  "  + position);
            }
        });

        initData();
        mRycvMusicList.setmData(musicLists);


    }


    private void initData() {
        if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            MyToast.showShortText("NO sdcard");
        } else {
            Cursor cursor = ContextUtil.getInstance().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    null, null, null);

            if (cursor != null) {
                cursor.moveToNext();  //自己过掉第一条先，那是个录音
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (!path.endsWith("mp3")) {
                        continue;
                    }
                    MusicModel model = new MusicModel();
                    model.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    model.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    model.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                    model.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                    model.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    model.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    musicLists.add(model);
                    if (musicLists.size() > 20) {
                        break;
                    }
                }
                cursor.close();
            }
        }
    }

}
