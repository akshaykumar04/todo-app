package com.sstechcanada.todo.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.custom_views.PriorityStarImageView;
import com.sstechcanada.todo.models.TodoTaskFirestore;

public class TodoListFirestoreAdapter extends FirestoreRecyclerAdapter<TodoTaskFirestore, TodoListFirestoreAdapter.TodoListFirestoreHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TodoListFirestoreAdapter(@NonNull FirestoreRecyclerOptions<TodoTaskFirestore> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TodoListFirestoreHolder holder, int position, @NonNull TodoTaskFirestore model) {
        holder.tvTextDesc.setText(model.getDescription());
        Log.d("Description",model.getDescription());
        holder.tvBenefits.setText(model.getBenefitsString());
    }

    @NonNull
    @Override
    public TodoListFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_list,
                parent,false);
        return new TodoListFirestoreHolder(v);
    }

    class TodoListFirestoreHolder extends RecyclerView.ViewHolder{
        final AppCompatCheckBox cbTodoDescription;
        TextView tvTodoDueDate, tvTextDesc, circle_per, tvBenefits;
        final TextView tvTodoPriority;
        final PriorityStarImageView ivTodoPriorityStar;
        final ConstraintLayout clTodoListItem;
        CheckBox customCheckbox;
        public TodoListFirestoreHolder(@NonNull View itemView) {
            super(itemView);

            cbTodoDescription = itemView.findViewById(R.id.cb_todo_description);
            tvTextDesc = itemView.findViewById(R.id.tv_todo_desc);
            tvTodoDueDate = itemView.findViewById(R.id.tv_todo_due_date);
            tvTodoPriority = itemView.findViewById(R.id.tv_todo_priority);
            ivTodoPriorityStar = itemView.findViewById(R.id.iv_todo_priority_star);
            customCheckbox = itemView.findViewById(R.id.checkb);
            clTodoListItem = (ConstraintLayout) itemView;
            tvBenefits = itemView.findViewById(R.id.todo_benefits);
//            itemView.setOnClickListener(this);
//            cbTodoDescription.setOnClickListener(this);
            //Circle
            circle_per = itemView.findViewById(R.id.circle_per_item);
        }
    }
}

