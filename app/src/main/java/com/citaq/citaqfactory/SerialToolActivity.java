package com.citaq.citaqfactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.citaq.util.SerialPortManager;
import com.citaq.util.ShellUtils;
import com.printer.util.CallbackSerial;

import java.io.IOException;

import android_serialport_api.SerialPortFinder;

public class SerialToolActivity extends Activity {
    protected static final String TAG = "SerialToolActivity";

    private Spinner spinner_devices,spinner_baudrates;
    private ArrayAdapter<?> adapter_devices, adapter_baudrates;
    private String device, baudrate;
    private EditText et_data;
    private Button bt_send = null;
    private Button bt_clear_receive = null;
    private TextView tv_receive;

    private String[] mDevices;
    private String[] mBaudrates;
    ShellUtils mShellUtils;
    SerialPortManager mSerialPortManager = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialtool);

        initView();
        initSerial();

        try {
           Runtime.getRuntime().exec("cat /dev/ttyS1");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initView(){
        tv_receive= (TextView) findViewById(R.id.tv_receive);
        et_data = (EditText) findViewById(R.id.et_data);
        et_data.setText("ttyS3->ttyS1->ttyS3");

        bt_send = (Button) findViewById(R.id.bt_send);
        bt_clear_receive = (Button) findViewById(R.id.bt_clear_receive);

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = et_data.getText().toString();
                serialWrite(txt.getBytes());
            }
        });

        bt_clear_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_receive.setText("");
            }
        });


    }

    private void initSpinner(){
        spinner_devices = (Spinner) findViewById(R.id.spinner_devices);
        spinner_baudrates = (Spinner) findViewById(R.id.spinner_baudrates);

        //        adapter_devices= ArrayAdapter.createFromResource(this, R.array.baudrates, android.R.layout.simple_spinner_item);
        adapter_devices = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getDevice());
        adapter_devices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_devices.setAdapter(adapter_devices);
        spinner_devices.setSelection(0);

        adapter_baudrates= ArrayAdapter.createFromResource(this, R.array.baudrates, android.R.layout.simple_spinner_item);
        adapter_baudrates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_baudrates.setAdapter(adapter_baudrates);
        spinner_baudrates.setSelection(0);

        spinner_devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                device =((Spinner)parent).getSelectedItem().toString().trim();
                Log.v(TAG, "device = " + device);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_baudrates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baudrate =((Spinner)parent).getSelectedItem().toString().trim();
                Log.v(TAG, "baudrate = " + baudrate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initSerial(){
        mSerialPortManager = new SerialPortManager(this,SerialPortManager.SERIALPORT_TTYS3);
        mSerialPortManager.setCallback(new CallbackSerial() {
            @Override
            public void onDataReceived(final byte[] buffer, final int size) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        for(int i = 0; i < size; i++){
                            String s = Integer.toHexString((int)buffer[i]);//String.valueOf(((char)buffer[i]));
//                        tv_receive.append(s + ' ');

                        }
                        tv_receive.append(new String(buffer) + '\n');
                    }

                });
            }
        });
    }

    /**
     * 初始化设备列表
     */
    private String[] getDevice() {

        SerialPortFinder serialPortFinder = new SerialPortFinder();

        // 设备
        mDevices = serialPortFinder.getAllDevicesPath();
        if (mDevices.length == 0) {
            mDevices = new String[] {
                    getString(R.string.no_serial_device)
            };
        }
       return mDevices;
    }


    private  boolean serialWrite(byte[] cmd){
        boolean returnValue=true;
        try{

            mSerialPortManager.write(cmd);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            returnValue=false;

            // more 后返回 java.io.IOException: write failed: EBADF (Bad file number)

            initSerial();

            try{

                mSerialPortManager.write(cmd);
            }
            catch(Exception e)
            {
                ex.printStackTrace();
                returnValue=false;
            }

        }
        return returnValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mSerialPortManager != null)
            mSerialPortManager.destroy();

    }
}
