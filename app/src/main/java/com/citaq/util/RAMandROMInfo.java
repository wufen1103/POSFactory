package com.citaq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

@SuppressLint("NewApi")
public class RAMandROMInfo {
	private static final String TAG = "RAMandROMInfo";
	
	Context context;
	
	String mmcblk ="mmcblk0";
	String zram0 ="zram0";
	
	public RAMandROMInfo(Context ctx) {
		context = ctx;
//		 showRAMInfo();
//		 
//		 showROMInfo();
//		 
//		 showSDInfo();
		
		if(MainBoardUtil.isRK3288()||MainBoardUtil.isRK3288_CTE()){
			mmcblk ="mmcblk2";
		}
	}
	
	
	/*显示RAM的可用和总容量，RAM相当于电脑的内存条*/  
    public String showRAMInfo(){  
        ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);  
        MemoryInfo mi=new MemoryInfo();  
        am.getMemoryInfo(mi);  
        String[] available=fileSize(mi.availMem);  
        String[] total=fileSize(mi.totalMem);  
  //      Log.i(TAG, "RAM "+available[0]+available[1]+"/"+total[0]+total[1]) ;
        
        return total[0]+total[1];
    }  
    
    public String showRAMInfo2(){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }
    
    
    public String showROMInfo2(){//GB
        String path = "/proc/partitions";
        String line = null;
        Float totalRom = (float) 0;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            
            while ((line = br.readLine()) != null) {//如果之前文件为空，则不执 行输出
            	if(line.endsWith(mmcblk)
            			|| line.endsWith(zram0)
//            			|| line.endsWith("mmcblk2")|| line.endsWith("ram")
            			){
            		String  blk = line.split("\\s+")[3];
            		if(isNumeric(blk)){
            			
            			totalRom =totalRom+ Float.valueOf(blk);
            		}
            		
            	}

            }
            
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
       
          int   total = (int)Math.ceil((new Float(totalRom/ (1024 * 1024)).doubleValue()));
        

        return total + "GB";//返回1GB/2GB/3GB/4GB
    }
    
/*  1+4  3188@ctepos:/ # cat /proc/partitions
    major minor  #blocks  name

     179        0    3866624 mmcblk0
     179        1       4096 mmcblk0p1
     179        2       8192 mmcblk0p2
     179        3      16384 mmcblk0p3
     179        4      32768 mmcblk0p4
     179        5      65536 mmcblk0p5
     179        6     131072 mmcblk0p6
     179        7    1044480 mmcblk0p7
     179        8       4096 mmcblk0p8
     179        9       4096 mmcblk0p9
     179       10     786432 mmcblk0p10
     179       11    1761280 mmcblk0p11

 2+16    root@ctepos:/proc # cat partitions
    major minor  #blocks  name

     179        0   15388672 mmcblk0
     179        1       4096 mmcblk0p1
     179        2       8192 mmcblk0p2
     179        3      16384 mmcblk0p3
     179        4      32768 mmcblk0p4
     179        5      65536 mmcblk0p5
     179        6     131072 mmcblk0p6
     179        7    1044480 mmcblk0p7
     179        8       4096 mmcblk0p8
     179        9       4096 mmcblk0p9
     179       10     786432 mmcblk0p10
     179       11   13283328 mmcblk0p11


     */
    
    
/*  1+8  3368@justeat_32:/ $ cat /proc/partitions
    major minor  #blocks  name

     254        0     520912 zram0
     179        0    7634944 mmcblk0
     179        1       4096 mmcblk0p1
     179        2       4096 mmcblk0p2
     179        3       4096 mmcblk0p3
     179        4      16384 mmcblk0p4
     179        5      16384 mmcblk0p5
     179        6      32768 mmcblk0p6
     179        7      32768 mmcblk0p7
     179        8     114688 mmcblk0p8
     179        9     131072 mmcblk0p9
     179       10       4096 mmcblk0p10
     179       11    1572864 mmcblk0p11
     179       12      16384 mmcblk0p12
     179       13       4096 mmcblk0p13
     179       14    4194304 mmcblk0p14
     179       15      65536 mmcblk0p15
     179       16    1413120 mmcblk0p16
     179       32       4096 mmcblk0rpmb

   2+16  1|shell@Citaq_32:/ $ cat proc/partitions
    major minor  #blocks  name

     254        0     520912 zram0
     179        0   15388672 mmcblk0
     179        1       4096 mmcblk0p1
     179        2       4096 mmcblk0p2
     179        3       4096 mmcblk0p3
     179        4      16384 mmcblk0p4
     179        5      16384 mmcblk0p5
     179        6      32768 mmcblk0p6
     179        7      32768 mmcblk0p7
     179        8     114688 mmcblk0p8
     179        9     131072 mmcblk0p9
     179       10       4096 mmcblk0p10
     179       11    1572864 mmcblk0p11
     179       12      16384 mmcblk0p12
     179       13       4096 mmcblk0p13
     179       14    4194304 mmcblk0p14
     179       15      65536 mmcblk0p15
     179       16    9166848 mmcblk0p16
     179       32       4096 mmcblk0rpmb

     */
    
    
/*    2|rk3288:/ $ cat /proc/partitions
    major minor  #blocks  name

       1        0       8192 ram0
       1        1       8192 ram1
       1        2       8192 ram2
       1        3       8192 ram3
       1        4       8192 ram4
       1        5       8192 ram5
       1        6       8192 ram6
       1        7       8192 ram7
       1        8       8192 ram8
       1        9       8192 ram9
       1       10       8192 ram10
       1       11       8192 ram11
       1       12       8192 ram12
       1       13       8192 ram13
       1       14       8192 ram14
       1       15       8192 ram15
     254        0    1022060 zram0
     179        0    7636992 mmcblk0
     179        1    7632896 mmcblk0p1
     179       32   15388672 mmcblk2
     179       33       4096 mmcblk2p1
     179       34       4096 mmcblk2p2
     179       35       4096 mmcblk2p3
     179       36      16384 mmcblk2p4
     179       37      32768 mmcblk2p5
     179       38      32768 mmcblk2p6
     179       39      65536 mmcblk2p7
     179       40     114688 mmcblk2p8
     179       41       4096 mmcblk2p9
     179       42     524288 mmcblk2p10
     179       43    2097152 mmcblk2p11
     179       44      16384 mmcblk2p12
     179       45     262144 mmcblk2p13
     179       46     262144 mmcblk2p14
     179       47        512 mmcblk2p15
     179       48   11939328 mmcblk2p16
     179      128       4096 mmcblk2rpmb
     179       96       4096 mmcblk2boot1
     179       64       4096 mmcblk2boot0
     253        0   11939328 dm-0*/

    
    public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
 }
    
/*	public static String getTotalRam(){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }*/
    
	/*显示ROM的可用和总容量，ROM相当于电脑的C盘*/  
    public String showROMInfo(){  
        File file=Environment.getDataDirectory();   
        StatFs statFs=new StatFs(file.getPath());    
        long blockSize=statFs.getBlockSize();    
        long totalBlocks=statFs.getBlockCount();    
        long availableBlocks=statFs.getAvailableBlocks();    
            
        String[] total=fileSize(totalBlocks*blockSize);    
        String[] available=fileSize(availableBlocks*blockSize);   
        
        Log.i(TAG, "ROM "+available[0]+available[1]+"/"+total[0]+total[1]) ;  
        
        return "ROM "+available[0]+available[1]+"/"+total[0]+total[1];
    }  
    
    
    
    /*显示SD卡的可用和总容量，SD卡就相当于电脑C盘以外的硬盘*/  
    private void showSDInfo(){  
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){    
            File file=Environment.getExternalStorageDirectory();  //  /mnt/internal_sd
            StatFs statFs=new StatFs(file.getPath());    
            long blockSize=statFs.getBlockSize();    
            long totalBlocks=statFs.getBlockCount();    
            long availableBlocks=statFs.getAvailableBlocks();    
                
            String[] total=fileSize(totalBlocks*blockSize);    
            String[] available=fileSize(availableBlocks*blockSize);    
                
            Log.i(TAG, "SD "+available[0]+available[1]+"/"+total[0]+total[1]) ;   
        }else {    
            
            Log.i(TAG, "SD card removed") ;   
        }    
    }  
    /*返回为字符串数组[0]为大小[1]为单位KB或者MB*/    
    private String[] fileSize(long size){    
        String str="";    
        if(size>=1024){    
            str="KB";    
            size/=1000;    
            if(size>=1024){    
                str="MB";    
                size/=1024;    
            }    
            if(size>=1024){    
                str="GB";    
                size/=1024;    
            } 
        }    
        /*将每3个数字用,分隔如:1,000*/    
        DecimalFormat formatter=new DecimalFormat();    
        formatter.setGroupingSize(3);    
        String result[]=new String[2];    
        result[0]=formatter.format(size);    
        result[1]=str;    
        return result;    
    }    
}
