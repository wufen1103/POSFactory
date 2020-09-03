package com.citaq.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    public static int REQUEST_CODE = 22;
    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_AUDIO = {Manifest.permission.RECORD_AUDIO};
    public static String[] PERMISSIONS_PHONE = {Manifest.permission.READ_PHONE_STATE};
    /**
     * 检查单个权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkPermission(Context context, @NonNull String permission) {
        List<String> noPermission = new ArrayList<>();
        // 检查该权限是否已经获取
        int i = ContextCompat.checkSelfPermission(context, permission);
        // 权限是否已经 授权 GRANTED---授权  DENIED---拒绝
        if (i == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * 检查多权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkPermission(Context context, @NonNull String... permissions) {
        List<String> noPermission = new ArrayList<>();
        boolean mPermissionOK = true;
        for (String permission : permissions) {
            // 检查该权限是否已经获取
            if (!checkPermission(context, permission)) {
                noPermission.add(permission);
                mPermissionOK =false;
            }
        }
//        String[] result = new String[noPermission.size()];
//        return noPermission.toArray(result);
        return mPermissionOK;
    }

    /**
     * 动态申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermission(Activity context, String... permissions) {
        if (permissions.length > 0) {
            context.requestPermissions(permissions, REQUEST_CODE);   //2
        }
    }

    /**
     * 动态申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkAndRequestPermissions(Activity context, String... permissions) {
        requestPermission(context, permissions);    //1
    }

}
