package com.sstechcanada.todo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.AddOrEditTaskActivity2;
import com.sstechcanada.todo.custom_views.PriorityStarImageView;
import com.sstechcanada.todo.models.TodoTaskFirestore;

import static com.sstechcanada.todo.activities.TodoListActivity2.db_cnt;

public class TodoListFirestoreAdapter extends FirestoreRecyclerAdapter<TodoTaskFirestore, TodoListFirestoreAdapter.TodoListFirestoreHolder> {

    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    String userID=user.getUid();
    Context context;
    private DatabaseReference databaseReference;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference usersColRef=db.collection("Users");
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TodoListFirestoreAdapter(@NonNull FirestoreRecyclerOptions<TodoTaskFirestore> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull TodoListFirestoreHolder holder, int position, @NonNull TodoTaskFirestore model) {

        holder.tvTextDesc.setText(model.getDescription());
        holder.tvBenefits.setText(model.getBenefitsString());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentSnapshot documentSnapshot=getSnapshots().getSnapshot(position);
                model.setDocumentID(documentSnapshot.getId());

                String doc_id = String.valueOf(model.getDocumentID());
                TodoTaskFirestore todoTask = new TodoTaskFirestore(model.getDescription(),
                       model.getPriority(),
                        model.getDueDate(),
                        model.getDocumentID(),
                        model.getStatus(),
                        model.getCategory(),
                        2,
                        model.getBenefits(),
                        model.getBenefitsString());

                Intent intent = new Intent(v.getContext(), AddOrEditTaskActivity2.class);
                intent.putExtra("Adding or editing", "Edit Task");
                intent.putExtra("Todo",todoTask);
                v.getContext().startActivity(intent);

            }
        });
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
        CardView cardView;
        CheckBox customCheckbox;
        public TodoListFirestoreHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.materialCard);
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

    @Override
    public void onDataChanged() {
        super.onDataChanged();
//        if(getItemCount()>=3){
////            showPlaceHoder();
//        } else{
////            hidePlaceHoder();
//        }
        db_cnt=getItemCount();
    }
}

