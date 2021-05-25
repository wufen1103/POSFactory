package com.citaq.citaqfactory;


import android.app.Application;

import com.citaq.util.SoundManager;


//D:\workspace\androidStudioProjects\SerialPortNDK\app\build\intermediates\cmake\debug\obj
public class CitaqApplication extends Application {
	private static final CitaqApplication instance = new CitaqApplication();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		/*SoundManager.prepareInstance();
		SoundManager.initSounds(this);
		SoundManager.loadSounds();*/
	}

}
