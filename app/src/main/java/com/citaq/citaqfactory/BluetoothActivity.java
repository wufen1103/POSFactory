package com.citaq.citaqfactory;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.citaq.util.BlueToothDeviceStruct;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends Activity {
    protected static final String TAG = "BluetoothActivity";
    protected static final int DISCOVERY_TIMES = 5;

    private BluetoothAdapter bluetoothAdapter;
    ToggleButton tb_bluetooth;
    Button bt_search, bt_clear;
    TextView tv_show;
    List<BlueToothDeviceStruct> blueToothList = new ArrayList<BlueToothDeviceStruct>();
    String btdetail;
    FoundReceiver mFoundReceiver;
    int discovery_count = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        tb_bluetooth = findViewById(R.id.tb_bluetooth);
        tv_show = findViewById(R.id.tv_show);
        bt_search = findViewById(R.id.bt_search);
        bt_clear = findViewById(R.id.bt_clear);

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getBlueToothIsDiscovering()) {
                    Log.i(TAG,"bluetoothAdapter is Discovering");
                    cancelBlueToothDiscovery();
                }else{
                    Log.i(TAG,"bluetoothAdapter not Discovering");
                }

                discovery_count = 0;
                blueToothList.clear();
                startBlueToothDiscovery();

            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tv_show.setText("");
            }
        });

        tb_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if(arg1){
                    discovery_count = 0;
                    blueToothList.clear();
                    Log.i(TAG,"true");
                    openBlueTooth();
                }else{
                    Log.i(TAG,"false");
                    colseBlueTooth();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter mIntentFilter = new IntentFilter();
        //发现设备
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //开始扫描
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始扫描
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束扫描
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //设置广播接收器和安装过滤器
        mFoundReceiver = new FoundReceiver();
        registerReceiver(mFoundReceiver, mIntentFilter);

        if(getBlueToothState()){
            tb_bluetooth.setChecked(true);
            startBlueToothDiscovery();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mFoundReceiver);
        if(getBlueToothIsDiscovering()) {
            cancelBlueToothDiscovery();
        }

    }

    public boolean startBlueToothDiscovery(){
        return bluetoothAdapter.startDiscovery();
    }

    public boolean cancelBlueToothDiscovery(){
        return bluetoothAdapter.cancelDiscovery();
    }

    public boolean getBlueToothIsDiscovering() {
        // 获取蓝牙是否搜索
        return bluetoothAdapter.isDiscovering();
    }

    public boolean getBlueToothState() {
        // 获取蓝牙状态
        return bluetoothAdapter.isEnabled();
    }
    public boolean openBlueTooth() {
        if (getBlueToothState()) return true;
        // 打开蓝牙
        return bluetoothAdapter.enable();
    }
    public boolean colseBlueTooth() {
        if (!getBlueToothState()) return true;
        // 关闭蓝牙
        return bluetoothAdapter.disable();
    }

    private void isIterative(BluetoothDevice device, short rssi){
        BlueToothDeviceStruct mBlueToothDeviceStruct = new BlueToothDeviceStruct();
        mBlueToothDeviceStruct.setName(device.getName());
        mBlueToothDeviceStruct.setAddress(device.getAddress());
        mBlueToothDeviceStruct.setRssi(rssi);
        int i;
        for(i = 0; i < blueToothList.size(); i++){
            String name = blueToothList.get(i).getName();
            if(name != null && name.equals(mBlueToothDeviceStruct.getName())){
                break;
            }
        }
        if(i == blueToothList.size()){
            blueToothList.add(mBlueToothDeviceStruct);
            btdetail = mBlueToothDeviceStruct.toString() + "\n";
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_show.setText(tv_show.getText().toString() + btdetail);
                }
            });
        }
    }
    /**
     * 内部类：当找到一个远程蓝牙设备时执行的广播接收者
     * @author Administrator
     *
     */
    class FoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                // TODO Auto-generated method stub
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);//获取额外rssi值

                isIterative(device, rssi);

            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                tv_show.setText(tv_show.getText().toString() +"BluetoothAdapter.ACTION_DISCOVERY_STARTED\n");
                Log.i(TAG,"BluetoothAdapter.ACTION_DISCOVERY_STARTED");
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                tv_show.setText(tv_show.getText().toString() +"BluetoothAdapter.ACTION_DISCOVERY_FINISHED\n\n");
                Log.i(TAG,"BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
                discovery_count++;
                if(discovery_count < DISCOVERY_TIMES){
                    startBlueToothDiscovery();
                }
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,  BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "STATE_ON");
                        startBlueToothDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "STATE_TURNING_ON");
                        break;
                }
            }

        }
    }
}
