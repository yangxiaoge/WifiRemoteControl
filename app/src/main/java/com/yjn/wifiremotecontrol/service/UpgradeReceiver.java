package com.yjn.wifiremotecontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;
import com.yjn.wifiremotecontrol.util.ServiceHelper;

/**
 * <pre>
 *     author: Bruce_Yang
 *     blog  : https://yangjianan.gitee.io
 *     time  : 2019/10/21
 *     desc  : 应用升级广播
 * </pre>
 */
public class UpgradeReceiver extends BroadcastReceiver {
    public static final String TAG = UpgradeReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());
        ServiceHelper.dealService();
    }
}