package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.citaq.saxxml.AnalyzeSAX;
import com.citaq.saxxml.MetroItem;
import com.citaq.saxxml.ViewHolderMetro;
import com.citaq.util.CitaqBuildConfig;
import com.citaq.util.MainBoardUtil;
import com.citaq.util.PermissionUtil;
import com.citaq.util.SharePreferencesHelper;
import com.citaq.view.MetroItemAdapter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@SuppressLint("NewApi")
public class MainActivity3 extends Activity {
	protected static final String Tag = "MainActivity3";
	MetroItemAdapter adapter;
	Context mContext;
	Intent mIntent;
	String permission;

	private int[] metroIgnoreList1 ={1001}; //3288 LED Test
	private int[] metroIgnoreList2 ={1010,1012,1013}; //3368 Serial test , Other Test , Printer Firmware Upgrade ,

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_main_gride);

		loadMetro();
	}

	private void loadMetro(){
		GridView gridView = (GridView) findViewById(R.id.gridview);
		adapter = new MetroItemAdapter(this, metroForBoard());
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mIntent = ((ViewHolderMetro)view.getTag()).intent;

				permission = ((ViewHolderMetro)view.getTag()).permission;

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && permission != null && !"".equals(permission) ){
					String[] strarr = permission.split("\\|");
					if (strarr.length>0){
						PermissionUtil.checkAndRequestPermissions(MainActivity3.this, strarr);
						return;
					}
				}
				//android 6.0  android.permission.READ_PHONE_STATE 需要申请权限，其他不用
				if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M && permission != null && "android.permission.READ_PHONE_STATE".equals(permission) ){
					String[] strarr = permission.split("\\|");
					if (strarr.length>0){
						PermissionUtil.checkAndRequestPermissions(MainActivity3.this, strarr);
						return;
					}
				}else {
					mContext.startActivity(mIntent);
				}
			}

		});
	}

	/**
	 * 针对不同的主板设置那些MetroItem不显示
	 * @return
	 */
	private List<MetroItem> metroForBoard(){
		InputStream inputStream = getResources().openRawResource(R.raw.default_workspace);
		List<MetroItem> metroItemList = AnalyzeSAX.readXML(inputStream);

		Iterator<MetroItem> iterator = metroItemList.iterator();
		while (iterator.hasNext()) {
			MetroItem mMetroItem = iterator.next();
			if (!mMetroItem.isShow()) {
				iterator.remove();
			}
			if((MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63())){
				for(int i: metroIgnoreList1){
					if(mMetroItem.getItemId() == i){
						iterator.remove();
					}

				}

			}else if (MainBoardUtil.isRK3188() || MainBoardUtil.isRK3368()){
				for(int i: metroIgnoreList2){
					if(mMetroItem.getItemId() == i){
						iterator.remove();
					}

				}
			}

		}

		return metroItemList;
	}

	 @Override
	protected void onDestroy() {
		Log.i(Tag, "onDestroy----");

		SharePreferencesHelper mSharePreferencesHelper = new SharePreferencesHelper(this,CitaqBuildConfig.SHAREPREFERENCESNAME);
		mSharePreferencesHelper.clear();
		
		super.onDestroy();
		
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean hasPermissionDenin = false;
		if (requestCode == PermissionUtil.REQUEST_CODE) {
			for (int i = 0; i < grantResults.length; i++) {
				if (grantResults[i] == -1) {
					hasPermissionDenin = true;
					break;
				}
			}
			if (hasPermissionDenin) {
				Toast.makeText(mContext, "Permissions ERROR!", Toast.LENGTH_LONG).show();
			}else {
				mContext.startActivity(mIntent);
			}
		}

	}

}
