package com.yjn.wifiremotecontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;

/**
 * <pre>
 *     author: Bruce_Yang
 *     blog  : https://yangjianan.gitee.io
 *     time  : 2019/10/21
 *     desc  : 锁屏，开屏广播
 * </pre>
 */
public class ScreenReceiver extends BroadcastReceiver {
    public static final String TAG = ScreenReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());
        if (ServiceUtils.isServiceRunning(ControlService.class)) {
            //开锁屏幕
            if (SocketIoManager.getInstance().serverSocket == null ||
                    SocketIoManager.getInstance().serverSocket.isClosed()) {
                //开启服务
                SocketIoManager.getInstance().startSocketIo();
            }
        } else {
            ServiceUtils.startService(ControlService.class);
        }
    }
}
