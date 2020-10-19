package com.citaq.util;

import androidx.annotation.NonNull;

import com.citaq.citaqfactory.R;

import java.io.File;

public class FileDialogListItem {
//    public static String PARENT_TAG = "sParent";   //上一层
    public static final int ROOT_TAG = 1001;
    public static final int PARENT_TAG = 1002;  //parent folder 上一层文件夹
    public static final int CURRENT_TAG = 1003;  //current directory 非根目录子文件夹
    public static final int FILE_TAG = 1004;  //current directory 非根目录子文件夹
    private int fileTpye;
    private int pngTpye;
    private String name;
    private String path;
    private long size;
    private boolean isNew = false;

/*    public FileDialogListItem(String thepath) {
        new FileDialogListItem(thepath, false,false);
    }*/

    /**
     *
     * @param path  路径
     * @param folderType  路径类型
     * @param newdownload 是否是刚下载的，用于设置字体颜色
     */
    public FileDialogListItem(String path, int folderType, boolean newdownload) {
        File file = new File(path);
        this.path = path;
        this.name = file.getName();
        this.isNew = newdownload;
        this.fileTpye = folderType;
        if (file.isFile()) {
            size = file.length();  //Byte
            String sf = getSuffix(name).toLowerCase();
            if (sf.equals("bmp")) {
                pngTpye = R.drawable.filedialog_bmpfile;
            } else if (sf.equals("bmp")) {
                pngTpye = R.drawable.filedialog_pngfile;
            } else if (sf.equals("png")) {
                pngTpye = R.drawable.filedialog_pngfile;
            } else if (sf.equals("jpeg") || sf.equals("jpg")) {
                pngTpye = R.drawable.filedialog_jpgfile;
            } else if (sf.equals("mp3")) {
                pngTpye = R.drawable.filedialog_jpgfile;
            } else if (sf.equals("apk")) {
                pngTpye = R.drawable.filedialog_apk;
            }else if (sf.equals("txt")) {
                pngTpye = R.drawable.filedialog_txt;
            } else {
                pngTpye = R.drawable.filedialog_unknown;
            }
        } else if (file.isDirectory()) {
            size = -1;
            switch (folderType) {
                case ROOT_TAG:
                    this.name = "~";
                    pngTpye = R.drawable.filedialog_root;
                    break;
                case PARENT_TAG:
                    this.name = "..";
                    pngTpye = R.drawable.filedialog_folder_up;
                    break;
                case CURRENT_TAG:
                    pngTpye = R.drawable.filedialog_folder;
                    break;
                default:
                    break;
            }

        }
    }

    public int getPngTpye() {
        return pngTpye;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public int getFileTpye() {
        return fileTpye;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isNew() {
        return isNew;
    }

    private String getSuffix(String filename){
        int dix = filename.lastIndexOf('.');
        if(dix<0){
            return "";
        }
        else{
            return filename.substring(dix+1);
        }
    }

    @Override
    public String toString() {

        String  item = "FileDialogListItem -> path = " + path + ",name = " + name + ",size = " + size + ",fileTpye = " + fileTpye;
        return item;
//        return super.toString();
    }
}
