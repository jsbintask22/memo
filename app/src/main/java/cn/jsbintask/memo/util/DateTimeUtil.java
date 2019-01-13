package cn.jsbintask.memo.util;

import cn.jsbintask.memo.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 15:53
 */

public class DateTimeUtil {
    public static String dateToStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT, Locale.CHINA);
        return sdf.format(date);
    }

    public static Date str2Date(String src)  {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT, Locale.CHINA);
        try {
            return sdf.parse(src);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean needClock(Date date, Date date2) {
       if (Math.abs(((double) (date.getTime() - date2.getTime())) / 1000) <= 1.00001) {
           return true;
       }
       return false;
    }

    public static boolean canClock(Date date) {
        if (Math.abs(date.getTime() - new Date().getTime()) < 5000) {
            return true;
        }
        return false;
    }
}
