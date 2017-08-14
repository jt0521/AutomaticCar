package com.handsome.boke2.AccessibilityService;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * description: 权限请求，简化过程
 * author: tgl
 * date: 2017/8/1 17:04
 * update: 2017/8/1
 */

public abstract class PermissionUtil {

    private ArrayList<String> listPermission;
    private HashSet<Integer> requestCodeSet;
    private Context context;
    private boolean isCancel = true;//取消
    private Dialog askDialog;
    //请求设置页面code
    public static int REQUEST_CODE_SETTING = 999;

    public PermissionUtil(Context context) {
        this.context = context;
        listPermission = new ArrayList<>();
        requestCodeSet = new HashSet<>();
    }

    /**
     * 判断哪些权限未授予
     * 以便必要的时候重新申请
     */
    /**
     * 权限请求
     *
     * @param requestCode
     */
    public void requestPermissions(int requestCode, @NonNull String[] permissions) {
        listPermission.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isCancel = false;
            requestCodeSet.add(requestCode);
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermission.add(permission);
                }
            }
            if (listPermission.isEmpty()) {//未授予的权限为空，表示都授予了
                requestPermissionsSuccess();
            } else {
                String[] notPermissions = listPermission.toArray(new String[listPermission.size()]);//将List转为数组
                ActivityCompat.requestPermissions((Activity) context, notPermissions, 1);
                return;
            }
        }
        requestPermissionsSuccess();
    }

    /**
     * 获得反馈，检查是否允许了权限
     * activity 重写此方法onRequestPermissionsResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void requestResponse(int requestCode, String[] permissions, int[] grantResults) {
        List<String> notPermissions = new ArrayList<>();
        boolean showRequestPermission = false;//是否勾选了禁止询问
        if (requestCodeSet.contains(requestCode)) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    if (!showRequestPermission) {
                        showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissions[i]);
                    }
                    notPermissions.add(permissions[i]);
                }
            }
            if (notPermissions.isEmpty()) {
                requestPermissionsSuccess();
            } else {
                if (showRequestPermission) {//重新申请权限
                    requestPermissions(requestCode, notPermissions.toArray(new String[notPermissions.size()]));
                } else {
                    //提示用户手动设置
                    askForPermission();
                }
            }
        }
    }

    /**
     * 提示无权限，需要手动开启
     */
    private void askForPermission() {
        if (askDialog != null && askDialog.isShowing()) {
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("权限被禁用，需要手动开启");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isCancel = true;
                    dialog.dismiss();
                    requestPermissionsFail();
                }
            });
            builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName())); // 根据包名打开对应的设置界面
                    ((Activity)context).startActivityForResult(intent,PermissionUtil.REQUEST_CODE_SETTING);
                }
            });
            askDialog = builder.create();
            askDialog.setCanceledOnTouchOutside(false);
            askDialog.show();
        }
    }

    /**
     * 去到设置页面返回后，再次检查是否拥有权限
     */
    public void doubleCheck(int requestCode) {
        if (listPermission == null || listPermission.isEmpty() || isCancel) {
        } else {
            requestPermissions(requestCode, listPermission.toArray(new String[listPermission.size()]));
        }
    }

    protected abstract void requestPermissionsSuccess();

    protected abstract void requestPermissionsFail();
}
