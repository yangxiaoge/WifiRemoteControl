package com.yjn.wifiremotecontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.service.ControlService;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = findViewById(R.id.ip);
        ServiceUtils.startService(ControlService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        msg.setText("本机ip " + getIP() + "\n\n端口 " + SocketIoManager.PORT);
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

    public void start(View view) {
        if (ServiceUtils.isServiceRunning(ControlService.class)) {
            if (SocketIoManager.getInstance().serverSocket == null ||
                    SocketIoManager.getInstance().serverSocket.isClosed()) {
                //开启服务
                SocketIoManager.getInstance().startSocketIo();
            }
        } else {
            ServiceUtils.startService(ControlService.class);
        }
    }

    public void stop(View view) {
        ServiceUtils.stopService(ControlService.class);
    }
}
