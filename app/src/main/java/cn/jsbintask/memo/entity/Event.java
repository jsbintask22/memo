package cn.jsbintask.memo.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 14:46
 */

/**
 * 继承parcelable,让该类可以序列化（传输）
 */
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
