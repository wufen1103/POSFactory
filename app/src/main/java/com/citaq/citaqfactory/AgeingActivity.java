package com.citaq.citaqfactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.citaq.util.CitaqBuildConfig;
import com.citaq.util.Command;
import com.citaq.util.HttpUtil;
import com.citaq.util.MainBoardUtil;
import com.citaq.util.SerialPortManager;
import com.citaq.util.SharePreferencesHelper;
import com.citaq.util.SharePreferencesHelper.ContentValue;
import com.citaq.util.SoundManager;
import com.citaq.util.MobileNetworkUtils;
import com.citaq.util.ThreadPoolManager;
import com.printer.util.CallbackUSB;
import com.printer.util.USBConnectUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class AgeingActivity extends Activity {
	protected static final String TAG = "AgeingActivity";
	
	protected static final String TAG_HASEXTERNALVIDEO = "hasExternalVideo";
	protected static final String TAG_REBOOT_INTERVAL = "reboot_type";
	protected static final String TAG_CUT_TIME = "cut_time";
	
	protected static final int DEFAULT_HOURS = 0;   //切刀老化多少小时后重启test
	protected static final int DEFAULT_MIN = 5;   //重启间隔
	TextView tv_ok, tv_fail, tv_3g_restart, tv_ageing_success_rate, tv_run_time;
	Button bt_print, bt_network, bt_video;
	CheckBox cb_black, cb_grey, cb_cut, cb_sound;

	private static final int NETWORK_CHECK_CODES = 1000;
	private static final int REFRESH_NUM_CODES = 1001;
	private static final int PRINT_CHECK_CODES = 1002;
	private static final int RESET_MOBILENETWORK_CODES = 1003;

	private int countSuccess = 0;
	private int countFailue = 0;
	
	private int recLen = 0;   //运行时间

	private byte[] blackblock;
	private byte[] cutpaper;
	private byte[] grayblock;
	
	private int failueCount = 0;
	
	private boolean isTestWeb = false;
	private boolean isPrint = false;
	
	EditText edit_cut_hours,edit_reboot_min;
	CheckBox checkBox_playvideo;
	long cutbegintime=0;
	
	RadioGroup rg_speed;
	RadioButton speed_1000 = null;
	RadioButton speed_3000 = null;
	RadioButton speed_5000 = null;
	
	int printSpeed = 0;
	
	RadioGroup print_type;
	RadioButton print_serial = null;
	RadioButton print_usb = null;
	
	int printType = 0;
	
	SharePreferencesHelper mSharePreferencesHelper;
	
	RadioGroup reboot_type;
	RadioButton reboot_no = null;
	RadioButton reboot_5min = null;
	RadioButton reboot_1min = null;
	float rebootInterval = 0;
	
	int cuttime = 0;
	
	////////////////
	USBConnectUtil mUSBConnectUtil = null;

	SerialPortManager mSerialPortManager = null;
	
    private String videoName ="Video_Test";
    private String mVideoUriPath = "http://oleeed73x.bkt.clouddn.com/me.mp4";
    private String mVideoPath = "/mnt/external_sd/";
    private String mVideoPath2 = "/mnt/usb_storage/";
    private String mVideoPath3 = "/mnt/internal_sd/";
    private String[] videType ={".mp4",".rmvb",".mkv",".f4v",".flv",".avi"};

	Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		ctx = this;
		setContentView(R.layout.activity_ageing);

		SyncThread syncThread = new SyncThread();
		syncThread.start();
		initView();

		if(MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63()){
        	print_usb.setChecked(true);
			print_serial.setVisibility(View.GONE);

			cb_black.setChecked(true);
			cb_grey.setChecked(true);
			cb_cut.setChecked(false);
			cb_cut.setEnabled(false);
		}else{
			initSerial();
		}

		//通过构造方法来传入上下文和文件名
        mSharePreferencesHelper = new SharePreferencesHelper(this,CitaqBuildConfig.SHAREPREFERENCESNAME);
	}
	
	private void initSerial(){
		mSerialPortManager = new SerialPortManager(this,SerialPortManager.PRINTSERIALPORT_TTYS1);
	}

	private void initView() {	
		tv_ok = (TextView) findViewById(R.id.tv_ageing_network_ok_time);
		tv_fail = (TextView) findViewById(R.id.tv_ageing_network_fail_time);
		tv_ageing_success_rate = (TextView) findViewById(R.id.tv_ageing_success_rate);
		
		tv_3g_restart = (TextView) findViewById(R.id.tv_ageing_3g_restart_times);
		
		tv_run_time = (TextView) findViewById(R.id.tv_run_time);

		cb_black = (CheckBox) findViewById(R.id.checkBox_BlackBlock);
		cb_grey = (CheckBox) findViewById(R.id.checkBox_GreyBlock);
		cb_cut = (CheckBox) findViewById(R.id.checkBox_OpenCut);
		cb_sound = (CheckBox) findViewById(R.id.checkBox_Sound);

		bt_print = (Button) findViewById(R.id.bt_ageing_printCut);
		bt_network = (Button) findViewById(R.id.bt_ageing_network);
		
		bt_video = (Button) findViewById(R.id.bt_ageing_video);
		
		edit_cut_hours = (EditText) findViewById(R.id.edit_cut_hours);
		edit_cut_hours.setText(DEFAULT_HOURS+"");
		checkBox_playvideo = (CheckBox) findViewById(R.id.checkBox_playvideo);
		
		edit_cut_hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                //禁止输入51-99之间的整数  通过for循环
                /*for(int i = 51;i<=99;i++) {
                    if (edit_cut_hours.getText().toString().equals(String.valueOf(i))) {
                    	edit_cut_hours.setText("");
                    }
                }*/
//            	if (edit_cut_hours.getText().toString().equals(String.valueOf(0))) {
//                	edit_cut_hours.setText("");
//                }
            	
            }
        });

		edit_reboot_min = (EditText) findViewById(R.id.edit_reboot_min);
		edit_reboot_min.setText(DEFAULT_MIN+"");

		bt_print.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if (v.getTag().toString().equals("start")) {
					v.setTag("stop");
					isPrint = true;
					// cs
					((Button) v).setText(ctx.getString(R.string.stop_testing));
					handler.sendEmptyMessage(PRINT_CHECK_CODES);
			/*		Log.i(TAG,
							"checkout--->" + cb_black.isChecked()
									+ cb_grey.isChecked() + cb_cut.isChecked());*/
					
					if(checkBox_playvideo.isChecked()){
//						cutbegintime = System.currentTimeMillis();
						

						cuttime = Integer.parseInt(edit_cut_hours.getText().toString()) * 60;   //rebootType 是分钟计数
//						bt_video.callOnClick();
						Intent intent = new Intent(ctx,VideoAcivity.class);
						intent.putExtra(TAG_HASEXTERNALVIDEO, false);
						intent.putExtra(TAG_REBOOT_INTERVAL, Float.valueOf(edit_reboot_min.getText().toString()));
						intent.putExtra(TAG_CUT_TIME, cuttime);
						startActivityForResult(intent,1);
					}
				} else {
					// 执行 停止
					v.setTag("start");
					isPrint = false;
					((Button) v).setText(ctx.getString(R.string.start_testing));
				}

			}
		});

		bt_network.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag().toString().equals("start")) {
					v.setTag("stop");
					// cs
					((Button) v).setText(ctx.getString(R.string.stop_testing));

					isTestWeb = true;
					handler.sendEmptyMessage(NETWORK_CHECK_CODES);
				} else {
					// 执行 停止
					v.setTag("start");
					((Button) v).setText(ctx.getString(R.string.start_testing));
					isTestWeb = false;
				}

			}
		});
		
		bt_video.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String path = null;
				Intent intent = new Intent(ctx,VideoAcivity.class);
				
				path =getVideo(mVideoPath,videoName,videType);
				
				if(null ==path){
					path =getVideo(mVideoPath2,videoName,videType);	
				}
				if(null ==path){
					path =getVideo(mVideoPath3,videoName,videType);
					
				}

				if(null == path){
//					Toast.makeText(ctx, R.string.no_video, Toast.LENGTH_LONG).show();
					intent.putExtra(TAG_HASEXTERNALVIDEO, false);
					intent.putExtra(TAG_REBOOT_INTERVAL, rebootInterval);
				}else{
					intent.putExtra(TAG_HASEXTERNALVIDEO, true);
					intent.putExtra(TAG_REBOOT_INTERVAL, rebootInterval);
					intent.putExtra("path", path);
				}
				
				startActivityForResult(intent,1);
				
			}
		});
		
//		long a = System.currentTimeMillis();
//		blackblock = Command.transToPrintText(getString(R.string.black_block));
//		long b = System.currentTimeMillis();
//		cutpaper = Command.transToPrintText(getString(R.string.cut_paper));
//
//		grayblock = Command.transToPrintText(getString(R.string.gray_block));
//		long d = System.currentTimeMillis();
		
//		Log.i(TAG, "b-a =" + (b - a) + ",,,,,d - a =" + (d - a));

		rg_speed = (RadioGroup) findViewById(R.id.rg_speed);
		speed_1000 = (RadioButton) findViewById(R.id.speed_1000);
		speed_3000 = (RadioButton) findViewById(R.id.speed_3000);
		speed_5000 = (RadioButton) findViewById(R.id.speed_5000);
		
		rg_speed.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(speed_1000.getId()==checkedId){
					printSpeed =1000;
				}else if(speed_3000.getId()==checkedId){
					printSpeed =3000;
				}else if(speed_5000.getId()==checkedId){
					printSpeed =5000;
				}
				
			}
		});
		
		speed_3000.setChecked(true);

		reboot_type = (RadioGroup) findViewById(R.id.reboot_type);
		reboot_no = (RadioButton) findViewById(R.id.reboot_no);
		reboot_5min = (RadioButton) findViewById(R.id.reboot_5min);
		reboot_1min = (RadioButton) findViewById(R.id.reboot_1min);
		
		reboot_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(reboot_no.getId()==checkedId){
					rebootInterval =0;
				}else if(reboot_5min.getId()==checkedId){
					rebootInterval =5;
				}else if(reboot_1min.getId()==checkedId){
					rebootInterval =1;
				}
			}
		});
		
		reboot_5min.setChecked(true);

		print_type = (RadioGroup) findViewById(R.id.print_type);
		print_serial = (RadioButton) findViewById(R.id.print_serial);
		print_usb = (RadioButton) findViewById(R.id.print_usb);
		
		print_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(print_serial.getId()==checkedId){
					printType = 0;
				}else if(print_usb.getId()==checkedId){
					printType = 1;
					if(mUSBConnectUtil == null){
						initUSBConnect();
					}
				}
				
			}
		});
		
		timeHandler.postDelayed(runnable, 1000);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			LinearLayout ll_3g_restart = (LinearLayout) findViewById(R.id.ll_ageing_3g_restart_times);
			ll_3g_restart.setVisibility(View.INVISIBLE);
			TextView tv_3g_restart_tips = (TextView) findViewById(R.id.tv_ageing_3g_restart_times_tips);
			tv_3g_restart_tips.setVisibility(View.GONE);
		}

	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if (resultCode==2){
                	if (bt_print.getTag().toString().equals("start")) {
    					
    				} else {
    					// 执行 停止
    					bt_print.setTag("start");
    					isPrint = false;
    					bt_print.setText(ctx.getString(R.string.start_testing));
    				}
                }
        }
    }
	
	private void initUSBConnect() {       // remember to destroyPrinter on
		if(mUSBConnectUtil == null){
			 mUSBConnectUtil = USBConnectUtil.getInstance();
		       
		        
			 mUSBConnectUtil.setCallback(new CallbackUSB() {
					
					@Override
					public void callback(final String str,boolean toShow) {
//						Log.v(TAG, str.toString());
					}

					@Override
					public void hasUSB(boolean hasUSB) {
						if(!hasUSB) Toast.makeText(ctx, R.string.nousbdevice, Toast.LENGTH_SHORT).show();
						
					}
	
				});
		        
			 mUSBConnectUtil.initConnect(this,USBConnectUtil.TYPE_PRINT);
		}
			
	}
	
	Handler timeHandler = new Handler();    
    Runnable runnable = new Runnable() {    
        @Override    
        public void run() {    
            recLen++;
            tv_run_time.setText(getRunTime(recLen));    
            handler.postDelayed(this, 1000);    
        }    
    };   
    
    private String getRunTime(int second){
    	int d = recLen/3600/24;
    	int h = recLen/3600;
 		int m = recLen%3600/60;
 		int s = recLen%3600%60;
 		
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
	
    private class SyncThread extends Thread {
        @Override
        public void run() {

    		blackblock = Command.transToPrintText(getString(R.string.black_block));
    		cutpaper = Command.transToPrintText(getString(R.string.cut_paper));
    		grayblock = Command.transToPrintText(getString(R.string.gray_block));
    		
    		long a = System.currentTimeMillis();
    		
//    		Log.i(TAG, "a = " + a);

        }
    }
    private boolean fileIsExists(String path){
        try{
                File f=new File(path);
                if(!f.exists()){
                        return false;
                }
                
        }catch (Exception e) {
                // TODO: handle exception
                return false;
        }
        return true;
    }
    
    private String getVideo(String path,String name,String[] type){
    	List<String> videoInfos=new ArrayList<String>();
    	
    	for(String ty:type){
    		String videoPath = path+name+ty;
    		if(fileIsExists(videoPath)){
    			return videoPath;
    		}
    	}
    	
    	return null;
    	
    }


	private boolean serialWrite(byte[] cmd) {
		boolean returnValue = true;
		try {

			mSerialPortManager.write(cmd);
		} catch (Exception ex) {
			returnValue = false;
		}
		return returnValue;
	}
	
	private  boolean usbWrite(byte[] cmd){
		return mUSBConnectUtil.sendMessageToPrint(cmd);
    }
	
	private boolean printerWrite(byte[] cmd) {
		boolean returnValue = true;	
 		if(printType == 0){   //serial
			returnValue = serialWrite(cmd);

 		}else{   //usb
			returnValue = usbWrite(cmd);
		
 		}
		return returnValue;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_NUM_CODES:
				tv_ok.setText(countSuccess+"");
				tv_fail.setText(countFailue+"");
				
				
				if(countSuccess + countFailue > 0){
					tv_ageing_success_rate.setText(String.format("%.2f", (double)countSuccess/(double)( countSuccess+countFailue) * 100) +"%");
				}
				
				tv_3g_restart.setText(MobileNetworkUtils.get3GResetSum()+"");
				handler.sendEmptyMessageDelayed(NETWORK_CHECK_CODES, 2000);
				break;
			case NETWORK_CHECK_CODES:
				checkNet();
				break;
			case PRINT_CHECK_CODES:
				doPrint();
				
			/*	if(checkBox_playvideo.isChecked()){
					//System.currentTimeMillis() 该方法的作用是返回当前的计算机时间，时间的表达格式为当前计算机时间和GMT时间(格林威治时间)1970年1月1号0时0分0秒所差的毫秒数
					if(System.currentTimeMillis()- cutbegintime>=1 *1000 *60 * 3){ //3分钟
					
					}
				}*/
				break;
			case RESET_MOBILENETWORK_CODES:
				resetMobileNetwork();
				break;
			default:
				break;
			}
		}
	};



	private void resetMobileNetwork() {
		ThreadPoolManager.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				MobileNetworkUtils.resetMobileNetwork(ctx, false);
				MobileNetworkUtils.resetMobileNetwork(ctx, true);
				MobileNetworkUtils.reset3GCount();
			}
		});
	}

	private void doPrint() {
		if(!isPrint)return;
		if (cb_black.isChecked()) {
            if(blackblock == null ) {
            	blackblock = Command.transToPrintText(getString(R.string.black_block));
            }
            printerWrite(blackblock);
		}
		if (cb_grey.isChecked()) {
			if(grayblock == null ) grayblock = Command.transToPrintText(getString(R.string.gray_block));
			printerWrite(grayblock);
		}
		if (cb_cut.isChecked()) {
			if(cutpaper == null ) cutpaper = Command.transToPrintText(getString(R.string.cut_paper));;
			printerWrite(cutpaper);
		}
		
		handler.sendEmptyMessageDelayed(PRINT_CHECK_CODES, printSpeed);
	}

	private void checkNet(){
		if(!isTestWeb)return;
		ThreadPoolManager.getInstance().executeTask(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				String url ="http://yx.ideliver.cn/checkNet.asp";
				String url ="http://www.baidu.com";
				boolean b = HttpUtil.httpString(url);
				if (b){
					countSuccess++;
					failueCount =0;
				}else{
					countFailue++;
					failueCount++;
					if(cb_sound.isChecked())SoundManager.playSound(0, 1);;
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
						if(failueCount>=10){
							failueCount = 0;
							handler.sendEmptyMessage(RESET_MOBILENETWORK_CODES);
						}
					}

				}
				handler.sendEmptyMessage(REFRESH_NUM_CODES);

			}});
	}
	
	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		   if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
			   isPrint = false;
			   isTestWeb = false;
			   finish();
//			   System.exit(0);
			   return true;
		   }
		   return super.onKeyDown(keyCode, event);
		}
	 
	 
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//
		
		recLen = mSharePreferencesHelper.getInt("recLen");
	}
	 
	 
	 @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		ContentValue contentValues = new ContentValue("recLen",recLen);
		mSharePreferencesHelper.putValues(contentValues);
	} 

	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mUSBConnectUtil != null)
			mUSBConnectUtil.destroyPrinter();

		 if(mSerialPortManager != null)
			 mSerialPortManager.destroy();
	}
}
