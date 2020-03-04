package com.citaq.util;

import com.example.gpioled.PosCtrl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class GPIOUtils {

    ///////////////RK3288 Cash///////////////////////////
	//通过控制/sys/class/gpio/gpio58/value的值来控制钱箱状态，默认钱箱关闭，value值为0
    public static String Cash_3288 = "/sys/class/gpio/gpio58/value";

	/**
	 * 修改gpio值为b
	 * @param b
	 * @param fileName
	 * @return
	 */
	public static int witchStatus(byte b, String fileName)
    {
    	File f_red_led = new File(fileName);//red led

    	OutputStream outRed = null;

    	int ret = 0;
    	try 
    	{
    		outRed = new FileOutputStream(f_red_led);

        	outRed.write(b);
        	outRed.flush();
        	
        	ret = 1;
    	} 
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	} 
    	finally
    	{
    		try
    		{
    			outRed.close();
    		} 
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	return ret;
    }

	/**
	 *  获取gpio的值
	 * @param fileName
	 * @return
	 */
	public static byte getGPIOStatus(String fileName)//
	{
		String str = "";
		byte[] buffer = new byte[1];
		File f_led = new File(fileName);
		try {
			FileInputStream fileInputStream = new FileInputStream (f_led);
			fileInputStream.read(buffer);
			fileInputStream.close();
			str = new String(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer[0];
	}


	/**
	 * 改变gpio值1s
	 *
	 * @param onff   gpio原始值
	 * @param fileName
	 */
	public static void witchStatus_SEC(final byte onff, final String fileName)
	{
		byte status = 0x30; //0
		if(onff == 0x31){
			status = 0x30;
		}else if(onff == 0x30){
			status = 0x31;
		}else{
			return;
		}
		witchStatus( status,  fileName);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GPIOUtils.witchStatus(onff,fileName);

			}

		}.start();
	}
}
