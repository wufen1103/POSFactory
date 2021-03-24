package com.citaq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.regex.Pattern;


public class MainBoardUtil {
	static String MainBoardUtilType = null;
	static String MainBoardPlatform = null;
	static String MainBoardKernelVersion = null;
	static String MainBoardProductName = null;
	static String MainBoardSerial = null;

	private static final String SMDKV210 = "SMDKV210";
	private static final String RK3188 = "RK30BOARD";
	private static final String RK30BOARD = "SUN50IW1P1";
	private static final String MSM8625Q = "QRD MSM8625Q SKUD";
	private static final String RK3368 = "RK3368";
	private static final String RK3288 = "GENERIC DT BASED SYSTEM"; //CTD RK3288
	private static final String RK3288_CTE = "CTE RK3288"; //CTE RK3288
	private static final String AllwinnerA63 = "SUN50IW3";
	
    public static String getCpuHardware() {
    	if(MainBoardUtilType != null){
    		return MainBoardUtilType;
    	}
    	
        String hardware = "";
        String str = "";
        try {
                Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
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
//		return "RK3368";
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

	public static boolean isSerialPrinterBoard() {
		if(MainBoardUtil.isRK3188() || MainBoardUtil.isRK3368() || MainBoardUtil.isRK30BOARD()
				|| MainBoardUtil.isRK3368_8_1()|| MainBoardUtil.isMSM8625Q() || MainBoardUtil.isRK3288_CTE()){

			return true;
		}
		return false;
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

	public static boolean isRK3288_CTE() {
		if(getCpuHardware().contains(MainBoardUtil.RK3288_CTE)){
			return true;
		}
		return false;
	}

	public static boolean isRK3368_8_1() {
		if(getCpuHardware().equals("")){
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

	public static String getBuildDisplayID() {
		String serial = null;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.build.display.id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serial;
	}

	public static String getPlatform() {
		if(MainBoardPlatform != null){
			return MainBoardPlatform;
		}
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			MainBoardPlatform = (String) get.invoke(c, "ro.board.platform");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MainBoardPlatform;
	}


	public static int getCPUNumCoresInt() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Default to return 1 core
			return 1;
		}
	}

	public static String getSystemVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static String getLinuxCore_Ver() {
		if(MainBoardKernelVersion != null){
			return MainBoardKernelVersion;
		}
		Process process = null;

		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);

		String result = "";
		String line;
		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (result != "") {
				String Keyword = "version ";
				int index = result.indexOf(Keyword);
				line = result.substring(index + Keyword.length());
				index = line.indexOf(" ");
				MainBoardKernelVersion = line.substring(0, index);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return MainBoardKernelVersion;
	}

	public static String getProductName() {
		if(MainBoardKernelVersion != null){
			return MainBoardKernelVersion;
		}
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			MainBoardProductName = (String) get.invoke(c, "ro.product.model");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MainBoardProductName;
	}

	public static String getSerial() {
		if(MainBoardSerial != null){
			return MainBoardSerial;
		}
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			MainBoardSerial = (String) get.invoke(c, "ro.serialno");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MainBoardSerial;
	}

}
