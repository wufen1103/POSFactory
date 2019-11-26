package com.citaq.citaqfactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


public class BootBroadcastReceiver extends BroadcastReceiver {

        static final String action_boot="android.intent.action.BOOT_COMPLETED";
     
        
        Context ctx;
        
        @Override
        public void onReceive(Context context, Intent intent) {
            ctx = context;
//            android.os.Debug.waitForDebugger();
            
            
            SharedPreferences sharedPreferences = context.getSharedPreferences("info2", Context.MODE_PRIVATE);
            
            boolean reboot  = sharedPreferences.getBoolean("reboot", false);
            
            
            if (reboot && intent.getAction().equals(action_boot)){
                Log.i("citaq", "boot.....");
//                
              Intent ootStartIntent=new Intent(context,VideoAcivity.class);
              ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              ootStartIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
              context.startActivity(ootStartIntent);
            }
     
        }
        

}
