---
title: [干货]Android入门完整项目：一个有定时提醒功能的备忘录
date: 2019-02-23 16:05
updated: 2019-02-23 16:05
tag:
  - java
  - android
---
![android](http://qiniu.jsbintask.cn/d396zy4-aff6fa96-0caa-44a3-87f2-a2f0bc0d1c61.jpg-blog_960_440.jpg)
## 介绍
今天给大家分享一个以前学习`android`时做的小项目，一个`带有定时提醒功能的备忘录`，主要用到`RecycleView`, `sqlite`, `butterknife`，效果图图下：
![android](https://raw.githubusercontent.com/jsbintask22/static/master/memo/memo.gif)

## 详细功能实现
### 建立db，编写db helper类
1. 新建一个常量类，包含所有操作db的语句，`ColumnContacts`:
```java
public class ColumnContacts {
    public static final String EVENT_TABLE_NAME = "event";
    public static final String EVENT_TITLE_COLUMN = "title";
    public static final String EVENT_CONTENT_COLUMN = "content";
    public static final String EVENT_CREATED_TIME_COLUMN = "created_time";
    public static final String EVENT_UPDATED_TIME_COLUMN = "updated_time";
    public static final String EVENT_REMIND_TIME_COLUMN = "remind_time";
    public static final String EVENT_IS_IMPORTANT_COLUMN = "is_important";
    public static final String EVENT_IS_CLOCKED = "is_clocked";
}
```
2. 新建一个`DBTemplate`,此处用到设计模式模板方法，所以还包含一个回调接口`DBCallbackk`
```java
public class DBTemplate<T> {
    private DBOpenHelper dbHelper;

    public DBTemplate() {
        dbHelper = new DBOpenHelper();
    }

    public T queryOne(String sql, DBCallback<T> callback, String...args) {
        T t = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null && cursor.moveToNext()) {
            t = callback.cursorToInstance(cursor);
            cursor.close();
        }

        return t;
    }

    public List<T> query(String sql, DBCallback<T> callback, String... args) {
        List<T> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                T t = callback.cursorToInstance(cursor);
                list.add(t);
            }
            cursor.close();
        }

        return list;
    }

    public long create(String table, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(table, null, values);
    }

    public int remove(String table, String whereConditions, String... args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(table, whereConditions, args);
    }

    public int getLatestId(String table) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT MAX(" + BaseColumns._ID + ") FROM " + table;
        Cursor cursor = db.rawQuery(sql, new String[]{});
        int result = -1;
        if (cursor != null && cursor.moveToNext()) {
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    public int update(String table, ContentValues contentValues, String whereConditions, String... args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(table, contentValues, whereConditions, args);
    }
}
```
```java
public interface DBCallback <T> {
    /**
     * Gets a instance of T by cursor
     */
    T cursorToInstance(Cursor cursor);
}
```
3. 新建一个类继承`SQLiteOpenHelper`，`DBOpenHelper`，用于启动app时创建数据库，并且初始化数据，加入一条记录:
```java
public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = DBOpenHelper.class.getSimpleName();
    private static final int VERSION = 1;
    private static final String DB_NAME = "memo.db";

    public DBOpenHelper() {
        super(MemoApplication.getContext(), DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*第一次初始化app，创建表结构 */
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ColumnContacts.EVENT_TABLE_NAME + "( "
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ColumnContacts.EVENT_TITLE_COLUMN + " text, "
                    + ColumnContacts.EVENT_CONTENT_COLUMN + " text, "
                    + ColumnContacts.EVENT_CREATED_TIME_COLUMN + " datetime, "
                    + ColumnContacts.EVENT_UPDATED_TIME_COLUMN + " datetime, "
                    + ColumnContacts.EVENT_REMIND_TIME_COLUMN + " datetime, "
                    + ColumnContacts.EVENT_IS_IMPORTANT_COLUMN + " INTEGER, "
                    + ColumnContacts.EVENT_IS_CLOCKED + " INTEGER"
        + ")");

        String sql = "INSERT INTO " + ColumnContacts.EVENT_TABLE_NAME + " VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        db.execSQL(sql, new Object[]{"jsbintask->memo",
                "Memo是一个小巧方便带有闹铃功能的记事本app，主要使用butterknife和recycleview，clockmanager构建\n" +
                        "git地址：https://github.com/jsbintask22/memo.git",
                "2018-04-25 17:28:23",
                "2018-04-25 17:28",
                "2018-04-25 17:28",
                0, 0});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /*版本更新时会执行该方法，如版本变更 => 2  */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing to do
    }
}
```

### 编写dao层
记事本中是包含一个实体类，`Event`，编写实体类和dao层代码对该表进行增删改查
`Event`:
```java
public class Event implements BaseColumns, Parcelable{
    //id
    private Integer mId;
    //事件
    private String mTitle;
    //事件内容
    private String mContent;
    //创建时间
    private String mCreatedTime;

    public Integer getmIsClocked() {
        return mIsClocked;
    }

    public void setmIsClocked(Integer mIsClocked) {
        this.mIsClocked = mIsClocked;
    }

    //更新时间
    private String mUpdatedTime;
    //闹钟表示位：该事件是否已经响过铃了，默认没有
    private Integer mIsClocked = 0;

    public Event(){}

    protected Event(Parcel in) {
        if (in.readByte() == 0) {
            mId = null;
        } else {
            mId = in.readInt();
        }
        mTitle = in.readString();
        mContent = in.readString();
        mCreatedTime = in.readString();
        mUpdatedTime = in.readString();
        if (in.readByte() == 0) {
            mIsClocked = null;
        } else {
            mIsClocked = in.readInt();
        }
        if (in.readByte() == 0) {
            mIsImportant = null;
        } else {
            mIsImportant = in.readInt();
        }
        mRemindTime = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Integer getmIsImportant() {
        return mIsImportant;
    }

    public void setmIsImportant(Integer mIsImportant) {
        this.mIsImportant = mIsImportant;
    }

    private Integer mIsImportant;

    public Integer getmId() {
        return mId;
    }

    public void setmId(Integer mId) {
        this.mId = mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmCreatedTime() {
        return mCreatedTime;
    }

    public void setmCreatedTime(String mCreatedTime) {
        this.mCreatedTime = mCreatedTime;
    }

    public String getmUpdatedTime() {
        return mUpdatedTime;
    }

    public void setmUpdatedTime(String mUpdatedTime) {
        this.mUpdatedTime = mUpdatedTime;
    }

    public String getmRemindTime() {
        return mRemindTime;
    }

    public void setmRemindTime(String mRemindTime) {
        this.mRemindTime = mRemindTime;
    }

    private String mRemindTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mId);
        }
        dest.writeString(mTitle);
        dest.writeString(mContent);
        dest.writeString(mCreatedTime);
        dest.writeString(mUpdatedTime);
        if (mIsClocked == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mIsClocked);
        }
        if (mIsImportant == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mIsImportant);
        }
        dest.writeString(mRemindTime);
    }
}
```
值得注意的是，为了让其在Activity之间传递数据，需要继承`Parcelable`接口，接下编写`EventDao`,因为用到了模板方法，所以加入回调类`EventCallback`：
```java
public class EventDao {
    //使用模板模式进行DB操作
    private DBTemplate<Event> mTemplate = new DBTemplate<>();
    private EventCallback mCallback = new EventCallback();
    //设置为单例模式，其他类均是此方法
    private static EventDao mEventDao = new EventDao();

    private EventDao() {
    }

    public static EventDao getInstance() {
        return mEventDao;
    }

    public List<Event> findAll() {
        String sql = "SELECT * FROM " + ColumnContacts.EVENT_TABLE_NAME + " ORDER BY " + ColumnContacts.EVENT_IS_IMPORTANT_COLUMN + " DESC, " + ColumnContacts.EVENT_CREATED_TIME_COLUMN + " DESC";
        return mTemplate.query(sql, mCallback);
    }

    public List<Event> findAllWithNOClocked() {
        String sql = "SELECT * FROM " + ColumnContacts.EVENT_TABLE_NAME + " WHERE " + ColumnContacts.EVENT_IS_CLOCKED + " = " + Constants.EventClockFlag.NONE;
        return mTemplate.query(sql, mCallback);
    }

    public int updateEventClocked(Integer id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ColumnContacts.EVENT_IS_CLOCKED, Constants.EventClockFlag.CLOCKED);
        return mTemplate.update(ColumnContacts.EVENT_TABLE_NAME, contentValues, BaseColumns._ID + " = ?", Integer.toString(id));
    }

    public Event findById(Integer id) {
        String sql = "SELECT * FROM " + ColumnContacts.EVENT_TABLE_NAME + " WHERE " + BaseColumns._ID + " = ?";
        return mTemplate.queryOne(sql, mCallback, Integer.toString(id));
    }

    public int remove(List<Integer> ids) {
        StringBuilder whereConditions = new StringBuilder(BaseColumns._ID + " IN(");
        for (Integer id : ids) {
            whereConditions.append(id).append(",");
        }
        whereConditions.deleteCharAt(whereConditions.length() - 1).append(")");
        return mTemplate.remove(ColumnContacts.EVENT_TABLE_NAME, whereConditions.toString());
    }

    public int create(Event event) {
        return (int) mTemplate.create(ColumnContacts.EVENT_TABLE_NAME, generateContentValues(event, false));
    }

    public int update(Event event) {
        return mTemplate.update(ColumnContacts.EVENT_TABLE_NAME, generateContentValues(event, true), BaseColumns._ID + "  = ?", Integer.toString(event.getmId()));
    }

    public int getLatestEventId() {
        return mTemplate.getLatestId(ColumnContacts.EVENT_TABLE_NAME);
    }

    private ContentValues generateContentValues(Event event, boolean isUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ColumnContacts.EVENT_TITLE_COLUMN, event.getmTitle());
        contentValues.put(ColumnContacts.EVENT_CONTENT_COLUMN, event.getmContent());
        if (!isUpdate) {
            contentValues.put(ColumnContacts.EVENT_CREATED_TIME_COLUMN, DateTimeUtil.dateToStr(new Date()));
        } else {
            contentValues.put(ColumnContacts.EVENT_CREATED_TIME_COLUMN, event.getmCreatedTime());
        }
        contentValues.put(ColumnContacts.EVENT_IS_CLOCKED, event.getmIsClocked());
        contentValues.put(ColumnContacts.EVENT_UPDATED_TIME_COLUMN, DateTimeUtil.dateToStr(new Date()));
        contentValues.put(ColumnContacts.EVENT_REMIND_TIME_COLUMN, event.getmRemindTime());
        contentValues.put(ColumnContacts.EVENT_IS_IMPORTANT_COLUMN, event.getmIsImportant());
        return contentValues;
    }
}
```
```java
public class EventCallback implements DBCallback<Event> {
    @Override
    public Event cursorToInstance(Cursor cursor) {
        Event event = new Event();
        event.setmId(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)));
        event.setmTitle(cursor.getString(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_TITLE_COLUMN)));
        event.setmContent(cursor.getString(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_CONTENT_COLUMN)));
        event.setmCreatedTime(cursor.getString(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_CREATED_TIME_COLUMN)));
        event.setmUpdatedTime(cursor.getString(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_UPDATED_TIME_COLUMN)));
        event.setmRemindTime(cursor.getString(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_REMIND_TIME_COLUMN)));
        event.setmIsImportant(cursor.getInt(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_IS_IMPORTANT_COLUMN)));
        event.setmIsClocked(cursor.getInt(cursor.getColumnIndexOrThrow(ColumnContacts.EVENT_IS_CLOCKED)));
        return event;
    }
}
```

### 编写manager层
1. EventManager用于管理所有的event如下：
```java
public class EventManager {
    public static final String TAG = "EventManager";
    private static EventManager mEventManager = new EventManager();
    private EventDao mEventDao = EventDao.getInstance();
    //保存一份数据

    public List<Integer> getDeletedIds() {
        return deletedIds;
    }

    public void setDeletedIds(List<Integer> deletedIds) {
        this.deletedIds = deletedIds;
    }

    private List<Event> events = new ArrayList<>();
    private List<Integer> deletedIds = new ArrayList<>();

    private EventManager(){
    }

    public static EventManager getInstance() {
        return mEventManager;
    }

    public List<Event> findAll() {
        events =  mEventDao.findAll();
        return events;
    }

    public void flushData() {
        events = mEventDao.findAll();
    }

    public List<Event> getEvents() {
        return events;
    }

    public void removeEvents(final Handler handler, final List<Integer> ids) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int result = mEventDao.remove(ids);
                    Message message = new Message();
                    message.what = Constants.HANDLER_SUCCESS;
                    message.obj = result;
                    message.setTarget(handler);
                    message.sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                    handler.obtainMessage(Constants.HANDLER_FAILED, new MemoException(e)).sendToTarget();
                }
            }
        }).start();
    }

    public boolean removeEvent(Integer id) {
        return mEventDao.remove(Collections.singletonList(id)) != 0;
    }

    public boolean saveOrUpdate(Event event) {
        try {
            if (event.getmId() != null) {
                mEventDao.update(event);
            } else {
                mEventDao.create(event);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "saveOrUpdate: ", e);
            return false;
        }
    }

    public int getLatestEventId() {
        return mEventDao.getLatestEventId();
    }

    public Event getOne(Integer id) {
        return mEventDao.findById(id);
    }

    public boolean checkEventField(Event event) {
        if (event == null) {
            return false;
        }
        if (StringUtil.isBlank(event.getmTitle())) {
            ToastUtil.showToastShort(R.string.event_can_not_empty);
            return false;
        }
        if (StringUtil.isBlank(event.getmContent())) {
            ToastUtil.showToastShort(R.string.content_can_not_empty);
            return false;
        }
        if (StringUtil.isBlank(event.getmRemindTime())) {
            ToastUtil.showToastShort(R.string.remind_time_can_not_empty);
            return false;
        }
        if (DateTimeUtil.str2Date(event.getmRemindTime()) == null) {
            ToastUtil.showToastShort(R.string.invalid_remind_time_format);
            return false;
        }
        if (new Date().getTime() > DateTimeUtil.str2Date(event.getmRemindTime()).getTime()) {
            ToastUtil.showToastShort(R.string.remind_time_deprecated);
            return false;
        }

        return true;
    }
}
```
接着编写一个`ClockManager`用于管理系统闹钟服务，用于app定时提醒：
```java
public class ClockManager {
    private static ClockManager instance = new ClockManager();

    private ClockManager() {
    }

    public static ClockManager getInstance() {
        return instance;
    }

    /**
     * 获取系统闹钟服务
     * @return
     */
    private static AlarmManager getAlarmManager() {
        return (AlarmManager) MemoApplication.getContext().getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 取消闹钟
     * @param pendingIntent
     */
    public void cancelAlarm(PendingIntent pendingIntent) {
        getAlarmManager().cancel(pendingIntent);
    }

    /**
     * 添加闹钟
     * @param pendingIntent 执行动作
     * @param performTime  执行时间
     */
    public void addAlarm(PendingIntent pendingIntent, Date performTime) {
        cancelAlarm(pendingIntent);
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, performTime.getTime(), pendingIntent);
    }
}
```
### 编写系统Service和Receiver
为了让我们的提醒服务在后台保活，我们需要编写一个`ClockService`或者`ClockReceiver`在后台运行（任意一种都行，此处用的`service`:
```java
/**
 * Service和Broadcast都行，此处选一个，service存活率更高
 */
public class ClockService extends Service {
    private static final String TAG = "ClockService";
    public static final String EXTRA_EVENT_ID = "extra.event.id";
    public static final String EXTRA_EVENT_REMIND_TIME = "extra.event.remind.time";
    public static final String EXTRA_EVENT = "extra.event";
    private EventDao mEventDao = EventDao.getInstance();
    public ClockService() {
        Log.d(TAG, "ClockService: Constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: onStartCommand");
        WakeLockUtil.wakeUpAndUnlock();
        postToClockActivity(getApplicationContext(), intent);
        return super.onStartCommand(intent, flags, startId);
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
```
`ClockReceiver`如下：
```java
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
```

### Activity
接着编写Activity，首先，写一个主界面，用于展示事件清单，因为有多个`Activity`，所以我们加入`BaseActivity`:
```java
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
```
`MainActivity`如下：
```java
public class MainActivity extends BaseActivity implements BaseHandler.HandlerResultCallBack {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.search_view)
    SearchView mSearchView;
    private EventRecyclerViewAdapter mAdapter;
    private EventManager mEventManger = EventManager.getInstance();
    private ClockManager mClockManager = ClockManager.getInstance();
    private BaseHandler mBaseHandler = new BaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        mAdapter = new EventRecyclerViewAdapter(this);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSearchView.getLayoutParams();
        //将文字内容略微下移，SearchView  bug
        params.bottomMargin = -3;
        mSearchView.setLayoutParams(params);
        mSearchView.onActionViewExpanded();
        initSearchView();
    }

    private void initSearchView() {
        //一处searchView进入屏幕时候的焦点
        mSearchView.clearFocus();
        Class<? extends SearchView> aClass = mSearchView.getClass();
        try {
            //去掉SearchView自带的下划线
            Field mSearchPlate = aClass.getDeclaredField("mSearchPlate");
            mSearchPlate.setAccessible(true);
            View o = (View) mSearchPlate.get(mSearchView);
            o.setBackgroundColor(getColor(R.color.transparent));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //隐藏键盘
        AppUtil.hideSoftInput(this, mSearchView);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void initData() {
        //设置数据源，适配器等等
        mAdapter.setDatabases(mEventManger.findAll());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void setListener() {
       mAdapter.setOnItemClickListener(mOnItemClickListener);
       mSearchView.setOnQueryTextListener(mQueryListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //判断是点击了那个按钮，删除，添加
        if (item.getItemId() == R.id.menu_add) {
            Intent intent = new Intent();
            intent.setClass(this, EventDetailActivity.class);
            intent.putExtra(EventDetailActivity.EXTRA_IS_ADD_EVENT, true);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_delete) {
            if (mAdapter.getIsDeleteMode()) {
                //删除数据
                if (mAdapter.getSelectedEventIds().size() == 0) {
                    ToastUtil.showToastShort(R.string.no_event_selected_msg);
                } else {
                    int msg = mAdapter.getSelectedEventIds().size() == 1 ? R.string.delete_event_msg : R.string.delete_events_msg;
                    AlertDialogUtil.showDialog(this, msg, mConfirmListener);
                }
            } else {
                mAdapter.setDeleteMode(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }

    private EventRecyclerViewAdapter.OnItemClickListener mOnItemClickListener = new EventRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!mAdapter.getIsDeleteMode()) {
                //跳屏，此时为查看详情，不是编辑状态
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_IS_EDIT_EVENT, false);
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATA, mAdapter.getDatabases().get(position));
                startActivity(intent);
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            ToastUtil.showToastShort("Long clicked");
            mAdapter.setDeleteMode(true);
        }
    };

    private DialogInterface.OnClickListener mConfirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mEventManger.setDeletedIds(mAdapter.getSelectedEventIds());
            mEventManger.removeEvents(mBaseHandler, mEventManger.getDeletedIds());
        }
    };

    /**
     * 从编辑屏幕回来时调用该方法，做数据更新
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mAdapter.setDatabases(mEventManger.getEvents());
    }

    private SearchView.OnQueryTextListener mQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            //做本地查询
            if (!StringUtil.isBlank(newText)) {
                List<Event> events = new ArrayList<>();
                for (Event event : mAdapter.getDatabases()) {
                    if (event.getmTitle().contains(newText)) {
                        events.add(event);
                    }
                }
                mAdapter.setDatabases(events);
            } else {
                mAdapter.setDatabases(mEventManger.getEvents());
            }
            return true;
        }
    };

    /**
     * handler处理成功的回调函数
     * @link com.jsbintask@gmail.com.memo.base.BaseHandler
     */
    @Override
    public void handlerSuccess(Message msg) {
        ToastUtil.showToastShort(R.string.delete_successful);
        for (PendingIntent pendingIntent : buildIntent(mEventManger.getDeletedIds())) {
            mClockManager.cancelAlarm(pendingIntent);
        }
        mAdapter.setDatabases(mEventManger.findAll());
    }
    /**
     * 处理失败的回调函数
     */
    @Override
    public void handlerFailed(MemoException e) {
        ToastUtil.showToastShort(R.string.delete_failed);
        mAdapter.setDeleteMode(false);
    }

    private List<PendingIntent> buildIntent(List<Integer> ids) {
        List<PendingIntent> pendingIntents = new ArrayList<>();
        for (Integer id : ids) {
            Intent intent = new Intent();
            intent.putExtra(ClockService.EXTRA_EVENT_ID, id);
            intent.setClass(this, ClockService.class);

            pendingIntents.add(PendingIntent.getService(this, 0x001, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        return pendingIntents;
    }
}
```
接着，编写个用于展示事件详情的`EventDetailActivity`:
```java
public class EventDetailActivity extends BaseActivity {
    public static final String EXTRA_IS_EDIT_EVENT = "extra.is.edit.event";
    public static final String EXTRA_EVENT_DATA = "extra.event.data";
    public static final String EXTRA_IS_ADD_EVENT = "extra.is.create.event";
    //从主屏进来的操作是不是编辑操作
    private boolean isEditEvent;
    //从主屏进来的操作是不是添加操作
    private boolean isAddEvent;
    private EventManager mEventManager = EventManager.getInstance();
    private ClockManager mClockManager = ClockManager.getInstance();
    @BindView(R.id.ll_update_time)
    LinearLayout llUpdateTime;
    @BindView(R.id.ed_title)
    EditText edTitle;
    @BindView(R.id.tv_remind_time_picker)
    EditText tvRemindTime;
    @BindView(R.id.ed_content)
    EditText edContent;
    @BindView(R.id.tv_last_edit_time)
    TextView tvUpdateTime;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.iv_delete)
    ImageView ivDelete;
    @BindView(R.id.iv_edit)
    ImageView ivEdit;
    @BindView(R.id.chb_is_important)
    CheckBox chbIsImportant;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void initView() {
        isEditEvent = getIntent().getBooleanExtra(EXTRA_IS_EDIT_EVENT, false);
        isAddEvent = getIntent().getBooleanExtra(EXTRA_IS_ADD_EVENT, false);
        judgeOperate();
    }

    private void judgeOperate() {
        //是否显示上方上次编辑时间
        llUpdateTime.setVisibility(isAddEvent ? View.GONE : View.VISIBLE);
        //是否能够编辑标题
        setEditTextReadOnly(edTitle, !isEditEvent && !isAddEvent);
        //是否能够编辑内容
        setEditTextReadOnly(edContent, !isEditEvent && !isAddEvent);
        //设置提醒时间不能手动输入
        setEditTextReadOnly(tvRemindTime, true);
        //设置提醒时间是否能够点击：弹出时间选择器
        tvRemindTime.setClickable(isEditEvent || isAddEvent);
        //设置右上角确定按钮是否可见
        tvConfirm.setVisibility(isEditEvent || isAddEvent ? View.VISIBLE : View.GONE);
        //设置右下角编辑按钮是否可见
        ivEdit.setVisibility(!isEditEvent && !isAddEvent ? View.VISIBLE : View.GONE);
        //设置左下角删除按钮是否可见
        ivDelete.setVisibility(!isAddEvent ? View.VISIBLE : View.GONE);
        //设置checkbox能不能点击
        chbIsImportant.setClickable(isEditEvent || isAddEvent);
    }

    @Override
    protected void initData() {
        if (!isAddEvent) {
            Event event = getIntent().getParcelableExtra(EXTRA_EVENT_DATA);
            //填充值
            tvUpdateTime.setText(event.getmUpdatedTime());
            edTitle.setText(event.getmTitle());
            edContent.setText(event.getmContent());
            tvRemindTime.setText(event.getmRemindTime());
            chbIsImportant.setChecked(event.getmIsImportant() == Constants.EventFlag.IMPORTANT);
        }
    }

    @OnClick(R.id.iv_back)
    public void backImageClick(View view) {
        finish();
    }

    @OnClick(R.id.iv_delete)
    public void deleteImageClick(View view) {
        if (!isAddEvent) {
            AlertDialogUtil.showDialog(this, R.string.delete_event_msg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Event event = getIntent().getParcelableExtra(EventDetailActivity.EXTRA_EVENT_DATA);
                    if (mEventManager.removeEvent(event.getmId())) {
                        ToastUtil.showToastShort(R.string.delete_successful);
                        mClockManager.cancelAlarm(buildIntent(event.getmId()));
                        mEventManager.flushData();
                        postToMainActivity();
                    } else {
                        ToastUtil.showToastShort(R.string.delete_failed);
                    }
                }
            });
        }
    }

    /**
     * 回到主屏幕
     */
    private void postToMainActivity() {
        Intent intent = new Intent();
        intent.setClass(EventDetailActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_event_detail;
    }

    /**
     * 弹出时间选择器，选择闹钟执行时间
     * @param view
     */
    @OnClick(R.id.tv_remind_time_picker)
    public void datePickClick(View view) {
        if (isEditEvent || isAddEvent) {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(EventDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String time = year + "-" + StringUtil.getLocalMonth(month) + "-" + StringUtil.getMultiNumber(dayOfMonth) + " " + StringUtil.getMultiNumber(hourOfDay) + ":" + StringUtil.getMultiNumber(minute);
                            tvRemindTime.setText(time);
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            dialog.show();
        }
    }

    @OnClick(R.id.iv_edit)
    public void editImageClick(View view) {
        if (!isEditEvent) {
            ToastUtil.showToastShort(R.string.enter_edit_mode);
            ivEdit.setVisibility(View.GONE);
            isEditEvent = true;
            judgeOperate();
        }
    }

    @OnClick(R.id.tv_confirm)
    public void confirmClick(View view) {
        //更新
        if (isEditEvent || isAddEvent) {
            Event event = buildEvent();
            //检查属性并且提醒
            if (!mEventManager.checkEventField(event)) {
                return;
            }
            if (mEventManager.saveOrUpdate(event)) {
                if (isEditEvent) {
                    ToastUtil.showToastShort(R.string.update_successful);
                } else if (isAddEvent) {
                    ToastUtil.showToastShort(R.string.create_successful);
                    event.setmId(mEventManager.getLatestEventId());
                }
                //添加闹钟
                mClockManager.addAlarm(buildIntent(event.getmId()), DateTimeUtil.str2Date(event.getmRemindTime()));
                mEventManager.flushData();
                postToMainActivity();
            } else {
                if (isEditEvent) {
                    ToastUtil.showToastShort(R.string.update_failed);
                } else if (isAddEvent) {
                    ToastUtil.showToastShort(R.string.create_failed);
                }
            }
        }
    }

    private PendingIntent buildIntent(int id) {
        Intent intent = new Intent();
        intent.putExtra(ClockReceiver.EXTRA_EVENT_ID, id);
        intent.setClass(this, ClockService.class);

        return PendingIntent.getService(this, 0x001, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @OnClick(R.id.scroll_view)
    public void scrollViewClick(View view) {
        if (isAddEvent || isEditEvent) {
            //打开软键盘
            setEditTextReadOnly(edContent, false);
        }
    }

    @NonNull
    private Event buildEvent() {
        Event event = new Event();
        if (isEditEvent) {
            event.setmId(((Event) getIntent().getParcelableExtra(EXTRA_EVENT_DATA)).getmId());
            event.setmCreatedTime(((Event) getIntent().getParcelableExtra(EXTRA_EVENT_DATA)).getmCreatedTime());
        }
        event.setmRemindTime(tvRemindTime.getText().toString());
        event.setmTitle(edTitle.getText().toString());
        event.setmIsImportant(chbIsImportant.isChecked() ? Constants.EventFlag.IMPORTANT : Constants.EventFlag.NORMAL);
        event.setmContent(edContent.getText().toString());
        event.setmUpdatedTime(DateTimeUtil.dateToStr(new Date()));
        return event;
    }

    private void setEditTextReadOnly(EditText editText, boolean readOnly) {
        editText.setFocusable(!readOnly);
        editText.setFocusableInTouchMode(!readOnly);
        editText.setCursorVisible(!readOnly);
        editText.setTextColor(getColor(readOnly ? R.color.gray3 : R.color.black));
    }
}
```
最后，再写一个Activity用于展示闹铃提醒，`ClockActivity`:
```java
public class ClockActivity extends BaseActivity {
    private static final String TAG = "ClockActivity";
    public static final String EXTRA_CLOCK_EVENT = "clock.event";
    //闹铃
    private MediaPlayer mediaPlayer;
    //震动
    private Vibrator mVibrator;
    private EventManager mEventManger = EventManager.getInstance();
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void initView() {
    }

    private void clock() {
        mediaPlayer.start();
        long[] pattern = new long[]{1500, 1000};
        mVibrator.vibrate(pattern, 0);
        //获取自定义布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_alarm_layout, null);
        TextView textView = inflate.findViewById(R.id.tv_event);
        textView.setText(String.format(getString(R.string.clock_event_msg_template), event.getmTitle()));
        Button btnConfirm = inflate.findViewById(R.id.btn_confirm);
        final AlertDialog alertDialog = AlertDialogUtil.showDialog(this, inflate);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mVibrator.cancel();
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        clock();
    }

    @Override
    protected void initData() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.clock);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Intent intent = getIntent();
        event = getIntent().getParcelableExtra(ClockService.EXTRA_EVENT);
        if (event == null) {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        clock();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_clock;
    }
}
```

### 系统界面，服务注册清单
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="cn.jsbintask.memo">

    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />

    <application
        android:name="cn.jsbintask.memo.MemoApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_logo"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_logo"
        android:supportsRtl="true"
        android:theme="@style/AppTheme"
        tools:ignore="GoogleAppIndexingWarning">
        <activity
            android:name="cn.jsbintask.memo.ui.activity.MainActivity"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name="cn.jsbintask.memo.ui.activity.EventDetailActivity"
            android:theme="@style/NoActionBarTheme" />

        <!-- <service android:name=".service.ClockService" /> -->

        <activity
            android:name="cn.jsbintask.memo.ui.activity.ClockActivity"
            android:launchMode="singleTask"
            android:theme="@style/FullScreen" />

        <receiver android:name="cn.jsbintask.memo.receiver.ClockReceiver">
            <intent-filter android:priority="100">
                <action android:name="com.liuzhengwei.memo.action.CLOCK_RECEIVER" />
            </intent-filter>
        </receiver>

        <service
            android:name="cn.jsbintask.memo.service.ClockService"
            android:enabled="true"
            android:exported="true" />
    </application>

</manifest>
```

## 成果展示
闹铃提醒：
![clock](https://raw.githubusercontent.com/jsbintask22/static/master/memo/memo_clock.jpg)
批量删除:
![clock](https://raw.githubusercontent.com/jsbintask22/static/master/memo/memo_delete.jpg)
新增和编辑：
![clock](https://raw.githubusercontent.com/jsbintask22/static/master/memo/memo_edit_save.jpg)
搜索：
![search](https://raw.githubusercontent.com/jsbintask22/static/master/memo/memo_search.jpg)

`关注我！这里只有干货！`
本文原创地址，我的博客：[https://jsbintask.cn/2019/02/23/android/android-memo/](https://jsbintask.cn/2019/02/23/android/android-memo/)，转载请注明出处!