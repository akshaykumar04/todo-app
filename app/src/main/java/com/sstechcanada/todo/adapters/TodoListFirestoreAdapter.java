package com.sstechcanada.todo.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.AddOrEditTaskActivity2;
import com.sstechcanada.todo.activities.AppUpgradeActivity2;
import com.sstechcanada.todo.models.TodoTaskFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.TodoListActivity2.hidePlaceHolder;
import static com.sstechcanada.todo.activities.TodoListActivity2.showPlaceHolder;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class TodoListFirestoreAdapter extends FirestoreRecyclerAdapter<TodoTaskFirestore, TodoListFirestoreAdapter.TodoListFirestoreHolder> {

    Context context;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference databaseReference;
    private final CollectionReference usersColRef = db.collection("Users");
    String userID = user.getUid();
    CollectionReference UserColRef = db.collection("Users").document(userID).collection("Lists").document(listId).collection("Todo");


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TodoListFirestoreAdapter(@NonNull FirestoreRecyclerOptions<TodoTaskFirestore> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull TodoListFirestoreHolder holder, int position, @NonNull TodoTaskFirestore model) {

        holder.tvTextDesc.setText(model.getDescription());
        if (model.getBenefitsString() != null || model.getBenefitsString() != "") {
            holder.tvBenefits.setText(model.getBenefitsString());
        }

        holder.customCheckbox.setChecked(model.getStatus().equals("Completed"));


        //Circle
        // Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) holder.circle_per.getBackground();
        // Get the appropriate background color based on the current earthquake magnitude
        int magnitudeColor = getMagnitudeColor(position % 9);
        // Set the color on the magnitude circle
        magnitudeCircle.setColor(magnitudeColor);
//        holder.circle_per.setBackgroundColor(magnitudeColor);
        holder.tvBenefits.setTextColor(magnitudeColor);

        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        model.setDocumentID(documentSnapshot.getId());


        holder.customCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onclick", "checkbox");

                if (holder.customCheckbox.isChecked()) {

                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Confirm Complete")
                            .setMessage("Are you sure you want to mark this task as completed?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Map<String, Object> updateTaskMap = new HashMap<>();
                                    String task_status = "Completed";
                                    Calendar calendar = Calendar.getInstance();
                                    String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                                    Log.i("dateTime", "TimestampCompleted" + dateStr);
                                    String timeStr = sdf.format(calendar.getTime());
                                    updateTaskMap.put("TimestampCompleted", dateStr + " " + timeStr);

                                    updateTaskMap.put("Status", task_status);
                                    UserColRef.document(model.getDocumentID()).set(updateTaskMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toasty.success(context, "Todo-task marked as completed");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(context, "Something went wrong");
                                        }
                                    });
//
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.customCheckbox.setChecked(false);

                                }
                            })
                            .setCancelable(false)
                            .show();


                } else {

                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Confirm Incomplete")
                            .setMessage("Are you sure you want to mark this task as incomplete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String, Object> updateTaskMap = new HashMap<>();
                                    String task_status = "Pending";
                                    Calendar calendar = Calendar.getInstance();
                                    String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                                    Log.i("dateTime", "TimestampCompleted" + dateStr);
                                    String timeStr = sdf.format(calendar.getTime());
                                    updateTaskMap.put("TimestampCompleted", dateStr + " " + timeStr);

                                    updateTaskMap.put("Status", task_status);
                                    UserColRef.document(model.getDocumentID()).set(updateTaskMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toasty.success(context, "Todo-task marked as incomplete");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(context, "Something went wrong");
                                        }
                                    });
//
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.customCheckbox.setChecked(true);

                                }
                            })
                            .setCancelable(false)
                            .show();


                }
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onclick", "card");

//                DocumentSnapshot documentSnapshot=getSnapshots().getSnapshot(position);
//                model.setDocumentID(documentSnapshot.getId());

                 if(purchaseCode.equals("0") && position+1>(Integer.parseInt(userAccountDetails.get(1)))) {

                     Log.d("subscriptionFeature", "subscription expired!");
                     Toasty.warning(context, "Your subscription expired! Renew subscription to continue using premium features", Toast.LENGTH_SHORT).show();
                     Intent intent = new Intent(v.getContext(), AppUpgradeActivity2.class);
//                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                     v.getContext().startActivity(intent);
                 }else {

                     String doc_id = String.valueOf(model.getDocumentID());

                     TodoTaskFirestore todoTask = new TodoTaskFirestore(model.getDescription(),
                             model.getPriority(),
                             model.getDueDate(),
                             model.getDocumentID(),
                             model.getStatus(),
                             model.getCategory(),
                             2,
                             model.getBenefits(),
                             model.getBenefitsString(),
                             model.getTimestampCompleted());

                     Intent intent = new Intent(v.getContext(), AddOrEditTaskActivity2.class);
                     intent.putExtra("Adding or editing", "Edit Task");
                     intent.putExtra("Todo", todoTask);
                     v.getContext().startActivity(intent);
                 }

            }
        });
    }

    @NonNull
    @Override
    public TodoListFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_list,
                parent, false);
        return new TodoListFirestoreHolder(v);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (getItemCount() <= 1) {
            showPlaceHolder();
        } else {
            hidePlaceHolder();
        }
    }

    private int getMagnitudeColor(int pos) {
        int magnitudeColorResourceId;
        int magnitudeFloor = pos;
        switch (magnitudeFloor) {
            case 0:
                magnitudeColorResourceId = R.color.circle8;
                break;
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
        return ContextCompat.getColor(context, magnitudeColorResourceId);
    }

    class TodoListFirestoreHolder extends RecyclerView.ViewHolder {
        final AppCompatCheckBox cbTodoDescription;
//        final TextView tvTodoPriority;
//        final PriorityStarImageView ivTodoPriorityStar;
//        final ConstraintLayout clTodoListItem;
//        TextView tvTodoDueDate
        TextView tvTextDesc, circle_per, tvBenefits;
        CardView cardView;
        CheckBox customCheckbox;

        public TodoListFirestoreHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.materialCard);
            cbTodoDescription = itemView.findViewById(R.id.cb_todo_description);
            tvTextDesc = itemView.findViewById(R.id.tv_todo_desc);
//            tvTodoDueDate = itemView.findViewById(R.id.tv_todo_due_date);
//            tvTodoPriority = itemView.findViewById(R.id.tv_todo_priority);
//            ivTodoPriorityStar = itemView.findViewById(R.id.iv_todo_priority_star);
            customCheckbox = itemView.findViewById(R.id.checkb);
//            clTodoListItem = (ConstraintLayout) itemView;
            tvBenefits = itemView.findViewById(R.id.todo_benefits);
            //Circle
            circle_per = itemView.findViewById(R.id.circle_per_item);


        }
    }
}

