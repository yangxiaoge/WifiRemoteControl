package com.yjn.wifiremotecontrol.util;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.SurfaceControl;

import com.yjn.wifiremotecontrol.service.ControlService;

public class ScreenUtils {
    private static final Point sDisplaySize = new Point();

    /**
     * 宽高，永远是竖屏的（系统截屏时也是如此才正确）
     *
     * @return
     */
    public static Point getDisplaySize() {
        return new Point(sDisplaySize.x, sDisplaySize.y);
    }

    /**
     * 缩放后的Point，这样原始截屏screenshot时就是缩放后的图片，节省时间
     *
     * @return
     */
    public static Point getScaledDisplaySize() {
        return new Point((int) (sDisplaySize.x * ControlService.scale), (int) (sDisplaySize.y * ControlService.scale));
    }

    /**
     * 这里得到屏幕的宽高(永远是竖屏的宽高)
     */
    static {
        if (VERSION.SDK_INT >= 18) {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getInitialDisplaySize(0, sDisplaySize);
        } else if (VERSION.SDK_INT == 17) {
            DisplayInfo di =
                    IDisplayManager.Stub.asInterface((IBinder) ServiceManager.getService("display")).getDisplayInfo(0);
            sDisplaySize.x = di.logicalWidth;
            sDisplaySize.y = di.logicalHeight;
        } else {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getRealDisplaySize(sDisplaySize);
        }
    }

    /**
     * 截图： 系统返回的是竖屏图片，不管横竖屏。因此传递给系统的宽高是竖屏的宽高，否则有错！
     *
     * @return bitmap
     * @throws Exception </>
     */
    public static Bitmap screenshot() throws Exception {
        //   这里直接截屏时宽高压缩
        Point size = ScreenUtils.getDisplaySize();
        Bitmap b;
        if (VERSION.SDK_INT <= 17) {
            String surfaceClassName = "android.view.Surface";
            b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{int.class, int.class})
                    .invoke(null, new Object[]{size.x, size.y});

        } else if (VERSION.SDK_INT < 28) {
            b = SurfaceControl.screenshot(size.x, size.y);
        } else {
            b = SurfaceControl.screenshot(new Rect(0, 0, size.x, size.y), size.x, size.y, 0);
        }
        return b;
    }
}