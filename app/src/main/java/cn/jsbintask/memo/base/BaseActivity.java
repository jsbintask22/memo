package cn.jsbintask.memo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 14:53
 */

public abstract class BaseActivity extends AppCompatActivity {
    /*Butterknife绑定器，该activity销毁时要取消绑定，避免内存泄漏  */
    private Unbinder mUnbinder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        mUnbinder = ButterKnife.bind(this);
        /*Base activity中的公共的方法，因为每个类都需要初始化，所以在基类中定义 */
        initView();
        initData();
        setListener();
    }

    protected abstract void setListener();

    protected abstract void initView();

    protected abstract void initData();

    public abstract int getContentView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
