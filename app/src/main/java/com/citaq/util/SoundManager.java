package com.citaq.util;

import java.util.HashMap;

import com.citaq.citaqfactory.R;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	 
	private static SoundManager _instance;
	private static SoundPool mSoundPool;
	private static HashMap<Integer, Integer> mSoundPoolMap;
	private static AudioManager  mAudioManager;
	private static Context mContext;
 
	private SoundManager()
	{
	}
 
	public static  void  prepareInstance()
	{
	    if (_instance == null){
	      _instance = new SoundManager();
	    }
	 }
	
	/**
	 * 单例模式，如果SoundManager实例不存在就创建，反之则直接返回已经
         *  存在的SoundManager实例
	 *
	 * @return Returns the single instance of the SoundManager
	 */
	static synchronized public SoundManager getInstance()
	{
	    if (_instance == null)
	      _instance = new SoundManager();
	    return _instance;
	 }
 
	/**
	 * Initialises the storage for the sounds
	 *
	 * @param theContext The Application context
	 */
	public static  void initSounds(Context theContext)
	{
		mContext = theContext;
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	  
		float streamVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//	     mSoundPool = new SoundPool((int) streamVolume, AudioManager.STREAM_MUSIC, 0);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			AudioAttributes audioAttributes = null;
			audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.build();

			mSoundPool = new SoundPool.Builder()
					.setMaxStreams((int) streamVolume)
					.setAudioAttributes(audioAttributes)
					.build();
		} else { // 5.0 以前
			mSoundPool = new SoundPool((int) streamVolume, AudioManager.STREAM_MUSIC, 0);  // 创建SoundPool
		}
	     mSoundPoolMap = new HashMap<Integer, Integer>();

	} 
 
	/**
	 * Add a new Sound to the SoundPool
	 *
	 * @param Index - The Sound Index for Retrieval
	 * @param SoundID - The Android ID for the Sound asset.
	 */
	public static void addSound(int Index,int SoundID)
	{
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
	}
 
	/**
	 * Loads the various sound assets
	 */
	public static void loadSounds()
	{
		mSoundPoolMap.put(0, mSoundPool.load(mContext,R.raw.click, 1));
		mSoundPoolMap.put(1, mSoundPool.load(mContext,R.raw.laser, 1));
	}
 
	/**
	 * Plays a Sound
	 *
	 * @param index - The Index of the Sound to be played
	 * @param speed - The Speed to play not, not currently used but included for compatibility
	 */
	public static int playSound(int index, boolean isLoop, float speed)
	{
		int loop =0;
		if(isLoop){
			loop = -1;
		}
		int isOk= mSoundPool.play( mSoundPoolMap.get(index), 1.0f, 1.0f, 1, loop, speed); //non-zero streamID if successful, zero if failed
		return isOk;
	}

	public static int playSoundLeft(int index, boolean isLoop, float speed)
	{
		int loop =0;
 		if(isLoop){
			loop = -1;
		}
		int isOk= mSoundPool.play( mSoundPoolMap.get(index), 1.0f, 0, 1, loop, speed); //non-zero streamID if successful, zero if failed
		return isOk;
	}

	public static int playSoundRight(int index, boolean isLoop, float speed)
	{
		int loop =0;
		if(isLoop){
			loop = -1;
		}
		int isOk= mSoundPool.play( mSoundPoolMap.get(index), 0, 1.0f, 1, loop, speed); //non-zero streamID if successful, zero if failed
		return isOk;
	}

	public static void pause(int id)
	{
		mSoundPool.pause(id);
	}

	public static void resume(int id)
	{
		mSoundPool.resume(id);
	}
 
	/**
	 * Stop a Sound
	 * @param index - index of the sound to be stopped
	 */
	public static void stopSound(int index)
	{
		mSoundPool.stop(mSoundPoolMap.get(index));
	}
 
	/**
	 * Deallocates the resources and Instance of SoundManager
	 */
	public static void cleanup()
	{
		mSoundPool.release();
		mSoundPool = null;
	    mSoundPoolMap.clear();
	    mAudioManager.unloadSoundEffects();
	    _instance = null;
 
	}
 
}
