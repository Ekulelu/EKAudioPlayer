package com.ekulelu.ekaudioplayer.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ekulelu.ekaudioplayer.Model.MusicEvent;
import com.ekulelu.ekaudioplayer.Model.MusicModel;
import com.ekulelu.ekaudioplayer.common.MusicList;
import com.ekulelu.ekaudioplayer.R;
import com.ekulelu.ekaudioplayer.service.MusicService;
import com.ekulelu.ekaudioplayer.util.ContextUtil;
import com.ekulelu.ekaudioplayer.util.MyLog;
import com.ekulelu.ekaudioplayer.util.MyToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

/** App main activity, which shows a music list. The music list data is from content provider.
 * Created by aahu on 2016/8/11 0011.
 */
public class MainActivity extends AppCompatActivity {

    public static String MUSIC_MODEL = "MusicModel";
    public static String PLAY_NEW_MUSIC = "NewMusic";

    @BindView(R.id.recycler_view_music_list)
    MusicList mRycvMusicList;


    private ArrayList<MusicModel> mMusicLists = new ArrayList<>();

    private MusicService mMusicService;
    private ServiceConnection conn;
    private int mMusicPos; //记录当前音乐在音乐list的位置
    private BroadcastReceiver mMusicBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MyLog.e("----MainActivity create");

        //当在shell用rm删除文件的时候，并不会同步contentProvide，需要自己删除。
//        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 51);
//        ContextUtil.getInstance().getContentResolver().delete(uri, null, null);
        ContextUtil.getInstance().getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null);
        checkPermission(REQUEST_CODE_ASK_SDCARD_PERMISSIONS,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.request_sdcard_permission_message)); //SD卡权限
        int hasPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            MyToast.showShortText(getString(R.string.fail_to_access_sdcard));
        } else {
            initData();
        }
        checkPermission(REQUEST_CODE_ASK_PHONE_STATE_PERMISSIONS,Manifest.permission.READ_PHONE_STATE,
                getString(R.string.request_phone_state_permission_message)); //通话状态权限
        checkPermission(REQUEST_CODE_ASK_SMS_RECEIVE_PERMISSIONS,Manifest.permission.RECEIVE_SMS,
                getString(R.string.request_sms_permission_message));
        checkPermission(REQUEST_CODE_ASK_OUT_GOING_CALL_PERMISSIONS,Manifest.permission.PROCESS_OUTGOING_CALLS,
                getString(R.string.request_outgoing_call_permission_message));

        EventBus.getDefault().register(this);

        bindMusicService();

        mMusicBroadcastReceiver = new MusicBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.MUSIC_COMPLETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicBroadcastReceiver, intentFilter);


        //TODO 下面这段抽出来
        MusicList.MusicListAdapter adapter = (MusicList.MusicListAdapter) mRycvMusicList.getAdapter();
        adapter.setmOnItemClickLitener(new MusicList.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                mMusicPos = position;
                startMusic();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                MyToast.showShortText("长按了  "  + position);
                mMusicService.pausePlay();
            }
        });
    }


    /**
     * 发送intent，启动PlayActivity。
     */
    private void startMusic() {
        Intent intent = new Intent(MainActivity.this, PlayMusicActivity.class);
        intent.putExtra(MUSIC_MODEL, mMusicLists.get(mMusicPos));
        startActivity(intent);
    }


    /**
     * 读取sd卡获得歌曲列表
     */
    private void initData() {
        if(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            MyToast.showShortText(getString(R.string.no_sdcard));
        } else {
            Cursor cursor = ContextUtil.getInstance().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    null, null, null);

            if (cursor != null) {
                String artistFilter = getString(R.string.artist_filter);
                String pathSuffixFilter = getString(R.string.path_suffix_filter);
                int musicListSize = Integer.parseInt(getString(R.string.music_list_size));
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String ar = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    if (!path.endsWith(pathSuffixFilter)) {
                        continue;
                    }
                    boolean artistFilterSwitch = Boolean.parseBoolean(getString(R.string.artist_filter_switch));

                    if (artistFilterSwitch && (null == ar || !ar.equals(artistFilter))) {
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

                    if (mMusicLists.size() >= musicListSize) {
                        break;
                    }
                }
                cursor.close();
            }
        }
//        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 58);
//        ContextUtil.getInstance().getContentResolver().delete(uri, null, null);
        mRycvMusicList.setmData(mMusicLists);
        if (0 == mMusicLists.size()) {
            MyToast.showShortText(getString(R.string.no_music));
        }
    }


    /**
     * 绑定service，得到service对象。
     */
    private void bindMusicService() {
        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMusicService = ((MusicService.LocalBinder)service).getService();
            }
        };
        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     *  接收到上一首,下一首的按钮事件
     */
    @Subscribe
    public void OnMusicEvent(MusicEvent event) {
        int action = event.getAction();
        if (action == MusicEvent.NEXT || action == MusicEvent.PREVIOUS){
            if (action == MusicEvent.NEXT){
                mMusicPos = (mMusicPos +1 )% mMusicLists.size();
            } else {
                mMusicPos--;
                if (mMusicPos < 0){
                    mMusicPos = mMusicLists.size() - 1;
                }
            }
            MusicModel musicModel = mMusicLists.get(mMusicPos);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PLAY_NEW_MUSIC);
            broadcastIntent.putExtra(MUSIC_MODEL,musicModel);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(broadcastIntent);
        }
    }

    /**
     * 用来处理播放完一首歌的情况
     */
    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.e("----music broadcast receive");
            String action = intent.getAction();
            if (MusicService.MUSIC_COMPLETED.equals(action)){
                //TODO 可以根据循环模式选择下一首,这里先直接调用下一首
                MyLog.e("----music completed");
                mMusicPos = (mMusicPos +1 )% mMusicLists.size();
                MusicModel musicModel = mMusicLists.get(mMusicPos);
                //这里不能直接启动PlayActivity,因为它可能没有显示，没有显示的时候，可能被destroy掉了，
                // 所以要调用service的方法播放音乐。
                mMusicService.startPlay(musicModel.getPath());
                //选择发送广播，如果能接受到那就是有，更新PlayActivity界面。
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(PLAY_NEW_MUSIC);
                broadcastIntent.putExtra(MUSIC_MODEL,musicModel);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(broadcastIntent);
//                startMusic();
            }
        }
    }

    /**************************************
    权限相关函数
    **************************************/
    final private int REQUEST_CODE_ASK_SDCARD_PERMISSIONS = 1;
    final private int REQUEST_CODE_ASK_PHONE_STATE_PERMISSIONS = 2;
    final private int REQUEST_CODE_ASK_SMS_RECEIVE_PERMISSIONS = 3;
    final private int REQUEST_CODE_ASK_OUT_GOING_CALL_PERMISSIONS = 4;

    private void checkPermission(final int requestCode, final String permission, String rationale) {
        //检查权限
        //TODO 逻辑需要修改
        int hasPermission = ContextCompat.checkSelfPermission(this,permission);

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {
                showMessageOKCancel(rationale,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] {permission},
                                        requestCode);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {permission},
                    requestCode);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_SDCARD_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initData();
                } else {
                    MyToast.showShortText(getString(R.string.fail_to_access_sdcard));
                }
                break;
            case REQUEST_CODE_ASK_PHONE_STATE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    MyToast.showShortText(getString(R.string.fail_to_obtain_phone_state_permission));
                }
                break;
            case REQUEST_CODE_ASK_OUT_GOING_CALL_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    MyToast.showShortText(getString(R.string.fail_to_obtain_out_going_call_permission));
                }
                break;
            case REQUEST_CODE_ASK_SMS_RECEIVE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    MyToast.showShortText(getString(R.string.fail_to_obtain_sms_receive_permission));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    /**************************************/
    @Override
    protected void onStop() {
        MyLog.e("--- mainActivity stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //下面这段可以让activity退出后结束service
//        Intent intent = new Intent(ContextUtil.getInstance(), MusicService.class);
//        stopService(intent);
        unbindService(conn);
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicBroadcastReceiver);
        MyLog.e("-- MainActivity stop");
        super.onDestroy();
    }


}
