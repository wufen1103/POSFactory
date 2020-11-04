/*
 * Copyright 2015 Umbrela Smart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.citaq.citaqfactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import co.umbrela.tools.stm32dfuprogrammer.Dfu;
import co.umbrela.tools.stm32dfuprogrammer.Usb;

public class PrinterUpgradeActivity extends Activity implements
        Handler.Callback, Usb.OnUsbChangeListener, Dfu.DfuListener {
    private static final String TAG = "PrinterUpgradeActivity";
    private Usb usb;
    private Dfu dfu;
    Button selfPrint,btnInit;

    private TextView status;
    private TextView firmwareFileName;
    private TextView upgradeResults;
    protected String filepath = null;
    protected String filename;

    Handler mHandler;

    Context mContext;
    private static byte[] enterDfuCommand = new byte[]{0x1D, 0x75, 0x55, (byte) 0xAA};
    private static byte[] printSelf = new byte[] { 0x1D, 0x28, 0x41, 0x02, 0x00, 0x00, 0x002};  //F15
    private static byte[] printInit = new byte[] { 0x1D, 0x47, 0x08, 0x01};  //1DH 47H 08H 01H
    private static byte[] printVersion = new byte[] { 0x1D, 0x49, 0x41};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printerupgrade);

        mContext = this;

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1001:
//                        dfu.massErase();
//                        dfu.program();
//                        dfu.leaveDfuMode();
                        break;
                    case 1002:  //status
                        status.append(msg.obj.toString());
                        break;
                    case 1003:  //results
                         upgradeResults.setText(msg.obj.toString());
                        break;
                    default:
                        break;
                }


                super.handleMessage(msg);
            }

        };

        dfu = new Dfu(Usb.USB_VENDOR_ID, Usb.USB_PRODUCT_ID);
        dfu.setListener(this);

        Button openFirmwareFile = (Button) findViewById(R.id.btnOpenFirmwareFile);
        openFirmwareFile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        Button autoOperation = (Button) findViewById(R.id.btnAutoOperation);
        autoOperation.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(usb.getUsbDevice().getProductId() == Usb.USB_PRINTER_PRODUCT_ID) {
                    byte[] enterDfuCommand = new byte[]{0x1D, 0x75, 0x55, (byte) 0xAA};
                    usb.sentData(enterDfuCommand);
                    status.append("\nWait the printer change to DFU mode and try again.....\n");
                    return;
                }

                dfu.massErase();
                dfu.program();
                dfu.leaveDfuMode();*/
                if(filepath == null){
                    Toast.makeText(mContext, R.string.str_no_firmware, Toast.LENGTH_SHORT).show();
                    return;
                }
                upgradeResults.setText("Please Wait...");
                new Thread() {
                    public void run() {
                        if (usb.getUsbDevice().getProductId() == Usb.USB_PRINTER_PRODUCT_ID) {
                            Message mMessage = new Message();
                            mMessage.what = 1002;
                            mMessage.obj = "\nWait the printer change to DFU mode .....\n";
                            mHandler.sendMessage(mMessage);
                            boolean isOK = usb.sentData(enterDfuCommand);
                            Log.i(TAG, "usb.sentData(enterDfuCommand): " + isOK);
                            try {
//                                Thread.sleep(1500);  //ok
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            Message mMessage2 = new Message();
//                            mMessage2.what = 1001;
//                            mMessage2.obj = "\nin DFU mode Now.....\n";
//                            mHandler.sendMessage(mMessage2);
                            dfu.massErase();
                            dfu.program();
                            dfu.leaveDfuMode();
                        }
                    }

                }.start();
            }
        });

        status = (TextView) findViewById(R.id.status);

        Button massErase = (Button) findViewById(R.id.btnMassErase);
        massErase.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dfu.massErase();
            }
        });

        Button program = (Button) findViewById(R.id.btnProgram);

        program.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dfu.program();
            }
        });

        Button forceErase = (Button) findViewById(R.id.btnForceErase);
        forceErase.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dfu.fastOperations();
            }
        });

        Button verify = (Button) findViewById(R.id.btnVerify);
        verify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dfu.verify();
            }
        });

        Button enterDfu = (Button) findViewById(R.id.btnEnterDFU);
        enterDfu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Outputs.enterDfuMode();
                 usb.sentData(enterDfuCommand);
            }
        });

        selfPrint = (Button) findViewById(R.id.btnSelfPrint);
        selfPrint.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Outputs.enterDfuMode();
                usb.sentData(printSelf);
            }
        });

        btnInit = (Button) findViewById(R.id.btnInit);
        btnInit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Outputs.enterDfuMode();
                usb.sentData(printInit);
            }
        });

        Button leaveDfu = (Button) findViewById(R.id.btnLeaveDFU);
        leaveDfu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Outputs.leaveDfuMode();
                dfu.leaveDfuMode();
            }
        });
        Button releaseReset = (Button) findViewById(R.id.btnReleaseReset);
        releaseReset.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Outputs.enterNormalMode();
            }
        });

        firmwareFileName = (TextView) findViewById(R.id.tvFirmwareFileName);
        upgradeResults = (TextView) findViewById(R.id.tvUpgradeResults);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Setup USB */
        usb = new Usb(this);
        usb.setUsbManager((UsbManager) getSystemService(Context.USB_SERVICE));
        usb.setOnUsbChangeListener(this);

        // Handle two types of intents. Device attachment and permission
        registerReceiver(usb.getmUsbReceiver(), new IntentFilter(Usb.ACTION_USB_PERMISSION));
        registerReceiver(usb.getmUsbReceiver(), new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(usb.getmUsbReceiver(), new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));


        // Handle case where USB device is connected before app launches;
        // hence ACTION_USB_DEVICE_ATTACHED will not occur so we explicitly call for permission
        usb.requestPermission(this, Usb.USB_VENDOR_ID, Usb.USB_PRODUCT_ID);

        if (!usb.isConnected()) {
            usb.requestPermission(this, Usb.USB_PRINTER_VENDOR_ID, Usb.USB_PRINTER_PRODUCT_ID);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* USB */
        boolean hasUsb = dfu.setUsb(null);
        if (hasUsb) {
            usb.release();
            try {
                unregisterReceiver(usb.getmUsbReceiver());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                /* Already unregistered */
            }
        }
    }

    @Override
    public void onStatusMsg(int tag, String msg) {
        // TODO since we are appending we should make the TextView scrollable like a log
        //status.append(msg);

        Message mMessage = new Message();
        mMessage.what = tag;
        mMessage.obj = msg;
        mHandler.sendMessage(mMessage);
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }

    @Override
    public void onUsbConnected() {
        final String deviceInfo = usb.getDeviceInfo(usb.getUsbDevice());
        //status.setText(deviceInfo);
        status.append('\n'+ deviceInfo + '\n');
        dfu.setUsb(usb);
    }

    Dialog dialog = null;

    protected void showDialog() {
        if (OpenFileDialog.isDialogCreate &&
                OpenFileDialog.FileSelectView.getCurrentPath().equals(OpenFileDialog.sRoot)) {

            dismissDialog();

        }

        if (dialog == null) {
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);    // 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);    //文件夹图标
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            dialog = OpenFileDialog.createDialog(this, getResources().getString(R.string.str_open_File), new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            filepath = bundle.getString("path");
                            firmwareFileName.setText(filepath); //"/storage/B4FE-5315/PT483CB1_V2.01.10_01.dfu"
                            dfu.setDfuFile(filepath);
                            dialog.dismiss();
                        }
                    },
                    ".dfu",
                    images);
            dialog.show();
        } else {
            if (!dialog.isShowing())
                dialog.show();
        }
    }

    protected void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;

    }

}
