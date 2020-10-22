package com.citaq.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class SmbUtil {

    //publishProgress的更新标记
    private static final int PROGRESS_MAX = 0X1;   //download file 大小
    private static final int UPDATE = 0X2;         //实时

    private static final String SYSTEM_PREINSTALL =  "system/preinstall/";

    //publishProgress的更新标记

    Context mContext;
    ProgressBar mProgressBar = null;
    CallbackSMB  mCallbackSMB;

    int BUFFER = 1024; //1024 * 8; //8k

    public SmbUtil() {
    }

//    public SmbUtil(Context context) {
//        mContext = context;
//        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
//    }

    public SmbUtil(CallbackSMB  callbackSMB) {
        mCallbackSMB = callbackSMB;
    }

    private static List getFilelist(String remoteUrl) { ////不能在主线程运行
        List list = new ArrayList();
        try {
            SmbFile smbFile = new SmbFile(remoteUrl);
            SmbFile[] a = smbFile.listFiles();
            for (SmbFile f : a) {
                list.add(f.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void smbDownload(String remoteUrl,String localDir){
        String[] urls = {remoteUrl, localDir};
        new DownLoad().execute(urls);
    }


    /**
     * 从局域网中共享文件中得到文件并保存在本地磁盘上
     * @param remoteUrl 共享电脑路径 如：smb://administrator:123456@172.16.10.136/smb/1221.zip  , smb为共享文件
     * 注：如果一直出现连接不上，有提示报错，并且错误信息是 用户名活密码错误 则修改共享机器的文件夹选项 查看 去掉共享简单文件夹的对勾即可。
     * @param localDir 本地路径 如：D:/
     */
    private boolean smbGet(String remoteUrl,String localDir){
        boolean result = false;
        InputStream in = null;
        OutputStream out = null;
        File localFile = null;

        try {
            System.out.println("SmbUtil: dowload start.");
            long time1=System.currentTimeMillis();
            SmbFile smbFile = new SmbFile(remoteUrl);
            String fileName = smbFile.getName();
            localFile = new File(localDir+File.separator+fileName);
            in = new BufferedInputStream(new SmbFileInputStream(smbFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte []buffer = new byte[BUFFER];
            while((in.read(buffer)) != -1){
                out.write(buffer);
                buffer = new byte[BUFFER];
            }

            long time2=System.currentTimeMillis();
            System.out.println("SmbUtil:"+ remoteUrl +" download time =" + (time2 - time1));
            result = true;
        }catch (Exception e) { //jcifs.smb.SmbException //jcifs.util.transport.TransportException
            //下载一部分后断网，删掉已经下载的文件
            if(localFile != null){
                if (localFile.delete()) {
                    System.out.println("SmbUtil:"+"delete " + localFile);
                }
            }
            e.printStackTrace();
        }finally{
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * 把本地磁盘中的文件上传到局域网共享文件下
     * @param remoteUrl 共享电脑路径 如：smb://administrator:123456@172.16.10.136/smb
     * @param localFilePath 本地路径 如：D:/
     */
    public static void smbPut(String remoteUrl,String localFilePath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);
            String fileName = localFile.getName();
            SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName);
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while ((in.read(buffer)) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //AsyncTask<Params, Progress, Result>
    //doInBackground(Params...)
    //publishProgress(Progress...)
    //onProgressUpdate(Progress...)
    //onPostExecute(Result)
    class DownLoad extends AsyncTask<String, Void, List<String>> {
        //在执行实际的后台操作前被UI Thread调用
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //准备下载前的初始进度
//            mCallbackSMB.onShowProgress(true);
        }

        @Override
        public List<String> doInBackground(String... params) {
            System.out.println("smb:doInBackground run..");
            List<String> download_remoteFileName = new ArrayList<String>();

            try {
//                long time1=System.currentTimeMillis();
                SmbFile smbFile2 = new SmbFile(params[0]);
//                long time2=System.currentTimeMillis();
                SmbFile[] a = smbFile2.listFiles();
//                long time3=System.currentTimeMillis();
//                System.out.println("SmbUtil:" + "SmbFile time =" + (time2 - time1) + ",listFiles() time =" + (time3 - time2)); //[20ms]
                for (SmbFile f : a) {
                    System.out.println(f.getName());
                    //smbGet(params[0]+File.separator+f.getName(),params[1]);
                    //判断文件是否存在，不存在下载
                    String localFile = params[1]+File.separator+f.getName();
                    if(f.getName().endsWith(".zip")){
                        localFile = localFile.substring(0,localFile.length()-4) +".apk";
                    }
                    //判断文件是否存在，不存在下载
                    if(fileIsExists(localFile)){

                    }else{
                        //准备下载进度
                        publishProgress();

                        boolean isDownloadOK = smbGet(f.getPath(),params[1]);
                        if(isDownloadOK){
                            if(f.getName().endsWith(".zip")){
                                try {
                                    List<String> allfile = unZip2(params[1]+File.separator+f.getName(), params[1]);
                                    File zipFile = new File(params[1]+File.separator+f.getName());
                                    for(int i = 0; i<allfile.size(); i++){
                                        download_remoteFileName.add(allfile.get(i));
                                    }
                                    if (zipFile.delete()) {
                                        System.out.println("SmbUtil:"+"delete " + zipFile.getPath());
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else{
                                download_remoteFileName.add(f.getName());
                            }
                        }
                    }
                }
                ////////////////////////////////如果预安装有MobiControl，拷贝到microSD(microSD没有MobiControl)//////////////////////
                File preinstallFile = new File(SYSTEM_PREINSTALL);
                File[] preinstallFiles = preinstallFile.listFiles();
                for (File preinstalF : preinstallFiles) {
                    if (preinstalF.getName().toUpperCase().contains("MOBICONTROL")){
                        File[] mMobiControl = new File(preinstalF.getPath()).listFiles();
                        for (File ff : mMobiControl) {
                            String localpreinstalFile =   params[1]+ff.getName();
                            if(!fileIsExists(localpreinstalFile)) {   //microSD没有该文件下载
                                boolean isOK = copyFile(ff.getPath(), localpreinstalFile);
                                if (isOK) {
                                    download_remoteFileName.add(ff.getName());
                                }
                            }
                        }
                    }
                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }catch (Exception e) {
                e.printStackTrace();
            }
            return download_remoteFileName;
        }
        @Override
        public void onPostExecute(List<String> download_remoteFileName) {
            //完成
            if(mCallbackSMB != null){
                mCallbackSMB.onDownLoadResult(download_remoteFileName);
                mCallbackSMB.onShowProgress(false);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mCallbackSMB.onShowProgress(true);
        }

    };

    //判断文件是否存在
    private boolean fileIsExists(String strFile){
        try{
            File f=new File(strFile);
            if(!f.exists()){
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public List<String> unZip(String unZipfileName, String mDestPath) {
        System.out.println("SmbUtil: unZip start.");
        long time1=System.currentTimeMillis();
        List<String> fileName = new ArrayList<String>();
        if (!mDestPath.endsWith("/")) {
            mDestPath = mDestPath + "/";
        }
        FileOutputStream fileOut = null;
        ZipInputStream zipIn = null;
        ZipEntry zipEntry = null;
        File file = null;
        int readedBytes = 0;
        byte buf[] = new byte[4096];
        try {
            zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(unZipfileName)));
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                file = new File(mDestPath + zipEntry.getName());
                fileName.add(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    //如果文件存在不需要解压
                    if(!file.exists()){
                        fileOut = new FileOutputStream(file);
                        while ((readedBytes = zipIn.read(buf)) > 0) {
                            fileOut.write(buf, 0, readedBytes);
                        }
                        fileOut.close();
                    }
                }
                zipIn.closeEntry();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        long time2=System.currentTimeMillis();
        System.out.println("SmbUtil:"+ "unZip time =" + (time2 - time1));

        return fileName;
    }


    public List<String> unZip2(String unZipfileName, String storageDirectory) {
        System.out.println("SmbUtil: unZip start.");
        long time1=System.currentTimeMillis();
        List<String> fileName = new ArrayList<String>();
        boolean result = false;
        String filePath = storageDirectory;
        ZipFile zipFile = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            zipFile = new ZipFile(unZipfileName);
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();
                if (entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(filePath + entry.getName());
                fileName.add(entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos, BUFFER);
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
            }
            result = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        finally {
            try {
                if (null != bos) {
                    bos.close();
                }
                if (null != bis) {
                    bis.close();
                }
                if (null != zipFile) {
                    zipFile.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }
        long time2=System.currentTimeMillis();
        System.out.println("SmbUtil:"+ "unZip time =" + (time2 - time1));

        return fileName;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     *         <code>false</code> otherwise
     */
    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--SmbUtil--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--SmbUtil--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--SmbUtil--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */
            long time1=System.currentTimeMillis();
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            long time2=System.currentTimeMillis();
            System.out.println("SmbUtil:"+ "copyFile time =" + (time2 - time1));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public interface CallbackSMB {
        abstract void onDownLoadResult(List<String> download_remoteFileName);
        abstract void onShowProgress (boolean isshow);

    }
}
