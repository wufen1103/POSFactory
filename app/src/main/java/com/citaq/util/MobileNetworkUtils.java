package com.citaq.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;

public class MobileNetworkUtils {
	
    public static void resetMobileNetwork(Context context, boolean enable){
    	///////////////////5.1 没有权限/////////////////////////
//    	 try {
//	            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//	            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
//
//	            if (null != setMobileDataEnabledMethod) {
//	                setMobileDataEnabledMethod.invoke(telephonyService, enable);
//	            }

          ///////////////////5.1 这个方法/////////////////////////
//		从Android L及更高版本开始，setMobileDataEnabled方法不再可调用

//		我已经向Google报告了问题78084，因为该setMobileDataEnabled()方法不再可以通过反射调用。
//		它可以通过反射从Android 2.1（API 7）到Android 4.4（API 19）进行调用，
//		但是从Android L及更高版本开始，即使使用root，该setMobileDataEnabled()方法也不可调用。
		    ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        Class<?> cmClass = connManager.getClass();    
	        Class<?>[] argClasses = new Class[1];    
	        argClasses[0] = boolean.class;    
	        // 反射ConnectivityManager中hide的方法setMobileDataEnabled，可以开启和关闭GPRS网络    
	        
	        try {
	        	Method method = cmClass.getMethod("setMobileDataEnabled", argClasses);    
				method.invoke(connManager, enable);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
    
    
   private static int sumForReset3G = 0;
	public static int get3GResetSum() {
		// TODO Auto-generated method stub
		return sumForReset3G;
	}
	
	public static void reset3GCount(){
		sumForReset3G++;
	}

}
