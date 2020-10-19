package com.citaq.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;
import java.text.DecimalFormat;

public class SDcardUtil {

    String sdcard ="/mnt/external_sd";


    /**
     * 判断是否有SD卡
     *
     * @return
     */
     public Boolean isHasSD() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = new File(sdcard);
            if(path.exists()){
                   return true;
            }
        }
        return false;
    }


    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public String getSDTotalSize(Context context) {
//		getPrimaryStoragePath();
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File path = new File(sdcard);

            if(path.exists()){
                try{
                    StatFs stat = new StatFs(path.getPath());
                    long blockSize = stat.getBlockSize();
                    long totalBlocks = stat.getBlockCount();
                    if(totalBlocks ==0){
                        return "No SD.";
                    }
                    return Formatter.formatFileSize(context, blockSize * totalBlocks);
                }catch(Exception e) {
                    return "No SD.";
                }
            }
        }
        return "No SD.";

    }

    /**
     * 转换文件大小
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
