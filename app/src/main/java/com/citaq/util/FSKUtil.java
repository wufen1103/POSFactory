package com.citaq.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class FSKUtil {
	private static final String Tag = "FileUtil";
	
//	String fileName ="/mnt/sdcard/FSK.txt";
	String fileName ="/mnt/external_sd/FSK4";
	Context mContext;	
	byte[]  at= {0x40,0x40,0x40};
	FileOutputStream fout;
	
	public FSKUtil(Context context) {
		  mContext = context;	  
		  
		 
	}
    
    private void createFile(String filePath) {
  	    File file = new File(filePath); 
  	    if (!file.exists() && !file.isDirectory()) {  
  	        try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
  	    }
      }
       
    /**    
     * 追加文件：使用FileWriter    
     *     
     * @param fileName    
     * @param content    
     */    
    public void appendFile(byte[] mtxt,int size) {   
    	
		createFile(fileName);  //不存在 创建文件
		
		byte[] newBuffer = new byte[size];
    	System.arraycopy(mtxt, 0, newBuffer, 0, size);
	
    	int index = 0;
    	try {
    		FileOutputStream fout = new FileOutputStream(fileName,true);
//            FileOutputStream fout = mContext.openFileOutput(fileName, mContext.MODE_PRIVATE);  
    		    
    		    //fort test
	    	/*	byte[]  buffer = new byte[mtxt.length];
	    		
	    		for(int i = 0; i < mtxt.length; i++){
					if((mtxt[i] == 0x00) || (mtxt[i] == (byte)0x40)){
					}else{
						buffer[index] = mtxt[i];
						index++;
					}
				}
	    		byte[] newBuffer = new byte[index];
	    		System.arraycopy(buffer, 0, newBuffer, 0, index);
				
	    		fout.write(newBuffer);*/
    		 
     		fout.write(newBuffer);
//            fout.write(at);
            fout.close();  
            fout = null;
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }     
    
	public byte[] readFileInputStream() {
		byte[] buffer = {0x40,0x40,0x40};;
		try {
			FileInputStream fileInputStream = new FileInputStream ("/mnt/external_sd/FSK3");
			int len = fileInputStream.available();
			buffer = new byte[len];
			fileInputStream.read(buffer);
//			Log.v("读取到的文件为：", "" + new String(buffer));
			fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return buffer;
	}
	
 
	
}
