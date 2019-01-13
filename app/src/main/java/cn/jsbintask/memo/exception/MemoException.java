package cn.jsbintask.memo.exception;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 20:46
 */

//自定义异常，便于统一处理
public class MemoException extends RuntimeException {
    public MemoException(Throwable t) {
        super(t);
    }

    public MemoException(){}
}
