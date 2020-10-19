package com.citaq.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.citaq.citaqfactory.R;
import com.citaq.util.FileDialogListItem;
import com.citaq.util.SDcardUtil;

import java.util.ArrayList;
import java.util.List;

public class ProgressListAdapter extends BaseAdapter {
    private Context context;



    private List<FileDialogListItem> list;
    public ProgressListAdapter(Context context,List<FileDialogListItem> list){
        this.context = context;
        this.list = list;
    }

    public void setList(List<FileDialogListItem> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //将list_item.xml中的布局加载到listview中
        Holder holder = null;
        if(convertView == null){
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.progresslistdialog_item,null);
            holder.item_image = (ImageView)convertView.findViewById(R.id.filedialogitem_image);
            holder.item_name = (TextView)convertView.findViewById(R.id.filedialogitem_name);
            holder.item_path = (TextView)convertView.findViewById(R.id.filedialogitem_path);
            holder.item_size = (TextView)convertView.findViewById(R.id.filedialogitem_size);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        //设置文本内容
        holder.item_image.setImageResource(list.get(position).getPngTpye());

        String  name = list.get(position).getName();
        if(list.get(position).isNew()){
//            name ="<big><font color=\"#FFFF00\">" + name +"</font></big>" ;
            name ="<font color=\"#FFFF00\">" + name +"</font>" ;

        }
        holder.item_name.setText(Html.fromHtml(name));
        holder.item_path.setText(list.get(position).getPath());

        if(list.get(position).getSize() == -1){
            holder.item_size.setText("");
        }else{
            String size = SDcardUtil.formetFileSize(list.get(position).getSize());
            holder.item_size.setText(size);
        }

        return convertView;
    }
    class Holder{
        ImageView item_image;
        TextView item_name;
        TextView item_path;
        TextView item_size;
    }

}
