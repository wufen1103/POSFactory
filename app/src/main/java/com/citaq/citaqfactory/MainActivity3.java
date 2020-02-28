package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
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
import com.citaq.view.ImageAdapter;
import com.citaq.view.MetroItemAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class MainActivity3 extends Activity {
	protected static final String Tag = "MainActivity2";
	MetroItemAdapter adapter;
	Context mContext;
	Intent mIntent;
	String permission;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_main_gride);  

		GridView gridView = (GridView) findViewById(R.id.gridview);

		InputStream inputStream = getResources().openRawResource(R.raw.default_workspace);
		List<MetroItem> metroItemList = AnalyzeSAX.readXML(inputStream);

		//针对不同的主板设置那些MetroItem不显示

		Iterator<MetroItem> iterator = metroItemList.iterator();
		while (iterator.hasNext()) {
			MetroItem mMetroItem = iterator.next();
			if (!mMetroItem.isShow()) {
				iterator.remove();
			}
			//3288 不显示led测试 FSK来电显示
			if(MainBoardUtil.isRK3288() && (mMetroItem.getNameEN().contains("LED") || mMetroItem.getNameEN().contains("FSK"))) {
				iterator.remove();
			}
		}


		adapter = new MetroItemAdapter(this, metroItemList);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mIntent = ((ViewHolderMetro)view.getTag()).intent;

				permission = ((ViewHolderMetro)view.getTag()).permission;

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permission != null && !"".equals(permission) ){
					String[] strarr = permission.split("\\|");
					if (strarr.length>0){
						PermissionUtil.checkAndRequestPermissions(MainActivity3.this, strarr);
						return;
					}
				}
				mContext.startActivity(mIntent);
			}

		});


//		getMacDefault(this);
//		getWifiMacAddress();
	    
	}
	WifiManager WifiManager;
	private static String getMacDefault(Context context) {
		String mac = "02:00:00:00:00:00";
		if (context == null) {
			return mac;
		}

		WifiManager wifi = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi == null) {
			return mac;
		}
		WifiInfo info = null;
		try {
			info = wifi.getConnectionInfo();
		} catch (Exception e) {
		}
		if (info == null) {
			return null;
		}
		mac = info.getMacAddress();
		if (!TextUtils.isEmpty(mac)) {
			mac = mac.toUpperCase(Locale.ENGLISH);
		}
		return mac;
	}

	private String getWifiMacAddress() {
		String str = "";
		String mac = "";
		try {
			Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (; null != str; ) {
				str = input.readLine();
				if (str != null) {
					mac = str.trim();
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return mac;
	}

	 
	 @Override
	protected void onDestroy() {
		if(BuildConfig.DEBUG) Log.i(Tag, "onDestroy----");
		
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
