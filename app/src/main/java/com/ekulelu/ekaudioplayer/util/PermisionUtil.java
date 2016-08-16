package com.ekulelu.ekaudioplayer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.ekulelu.ekaudioplayer.R;

/**
 * Created by Ekulelu on 16/8/13.
 */
public class PermisionUtil {

    private Context context;

    public PermisionUtil (Context context) {
          this.context = context;
    }


    public void checkPermission(final int requestCode, final String permission, String rationale, final Activity activity) {
        //检查权限
        //TODO 逻辑需要修改
        int hasPermission = ContextCompat.checkSelfPermission(context,permission);

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                showMessageOKCancel(rationale,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[] {permission},
                                        requestCode);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(activity,
                    new String[] {permission},
                    requestCode);
            return;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), okListener)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .create()
                .show();
    }
}
