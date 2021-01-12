package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
	Button bt_mutitest;
	int screenNum = 0;
	private int[] metroIgnoreList1 ={1010}; //3288 LED Test
	private int[] metroIgnoreList2 ={1100,1120,1130,1140}; //3368 Serial test, Other Test, Diff Display and Touch, Printer Firmware Upgrade
	private int metroIgnore = 1130; //屏幕<2

	private static int OVERLAY_PERMISSION_REQ_CODE = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_main_gride);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			DisplayManager mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
			Display[] displays = mDisplayManager.getDisplays();
			screenNum = displays.length; //屏幕数
		}

		loadMetro();

		initMultiTest();
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
				}if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M && permission != null && permission.contains("android.permission.RECORD_AUDIO")){
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


	private void initMultiTest(){
		bt_mutitest = (Button)findViewById(R.id.bt_mutitest);
		bt_mutitest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent webviewIntent = new Intent(mContext, WebViewActivity.class);
				webviewIntent.putExtra("WIFI", true);
				mContext.startActivity(webviewIntent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			mContext.startActivity(mIntent);
		}*/

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			if (Settings.canDrawOverlays(mContext)) {
				//mContext.startActivity(mIntent);
			} else {
				Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_LONG).show();
			}
		}
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
			}else {
				if ((MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63())) {
					for (int i : metroIgnoreList1) {
						if (mMetroItem.getItemId() == i) {
							iterator.remove();
						}
					}

					if (screenNum < 2) {
						if (mMetroItem.getItemId() == metroIgnore) {
							iterator.remove();
						}
					}

				} else if (MainBoardUtil.isSerialPrinterBoard()) {
					for (int i : metroIgnoreList2) {
						if (mMetroItem.getItemId() == i) {
							iterator.remove();
						}

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
				for (String permission : permissions) {
					// 检查权限是否包含OVERLAY_PERMISSION_REQ_CODE
					if (permission.equals("android.permission.SYSTEM_OVERLAY_WINDOW") ){
						if(Settings.canDrawOverlays(mContext)){
							mContext.startActivity(mIntent);
						}else {
							Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
							startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
						}
					}else{
						Toast.makeText(mContext, "Permissions ERROR!", Toast.LENGTH_LONG).show();
					}
				}
			}else {
				mContext.startActivity(mIntent);
			}
		}

	}

}
