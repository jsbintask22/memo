package cn.jsbintask.memo.util;

import android.widget.Toast;

import cn.jsbintask.memo.MemoApplication;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 17:08
 */
public class ToastUtil {
    public static void showToastShort(String msg) {
        Toast.makeText(MemoApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToastShort(int res) {
        Toast.makeText(MemoApplication.getContext(), MemoApplication.getContext().getString(res), Toast.LENGTH_SHORT).show();
    }
}
