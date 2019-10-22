package com.yjn.wifiremotecontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.yjn.wifiremotecontrol.service.ControlService;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;

public class MainActivity extends AppCompatActivity {
    private TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name) + "for局域网  v" + BuildConfig.VERSION_NAME);
        }
        msg = findViewById(R.id.ip);
        ServiceUtils.startService(ControlService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        msg.setText("本机ip " + NetworkUtils.getIPAddress(true) + "\t\t端口 " + SocketIoManager.PORT);
    }

    public void start(View view) {
        if (ServiceUtils.isServiceRunning(ControlService.class)) {
            if (SocketIoManager.getInstance().serverSocket == null ||
                    SocketIoManager.getInstance().serverSocket.isClosed()) {
                //开启服务
                SocketIoManager.getInstance().startSocketIo();
            }else {
                ToastUtils.showShort("无需重复开启服务");
            }
        } else {
            ServiceUtils.startService(ControlService.class);
        }
    }

    public void stop(View view) {
        ServiceUtils.stopService(ControlService.class);
    }
}
