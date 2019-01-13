package cn.jsbintask.memo;

import android.app.Application;
import android.content.Context;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 14:43
 */
//定义一个自己的上下文，便于获取context（DB操作需要）
public class MemoApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
