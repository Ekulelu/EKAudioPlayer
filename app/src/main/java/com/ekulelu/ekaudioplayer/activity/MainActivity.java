package com.ekulelu.ekaudioplayer.activity;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.common.MusicList;
import com.ekulelu.ekaudioplayer.R;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyToast;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

/** App main activity, which shows a music list. The music list data is from content provider.
 * Created by aahu on 2016/8/11 0011.
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_music_list)
    MusicList mRycvMusicList;


    ArrayList<MusicModel> mMusicLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        //当在shell用rm删除文件的时候，并不会同步contentProvide，需要自己删除。
//        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 51);
//        ContextUtil.getInstance().getContentResolver().delete(uri, null, null);
        ContextUtil.getInstance().getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null);
        checkPermision();

        //TODO 下面这段抽出来
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



    }




    private void initData() {
        if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            MyToast.showShortText("NO sdcard");
        } else {
            Cursor cursor = ContextUtil.getInstance().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    null, null, null);

            if (cursor != null) {

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
                    mMusicLists.add(model);
                    if (mMusicLists.size() > 20) {
                        break;
                    }
                }
                cursor.close();
            }
        }
        mRycvMusicList.setmData(mMusicLists);
        if (0 == mMusicLists.size()) {
            MyToast.showShortText(getString(R.string.no_music));
        }
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private void  checkPermision() {
        //检查权限
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessageOKCancel("You need to allow access to SDCard",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        initData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    initData();
                } else {
                    // Permission Denied
                    MyToast.showShortText(getString(R.string.fail_to_access_sdcard));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
