package com.yjn.jetpack.controlgui_android;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lxj.xpopup.XPopup;
import com.lzf.easyfloat.EasyFloat;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private ImageView imageView;
    private LinearLayout configLine;
    private float scale = 1f;
    private Socket socket;
    private BufferedWriter writer;
    private boolean isMove = false;
    private int screenWidth;
    private int screenHeight;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //全屏
//        fullScreen();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        screenWidth = ScreenUtils.getAppScreenWidth();
        screenHeight = ScreenUtils.getAppScreenHeight();

        configLine = findViewById(R.id.config_line);

        imageView = findViewById(R.id.image);

        imageView.setOnTouchListener((v, event) -> {
            //imageView左上角坐标
            int[] location = new int[2];
            v.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    //按住事件发生后执行代码的区域
                    Log.i(TAG, "onCreate: DOWN");
                    new Thread(() -> {
                        try {
                            if (writer == null) return;
                            writer.write("down#" + ((event.getX() - location[0]) * 1.0f / v.getWidth()) + "#" + ((event.getY() - location[1]) * 1.0f / v.getHeight()));
                            writer.newLine();
                            writer.flush();
                            isMove = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    //移动事件发生后执行代码的区域
                    Log.i(TAG, "onCreate: MOVE");
                    new Thread(() -> {
                        try {
                            if (writer == null) return;

                            //这个组合可以实现“滑动”，也可以实现“长按”（在需要的地方按住后稍微拖动一点位置即可）
                            if (!isMove) {
                                isMove = true;
                                //writer.write("DOWN#" + ((event.getX() - location[0]) * 1.0f / v.getWidth()) + "#" + ((event.getY() - location[1]) * 1.0f / v.getHeight()));
                            } else {
                                writer.write("move#" + ((event.getX() - location[0]) * 1.0f / v.getWidth()) + "#" + ((event.getY() - location[1]) * 1.0f / v.getHeight()));
                            }
                            writer.newLine();
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    //松开事件发生后执行代码的区域
                    Log.i(TAG, "onCreate: UP");
                    new Thread(() -> {
                        try {
                            if (writer == null) return;
                            writer.write("up#" + ((event.getX() - location[0]) * 1.0f / v.getWidth()) + "#" + ((event.getY() - location[1]) * 1.0f / v.getHeight()));
                            writer.newLine();
                            writer.flush();
                            isMove = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                }
                default:
                    break;
            }
            return true;
        });

        EasyFloat.with(this)
                .setLayout(R.layout.floating_button)
                // 设置浮窗是否可拖拽
                .setDragEnable(true)
                // 设置浮窗的对齐方式和坐标偏移量
                .setGravity(Gravity.END, 0, 200)
                .show();

        findViewById(R.id.fab).setOnClickListener(view -> new XPopup
                .Builder(MainActivity.this)
                .hasShadowBg(false)
                .isRequestFocus(false)
                .atView(view)
                .asAttachList(new String[]{"连接", "断开", "back", "home", "menu", "recent", "power"},
                        new int[]{},
                        (position, text) -> {
                            switch (text) {
                                case "连接":
                                    startControl(null);
                                    break;
                                case "断开":
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "back":
                                    hardKey("back");
                                    break;
                                case "home":
                                    hardKey("home");
                                    break;
                                case "menu":
                                    hardKey("menu");
                                    break;
                                case "recent":
                                    hardKey("recent");
                                    break;
                                case "power":
                                    hardKey("power");
                                    break;
                            }
                        })
                .show());

    }

    @Override
    protected void onResume() {
        super.onResume();
//        fullScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全屏
     */
    private void fullScreen() {
        BarUtils.setStatusBarVisibility(this, false);
        BarUtils.setNavBarVisibility(this, false);
    }

    /**
     * socket连接远程设备
     *
     * @param view
     */
    public void startControl(View view) {
        String ip = ((EditText) findViewById(R.id.ip_et)).getText().toString();
        String port = ((EditText) findViewById(R.id.port_et)).getText().toString();
        read(ip, port);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 不退出程序，进入后台
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void read(final String ip, final String port) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 3000);
                    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Log.i(TAG, "run: 啊啊啊");
                    runOnUiThread(() -> toast("连接成功"));
                    runOnUiThread(() -> {
                        configLine.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        toast("连接成功");
                    });
                    byte[] bytes = null;
                    while (true) {
                        int version = inputStream.read();
                        if (version == -1) {
                            return;
                        }
                        int length = readInt(inputStream);
                        if (bytes == null) {
                            bytes = new byte[length];
                        }
                        if (bytes.length < length) {
                            bytes = new byte[length];
                        }
                        int read = 0;
                        while ((read < length)) {
                            read += inputStream.read(bytes, read, length - read);
                        }

                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        runOnUiThread(() -> imageView.setImageBitmap(
                                Bitmap.createScaledBitmap(bmp, (int) (screenWidth * scale),
                                        (int) (screenHeight * scale), false)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(() -> {
                        configLine.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        toast("连接断开");
                    });
                }
            }
        }.start();

    }

    private int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private void hardKey(String key) {
        new Thread(() -> {
            try {
                if (writer == null) return;
                writer.write(key);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void toast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
