package com.yjn.wifiremotecontrol.util;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.yjn.wifiremotecontrol.event.EventTAGConstants;
import com.yjn.wifiremotecontrol.service.ControlService;
import com.yjn.wifiremotecontrol.socket.SocketIoManager;

/**
 * <pre>
 *     author: Bruce_Yang
 *     blog  : https://yangjianan.gitee.io
 *     time  : 2019/10/21
 *     desc  : 广播处理服务工具类
 * </pre>
 */
public class ServiceHelper {
    public static void dealService() {
        if (!ServiceUtils.isServiceRunning(ControlService.class)) {
            ServiceUtils.startService(ControlService.class);
        } else {
            if (NetworkUtils.isConnected()) {
                BusUtils.post(EventTAGConstants.NOTIFICATION_MSG,
                        "ip:" + NetworkUtils.getIPAddress(true) + "\t\tport:" + SocketIoManager.PORT);
                if (SocketIoManager.getInstance().serverSocket == null ||
                        SocketIoManager.getInstance().serverSocket.isClosed()) {
                    //开启服务
                    SocketIoManager.getInstance().startSocketIo();
                }
            } else {
                BusUtils.post(EventTAGConstants.NOTIFICATION_MSG,
                        "当前没有网络！");
            }
        }
    }
}
