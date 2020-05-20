// filename: CallbackBundle.java
package com.printer.util;

//简单的Bundle参数回调接口
public interface CallbackSerial {
	abstract void onDataReceived(final byte[] buffer, final int size);

}
