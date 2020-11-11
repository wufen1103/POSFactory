package com.citaq.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.citaq.citaqfactory.R;
import com.citaq.util.FileDialogListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProgressListFileDialog extends Dialog {
    Context context;
    LinearLayout progress_layout;
    ListView listview_list;
    String root_path;
    ProgressListDialogListener pickDialogListener;
    ProgressListAdapter mProgressListAdapter;
    List<FileDialogListItem> items;
    FileDialogListItem choose_item;
    int nowFileType = FileDialogListItem.ROOT_TAG;

    public ProgressListFileDialog(Context context, String path, ProgressListDialogListener progressListDialogListener) {
//        super(context);
        super(context);
        this.context = context;
        if(path.length()>1 && path.endsWith("/")){
            this.root_path = path.substring(0,path.length()-1);
        }else{
            this.root_path = path;
        }
//        this.root_path = path;
        this.pickDialogListener = progressListDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将xml文件中的布局加入到 java文件中
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.progresslistdialog, null);
        progress_layout = (LinearLayout) layout.findViewById(R.id.progress_layout);
        listview_list = (ListView) layout.findViewById(R.id.listview);
        //将布局设置到view中。
        initListViewData();

        this.setContentView(layout);
    }

    private void initListViewData() {
        //将进度条设置不可见
//        progress_layout.setVisibility(View.GONE);
//        listview_list.setVisibility(View.VISIBLE);
        this.items = getFileList(root_path,null);
        mProgressListAdapter = new ProgressListAdapter(context, items);

        listview_list.setAdapter(mProgressListAdapter);
        //设置每个元素的 点击事件
        listview_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String current_path = items.get(position).getPath();
                //if(!current_path.equals(path)) {
                File file = new File(current_path);
                if(file.isDirectory()) {
                    notifyDataTypeChanged(current_path, items.get(position).getFileTpye());
                }else{
                    if(current_path.endsWith(".apk")){
                        File apkFile = new File(current_path);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");

                        context.startActivity(intent);
                    }else if (pickDialogListener!=null) {
                        pickDialogListener.onListItemLongClick(position, items.get(position).getPath());
                    }

                }
            }
        });
        listview_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //for test
//                choose_item = items.get(position);
//                showNormalDialog();
                return true;
            }
        });
    }

    public void notifyDataChanged(String path,List<String> download_remoteFileName){  // FileType = FileDialogListItem.ROOT_TAG
        items = getFileList(path,download_remoteFileName);
        mProgressListAdapter.setList(items);
        mProgressListAdapter.notifyDataSetChanged();
    }

    private void notifyDataTypeChanged(String path, int fileType){
        nowFileType = fileType;
        items = getFileList(path,null);
        mProgressListAdapter.setList(items);
        mProgressListAdapter.notifyDataSetChanged();
    }

    public interface ProgressListDialogListener {
        abstract void onListItemLongClick(int position, String path);

    }

    public void showProgress(boolean isShow){
        if(isShow){
            progress_layout.setVisibility(View.VISIBLE);
        }else{
            progress_layout.setVisibility(View.GONE);
        }

    }

    private List<FileDialogListItem> getFileList(String path,List<String> download_remoteFileName){
        //数据初始化
        File[] files = new File(path).listFiles();
        List<FileDialogListItem> list = new ArrayList<>();

        // 用来先保存文件夹和文件夹的两个列表
        ArrayList<FileDialogListItem> lfolders = new ArrayList<FileDialogListItem>();
        ArrayList<FileDialogListItem> lfiles = new ArrayList<FileDialogListItem>();
        if (files != null) {
            for (File file : files) {
                if(download_remoteFileName != null) {
                    boolean newdownload = false;
                    for (int i = 0; i < download_remoteFileName.size(); i++) {
                        if (download_remoteFileName.get(i).equals(file.getName())) {
                            newdownload = true;
                        }
                    }
                    if (file.isDirectory()) {
                        FileDialogListItem mFileDialogListItem = new FileDialogListItem(file.getPath(), FileDialogListItem.CURRENT_TAG, newdownload);
                        lfolders.add(mFileDialogListItem);
                    } else if (file.isFile()) {
                        FileDialogListItem mFileDialogListItem = new FileDialogListItem(file.getPath(), FileDialogListItem.FILE_TAG, newdownload);
                        lfiles.add(mFileDialogListItem);
                    }
                }else{
                    if (file.isDirectory()) {
                        FileDialogListItem mFileDialogListItem = new FileDialogListItem(file.getPath(), FileDialogListItem.CURRENT_TAG, false);
                        lfolders.add(mFileDialogListItem);
                    } else if (file.isFile()) {
                        FileDialogListItem mFileDialogListItem = new FileDialogListItem(file.getPath(), FileDialogListItem.FILE_TAG, false);
                        lfiles.add(mFileDialogListItem);
                    }
                }

            }

            switch (nowFileType) {
                case FileDialogListItem.ROOT_TAG:

                    break;
                case FileDialogListItem.PARENT_TAG:
                    if(path.equals(this.root_path)){

                    }else{
                        // 添加根目录 和 上一层目录
                        File fl = new File(path);
                        String parentPath = fl.getParent();
                        list.add(new FileDialogListItem(this.root_path, FileDialogListItem.ROOT_TAG, false));
                        list.add(new FileDialogListItem(parentPath, FileDialogListItem.PARENT_TAG, false));
                    }

                    break;
                default:
                    // 添加根目录 和 上一层目录
                    File fl = new File(path);
                    String parentPath = fl.getParent();
                    list.add(new FileDialogListItem(this.root_path, FileDialogListItem.ROOT_TAG, false));
                    list.add(new FileDialogListItem(parentPath, FileDialogListItem.PARENT_TAG, false));
                    break;
            }

            list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
            list.addAll(lfiles);    //再添加文件
        }else{
            // 添加根目录 和 上一层目录
            File fl = new File(path);
            String parentPath = fl.getParent();
            list.add(new FileDialogListItem(this.root_path, FileDialogListItem.ROOT_TAG, false));
            list.add(new FileDialogListItem(parentPath, FileDialogListItem.PARENT_TAG, false));
            list.addAll(lfolders);
        }

        //test 打印数据
//        for(int i = 0 ; i < list.size() ; i++){
//            System.out.println(list.get(i).toString());
//        }
        return list;
    }

    private void showNormalDialog(){
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
//        normalDialog.setTitle("我是一个普通Dialog");
        normalDialog.setMessage( context.getResources().getString(R.string.str_delete) + choose_item.getName() +"  ?");
        normalDialog.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
//                        choose_item
                        File file = new File(choose_item.getPath());
                        if (file.delete()) {
                            System.out.println("Delete OK!!");
                            //更新列表
                            List<FileDialogListItem> chooselist = new ArrayList<FileDialogListItem>();
                            chooselist.add(choose_item);
                            items.removeAll(chooselist);
                            mProgressListAdapter.setList(items);
                            mProgressListAdapter.notifyDataSetChanged();

                        }else{
                            
                        }
                    }
                });
        // 显示
        normalDialog.show();
    }
}


