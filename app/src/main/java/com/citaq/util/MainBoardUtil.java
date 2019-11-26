package com.citaq.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


public class MainBoardUtil {
	
	static String MainBoardUtilType = null;

	private static final String SMDKV210 = "SMDKV210";
	private static final String RK3188 = "RK30BOARD";
	private static final String RK30BOARD = "SUN50IW1P1";
	private static final String MSM8625Q = "QRD MSM8625Q SKUD";
	private static final String RK3368 = "RK3368";
	private static final String RK3288 = "GENERIC DT BASED SYSTEM";
	private static final String AllwinnerA63 = "SUN50IW3";
	
    public static String getCpuHardware() {
    	
    	if(MainBoardUtilType != null){
    		return MainBoardUtilType;
    	}
    	
        String hardware = "";
        String str = "";
        try {
                Process pp = Runtime.getRuntime().exec(
                                "cat /proc/cpuinfo");
                InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                LineNumberReader input = new LineNumberReader(ir);

                
                while((str = input.readLine()) != null){
                	if (str.startsWith("Hardware")){
                		int i = str.indexOf(":");
                		hardware = str.substring(i+1).trim().toUpperCase();
                		
                		MainBoardUtilType = hardware;
                		return hardware;
                	}
                }
                
        } catch (IOException ex) {
                //
                ex.printStackTrace();
        }
        return hardware;
    }
	
	public static String getModel(){
		String rs = "unknow board";
		
		String hw = getCpuHardware();
		
		if(hw.contains(SMDKV210)){
			rs = "smdkv210";
		}else if(hw.contains(RK3188)){
			rs = "rk30sdk";
		}else if(hw.contains(MSM8625Q)){
			rs = "c500";
		}
		
		return rs;
	}
	
	public static boolean isRK3368() {
		
		if(getCpuHardware().contains(MainBoardUtil.RK3368)){
			return true;
		}
		return false;
	}
	
	public static boolean isRK3188() {
		if(getCpuHardware().contains(MainBoardUtil.RK3188)){
			return true;
		}
		return false;
	}
	
	public static boolean isRK3288() {
		if(getCpuHardware().contains(MainBoardUtil.RK3288)){
			return true;
		}
		return false;
	}
	
	public static boolean isAllwinnerA63(){
		if(getCpuHardware().contains(MainBoardUtil.AllwinnerA63)){
			return true;
		}
		return false;
	}
	
	public static boolean isMSM8625Q() {
		if(getCpuHardware().contains(MainBoardUtil.MSM8625Q)){
			return true;
		}
		return false;
	}
	
	public static boolean isRK30BOARD() {
		if(getCpuHardware().contains(MainBoardUtil.RK30BOARD)){
			return true;
		}
		return false;
	}

}
