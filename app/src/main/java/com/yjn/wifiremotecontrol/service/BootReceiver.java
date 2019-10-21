package com.yjn.wifiremotecontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;

public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());
        if (ServiceUtils.isServiceRunning(ControlService.class)) {
            //网络变化等等
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
