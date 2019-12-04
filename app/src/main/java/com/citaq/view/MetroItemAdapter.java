package com.citaq.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.citaq.citaqfactory.R;
import com.citaq.saxxml.MetroItem;
import com.citaq.saxxml.ViewHolderMetro;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MetroItemAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    TypedArray iconsColor;

    private Context mContext;
    List<MetroItem> mMetroItems;

    boolean isShowTitleEn = true;

    public MetroItemAdapter(Context context, List<MetroItem> metroItems) {
        this.mContext = context;

        inflater = LayoutInflater.from(mContext);
        iconsColor = mContext.getResources().obtainTypedArray(R.array.plain_arr);
        isShowTitleEn = isLanguageEn();

        delANDsort(metroItems);
    }

    //

    //item 删除不显示的item 然后排序
    private void delANDsort(List<MetroItem> metroItems){
        Iterator<MetroItem> iterator = metroItems.iterator();
        while (iterator.hasNext()) {
            MetroItem mMetroItem = iterator.next();
            if (!mMetroItem.isShow()) {
                iterator.remove();
            }
        }

        Comparator<MetroItem> comparator = new Comparator<MetroItem>() {
            public int compare(MetroItem mMetroItem1, MetroItem mMetroItem2) {
                // 按position排序
                return mMetroItem1.getPosition() - mMetroItem2.getPosition();
            }
        };
        //这里就会自动根据规则进行排序
        Collections.sort(metroItems,comparator);

        this.mMetroItems = metroItems;
    }



    @Override
    public int getCount() {
        return mMetroItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMetroItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolderMetro viewHolder = null;

        if (convertView == null) {

            viewHolder = new ViewHolderMetro();
            convertView = inflater.inflate(R.layout.main_grid_item, null);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.img_bg);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);



            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderMetro) convertView.getTag();
        }
        int itemId = (int) (Math.random() * (iconsColor.length() - 1));
        viewHolder.img.setBackgroundDrawable(iconsColor.getDrawable(itemId));

        if(isShowTitleEn){
            viewHolder.title.setText(mMetroItems.get(position).getNameEN());
        }else {
            viewHolder.title.setText(mMetroItems.get(position).getNameCH());
        }

        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 设置ComponentName参数1:packagename参数2:MainActivity路径
        ComponentName cn = new ComponentName(mMetroItems.get(position).getPackageName(), mMetroItems.get(position).getClassName());

        mIntent.setComponent(cn);

//        viewHolder.title.setTag(mIntent);
        viewHolder.intent = mIntent;
        viewHolder.permission = mMetroItems.get(position).getPermission();

        //动态权限申请不好判断 故在activity里处理
        /*viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                // 设置ComponentName参数1:packagename参数2:MainActivity路径
                ComponentName cn = new ComponentName(mMetroItems.get(position).getPackageName(), mMetroItems.get(position).getClassName());

                intent.setComponent(cn);

                mContext.startActivity(intent);
            }
        });*/
        return convertView;
    }




    private boolean isLanguageEn() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        if ("zh".equals(language)) {
            return false;
        }
        return true;
    }

}
    