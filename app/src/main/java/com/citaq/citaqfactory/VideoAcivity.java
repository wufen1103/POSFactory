package com.citaq.citaqfactory;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;



public class VideoAcivity extends Activity {

	private static final String TAG = "VideoAcivity";
	
	protected static final String TAG_HASEXTERNALVIDEO = "hasExternalVideo";
	protected static final String TAG_REBOOT_INTERVAL = "reboot_type";
	protected static final String TAG_CUT_TIME = "cut_time";
	
	Context context;
	VideoView  videoView;
    String mVideoPath;
    
    // int count = 3 * 1;
     int count;    
     int min_new, min_old;
     TextView tv_count;   
     Handler mhandler;   
     Button setting;  
     SharedPreferences sharedPreferences;
     Editor editor;
     int min;
     
     int rebootInterval = 0;
 	 int cuttime = 0;
    
    boolean hasExternalVideo =false;
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_video); 
        
        context = this;
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra("path");
        
        hasExternalVideo= intent.getBooleanExtra(TAG_HASEXTERNALVIDEO,false);
        
        rebootInterval= intent.getIntExtra(TAG_REBOOT_INTERVAL,-100);//-100 软件运行了重启
        
        cuttime = intent.getIntExtra(TAG_CUT_TIME,-100);//切刀老化时间，即运行多久后第一次重启
        
        videoView = (FullScreenVideo) findViewById(R.id.supervideo);
        
//        mVideoPath = "http://oleeed73x.bkt.clouddn.com/me.mp4";
        
        if(hasExternalVideo){
        	 videoView.setVideoPath(mVideoPath); //设置媒体路径，网络媒体和本地媒体路径都使用此方法设置  
        }else{
        	 videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.anthemofchina1080p)); 
        }

        videoView.start();
        
        
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
              mp.setVolume(0f, 0f);
              mp.start();
              mp.setLooping(true);
            }
          });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                	videoView.setVideoPath(mVideoPath);
                	videoView.start();
                }
              });
        videoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				showNormalDialog();
				return true;
			}
		});
        
        
        
        context = this;
       
        
        setting = (Button) findViewById(R.id.setting);
        
        sharedPreferences = getSharedPreferences("info2", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        
        if(cuttime == -100){ 
        	
            if(rebootInterval==-100){     //软件自动启动的情况
          	  min = sharedPreferences.getInt(TAG_REBOOT_INTERVAL, 0);
          	
          }else{
          	 min = rebootInterval;         //老化界面进入的情况
          	 editor.putInt(TAG_REBOOT_INTERVAL,  rebootInterval );
          	 editor.commit();  
          }
        	
        }else{ //设置了距离第一次重启的时间
        	min = cuttime;
//      	 min = 1;     //for test
       	    editor.putInt(TAG_REBOOT_INTERVAL,  rebootInterval );
       	    editor.commit();
        }
        
       
//        min = sharedPreferences.getInt("reboot_interval", 0);
        
        count =  min * 60;
        min_new = min;
        min_old = min;
        
        
        tv_count = (TextView) findViewById(R.id.tv_count);
        
        if(count>0){
        	 tv_count.setText(count +" s");
        }else{
        	 tv_count.setText("no reboot.");
        }
       
        
        
        
        
        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // 更新你相应的UI
                    	
                    	if(min_new <= 0){
                    		 mhandler.sendEmptyMessageDelayed(0, 1000);
                    		 tv_count.setText("no reboot.");
                    		break;
                    	}

                        if(min_new != min_old){
                        	count = min_new * 60;
                        	min_old = min_new;
                        }
                        
//                        Log.i("count_new", min_new+"");
//                        Log.i("count", count+"");
//                        Log.i("count_old", min_old+"");

                        if (count == 0) {
                            mhandler.sendEmptyMessageDelayed(1, 1000);
                        }
                        
                        count = count - 1;
                        tv_count.setText(count + " s");
                        mhandler.sendEmptyMessageDelayed(0, 1000);

                        break;
                    case 1:
                    	/*try {
                    		onReboot();

                		} catch (Exception e) {
                			e.printStackTrace();
                			onReboot_fail();
                		}finally {
                			
                		}*/
                    	onReboot_fail();
//                      
                    	
                        break;
                }
            }
        };

       
        mhandler.sendEmptyMessageDelayed(0, 1000);
        
        
            
        
        
        setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showCustomizeDialog();
			}
		});
        
        
        editor.putBoolean("reboot", true);
        editor.commit();
    }  
	
	  private void showNormalDialog(){    
	        final AlertDialog.Builder normalDialog = 
	            new AlertDialog.Builder(context);
	        normalDialog.setMessage(R.string.video_error);
	        normalDialog.setPositiveButton(R.string.str_OK, 
	            new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	VideoAcivity.this.finish();
	            }
	        });
	        
	        // show
	        normalDialog.show();
	    }
  
    @Override  
    protected void onPause() {  
        super.onPause();  
        videoView.pause();  
    }  
  
    @Override  
    protected void onResume() {  
        super.onResume();  
        videoView.resume();  
    } 
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
//    	Log.i(TAG, "onStop");
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
//    	Log.i(TAG, "onDestroy");
    }
  
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	
//    	Log.i(TAG, "onBackPressed");
    	
    	 /**
         * 不起作用
         */
    	/*Intent intent = new Intent();
        setResult(2,intent);
        finish();*/
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//    	Log.i(TAG, "onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	editor.putBoolean("reboot", false);
       	    editor.putInt(TAG_REBOOT_INTERVAL,  0 );
       	    editor.commit();  
       	 
//       	 mhandler.removeMessages(0);
       	    mhandler.removeCallbacksAndMessages(null);
        	
        	Intent intent = new Intent();
            setResult(2,intent);
            finish();
            
        } 
         return super.onKeyDown(keyCode, event);
        
    }
    
    
    private void showCustomizeDialog() {
        /* @setView 装入自定义View ==> R.layout.dialog_customize
         * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
         * dialog_customize.xml可自定义更复杂的View
         */
        AlertDialog.Builder customizeDialog =  new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this) .inflate(R.layout.dialog_customize,null);
        
        final EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);
        edit_text.setText(sharedPreferences.getInt(TAG_REBOOT_INTERVAL, 0)+"");
        edit_text.setSelection(edit_text.getText().length());
        
        customizeDialog.setTitle("Please enter an integer for the reboot interval (min)  \n (Zero means no restart)");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取EditView中的输入内容   
                
                
                String value = edit_text.getText().toString().trim();   
                
                if(!"".equals(value) && isNumeric(value)){
                	min= Integer.parseInt(value);
                	 //使用editor保存数据
                    editor.putInt(TAG_REBOOT_INTERVAL,  min );
                    
                    //注意一定要提交数据，此步骤容易忘记
                    editor.commit();
                    
                    min_new = min;
                    
//                    Toast.makeText(NoticActivity.this, edit_text.getText().toString(), Toast.LENGTH_SHORT).show();
                	
                }else{
                	 Toast.makeText(VideoAcivity.this, "Must input an integer.", Toast.LENGTH_SHORT).show();
                }
                
               
            }
        });
        customizeDialog.show();
    }
    
    public boolean isNumeric(String str){ 
    	   Pattern pattern = Pattern.compile("-?[0-9]*"); 
    	   Matcher isNum = pattern.matcher(str);
    	   if( !isNum.matches() ){
    	       return false; 
    	   } 
    	   return true; 
    }
    
    
    public void onReboot() {
        Intent reboot = new Intent(Intent.ACTION_REBOOT);
        reboot.putExtra("nowait", 1);
        reboot.putExtra("interval", 1);
        reboot.putExtra("window", 0);
        sendBroadcast(reboot);
    }
    
	public void onReboot_fail() {
		
		Toast.makeText(VideoAcivity.this, "Reboot.", Toast.LENGTH_SHORT).show();
		/*
		 * Intent reboot = new Intent(Intent.ACTION_REBOOT);
		 * reboot.putExtra("nowait", 1); reboot.putExtra("interval", 1);
		 * reboot.putExtra("window", 0); sendBroadcast(reboot);
		 */

		try {
			Runtime.getRuntime().exec("su");
			Runtime.getRuntime().exec("reboot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Process process = null;

		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());

			// donnot use os.writeBytes(commmand), avoid chinese charset error
			os.write("reboot".getBytes());
			os.writeBytes("\n");
			os.flush();
			
			 os.writeBytes("exit\n");
	         os.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (process != null) {
				process.destroy();
			}

		}
	}
    

}