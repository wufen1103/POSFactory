package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.citaq.util.MainBoardUtil;
import com.citaq.util.NetworkUtil;
import com.citaq.util.RAMandROMInfo;
import com.citaq.util.SDcardUtil;
import com.citaq.util.ZXingUtil;
import com.citaq.view.ProgressListFileDialog;
import java.lang.reflect.Method;

public class SysInfoActivity extends Activity {
	Context ctx;
	protected static final int MSG_REFRESH_UI = 1000;
	TextView tv_serialNo, tv_ICCID, tv_buildNum, tv_IMEI, tv_productName, tv_platform, tv_android_ver, tv_sd,
		tv_battery_info_voltage, tv_battery_info_status, tv_sys_net_meg, tv_sys_uptime, tv_display, tv_version_name;
	String info="";
	ImageView iv_mac;
	Button sys_sd_content, sys_settings_wifi, sys_settings_app, sys_settings_datetime,
			sys_settings_reset, sys_settings, bt_stress;
	
	String sdcard ="/mnt/external_sd";  // sdcard ="/storage/23E5-4C27";  //8.1.0 /mnt/media_rw/0403-0201
	
	private Handler mHandler;

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sys_info);
		ctx = this;
		
		sys_sd_content = (Button) findViewById(R.id.sys_sd_content);
//		sys_settings_wifi = (Button) findViewById(R.id.sys_settings_wifi);
		sys_settings_app = (Button) findViewById(R.id.sys_settings_app);
		sys_settings_datetime = (Button) findViewById(R.id.sys_settings_datetime);
		sys_settings_reset = (Button) findViewById(R.id.sys_settings_reset);
		sys_settings = (Button) findViewById(R.id.sys_settings);
		bt_stress = (Button) findViewById(R.id.bt_stress);
		
		sys_sd_content.setOnClickListener(clickListener);
//		sys_settings_wifi.setOnClickListener(clickListener);
		sys_settings_app.setOnClickListener(clickListener);
		sys_settings_datetime.setOnClickListener(clickListener);
		sys_settings_reset.setOnClickListener(clickListener);
		sys_settings.setOnClickListener(clickListener);
		bt_stress.setOnClickListener(clickListener);
		
		tv_serialNo = (TextView) findViewById(R.id.tv_serialNo);
		tv_ICCID = (TextView) findViewById(R.id.tv_ICCID);
		tv_buildNum = (TextView) findViewById(R.id.tv_buildID);
		tv_IMEI = (TextView) findViewById(R.id.tv_IMEI);
		tv_productName = (TextView) findViewById(R.id.tv_productName);
		tv_platform = (TextView) findViewById(R.id.tv_platform);
		tv_android_ver = (TextView) findViewById(R.id.tv_android_ver);
		tv_sd = (TextView) findViewById(R.id.tv_sd);
		
		tv_battery_info_voltage = (TextView) findViewById(R.id.battery_info_voltage);
		tv_battery_info_status = (TextView) findViewById(R.id.battery_info_status);
		tv_sys_net_meg = (TextView) findViewById(R.id.net_meg);
		tv_sys_uptime = (TextView) findViewById(R.id.uptime);
		tv_display = (TextView) findViewById(R.id.display);
		tv_version_name = (TextView) findViewById(R.id.version_name);
		iv_mac = (ImageView) findViewById(R.id.iv_mac);
		
		String mac =  NetworkUtil.getEthMacAddress();
		
		if (mac !=null ){
			tv_sys_net_meg.setText(mac);
			Resources res = getResources();  
//			Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);  
//			Bitmap mBitmap = ZXingUtil.createQRImage2( mac, 250, 250, bmp);
			
			Bitmap mBitmap = ZXingUtil.createQRImage2( mac, 250, 250,null);
			iv_mac.setImageBitmap(mBitmap);
		}

	    String serial = MainBoardUtil.getSerial();
	    if(serial!=null && !"".equals(serial)){
	    	tv_serialNo.setText(serial);
	    }
	    
	    String iccid = getICCID();
	    if(iccid!=null){
	    	String newStr = iccid.substring(iccid.indexOf(":")+1,iccid.length());
	    	tv_ICCID.setText(newStr);
	    	
	    }
	    
	    String sys_buildID = MainBoardUtil.getBuildDisplayID();
	    if(sys_buildID!=null){
	    	tv_buildNum.setText(sys_buildID);
	    }
	    
	    String sys_IMEI = getIMEI();
	    if(sys_IMEI!=null){
	    	tv_IMEI.setText(sys_IMEI);
	    }
	    
	    String sys_productName = MainBoardUtil.getProductName();
	    if(sys_productName!=null){
	    	tv_productName.setText(sys_productName);
	    }
	    
	    String sys_platform = MainBoardUtil.getPlatform();
	    if(sys_platform!=null){
	    	RAMandROMInfo mRAMandROMInfo = new RAMandROMInfo(ctx);
	 	    String RAMInfo = mRAMandROMInfo.showRAMInfo2();
	 	    String ROMinfo =mRAMandROMInfo.showROMInfo2();
	 	    
	 	    tv_platform.setText(sys_platform+"(" + getCPUNumCores() +")  RAM+ROM("+RAMInfo+ "+" + ROMinfo + ")"      );
	    }
	    
	    String sys_android_ver = MainBoardUtil.getSystemVersion();
	    tv_android_ver.setText(sys_android_ver);
	    
	    
	   /* if(sys_android_ver.contains("6.0")){
	    	sdcard ="/storage/23E5-4C27";
	    }*/
	    
	    
	    String sys_sd = new SDcardUtil().getSDTotalSize(ctx);
	    tv_sd.setText(sys_sd); 
	    
	    String sys_version_name = getVersion();
	    if(sys_version_name!=null){
	    	tv_version_name.setText(sys_version_name);
	    }

	    tv_display.setText(getDisplayInformation());
		

	    mHandler = new Handler();
	    mHandler.post(new Runnable() {
	        @Override
	        public void run()
	        {
	            // TODO Auto-generated method stub
	        	tv_sys_uptime.setText(getUptime());  
	            mHandler.postDelayed(this, 1000);
	        }
	    });
	    
	    GridLayout mGridLayout = (GridLayout) findViewById(R.id.gridLayout);
	    int columnCount = mGridLayout.getColumnCount();
	    int screenWidth =  this.getWindowManager().getDefaultDisplay().getWidth()/2 -30;
        for (int i = 0; i < mGridLayout.getChildCount(); i++) {
            Button button = (Button) mGridLayout.getChildAt(i);
            button.setWidth(screenWidth / columnCount);
        }
	}
	
	
	private String getUptime(){
		//（1）统计系统从启动到现在的时间elapsedRealTime(); （2）统计系统从启动到当前处于非休眠的时间uptimeMillis() 以毫秒为单位
		long uptimeMillis = android.os.SystemClock.uptimeMillis();
		int uptime  = (int) (uptimeMillis/1000);
		 
    	int d = uptime/3600/24;
    	int h = uptime/3600;
 		int m = uptime%3600/60;
 		int s = uptime%3600%60;
 		
 		String time = "";
 		if(d != 0){
 			time = time + d + " d ";
 		}
 		if(h != 0){
 			time = time + String.format("%02d", h) + " : ";
 		}
 		if(m != 0){
 			time = time + String.format("%02d", m) + " : ";
 		}
 		time = time + String.format("%02d", s) + " ";
    	
		return time;
		    
	}

	@SuppressLint("MissingPermission")
	private String getICCID(){
		try {
			 TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			 String iccid =tm.getSimSerialNumber();  //取出ICCID
			 return iccid;
		} catch (Exception e) {

			e.printStackTrace();

		}
		return null;
	}

	@SuppressLint("MissingPermission")
	public String getIMEI() {
		try {
	    	return ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		} catch (Exception e) {

			e.printStackTrace();

		}
		return null;
	}
	
	public void getPrimaryStoragePath() {
		try {
			StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", new Class[ 0 ]);
			String[] paths = (String[]) getVolumePathsMethod.invoke(sm, new Object[]{});
			// first element in paths[] is primary storage path
			Log.d("TAG", "getPrimaryStoragePath: getStoragePath(this,true)==" + paths[0]);//内置sd路径
			Log.d("TAG", "getPrimaryStoragePath: getStoragePath(this,true)=="+paths[1]);//外置sd路径
		} catch (Exception e) {
			Log.e("TAG", "getPrimaryStoragePath() failed", e);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
	private  String getDisplayInformation() {
		StringBuilder displayInformation = new StringBuilder();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			DisplayManager mDisplayManager = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
			Display[] displays = mDisplayManager.getDisplays();
			int screenNum = displays.length; //屏幕数
			int i = 0;
			for (Display display : displays) {
				displayInformation.append("display").append('[').append(i).append(']').append(':')
						.append(display.getMode().getPhysicalWidth()).append('x').append(display.getMode().getPhysicalHeight()).append("\t\t");
				i++;
			}
		}else{
			Display display = getWindowManager().getDefaultDisplay();
//			int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//			int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

			Point point = new Point();
			display.getSize(point);
			int width = point.x;
			int height = point.y;
			displayInformation.append(width).append('x').append(height);

		}
		return displayInformation.toString();
	}
	
	private String getCPUNumCores(){
		int core = MainBoardUtil.getCPUNumCoresInt();
		String coreNum = "";
		  if(core == 8){
		    	coreNum="Octa-core[8]";
		    }else if(core == 4){
		    	coreNum="Quad-core[4]";
		    }else if(core == 2){
		    	coreNum="Dual-core[2]";
		    }else{
		    	coreNum = core+"-core";
		    }
		 return coreNum;
	}
	
	Handler mhHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REFRESH_UI:
//            	tv_battery_info_voltage.setText(msg.obj.toString());
//            	tv_battery_info_status.setText(msg.)
            	
            	Bundle bundle = msg.getData();
                String voltage = bundle.getString("voltage");
                String status = bundle.getString("status");
                String level = bundle.getString("level");
                
                tv_battery_info_voltage.setText(voltage+" mV");
//                tv_battery_info_status.setText(status);
                
                tv_battery_info_status.setText(status+"("+ level +"%)");
            }
        }
    };
	
    @Override  
    protected void onPause() {  
    super.onPause();  
    	unregisterReceiver(mBroadcastReceiver);
    } 
	
	 protected void onResume() {
	        super.onResume();
	        IntentFilter filter = new IntentFilter();
	        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
	        registerReceiver(mBroadcastReceiver, filter);
	    }
	 /*
	    “status”（int类型）…状态，定义值是BatteryManager.BATTERY_STATUS_XXX。
	    “health”（int类型）…健康，定义值是BatteryManager.BATTERY_HEALTH_XXX。
	    “present”（boolean类型）
	    “level”（int类型）…电池剩余容量
	    “scale”（int类型）…电池最大值。通常为100。
	    “icon-small”（int类型）…图标ID。
	    “plugged”（int类型）…连接的电源插座，定义值是BatteryManager.BATTERY_PLUGGED_XXX。
	    “voltage”（int类型）…mV。
	    “temperature”（int类型）…温度，0.1度单位。例如 表示197的时候，意思为19.7度。
	    “technology”（String类型）…电池类型，例如，Li-ion等等
	  */
	 
	 private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
	                int status = intent.getIntExtra("status", 0);
	                int health = intent.getIntExtra("health", 0);
	                boolean present = intent.getBooleanExtra("present", false);
	                int level = intent.getIntExtra("level", 0);
	                int scale = intent.getIntExtra("scale", 0);   // =100
	                int icon_small = intent.getIntExtra("icon-small", 0);
	                int plugged = intent.getIntExtra("plugged", 0);
	                int voltage = intent.getIntExtra("voltage", 0);
	                int temperature = intent.getIntExtra("temperature", 0);
	                String technology = intent.getStringExtra("technology");

	                String statusString = "";
	                switch (status) {
	                case BatteryManager.BATTERY_STATUS_UNKNOWN:
	                    statusString = "unknown";
	                    break;
	                case BatteryManager.BATTERY_STATUS_CHARGING:
	                    statusString = "charging";
	                    break;
	                case BatteryManager.BATTERY_STATUS_DISCHARGING:
	                    statusString = "discharging";
	                    break;
	                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
	                    statusString = "not charging";
	                    break;
	                case BatteryManager.BATTERY_STATUS_FULL:
	                    statusString = "full";
	                    break;
	                }

	                String healthString = "";
	                switch (health) {
	                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
	                    healthString = "unknown";
	                    break;
	                case BatteryManager.BATTERY_HEALTH_GOOD:
	                    healthString = "good";
	                    break;
	                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
	                    healthString = "overheat";
	                    break;
	                case BatteryManager.BATTERY_HEALTH_DEAD:
	                    healthString = "dead";
	                    break;
	                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
	                    healthString = "voltage";
	                    break;
	                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
	                    healthString = "unspecified failure";
	                    break;
	                }

	                String acString = "";

	                switch (plugged) {
	                case BatteryManager.BATTERY_PLUGGED_AC:
	                    acString = "plugged ac";
	                    break;
	                case BatteryManager.BATTERY_PLUGGED_USB:
	                    acString = "plugged usb";
	                    break;
	                }
	                String s="^Battery Info: \n";
	                s = s
//	                + "status:"+statusString+"\n"
//	                +"health:"+healthString+"\n"
//	                +"present:"+String.valueOf(present)+"\n"
//	                +"\t\t\t\t\tlevel:"+String.valueOf(level)+"%"+"\n"
//	                +"scale:"+String.valueOf(scale)+"\n"
//	                +"icon_small:"+ String.valueOf(icon_small)+"\n"
//	                +"plugged:"+acString+"\n"
	                +"\t\t\t\t\tvoltage:"+String.valueOf(voltage)+" mV"+"\n"
//	                +"temperature:"+String.valueOf(temperature)+"\n"
//	                +"technology:"+technology+"\n"
	                ;
	                Message msg = new Message();
	                msg.what=MSG_REFRESH_UI;
	                
	                Bundle bundle = new Bundle();
	                bundle.putString("voltage", String.valueOf(voltage));
	                bundle.putString("status", statusString);
	                bundle.putString("level", String.valueOf(level));
	                msg.setData(bundle);
	                mhHandler.sendMessage(msg);
	                
//	                msg.obj = String.valueOf(voltage)+" mV";
//	                mhHandler.sendMessage(msg);
	            }
	        }
	    };
	    
	public String getVersion() {
		String version = null;
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	OnClickListener clickListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			int mCurrentBt = v.getId();
			Intent intent ;
			switch(mCurrentBt){
			case R.id.sys_sd_content:
				if(new SDcardUtil().isHasSD()){
					showListDialog();
				}else{
					Toast.makeText(ctx, "No MicroSD.",Toast.LENGTH_SHORT).show();
				}
				break;
//			case R.id.sys_settings_wifi:
//				intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//				startActivity(intent);
//				break;
			case R.id.sys_settings_app:
//				跳转应用程序列表界面
//				intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
//				startActivity(intent);
//				或者【所有的】：
//				intent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
//				startActivity(intent);
//				或者【已安装的】：
//				Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//				startActivity(intent);
			
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
					startActivity(intent);
				}else{
					intent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
					startActivity(intent);
				}
				break;
			case R.id.sys_settings_datetime:
				intent = new Intent(Settings.ACTION_DATE_SETTINGS);
				startActivity(intent);
				break;
			case R.id.sys_settings_reset:
//				intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
//				startActivity(intent);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {//26 8.0.0
					intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
					startActivity(intent);
				} else {
					if(MainBoardUtil.isRK3288() || MainBoardUtil.isRK3288_CTD() || MainBoardUtil.isRK3288_CTE()){
						showConfirmDialog();
					}else{
						intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
						startActivity(intent);
					}
				}
				break;
			case R.id.sys_settings:
				intent = new Intent(Settings.ACTION_SETTINGS);
				startActivity(intent);
				break;
			case R.id.bt_stress:
				//5.1 //ComponentName componetName = new ComponentName("com.cghs.stresstest","com.cghs.stresstest.test.ArmFreqTest");
				// cmp=com.cghs.stresstest/.StressTestActivity
				ComponentName componetName = new ComponentName("com.cghs.stresstest","com.cghs.stresstest.StressTestActivity");
				try {
					intent = new Intent();
					intent.setComponent(componetName);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
			
	};

	/**
	 *  dialog
	 */
	private void showConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(R.string.str_Clear)
				.setMessage(R.string.str_factoryreset).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						doMasterClear();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
		builder.create().show();
	}

	@SuppressLint("WrongConstant")
	private void doMasterClear() {
		//发送广播的代码：这句话（intent1.addFlags(0x01000000)）会有红色的波浪线提醒，按Alt + Enter 消除，会添加这句话。@SuppressLint("WrongConstant")
		//给系统发送一个广播
		Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		intent.addFlags(0x01000000); //FLAG_RECEIVER_INCLUDE_BACKGROUND
		sendBroadcast(intent);
	}

	ProgressListFileDialog mProgressListFileDialog;
	private void showListDialog() {
		mProgressListFileDialog = new ProgressListFileDialog(ctx, sdcard,  new ProgressListFileDialog.ProgressListDialogListener() {
			@Override
			public void onListItemLongClick(int position, String path) {
//				Toast.makeText(mContext, path + "  " + position,Toast.LENGTH_SHORT).show();
			}
		});
		mProgressListFileDialog.setTitle(R.string.str_open_File);
		mProgressListFileDialog.show();
	}

}
