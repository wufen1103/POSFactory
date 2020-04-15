package com.citaq.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Locale;
public class NetworkUtil {
    WifiManager WifiManager;

    /**
     * 只有连接过一次后才正常获取
     * @return
     */
    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    public static String getWifiMacAddress() {
        String str = "";
        String mac = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    mac = str.trim();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mac;
    }

    public static String getEthMacAddress() {
        String str = "";
        String mac = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    mac = str.trim();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mac;
    }


}
