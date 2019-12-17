/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

//	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

	/**
	 * 打开串口
	 *
	 * @param device   串口设备文件
	 * @param baudrate 波特率
	 * @param stopBits  停止位，1 或 2  （默认 1）
	 * @param dataBits 数据位，5 ~ 8  （默认8）
	 * @param parity   奇偶校验，0 None（默认）； 1 Odd(奇）； 2 Even（偶）
	 * @param flowCon  标记 0（默认）
	 * @throws SecurityException
	 * @throws IOException
	 */
	public SerialPort(File device, int baudrate, int stopBits,
                      int dataBits, int parity, boolean flowCon) throws SecurityException, IOException {
		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/xbin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

//		mFd = open(device.getAbsolutePath(), baudrate, flags);
		mFd = open(device.getAbsolutePath(), baudrate, stopBits, dataBits, parity, flowCon);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
//	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native static FileDescriptor open(String path, int baudrate, int stopBits,
											 int dataBits, int parity, boolean flowCon);
	public native void close();
	static {
		System.loadLibrary("SerialPort");
	}
}
