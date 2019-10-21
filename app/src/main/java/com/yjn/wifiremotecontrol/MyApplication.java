package com.yjn.wifiremotecontrol;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.service.BatteryBroadcastReceiver;
import com.yjn.wifiremotecontrol.service.ControlService;
import com.yjn.wifiremotecontrol.service.ScreenReceiver;
import com.yjn.wifiremotecontrol.service.UpgradeReceiver;
import com.yjn.wifiremotecontrol.util.ServiceHelper;

/**
 * <pre>
 *     author: Bruce_Yang
 *     blog  : https://yangjianan.gitee.io
 *     time  : 2019/10/21
 *     desc  : Application
 * </pre>
 */
public class MyApplication extends Application {
    public static final Handler HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        //锁屏，开屏广播监听
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            registerReceiver(new UpgradeReceiver(), filter);
        }

        IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new ScreenReceiver(), filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Intent.ACTION_POWER_CONNECTED);
        filter1.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(new BatteryBroadcastReceiver(), filter1);

        NetworkUtils.registerNetworkStatusChangedListener(new NetworkUtils.OnNetworkStatusChangedListener() {
            @Override
            public void onDisconnected() {
                Log.i("MyApplication", "onDisconnected: 网络断开");
                ServiceHelper.dealService();
            }

            @Override
            public void onConnected(NetworkUtils.NetworkType networkType) {
                Log.i("MyApplication", "onDisconnected: 网络连接");
                ServiceHelper.dealService();
            }
        });
    }
}
