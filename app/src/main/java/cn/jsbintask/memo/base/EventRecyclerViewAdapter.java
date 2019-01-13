package cn.jsbintask.memo.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jsbintask.memo.Constants;
import cn.jsbintask.memo.R;
import cn.jsbintask.memo.entity.Event;
import cn.jsbintask.memo.ui.activity.EventDetailActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jsbintask.memo.entity.Event;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 16:13
 */
public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {
    //数据源
    private List<Event> mDatabases;
    //上下文
    private Context mContext;
    //点击事件
    private OnItemClickListener mOnItemClickListener;
    //是不是删除模式（ps：因为主界面可以删除元素，所以需要一个表示判断当前是什么操作）
    private boolean mIsDeleteMode = false;
    //批量删除时保存批量的元素id
    private List<Integer> mSelectedEventIds = new ArrayList<>();

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setDeleteMode(boolean mIsDeleteMenu) {
        //每次设置删除模式，清楚保存的id
        mSelectedEventIds.clear();
        this.mIsDeleteMode = mIsDeleteMenu;
        //通知适配器数据改变，重新渲染
        notifyDataSetChanged();
    }

    public boolean getIsDeleteMode() {
        return mIsDeleteMode;
    }

    public List<Integer> getSelectedEventIds() {
        return mSelectedEventIds;
    }

    //构造器
    public EventRecyclerViewAdapter(Context context) {
        super();
        mContext = context;
    }

    /**
     * 渲染回调函数
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo_layout, parent, false);
        return new EventViewHolder(itemView);
    }

    /**
     * 数据渲染
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final EventViewHolder holder, int position) {
        final Event event = mDatabases.get(position);
        //根据不同的状态渲染不同的图片
        if (!mIsDeleteMode) {
            if (event.getmIsImportant() == Constants.EventFlag.IMPORTANT) {
                holder.ivMemoIcon.setImageResource(R.drawable.ic_important_event);
                holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                holder.ivMemoIcon.setImageResource(R.drawable.ic_normal_event);
                holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        } else {
            holder.ivMemoIcon.setImageResource(R.drawable.ic_circle);
            if (event.getmIsImportant() == Constants.EventFlag.IMPORTANT) {
                holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                holder.tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        }
        holder.tvTitle.setText(event.getmTitle());

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*如果当前是删除模式，切换圆形图片和选中的图片   */
                if (mIsDeleteMode) {
                    if (Constants.MemoIconTag.FIRST == (Integer) holder.ivMemoIcon.getTag()) {
                        holder.ivMemoIcon.setTag(Constants.MemoIconTag.OTHER);
                        //切图
                        holder.ivMemoIcon.setImageResource(R.drawable.ic_selected);
                        mSelectedEventIds.add(event.getmId());
                    } else {
                        mSelectedEventIds.remove(event.getmId());
                        holder.ivMemoIcon.setTag(Constants.MemoIconTag.FIRST);
                        holder.ivMemoIcon.setImageResource(R.drawable.ic_circle);
                    }
                } else if (mOnItemClickListener != null) {
                    /* 如果不是删除模式，直接执行：跳屏 */
                    mOnItemClickListener.onItemClick(v, holder.getLayoutPosition());
                }
            }
        });

        //长按时将当前模式设置为删除模式
        holder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemLongClick(v, holder.getLayoutPosition());
                }
                return false;
            }
        });

        //设置后面的编辑按钮，跳屏
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_IS_EDIT_EVENT, true);
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATA, event);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * 设置数据源
     * @param events 数据源
     */
    public void setDatabases(List<Event> events) {
        mDatabases = events;
        mIsDeleteMode = false;
        //设置数据源，重新渲染一次
        notifyDataSetChanged();
    }

    public List<Event> getDatabases() {
        return mDatabases;
    }

    @Override
    public int getItemCount() {
        return mDatabases == null ? 0 : mDatabases.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    /**
     * 自定义Holder
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_memo)
        ImageView ivMemoIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.iv_edit)
        ImageView ivEdit;

        public EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ivMemoIcon.setTag(Constants.MemoIconTag.FIRST);
        }

        public View getItemView() {
            return this.itemView;
        }
    }
}
