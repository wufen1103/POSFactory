package com.citaq.citaqfactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.citaq.util.Command;
import com.citaq.util.MainBoardUtil;
import com.citaq.util.NetworkUtil;
import com.citaq.util.WifiAdmin;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebViewActivity extends Activity {
	protected static final String TAG = "MSRActivity";
	protected static final String url = "https://www.baidu.com";
	Context mContext;
	WebView mWebView;
	Button bt_refresh;

	NetworkChangeReceiver mNetworkChangeReceiver;
	FileOutputStream mFileOutputStream = null;


	WifiAdmin wifiAdmin;
//	String[] defaultWifiInfo ={ "Xiaomi_JS", "88990420", "3"};
	String[] defaultWifiInfo ={ "wifitest", "CitaqServerAp", "3"};
//	String[] defaultWifiInfo ={ "JustEat_Guest", "xa4aqusW", "3"};
	boolean isWifiToOn = false;
	boolean allowToOnWiFi = false;
	boolean isJE = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		setContentView(R.layout.webview_layout);
		mContext = this;
		mWebView = findViewById(R.id.webView);
		bt_refresh = findViewById(R.id.bt_refresh);

		Intent intent = getIntent();
		allowToOnWiFi = intent.getBooleanExtra("WIFI",false);

		mNetworkChangeReceiver = new NetworkChangeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mNetworkChangeReceiver,filter);

		if(allowToOnWiFi) {
			initWiFi();
			initTest();
		}

//		远程url是https协议，图片资源是http协议时。在Android 5.0之后，WebView默认不允许Https+Http的混合使用方式，所以当Url是Https的，图片资源是Http时，导致页面加载失败。需设置 MixedContentMode属性。
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
//		DOM Storage API导致的问题，android默认不开启DOM Storage
		mWebView.getSettings().setDomStorageEnabled(true);

//		 远程url是https协议， 证书问题，需重写onReceivedSslError方法
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				handler.proceed();
				super.onReceivedSslError(view, handler, error);
				Log.d(TAG, "onReceivedSslError: "); //如果是证书问题，会打印出此条log到console
			}
		});

		mWebView.getSettings().setJavaScriptEnabled(true);//启用js
		mWebView.getSettings().setBlockNetworkImage(false);//解决图片不显示


		mWebView.loadUrl(url);

		bt_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.reload();
			}
		});
	}

	private void initWiFi(){
		if(MainBoardUtil.getBuildDisplayID().contains("JE.H10")) {
			isJE = true;
		}
		wifiAdmin = new WifiAdmin(mContext);
		if(NetworkUtil.isNetworkAvailable(mContext)){

		}else{
			isWifiToOn = true;
			//打开wifi
//					WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//					wifi.setWifiEnabled(true);
			new Thread(){
				public void run() {
					wifiAdmin.openWifi();
					if (!isJE) {
						wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(defaultWifiInfo[0], defaultWifiInfo[1], Integer.valueOf(defaultWifiInfo[2])));
					}
				}

			}.start();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(TAG, "onDestroy");

//		if(isWifiToOn){
//			wifiAdmin.closeWifi();
//		}
		unregisterReceiver(mNetworkChangeReceiver);
	}


	private void initTest(){
		try {
			mFileOutputStream = new FileOutputStream("/dev/ttyS1");
			mFileOutputStream.write(Command.getChineseMode(0));

			handler.sendEmptyMessageDelayed(10008,1000);

			handler.sendEmptyMessageDelayed(Command.RED,2000);
			handler.sendEmptyMessageDelayed(Command.BLUE,3000);
			handler.sendEmptyMessageDelayed(Command.GREEN,4000);
			handler.sendEmptyMessageDelayed(Command.BLACK,5000);
			handler.sendEmptyMessageDelayed(Command.WHITE,6000);
			handler.sendEmptyMessageDelayed(10009,7000);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	//定义一个判断网络的广播  在网络连接或断开时，都会执行 onReceive 方法一次
	class NetworkChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if(networkInfo != null) {
				if (networkInfo.isConnected())
				{
					handler.sendEmptyMessageDelayed(2000, 1000);
//					mWebView.reload();
//					Toast.makeText(mContext, "loadUrl", Toast.LENGTH_SHORT).show();
//					mWebView.loadUrl(url);
				}
				if (networkInfo.isAvailable()) {
					//Toast.makeText(mContext, "网络打开", Toast.LENGTH_SHORT).show();
//					handler.sendEmptyMessageDelayed(2000, 2000);
//					mWebView.reload();

//					mWebView.loadUrl(url);
				} else {
//					Toast.makeText(mContext, "网络关闭", Toast.LENGTH_SHORT).show();
				}

			}
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Command.RED:
					try {
						mFileOutputStream = new FileOutputStream("/dev/ttyS3");
						mFileOutputStream.write(Command.getColorCmd(Command.RED));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case Command.BLUE:
					try {
						mFileOutputStream.write(Command.getColorCmd(Command.BLUE));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case Command.GREEN:
					try {
						mFileOutputStream.write(Command.getColorCmd(Command.GREEN));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case Command.BLACK:
					try {
						mFileOutputStream.write(Command.getColorCmd(Command.BLACK));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case Command.WHITE:
					try {
						mFileOutputStream.write(Command.getColorCmd(Command.WHITE));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 10008:
					try {
						mFileOutputStream.write(Command.printTest);
						mFileOutputStream.write(Command.getPrintDemo());
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 10009:
					try {
						mFileOutputStream.write(Command.transToPrintText("ESC @"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 2000:
					Log.v(TAG, "handler reload");
//					mWebView.reload();
					mWebView.loadUrl(url);
					break;

				default:
					break;
			}
		}
	};

}
