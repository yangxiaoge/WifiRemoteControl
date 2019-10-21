package com.yjn.wifiremotecontrol.socket;

import android.graphics.Point;
import android.util.Log;

import com.blankj.utilcode.util.BusUtils;
import com.yjn.wifiremotecontrol.event.EventTAGConstants;
import com.yjn.wifiremotecontrol.service.ControlService;
import com.yjn.wifiremotecontrol.util.ScreenUtils;
import com.yjn.wifiremotecontrol.util.TouchUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <pre>
 *     author: Bruce_Yang
 *     blog  : https://yangjianan.gitee.io
 *     time  : 2019/10/21
 *     desc  : socket
 * </pre>
 */
public class SocketIoManager {
    public static final String TAG = SocketIoManager.class.getSimpleName();
    public static final int PORT = 8888;

    private SocketIoManager() {
    }

    private static class SINGLE_TON {
        private static SocketIoManager INSTANCE = new SocketIoManager();
    }

    public static SocketIoManager getInstance() {
        return SINGLE_TON.INSTANCE;
    }

    public ServerSocket serverSocket;
    private Socket mSocket;
    public BufferedOutputStream outputStream;

    public boolean mSocketReady;

    /**
     * 开启服务server
     * 客户端连接上之后立刻上传图片
     */
    public synchronized void startSocketIo() {
        try {
            Log.i(TAG, "init: >>>>>start>>>>>");
            serverSocket = new ServerSocket(PORT);
            while (serverSocket != null && !serverSocket.isClosed()) {
                Log.i(TAG, "init: >>>>> listen");
                try {
                    mSocket = serverSocket.accept();
                    outputStream = new BufferedOutputStream(mSocket.getOutputStream());
                    Log.i(TAG, "init: >>>>> accepted");
                    mSocketReady = true;
                    read(mSocket);
                    //开始上传屏幕
                    BusUtils.post(EventTAGConstants.REMOTE_CONTROL_COMMAND,
                            new ControlService.ControlCommand(true, ControlService.ControlCommand.jpeg));
                } catch (Exception e) {
                    mSocketReady = false;
                    Log.i(TAG, "init: " + e.getMessage());
                    //serverSocket = new ServerSocket(PORT);
                    //开始上传屏幕
                    BusUtils.post(EventTAGConstants.REMOTE_CONTROL_COMMAND,
                            new ControlService.ControlCommand(false, ControlService.ControlCommand.jpeg));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "startSocketIo: 受控端socket服务开启失败");
        }

    }

    public void releaseClient() {
        mSocketReady = false;
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseServer() {
        releaseClient();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void read(final Socket socket) {

        new Thread() {
            private final String DOWN = "down";
            private final String MOVE = "move";
            private final String UP = "up";

            private final String MENU = "menu";
            private final String HOME = "home";
            private final String BACK = "back";
            private final String RECENT = "recent";
            private final String POWER = "power";
            private final String volumeincrease = "volumeincrease";
            private final String volumedecrease = "volumedecrease";

            private final String DEGREE = "degree";

            @Override
            public void run() {
                super.run();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            Log.i(TAG, "run: socket closed....");
                            return;
                        }
                        Log.i(TAG, "run: order " + line);
                        if (line.startsWith(DOWN)) {
                            handlerDown(line.substring(DOWN.length() + 1));
                        } else if (line.startsWith(MOVE)) {
                            handlerMove(line.substring(MOVE.length() + 1));
                        } else if (line.startsWith(UP)) {
                            handlerUp(line.substring(UP.length() + 1));
                        } else if (line.startsWith(MENU)) {
                            TouchUtils.menu();
                        } else if (line.startsWith(HOME)) {
                            TouchUtils.home();
                        } else if (line.startsWith(BACK)) {
                            TouchUtils.back();
                        } else if (line.startsWith(RECENT)) {
                            TouchUtils.recent();
                        } else if (line.startsWith(POWER)) {
                            TouchUtils.power();
                        } else if (line.startsWith(volumeincrease)) {
                            TouchUtils.volumeincrease();
                        } else if (line.startsWith(volumedecrease)) {
                            TouchUtils.volumedecrease();
                        } else if (line.startsWith(DEGREE)) {
                            ControlService.scale = Float.parseFloat(line.substring(DEGREE.length() + 1)) / 100;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }.start();
    }

    private void handlerUp(String line) {
        Point point = getXY(line);
        if (point != null) {
            TouchUtils.touchUp(point.x, point.y);
        }
    }

    private void handlerMove(String line) {
        Point point = getXY(line);
        if (point != null) {
            TouchUtils.touchMove(point.x, point.y);
        }
    }

    private void handlerDown(String line) {
        Point point = getXY(line);
        if (point != null) {
            TouchUtils.touchDown(point.x, point.y);
        }
    }


    private Point getXY(String nums) {
        try {
            Point point = new Point(ScreenUtils.getScaledDisplaySize().x, ScreenUtils.getScaledDisplaySize().y);
            String[] s = nums.split("#");
            float scaleX = Float.parseFloat(s[0]);
            float scaleY = Float.parseFloat(s[1]);
            point.x = (int) (point.x * scaleX);
            point.y = (int) (point.y * scaleY);
            Log.i(TAG, "getXY: pint = " + point.toString());
            return point;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return null;
    }
}
