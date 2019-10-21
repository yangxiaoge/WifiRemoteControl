package com.yjn.wifiremotecontrol.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.blankj.utilcode.util.BusUtils;
import com.yjn.wifiremotecontrol.MyApplication;
import com.yjn.wifiremotecontrol.R;
import com.yjn.wifiremotecontrol.event.EventTAGConstants;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;
import com.yjn.wifiremotecontrol.util.ScreenUtils;
import com.yjn.wifiremotecontrol.util.ThreadPoolUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 远程控制后台服务
 * 开启手机坐标，方便开发调试：adb shell settings put system pointer_location 1
 */
public class ControlService extends Service {
    public static final String TAG = ControlService.class.getSimpleName();
    public static final int SERVICE_ID = 20191021;

    //图片缩放比例， 宽度超过720的都按照scale缩放成720
    public static float scale = 1f;
    //图片质量压缩
    private int quality = 20;
    //宽度最高480
    private int targetWidthSize = 480;
    //每秒几帧图片
    private int fpsBitmap = 5;
    //web端需要的图片格式
    private boolean isWebp = false;

    //down#x#y move#x#y up#x#y click#x#y  back home recent power volumeincrease volumedecrease
    private static final String DOWN = "down";
    private static final String MOVE = "move";
    private static final String UP = "up";
    private static final String CLICK = "click";

    private static final String MENU = "menu";
    private static final String HOME = "home";
    private static final String BACK = "back";
    private static final String RECENT = "recent";
    private static final String POWER = "power";
    private static final String VOLUMEINCREASE = "volumeincrease";
    private static final String VOLUMEDECREASE = "volumedecrease";

    private static final String DEGREE = "DEGREE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BusUtils.register(this);
        Log.i(TAG, "onCreate: 远程控制后台服务");

        startForeground();

        //开启服务端start socket server
        new Thread(() -> SocketIoManager.getInstance().startSocketIo()).start();
    }

    @Override
    public void onDestroy() {
        BusUtils.unregister(this);
        SocketIoManager.getInstance().releaseServer();
        super.onDestroy();
    }

    /**
     * 设置前端状态
     */
    private void startForeground() {
        Notification.Builder builder = new Notification.Builder(this);
        PendingIntent contentIndent = null;
        builder.setContentIntent(contentIndent).setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("ip:" + getIP() + " port:" + SocketIoManager.PORT);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(SERVICE_ID, notification);
    }

    private String getIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 开启/远比远程控制
     *
     * @param command true:开启控屏  false:关闭控屏
     */
    @BusUtils.Bus(tag = EventTAGConstants.REMOTE_CONTROL_COMMAND)
    public void serviceStatus(ControlCommand command) {
        Log.i(TAG, "serviceStatus: status = " + command.status + " isWebp = " + command.isWebp);
        if (command.status) {
            toast("开启上传服务");
            //开启远程空控制，上传截屏
            uploadScreenBySocket();
        } else {
            toast("关闭上传服务");
            //关闭远程控制,给个加状态
            SocketIoManager.getInstance().releaseClient();
        }
    }

    private void toast(final String msg) {
        MyApplication.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ControlService.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 上传截屏
     */
    private synchronized void uploadScreenBySocket() {
        ThreadPoolUtils.execute(() -> {
            try {
                Log.i(TAG, "run: 开始远程控制start");
                toast("开始远程控制开始");
                //远程控制已开启
                while (SocketIoManager.getInstance().mSocketReady) {
                    ThreadPoolUtils.execute(() -> {
                        try {
                            compressAndUpload();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    //控制每秒上传几帧图片
                    Thread.sleep(1000 / fpsBitmap);
                }
                Log.i(TAG, "run: 此次远程控制结束end");
                toast("远程控制结束");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "run: 异常啦 ", e);
            }
        });
    }

    private void compressAndUpload() throws Exception {
        Bitmap bitmap = ScreenUtils.screenshot();
        //宽高压缩   ，ScreenUtils.screenshot截屏时直接比例压缩
        bitmap = Bitmap.createScaledBitmap(bitmap, ScreenUtils.getScaledDisplaySize().x,
                ScreenUtils.getScaledDisplaySize().y, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //压缩质量20，为了省流量压缩成webp比jpeg耗时，因此页面会有0.5秒的延时感。
        bitmap.compress(isWebp ? Bitmap.CompressFormat.WEBP : Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);

        sendBytes(byteArrayOutputStream);
    }

    private synchronized void sendBytes(ByteArrayOutputStream byteArrayOutputStream) {
        if (!SocketIoManager.getInstance().mSocketReady) {
            Log.i(TAG, "sendBytes: 无连接的客户端");
            return;
        }
        BufferedOutputStream outputStream = SocketIoManager.getInstance().outputStream;
        try {
            final int VERSION = 2;
            outputStream.write(VERSION);
            writeInt(outputStream, byteArrayOutputStream.size());
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
        } catch (Throwable e) {
            e.printStackTrace();
            SocketIoManager.getInstance().mSocketReady = false;
            Log.i(TAG, "sendBytes: " + e.getMessage());
        }
    }

    private void writeInt(OutputStream outputStream, int v) throws IOException {
        outputStream.write(v >> 24);
        outputStream.write(v >> 16);
        outputStream.write(v >> 8);
        outputStream.write(v);
    }

    public static class ControlCommand {
        private boolean status;
        private boolean isWebp;
        public static String webp = "webp";
        public static String jpeg = "jpeg";

        public ControlCommand(boolean start, String webpOrJpeg) {
            this.status = start;
            if (TextUtils.isEmpty(webpOrJpeg)) {
                this.isWebp = false;
            } else {
                this.isWebp = !webpOrJpeg.equalsIgnoreCase(jpeg);
            }

        }
    }
}
