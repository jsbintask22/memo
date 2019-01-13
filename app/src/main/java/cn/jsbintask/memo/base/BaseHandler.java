package cn.jsbintask.memo.base;

import android.os.Handler;
import android.os.Message;

import cn.jsbintask.memo.Constants;
import cn.jsbintask.memo.exception.MemoException;

import java.lang.ref.WeakReference;

import cn.jsbintask.memo.exception.MemoException;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 20:39
 */

public class BaseHandler extends Handler {
    /*定义为虚引用，防止内存泄漏：Handler持有引用，会造成内存泄漏  */
    private WeakReference<BaseActivity> mReference;

    public BaseHandler(BaseActivity baseActivity) {
        super();
        mReference = new WeakReference<BaseActivity>(baseActivity);
    }

    @Override
    public void handleMessage(Message msg) {
        /*Handler中公共的处理元素，处理成功还是失败 */
        if (msg.what == Constants.HANDLER_SUCCESS) {
            if (mReference.get() instanceof HandlerResultCallBack) {
                HandlerResultCallBack callBack = (HandlerResultCallBack) mReference.get();
                callBack.handlerSuccess(msg);
            }
        } else if (msg.what == Constants.HANDLER_FAILED) {
            if (mReference.get() instanceof HandlerResultCallBack) {
                HandlerResultCallBack callBack = (HandlerResultCallBack) mReference.get();
                callBack.handlerFailed((MemoException) msg.obj);
            }
        }
    }

    public interface HandlerResultCallBack {
        void handlerSuccess(Message msg);

        void handlerFailed(MemoException e);
    }
}
