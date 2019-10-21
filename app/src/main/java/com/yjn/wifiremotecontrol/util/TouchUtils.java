package com.yjn.wifiremotecontrol.util;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.view.InputDeviceCompat;

import java.lang.reflect.Method;

public class TouchUtils {

    public static InputManager sIm;
    public static Method sInjectInputEventMethod;
    public static long downTime;

    static {

        try {
            sIm = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
            sInjectInputEventMethod =
                    InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void touchUp(float clientX, float clientY) {
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, KeyEvent.ACTION_UP, downTime,
                SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    public static void touchMove(float clientX, float clientY) {
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, KeyEvent.ACTION_MULTIPLE, downTime,
                SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    public static void touchDown(float clientX, float clientY) {
        downTime = SystemClock.uptimeMillis();
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, KeyEvent.ACTION_DOWN, downTime, downTime, clientX,
                clientY, 1.0f);

    }

    public static void menu() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_MENU);
    }

    public static void back() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_BACK);
    }

    public static void home() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_HOME);
    }

    public static void recent() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_APP_SWITCH);
    }

    public static void power() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_POWER);
    }

    public static void volumeincrease() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_VOLUME_UP);
    }

    public static void volumedecrease() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    public static void injectMotionEvent(int inputSource, int action,
                                         long downTime, long eventTime, float x, float y, float pressure) {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        try {
            sInjectInputEventMethod.invoke(sIm, new Object[]{event, 0});
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void sendKeyEvent(int inputSource, int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, inputSource));
        injectKeyEvent(new KeyEvent(now, now, 1, keyCode, 0, 0, -1, 0, 0, inputSource));
    }

    public static void injectKeyEvent(KeyEvent event) {
        try {
            sInjectInputEventMethod.invoke(sIm, new Object[]{event, 0});
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
