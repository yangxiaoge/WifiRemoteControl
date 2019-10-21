package android.view;

import android.graphics.Bitmap;

/**
 * Created by wanjian on 2017/4/5.
 */

public class Surface {
    public static Bitmap screenshot(int x, int y) {
        return null;
    }

    //默认0度
    public static final int ROTATION_0 = 0;
    //逆时针90
    public static final int ROTATION_90 = 1;
    //逆时针180
    public static final int ROTATION_180 = 2;
    //逆时针270
    public static final int ROTATION_270 = 3;
}
