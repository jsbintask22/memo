package cn.jsbintask.memo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.jsbintask.memo.dao.EventDao;
import cn.jsbintask.memo.entity.Event;
import cn.jsbintask.memo.ui.activity.ClockActivity;
import cn.jsbintask.memo.util.WakeLockUtil;

/**
 * @see cn.jsbintask.memo.service.ClockService
 */
public class ClockReceiver extends BroadcastReceiver {
    private static final String TAG = "ClockReceiver";
    public static final String EXTRA_EVENT_ID = "extra.event.id";
    public static final String EXTRA_EVENT_REMIND_TIME = "extra.event.remind.time";
    public static final String EXTRA_EVENT = "extra.event";
    private EventDao mEventDao = EventDao.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        WakeLockUtil.wakeUpAndUnlock();
        postToClockActivity(context, intent);
    }

    private void postToClockActivity(Context context, Intent intent) {
        Intent i = new Intent();
        i.setClass(context, ClockActivity.class);
        i.putExtra(EXTRA_EVENT_ID, intent.getIntExtra(EXTRA_EVENT_ID, -1));
        Event event = mEventDao.findById(intent.getIntExtra(EXTRA_EVENT_ID, -1));
        if (event == null) {
            return;
        }
        i.putExtra(EXTRA_EVENT_REMIND_TIME, intent.getStringExtra(EXTRA_EVENT_REMIND_TIME));
        i.putExtra(EXTRA_EVENT, event);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public ClockReceiver() {
        super();
        Log.d(TAG, "ClockReceiver: Constructor");
    }
}
