package cn.jsbintask.memo.util;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/26 15:00
 */

public class StringUtil {
    public static String getMultiNumber(int number) {
        return number < 10 ? "0" + number : Integer.toString(number);
    }

    public static String getLocalMonth(int month) {
        return getMultiNumber(month + 1);
    }

    public static boolean isBlank(String src) {
        return src == null || src.trim().length() == 0;
    }
}
