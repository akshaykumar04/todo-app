package com.sstechcanada.todo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sstechcanada.todo.R;
import com.sstechcanada.todo.custom_views.PriorityStarImageView;
import com.sstechcanada.todo.data.TodoListContract;
import com.sstechcanada.todo.models.TodoTask;
import com.sstechcanada.todo.utils.TodoDateUtils;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoListAdapterViewHolder> {
    private final static String TAG = TodoListAdapter.class.getSimpleName();
    private final TodoListAdapterOnClickHandler mClickHandler;
    private final Context mContext;
    private final Resources mRes;
    private final ColorStateList completedCheckboxColors;
    private final ColorStateList unCompletedCheckboxColors;
    private Cursor mCursor;
    private int mDescriptionIndex;
    private int mPriorityIndex;
    private int mDueDateIndex;
    private int m_IDIndex;
    private int mCompletedIndex;
    //Circle Text View


    public TodoListAdapter(Context context, TodoListAdapterOnClickHandler todoListAdapterOnClickHandler) {
        mClickHandler = todoListAdapterOnClickHandler;
        mContext = context;
        mRes = context.getResources();

        completedCheckboxColors = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked},
                },
                new int[]{
                        Color.DKGRAY,
                        mRes.getColor(R.color.colorCompleted),
                });

        unCompletedCheckboxColors = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked},
                },
                new int[]{
                        Color.DKGRAY,
                        mRes.getColor(R.color.colorAccent),
                });
    }

    @Override
    public TodoListAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_todo_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        TodoListAdapterViewHolder viewHolder = new TodoListAdapterViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull TodoListAdapter.TodoListAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.tvTextDesc.setText(mCursor.getString(mDescriptionIndex));

        holder.tvTodoDueDate.setTextColor(holder.tvTodoPriority.getCurrentTextColor());

        String dueDateString;
        long dueDate = mCursor.getLong(mDueDateIndex);
        Log.d(TAG, mCursor.getString(mDescriptionIndex) + " " + dueDate);
        if (dueDate == TodoTask.NO_DUE_DATE) {
            dueDateString = mContext.getString(R.string.no_due_date);
        } else {
            dueDateString = TodoDateUtils.formatDueDate(mContext, dueDate);
        }
//
//        int priority = mCursor.getInt(mPriorityIndex);
        holder.tvTodoDueDate.setText(dueDateString);
//        holder.tvTodoPriority.setText(mRes.getStringArray(R.array.priorities)[priority]);
        int isCompleted = mCursor.getInt(mCompletedIndex);
        holder.cbTodoDescription.setChecked(isCompleted == TodoTask.TASK_COMPLETED);

        if (isCompleted == TodoTask.TASK_COMPLETED) {
            // if the task is completed, we want everything grey
            holder.clTodoListItem.setBackground(mRes.getDrawable(R.drawable.list_item_completed_touch_selector));
            holder.tvTextDesc.setTextColor(mRes.getColor(R.color.colorCompleted));
            holder.cbTodoDescription.setSupportButtonTintList(completedCheckboxColors);
            holder.tvTodoPriority.setText(mRes.getString(R.string.completed));
            holder.customCheckbox.setChecked(true);
//            priority = PriorityStarImageView.COMPLETED;
        } else {
            holder.clTodoListItem.setBackground(mRes.getDrawable(R.drawable.list_item_touch_selector));
            holder.tvTextDesc.setTextColor(mRes.getColor(R.color.textHeadings));
            holder.cbTodoDescription.setSupportButtonTintList(unCompletedCheckboxColors);
            holder.customCheckbox.setChecked(false);
//            holder.tvTodoPriority.setText(mRes.getStringArray(R.array.priorities)[priority]);
            if (dueDate < TodoDateUtils.getTodaysDateInMillis()) {
                // display overdue tasks with the date in red
                // yeah, I know red for both overdue and high priority may be not the best idea
                holder.tvTodoDueDate.setTextColor(mRes.getColor(R.color.colorOverdue));
            } else {
                holder.tvTodoDueDate.setTextColor(holder.tvTodoPriority.getCurrentTextColor());
                Log.d(TAG, "color is " + (holder.tvTodoPriority.getCurrentTextColor()));
            }
        }
//        holder.ivTodoPriorityStar.setPriority(priority);

        //Circle
        // Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) holder.circle_per.getBackground();
        // Get the appropriate background color based on the current earthquake magnitude
        int magnitudeColor = getMagnitudeColor(position%9);
        // Set the color on the magnitude circle
        magnitudeCircle.setColor(magnitudeColor);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (mCursor != null) {
            mDescriptionIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_DESCRIPTION);
            mPriorityIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_PRIORITY);
            mDueDateIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_DUE_DATE);
            m_IDIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_ID);
            mCompletedIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_COMPLETED);
        }
        notifyDataSetChanged();
    }

    public interface TodoListAdapterOnClickHandler {
        void onClick(TodoTask todoTask, View view);
    }

    public class TodoListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final AppCompatCheckBox cbTodoDescription;
        TextView tvTodoDueDate, tvTextDesc, circle_per;
        final TextView tvTodoPriority;
        final PriorityStarImageView ivTodoPriorityStar;
        final ConstraintLayout clTodoListItem;
        CheckBox customCheckbox;

        public TodoListAdapterViewHolder(View itemView) {
            super(itemView);
            cbTodoDescription = itemView.findViewById(R.id.cb_todo_description);
            tvTextDesc = itemView.findViewById(R.id.tv_todo_desc);
            tvTodoDueDate = itemView.findViewById(R.id.tv_todo_due_date);
            tvTodoPriority = itemView.findViewById(R.id.tv_todo_priority);
            ivTodoPriorityStar = itemView.findViewById(R.id.iv_todo_priority_star);
            customCheckbox = itemView.findViewById(R.id.checkb);
            clTodoListItem = (ConstraintLayout) itemView;
            itemView.setOnClickListener(this);
            cbTodoDescription.setOnClickListener(this);
            //Circle
            circle_per = itemView.findViewById(R.id.circle_per_item);
        }

        @Override
        public void onClick(View view) {
            mCursor.moveToPosition(getAdapterPosition());
            TodoTask todoTask = new TodoTask(mCursor.getString(mDescriptionIndex),
                    mCursor.getInt(mPriorityIndex),
                    mCursor.getLong(mDueDateIndex),
                    mCursor.getInt(m_IDIndex),
                    mCursor.getInt(mCompletedIndex));
            mClickHandler.onClick(todoTask, view);
        }
    }

    private int getMagnitudeColor(int pos) {
        int magnitudeColorResourceId;
        int magnitudeFloor = pos;
        switch (magnitudeFloor) {
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.circle1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.circle2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.circle3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.circle4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.circle5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.circle6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.circle7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.circle8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.circle9;
                break;
            default:
                magnitudeColorResourceId = R.color.circle1;
                break;
        }
        return ContextCompat.getColor(mContext, magnitudeColorResourceId);
    }
}
