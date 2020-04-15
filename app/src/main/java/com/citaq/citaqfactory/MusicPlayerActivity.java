package com.citaq.citaqfactory;


import java.io.File;
import java.io.IOException;

import com.citaq.util.LEDControl;
import com.citaq.util.SoundPoolManager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MusicPlayerActivity extends Activity {
	
	private MediaPlayer mMediaPlayer = null;
	Context mContext;
	/*Button bt_volume_decrease;
	Button bt_volume_increase;*/
	
	Button bt_volume_mute;
	Button bt_volume_medium;
	Button bt_volume_hight;
	
	
	CheckBox bt_play_pause;
	
	TextView tv_current_vol;
	
	ToggleButton bt_left;
	ToggleButton bt_right;
	
	int  soundID_left =-1;
	int  soundID_right =-1;
	
	int laser;
	
	SoundPoolManager mSoundPoolManager;
	
	//音量控制,初始化定义    
	AudioManager mAudioManager;   
	int maxVolume;
	
	int defaultVolume, currentVolume;
	
	//////////////////////////////////////////////////////////////////////////////
	
	
	MediaRecorder mMediaRecorder;
	private String outputFile = null;
	private Button bt_start, bt_stop, bt_play;
	ImageView img_volume;
	public static final int MAX_LENGTH = 1000 * 60 * 5;// 最大录音时长1000*60*10;
	private static final String TAG = "MusicPlayerActivity";
	
	MediaPlayer mMediaPlayer2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_music_microphone);
		mContext = this;
		
		mMediaPlayer = MediaPlayer.create(this, R.raw.delivery);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setLooping(true);
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){  
	            @Override  
	            public void onCompletion(MediaPlayer arg0) {  
	                Toast.makeText(MusicPlayerActivity.this, R.string.end, Toast.LENGTH_SHORT).show();  
	                mMediaPlayer.release();  
	            }  
	        });
		initView();
		
		bt_start = (Button) findViewById(R.id.bt_audio_start);
		bt_stop = (Button) findViewById(R.id.bt_audio_stop);
		bt_play = (Button) findViewById(R.id.bt_audio_play);
		img_volume = (ImageView) findViewById(R.id.img_audio_volume);
		img_volume.setBackgroundResource(R.drawable.audio_volume_high);

		bt_stop.setEnabled(false);
		bt_play.setEnabled(false);
		outputFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/myrecording.3gp";

	}
	
	
	private void initView(){
		/*bt_volume_decrease = (Button) findViewById(R.id.volume_decrease);
		bt_volume_increase = (Button) findViewById(R.id.volume_increase);*/
		
		bt_volume_mute = (Button) findViewById(R.id.volume_mute);
		bt_volume_medium = (Button) findViewById(R.id.volume_medium);
		bt_volume_hight = (Button) findViewById(R.id.volume_hight);
				
		bt_play_pause = (CheckBox) findViewById(R.id.play_pause);
		
		bt_left = (ToggleButton) findViewById(R.id.left_vol);
		bt_right = (ToggleButton) findViewById(R.id.right_vol);
		
		tv_current_vol = (TextView) findViewById(R.id.tv_current_vol);
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);   
		
		//最大音量    
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
		
		
		bt_volume_mute.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						setVolume(0);
					}
				});
		
		bt_volume_medium.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setVolume(maxVolume/2);
			}
		});
		
		bt_volume_hight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setVolume(maxVolume);
			}
		});
				
/*		bt_volume_decrease.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				currentVolume--;
				setVolume(currentVolume);
			}
		});
		
		bt_volume_increase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				currentVolume++;
				setVolume(currentVolume);
				
			}
		});*/


		bt_play_pause.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// 点击 播放png  isChecked true;
				// 暂停png  isChecked true;
				if (isChecked) {
					if (bt_left.isChecked()) {
						bt_left.setChecked(false);
					}
					if (bt_right.isChecked()) {
						bt_right.setChecked(false);
					}
					//选择暂停时
					if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
						mMediaPlayer.start();
					} else {
						try {
							mMediaPlayer.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mMediaPlayer.start();
					}

				} else {
					if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
						mMediaPlayer.pause();
					} else {

					}
				}

			}
		});
		
		mSoundPoolManager = new SoundPoolManager(getApplicationContext());
		laser = mSoundPoolManager.load(R.raw.laser);
		
		bt_left.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
						bt_play_pause.setChecked(false);
					}
					if(soundID_left == -1){
						soundID_left = mSoundPoolManager.playLeft(laser);
					}else{
						mSoundPoolManager.resume(soundID_left);
					}
				}else{
					if(soundID_left!=-1){
						mSoundPoolManager.pause(soundID_left);
					}
				}
			}
		});
		
		
		
		bt_right.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
						bt_play_pause.setChecked(false);
					}
					if(soundID_right == -1){
						soundID_right = mSoundPoolManager.playRight(laser);
					}else{
						mSoundPoolManager.resume(soundID_right);
					}
				}else{
					if(soundID_right!=-1){
						mSoundPoolManager.pause(soundID_right);
					}
				}
			
			}
		});
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		defaultVolume = currentVolume;
		
		tv_current_vol.setText(String.valueOf(currentVolume));
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0); 
		
		deleteFile(new File(outputFile));
		this.finish();
	}
	
	private void setVolume(int volume){
	    if(volume >=0 && volume<=maxVolume){
	    	mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0); 
	    	
	    	tv_current_vol.setText(String.valueOf(volume));
	    }else{
	    	
	    	if(volume <= 0){
	    		Toast.makeText(MusicPlayerActivity.this, mContext.getResources().getString(R.string.minimum_volume),Toast.LENGTH_SHORT).show();  
	    		currentVolume++;
	    	}else{
	    		Toast.makeText(MusicPlayerActivity.this, mContext.getResources().getString(R.string.max_volume),Toast.LENGTH_SHORT).show();  
	    		currentVolume--;
	    	}
	    }
		
	}
	
	@Override
	protected void onDestroy() {
		if(mMediaPlayer != null ){
			mMediaPlayer.release();
		}
		mSoundPoolManager.unloadAll();
		super.onDestroy();
	}
	
	
	
	public void start(View view) {

		/*//判断是否为android6.0系统版本，如果是，需要动态添加权限
		if (Build.VERSION.SDK_INT>=23) {
			//⑧申请录制音频的动态权限
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{
						android.Manifest.permission.RECORD_AUDIO}, 1);

			}
		}*/


		if (mMediaPlayer2 != null) {
			mMediaPlayer2.release();
			mMediaPlayer2 = null;
		}

		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
		mMediaRecorder.setOutputFile(outputFile);
		mMediaRecorder.setMaxDuration(MAX_LENGTH);

		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();

			updateMicStatus(); //
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bt_start.setEnabled(false);
		bt_stop.setEnabled(true);
		bt_play.setEnabled(false);
		// Toast.makeText(getApplicationContext(), "Recording started",
		// Toast.LENGTH_LONG).show();

	}
	
	public void stop(View view) {
		try {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		bt_start.setEnabled(true);
		bt_stop.setEnabled(false);
		bt_play.setEnabled(true);
//		Toast.makeText(getApplicationContext(), "Audio recorded successfully",
//				Toast.LENGTH_LONG).show();
		
		

	}

	public void play(View view) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {

		mMediaPlayer2 = new MediaPlayer();
		mMediaPlayer2.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				bt_play.setEnabled(true);

			}
		});
		mMediaPlayer2.setDataSource(outputFile);
		mMediaPlayer2.prepare();
		mMediaPlayer2.start();

		// Toast.makeText(getApplicationContext(), "Playing audio",
		// Toast.LENGTH_LONG).show();

		bt_play.setEnabled(false);

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what < 33) {
				img_volume.setBackgroundResource(R.drawable.audio_volume_low);
				img_volume.invalidate();
			} else if (msg.what < 66) {
				img_volume
						.setBackgroundResource(R.drawable.audio_volume_medium);
				img_volume.invalidate();
			} else {
				img_volume.setBackgroundResource(R.drawable.audio_volume_high);
				img_volume.invalidate();
			}

		}
	};

	private Runnable mUpdateMicStatusTimer = new Runnable() {
		public void run() {
			updateMicStatus();
		}
	};

	/**
	 * 更新话筒状态
	 * 
	 */
	private int BASE = 1;
	private int SPACE = 1000;// 间隔取样时间

	private void updateMicStatus() {
		if (mMediaRecorder != null) {
			double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
			double db = 0;// 分贝
			if (ratio > 1)
				db = 20 * Math.log10(ratio);
			Log.d(TAG, "分贝值：" + db);
			mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
			mHandler.sendEmptyMessage((int) db);
		}
	}

	public void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					this.deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
				}
			}
			file.delete();
		} else {
			Log.d(TAG, "file not exists");
		}
	}

}
