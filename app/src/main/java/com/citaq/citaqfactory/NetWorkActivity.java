package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.citaq.util.MainBoardUtil;
import com.citaq.util.NetworkUtil;
import com.citaq.util.PingLooperThread;
import com.citaq.util.PingLooperThread.Callbak;
import com.citaq.util.SDcardUtil;
import com.citaq.util.SmbUtil;
import com.citaq.util.WifiAdmin;
import com.citaq.view.ProgressListFileDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*源代码路径/media/resource/android/sourcecode/jellybean/frameworks/base/wifi/java/android/net/wifi/wifimanager.java
		源码中这样描述的*/
/** Anything worse than or equal to this will show 0 bars.
private static final int MIN_RSSI = -100;

*//** Anything better than or equal to this will show the max bars. *//*
private static final int MAX_RSSI = -55;

*//**
 * Number of RSSI levels used in the framework to initiate
 * {@link #RSSI_CHANGED_ACTION} broadcast
 * @hide
 *//*
public static final int RSSI_LEVELS = 5;*/
/**
 * Calculates the level of the signal. This should be used any time a signal
 * is being shown.
 *
 * rssi The power of the signal measured in RSSI.
 * numLevels The number of levels to consider in the calculated
 *            level.
 * @return A level of the signal, given in the range of 0 to numLevels-1
 *         (both inclusive).
 */

/*public static int calculateSignalLevel(int rssi, int numLevels) {
		if (rssi <= MIN_RSSI) {
		return 0;
		} else if (rssi >= MAX_RSSI) {
		return numLevels - 1;
		} else {
		float inputRange = (MAX_RSSI - MIN_RSSI);
		float outputRange = (numLevels - 1);
		return (int)((float)(rssi - MIN_RSSI) * outputRange / inputRange);
		}
		}*/

/**wifi
 * 一般信号强度在-30~-120之间。
 * 正常信号强度应该是-40 dbm ~ -85 dbm之间。
 * 小于 -90 dbm 就很差了，几乎没法连接。
 * android中wifi分为5个等级，对应的图标是0格，1格，2格，3格，4格.
 * 那么对应的信号强度是多少呢？
 * 根据wifimanager中的算法calculateSignalLevel可以算得：
 * 0 rssi<=-100
 * 1 (-100, -88]
 * 2 (-88, -77]
 * 3 (-66, -55]
 * 4 rssi>=-55
 */

/**
 * 中国移动的规范规定,手机接收电平>=(城市取-90dBm;乡村取-94dBm)时,则满足覆盖要求,
 * 也就是说此处无线信号强度满足覆盖要求，即接收电平>=-90dBm，就可以满足覆盖要求
 *
 * 电信
 * 2G CDMA
 * 3G CDMA2000
 * 4G TD-LTE，FDD-LTE
 *
 * 移动
 * 2G GSM
 * 3G TD-SCDMA
 * 4G TD-LTE，FDD-LTE
 *
 * 联通
 * 2G GSM
 * 3G WCDMA
 * 4G TD-LTE，FDD-LTE
 *
 * <-100
 *(-100,-85])dbm 1格信号
 * (-85,-70]) dbm　2格信号
 * (-70,-55] dbm 3格信号
 * [-55,0] dbm 满格(4格)信号
 *
 *
 *
 */


public class NetWorkActivity extends Activity {
	protected static final String TAG = "NetWorkActivity";

	protected static final String REMOTEURL_SMB = "smb://JE:abcd123@192.168.123.1/TEST/JE/"; //"smb://wufen:citaq123@192.168.123.1/Software/JE/"; //
	protected static final String LOCALDIR_SDCARD = "/mnt/external_sd/";

	protected static final int wifi_state = 1001;
	protected static final int ping_state = 1002;

	private int networkType = 0; // NETWORK_TYPE_UNKNOWN
	

	ConnectivityManager connectivityManager;
	private WifiInfo wifiInfo = null; // 获得的Wifi信息
	private WifiManager wifiManager = null; // Wifi管理器

	TelephonyManager telephoneManager;
	int mNetworkType;

	private int wifi_level;

	TextView tv_info;
	TextView tv_signal_strength;
	TextView tv_signal_strength_3G;
	TextView tv_ping_result;
	TextView tv_success_percentage;
	TextView tv_network_mac;
	Button bt_start;
	Button bt_baidu, bt_wifi, bt_server;
	EditText et_network_addr;
	
	Spinner spinner_time_count;
	private ArrayAdapter<?> adapter_time;
	int pingTime =10;

	Resources mResources;

	boolean isRun = true;
//	int timeOut = 10 * 1000;

	Handler uiHandler;

//	Runnable runnable;

	Timer timer;

	Context mContext;
	
	private ToggleButton tb_wifi;
	
	WifiAdmin wifiAdmin;
	String[] defaultWifiInfo ={ "wifitest", "CitaqServerAp", "3"};
	
	ProgressBar mProgressBar;
	
	PingLooperThread mPingLooperThread;
	
	boolean istesting =false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_network);

		wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
		telephoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		wifiAdmin = new WifiAdmin(mContext);

		initView();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(myNetReceiver, mFilter);

		telephoneManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		mNetworkType = telephoneManager.getNetworkType();
		Log.i(TAG, "mNetworkType = " +mNetworkType+";NetworkOperatorName = " + telephoneManager.getNetworkOperatorName());
		//Toast.makeText(mContext,"mNetworkType = " +mNetworkType +"  "+telephoneManager.getNetworkOperatorName(), Toast.LENGTH_SHORT).show();
	}

	private void initView() {
		tv_ping_result = (TextView) findViewById(R.id.result);
		tv_success_percentage = (TextView) findViewById(R.id.success_percentage);
		bt_start = (Button) findViewById(R.id.begin);
		bt_baidu = (Button) findViewById(R.id.baidu);
		bt_wifi = (Button) findViewById(R.id.wifi);
		bt_server= (Button) findViewById(R.id.server);
		if(!MainBoardUtil.getBuildDisplayID().contains("JE.H10")) {
			bt_server.setVisibility(View.GONE);
		}

		et_network_addr = (EditText) findViewById(R.id.web);
		et_network_addr.setSelection(et_network_addr.getText().length());
		et_network_addr.clearFocus();
		
		tb_wifi = (ToggleButton) findViewById(R.id.tb_wifi);
		mProgressBar = (ProgressBar) findViewById(R.id.myprobar);
		
		
		tv_info = (TextView) findViewById(R.id.tv_show_status);
		tv_signal_strength = (TextView) findViewById(R.id.tv_signal_strength);
		tv_signal_strength_3G = (TextView) findViewById(R.id.tv_signal_strength_3G);
		spinner_time_count = (Spinner) findViewById(R.id.spinner_time_count);
		tv_network_mac = (TextView) findViewById(R.id.tv_network_mac);

		tv_network_mac.setText("eth0 mac: " + NetworkUtil.getEthMacAddress() + "\nwlan0 mac: " + NetworkUtil.getWifiMacAddress());
		//for test
		/*try {
			NetworkUtil.macAddress() ;
		} catch (SocketException e) {
			e.printStackTrace();
		}*/

		adapter_time= ArrayAdapter.createFromResource(this, R.array.ping_time, android.R.layout.simple_spinner_item);
		adapter_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_time_count.setAdapter(adapter_time);
		spinner_time_count.setSelection(0);
		spinner_time_count.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switch (arg2) {
				case 0:
					pingTime = 10;
					break;
				case 1:
					pingTime = 12;
					break;
				case 2:
					pingTime = 14;
					break;
				case 3:
					pingTime = 16;
					break;
				}
				
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mResources = mContext.getResources();

		bt_start.setEnabled(false);
		et_network_addr.setEnabled(false);
		tv_signal_strength.setText("");

		uiHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				
				switch (msg.what) {
				case wifi_state:
					if (networkType == ConnectivityManager.TYPE_WIFI) {
						int level = WifiManager.calculateSignalLevel(msg.arg1,5);
						tv_signal_strength.setText(
								          mResources.getString(R.string.network_wifi)+", "
										+ mResources.getString(R.string.network_signal_strength)
										+ ":"
										+ msg.arg1
										+ "dBm"
										+ ", "
										+mResources.getString(R.string.network_signal_strength)
										+ ":"
										+ level
								);
//						if(Integer.valueOf(msg.what)!= -200)
						{
							tb_wifi.setEnabled(true);
				    		mProgressBar.setVisibility(View.INVISIBLE);
//				    		 System.out.println("1111111111111111111111");
						}
					}
					break;
				case ping_state:
					tv_ping_result.setText(mResources
					.getString(R.string.total_times)
					+ ":"
					+ (msg.arg1+msg.arg2)
					+ ";"
					+ mResources.getString(R.string.success_times)
					+ ":"
					+ msg.arg1
					+ ";"
					+ mResources.getString(R.string.fail_times)
					+ ":"
					+ msg.arg2);
					
					if(msg.arg1+msg.arg2 >0){
						tv_success_percentage.setText(mResources
								.getString(R.string.success_rate)
								+ ":"
								+ String.format("%.2f", (double)msg.arg1/(double)( msg.arg1+msg.arg2) * 100) +"%"
								);
					}
					break;

				default:
					break;
				}
				
				
				super.handleMessage(msg);
			}

		};

		bt_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (bt_start.getTag().toString().equals(
						mResources.getString(R.string.start))) {

//					bt_start.setText(mResources.getString(R.string.stop));
					bt_start.setTag(mResources.getString(R.string.stop));
					bt_start.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_pingstop_selector));
					et_network_addr.setEnabled(false);
					istesting =true;
					
					//ping
					mPingLooperThread.start();
					
					uiHandler.postDelayed(new Runnable() {  
					    @Override  
					    public void run() {  
					    	Message msg= new Message();
							mPingLooperThread.sendMessage(msg);
					        //要做的事情  
					        uiHandler.postDelayed(this, pingTime * 1000);  
					    }  
					}, 1000);
					//
					

				} else {

					// bt_start.setText(mResources.getString(R.string.start));
					bt_start.setTag(mResources.getString(R.string.start));
					bt_start.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.btn_ping_selector));
					et_network_addr.setEnabled(true);

					// stop ping
					mPingLooperThread.stop();
					istesting =false;
				}

			}
		});
		
		if(wifiManager.isWifiEnabled()){
			
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if(wifiInfo.getSSID().equals("\""+defaultWifiInfo[0]+"\"")){
				tb_wifi.setChecked(true);
			}
		}
		
		
		tb_wifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					
					bt_start.setTag(mResources.getString(R.string.start));
					bt_start.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.btn_ping_selector));
					et_network_addr.setEnabled(true);
					mPingLooperThread.stop();
					new Thread(){
						public void run(){


							wifiAdmin.openWifi();
							wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(defaultWifiInfo[0], defaultWifiInfo[1], Integer.valueOf(defaultWifiInfo[2])));

						}

					}.start();

					  tb_wifi.setEnabled(false);
					  mProgressBar.setVisibility(View.VISIBLE);
					  
					  /*new Handler().postDelayed(new Runnable(){   

						    public void run() {   

						    	if(wifiManager.isWifiEnabled() ){
						    		tb_wifi.setEnabled(true);
						    		mProgressBar.setVisibility(View.INVISIBLE);
						    	}else{
						    		mProgressBar.setVisibility(View.INVISIBLE);
						    		tb_wifi.setChecked(false);
;						    		tb_wifi.setEnabled(true);
						    	}

						    }   

						 }, 20000);*/
					  
				    
				}else{
					tb_wifi.setEnabled(false);
					mProgressBar.setVisibility(View.VISIBLE);
					wifiAdmin.closeWifi();
				}

			}
		});
		
		bt_baidu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openBrowser2();
			}
		});

		bt_server.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				jcifs.Config.setProperty("jcifs.smb.client.responseTimeout", "1200000");
				System.setProperty("jcifs.smb.client.dfs.disabled", "true"); ///禁用dfs,提高读取速度
				System.setProperty("jcifs.smb.client.soTimeout", "500");//100 //default: 35000 //jcifs.smb.client.soTimeout 不能太大了，否则切换不了用户，太小了，又登不进去。这个配置是关键
//				System.setProperty("jcifs.smb.client.soTimeout", "1000000");
//				System.setProperty("jcifs.smb.client.responseTimeout", "15000")
				System.setProperty("jcifs.smb.client.responseTimeout", "5000");// default: 30000//System.setProperty("jcifs.smb.client.responseTimeout", "30000");

				if(new SDcardUtil().isHasSD()){
					showListDialog();  //初始化mProgressListFileDialog
					downloadFileSmb();
				}else{
					if(mProgressListFileDialog != null) {
						mProgressListFileDialog.notifyDataChanged(LOCALDIR_SDCARD, null);
					}
					Toast.makeText(mContext, "No MicroSD.",Toast.LENGTH_SHORT).show();
				}

			}
		});
		
		bt_wifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				startActivity(intent);

				//for test
				/*Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings");
				intent.setComponent(cn);
				startActivity(intent);*/
			}
		});
		
		mPingLooperThread = new PingLooperThread(new Callbak() {

			@Override
			public boolean handleMessage(int allcount, int success, int faild, boolean lastResult) {

				Log.i(TAG, allcount + "   "+ success + "         "+faild);

				Message msg = new Message();
				msg.what = ping_state;
				msg.arg1 = success;
				msg.arg2 = faild;
				uiHandler.sendMessage(msg);
				
				return false;
			}
		}, pingTime,et_network_addr.getText().toString());
		
	}



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		handler.removeCallbacks(runnable);
		if (myNetReceiver != null) {
			unregisterReceiver(myNetReceiver);
		}
	}



	private void networkRssi(int which) {
		bt_start.setEnabled(true);
		switch (which) {
		case ConnectivityManager.TYPE_WIFI:
			// 使用定时器,每隔5秒获得一次信号强度值
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					wifiInfo = wifiManager.getConnectionInfo();
					// 获得信号强度值
					wifi_level = wifiInfo.getRssi();
					// 根据获得的信号强度发送信息

					Message msg = new Message();
					msg.what = wifi_state;
					msg.arg1 = wifi_level;
					uiHandler.sendMessage(msg);

				}

			}, 1000, 5000);

			break;
		case ConnectivityManager.TYPE_MOBILE:
			tv_signal_strength.setText("");
			break;
		case ConnectivityManager.TYPE_ETHERNET:
			tv_signal_strength.setText("");
			break;

		default:
			break;
		}
	}

	private BroadcastReceiver myNetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

				connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
				if (netInfo != null && netInfo.isAvailable()) {

					// ///////////网络连接
					// String name = netInfo.getTypeName();
					networkType = netInfo.getType();

					if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						// ///WiFi网络
						networkRssi(ConnectivityManager.TYPE_WIFI);
//						tv_info.setText(netInfo.toString());
						tv_info.setText(mResources.getString(R.string.network_type)+":"+mResources.getString(R.string.network_wifi));
							
						
						/*et_network_addr.setEnabled(true);
						bt_start.setEnabled(true);
						
						
						String mac = "";
						if (Build.VERSION.SDK_INT < 23) {
							WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
						    if (wifi == null) {
						        return ;
						    }
						    WifiInfo info = null;
						    try {
						        info = wifi.getConnectionInfo();
						    } catch (Exception e) {
						    }
						    if (info == null) {
						        return ;
						    }
						    mac = info.getMacAddress();
						    if (!TextUtils.isEmpty(mac)) {
						        mac = mac.toUpperCase(Locale.ENGLISH);
						    }
						    
					    } else if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 24) {
					    	
					        try {
					            mac = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
					        } catch (IOException e) {
					            e.printStackTrace();
					        }

					    } else if (Build.VERSION.SDK_INT > 24) {
					        mac = getMacFromHardware();
					    }
						
						
						
						tv_info.setText(mResources.getString(R.string.network_type)+":"+mResources.getString(R.string.network_wifi)
								+"   MAC:"+ mac
								);*/
						
					} else if (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
						// ///有线网络
						networkRssi(ConnectivityManager.TYPE_ETHERNET);
//						tv_info.setText(netInfo.toString());
						tv_info.setText(mResources.getString(R.string.network_type)+":"+mResources.getString(R.string.network_ethernet));
						/*tv_info.setText(mResources.getString(R.string.network_type)+":"+mResources.getString(R.string.network_ethernet)
								+"   MAC:"+netInfo.getExtraInfo()	
								);*/
						et_network_addr.setEnabled(true);
						bt_start.setEnabled(true);
						
					} else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
						// ///////3g网络
						networkRssi(ConnectivityManager.TYPE_MOBILE);
//						tv_info.setText(netInfo.toString());
						tv_info.setText(mResources.getString(R.string.network_type)+":"+mResources.getString(R.string.network_mobile));
						et_network_addr.setEnabled(true);
						bt_start.setEnabled(true);
					}
				} else {
					if(istesting) return;
					// //////网络断开
					tv_info.setText(mResources
							.getString(R.string.network_not_connect));
//					bt_start.setText(mResources.getString(R.string.start));
					bt_start.setTag(mResources.getString(R.string.start));
					bt_start.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_ping_selector));
					bt_start.setEnabled(false);
					et_network_addr.setEnabled(false);
					tv_signal_strength.setText("");
					
					
					System.out.println("2222222222222222222222");
					tb_wifi.setChecked(false);
					tb_wifi.setEnabled(true);
					mProgressBar.setVisibility(View.INVISIBLE);
					
				}
			}

		}
	};
	
	@SuppressLint("NewApi")
	private static String getMacFromHardware() {
	    try {
	        List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
	        for (NetworkInterface nif : all) {
	            if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

	            byte[] macBytes = nif.getHardwareAddress();
	            if (macBytes == null) {
	                return "";
	            }

	            StringBuilder res1 = new StringBuilder();
	            for (byte b : macBytes) {
	                res1.append(String.format("%02X:", b));
	            }

	            if (res1.length() > 0) {
	                res1.deleteCharAt(res1.length() - 1);
	            }
	            return res1.toString();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "02:00:00:00:00:00";
	}

	/*
	　　signalStrength.isGsm() 是否GSM信号 2G or 3G
	　　signalStrength.getCdmaDbm(); 联通3G 信号强度
	　　signalStrength.getCdmaEcio(); 联通3G 载干比
	　　signalStrength.getEvdoDbm(); 电信3G 信号强度
	　　signalStrength.getEvdoEcio(); 电信3G 载干比
	　　signalStrength.getEvdoSnr(); 电信3G 信噪比
	　　signalStrength.getGsmSignalStrength(); 2G 信号强度
	　　signalStrength.getGsmBitErrorRate(); 2G 误码率
	　　载干比 ，它是指空中模拟电波中的信号与噪声的比值
	　　*/
	// 3G
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			super.onSignalStrengthsChanged(signalStrength);
			StringBuffer sb = new StringBuffer();
			int asu = signalStrength
					.getGsmSignalStrength();

			String strength = String.valueOf(asu);
			String strengthdmb = String.valueOf(signalStrength.getCdmaDbm());
			//3368 909S-120 4G:type =17 3G=3
			//3288 909S-120 4G:type =13 3G=15
			if (mNetworkType == TelephonyManager.NETWORK_TYPE_UMTS
					|| mNetworkType == TelephonyManager.NETWORK_TYPE_HSDPA
					|| mNetworkType == TelephonyManager.NETWORK_TYPE_HSPAP)   //3288 TD-SCDMA ？？
					//|| mNetworkType == TelephonyManager.NETWORK_TYPE_UNKNOWN)
			{
				strengthdmb =String.valueOf(-113+(2*asu));
				// 联通3G

				sb.append(mResources.getString(R.string.network_Unicom_3G))
						.append(",").append(mResources.getString(R.string.network_signal_strength))
						.append(":").append(strengthdmb).append("dBm")
						.append("  ").append(strength).append("asu");

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					int levle = signalStrength.getLevel();
					sb.append(" ,").append(mResources.getString(R.string.network_signal_level)).append(":")
					.append(levle);
				}

				tv_signal_strength_3G.setText(sb);

			} else if (mNetworkType == TelephonyManager.NETWORK_TYPE_GPRS
					|| mNetworkType == TelephonyManager.NETWORK_TYPE_EDGE) {
				// 移动或者联通2g
				sb.append(
						mResources
								.getString(R.string.network_ChinaMobile_Unicom_2G))
						.append(",").append(mResources.getString(R.string.network_signal_strength))
						.append(":").append(strengthdmb).append("dBm")
						.append("  ").append(strength).append("asu");
				tv_signal_strength_3G.setText(sb);
			} else if (mNetworkType == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| mNetworkType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
				// 电信3g
				strengthdmb = String.valueOf(signalStrength.getEvdoDbm());
				strengthdmb =String.valueOf(-113+(2*asu));
				sb.append(mResources.getString(R.string.network_CMCC_3G))
						.append(",").append(mResources.getString(R.string.network_signal_strength))
						.append(":").append(strengthdmb).append("dBm")
						.append("  ").append(strength).append("asu");
				tv_signal_strength_3G.setText(sb);
			} else if (mNetworkType == TelephonyManager.NETWORK_TYPE_LTE) {
				// Reflection code starts from here
				try {
					Method[] methods = android.telephony.SignalStrength.class
							.getMethods();
					for (Method mthd : methods) {
						if (mthd.getName().equals("getLteSignalStrength")
								|| mthd.getName().equals("getLteRsrp")
								|| mthd.getName().equals("getLteRsrq")
								|| mthd.getName().equals("getLteRssnr")
								|| mthd.getName().equals("getLteCqi")) {
							Log.i(TAG,"onSignalStrengthsChanged: "
											+ mthd.getName() + " "
											+ mthd.invoke(signalStrength));
							
							
							if(mthd.getName().equals("getLteRsrp")){
								int strengthdmb_int = (Integer) mthd.invoke(signalStrength);
								strengthdmb = String.valueOf(mthd.invoke(signalStrength));
								sb.append(mResources.getString(R.string.network_lte))
								.append(",")
								.append(mResources
										.getString(R.string.network_signal_strength))
								.append(":").append(strengthdmb).append("dBm")
								.append("  ").append(strengthdmb_int+140).append("asu");

								if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
									int levle = signalStrength.getLevel();
									sb.append(" ,").append(mResources.getString(R.string.network_signal_level)).append(":")
											.append(levle);
								}
								tv_signal_strength_3G.setText(sb);
								
							}
							//LTE和2G算法不一样，LTE算法是dbm-aus=-140
						}
					}
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				// Reflection code ends here


			} else {
				sb.append(mResources.getString(R.string.network_Other))
						.append(",").append(mResources.getString(R.string.network_signal_strength))
						.append(":").append(strengthdmb).append("dBm")
						.append("  ").append(strength).append("asu");
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					int levle = signalStrength.getLevel();
					sb.append(" ,").append(mResources.getString(R.string.network_signal_level)).append(":")
							.append(levle);
				}
				tv_signal_strength_3G.setText(sb);
			}

		}

	};
	
	private void openBrowser(){
		Intent intent = new Intent();        
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse("http://www.baidu.com");
//		Uri content_url = Uri.parse("http://whatismyscreenresolution.net/");
		intent.setData(content_url);           
		intent.setClassName("com.android.browser","com.android.browser.BrowserActivity"); 
		if(getPackageManager().resolveActivity(intent, 0) == null) {
			//com.android.chrome/com.google.android.apps.chrome.Main
			intent.setClassName("com.android.chrome","com.google.android.apps.chrome.Main"); 
			if(getPackageManager().resolveActivity(intent, 0) == null) {
				Toast.makeText(this, "browser is not exit!!!", Toast.LENGTH_SHORT).show();
			}else{
				startActivity(intent);
			}
		}else{
			startActivity(intent);
		}
	}
	private void openBrowser2() {
		Intent webviewIntent = new Intent(mContext, WebViewActivity.class);
		startActivity(webviewIntent);
	}

	private void downloadFileSmb(){
		SmbUtil mSmbUtil = new SmbUtil(new SmbUtil.CallbackSMB() {
			@Override
			public void onDownLoadResult(List<String> download_remoteFileName) {
				if(download_remoteFileName.size() != 0){
					Toast.makeText(mContext, "Download Success.",Toast.LENGTH_SHORT).show();

					if(mProgressListFileDialog != null) {
						mProgressListFileDialog.notifyDataChanged(LOCALDIR_SDCARD, download_remoteFileName);
					}
					for(int i = 0; i<download_remoteFileName.size(); i++){
						System.out.println(TAG + " SMB:" + "download_remoteFileName = " + download_remoteFileName.get(i));
					}
					System.out.println(TAG + "SMB:Download Success.");
				}else{
//					Toast.makeText(mContext, "Download NULL.",Toast.LENGTH_SHORT).show();
					System.out.println(TAG + "SMB:Download NULL.");
				}
			}

			@Override
			public void onShowProgress(boolean isshow) {
				if(mProgressListFileDialog != null) {
					mProgressListFileDialog.showProgress(isshow);
				}
			}
		});

		mSmbUtil.smbDownload(REMOTEURL_SMB,LOCALDIR_SDCARD);
	}



	ProgressListFileDialog mProgressListFileDialog;
	private void showListDialog() {
		mProgressListFileDialog = new ProgressListFileDialog(mContext, LOCALDIR_SDCARD,  new ProgressListFileDialog.ProgressListDialogListener() {
			@Override
			public void onListItemLongClick(int position, String path) {
				Toast.makeText(mContext, path + "  " + position,Toast.LENGTH_SHORT).show();

			}
		});
		mProgressListFileDialog.setTitle(R.string.str_open_File);
		mProgressListFileDialog.show();
	}
}
