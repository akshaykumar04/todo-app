package com.sstechcanada.todo.adapters

import android.content.Context
import com.sstechcanada.todo.activities.TodoListActivity.Companion.showPlaceHolder
import com.sstechcanada.todo.activities.TodoListActivity.Companion.hidePlaceHolder
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sstechcanada.todo.models.TodoTaskFirestore
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter.TodoListFirestoreHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.DatabaseReference
import com.sstechcanada.todo.activities.MasterTodoListActivity
import android.graphics.drawable.GradientDrawable
import android.content.DialogInterface
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import com.sstechcanada.todo.activities.auth.LoginActivity
import android.widget.Toast
import android.content.Intent
import android.util.Log
import com.sstechcanada.todo.activities.AppUpgradeActivity2
import com.sstechcanada.todo.activities.AddOrEditTaskActivity2
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import com.sstechcanada.todo.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TodoListFirestoreAdapter(
    options: FirestoreRecyclerOptions<TodoTaskFirestore?>,
    var context: Context
) :
    FirestoreRecyclerAdapter<TodoTaskFirestore, TodoListFirestoreHolder>(options) {
    private val mAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    private val db = FirebaseFirestore.getInstance()
    private val databaseReference: DatabaseReference? = null
    private val usersColRef = db.collection("Users")
    var userID = user!!.uid
    var UserColRef = db.collection("Users").document(userID).collection("Lists")
        .document(MasterTodoListActivity.listId).collection("Todo")

    override fun onBindViewHolder(
        holder: TodoListFirestoreHolder,
        position: Int,
        model: TodoTaskFirestore
    ) {
        holder.tvTextDesc.text = model.description
        if (model.benefitsString != null || model.benefitsString !== "") {
            holder.tvBenefits.text = model.benefitsString
        }
        holder.customCheckbox.isChecked = (model.status == "Completed")

        val magnitudeCircle = holder.circleDot.background as GradientDrawable
        val magnitudeColor = getMagnitudeColor(position % 9)
        magnitudeCircle.setColor(magnitudeColor)
        holder.tvBenefits.setTextColor(magnitudeColor)

        val documentSnapshot = snapshots.getSnapshot(position)
        model.documentID = documentSnapshot.id
        holder.customCheckbox.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Log.i("onclick", "checkbox")
                if (holder.customCheckbox.isChecked) {
                    AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm Complete")
                        .setMessage("Are you sure you want to mark this task as completed?")
                        .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                val updateTaskMap: MutableMap<String, Any> = HashMap()
                                val task_status = "Completed"
                                val calendar = Calendar.getInstance()
                                val dateStr = DateFormat.getDateInstance(DateFormat.FULL)
                                    .format(calendar.time)
                                val sdf = SimpleDateFormat("h:mm a")
                                Log.i("dateTime", "TimestampCompleted$dateStr")
                                val timeStr = sdf.format(calendar.time)
                                updateTaskMap["TimestampCompleted"] = "$dateStr $timeStr"
                                updateTaskMap["Status"] = task_status
                                UserColRef.document(model.documentID)
                                    .set(updateTaskMap, SetOptions.merge()).addOnSuccessListener {
                                        Toasty.success(context, "Todo-task marked as completed")
                                            .show()
                                    }.addOnFailureListener {
                                        Toasty.error(context, "Something went wrong").show()
                                    }
                                //
                            }
                        })
                        .setNegativeButton("No", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                holder.customCheckbox.isChecked = false
                            }
                        })
                        .setCancelable(false)
                        .show()
                } else {
                    AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm Incomplete")
                        .setMessage("Are you sure you want to mark this task as incomplete?")
                        .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                val updateTaskMap: MutableMap<String, Any> = HashMap()
                                val task_status = "Pending"
                                val calendar = Calendar.getInstance()
                                val dateStr = DateFormat.getDateInstance(DateFormat.FULL)
                                    .format(calendar.time)
                                val sdf = SimpleDateFormat("h:mm a")
                                Log.i("dateTime", "TimestampCompleted$dateStr")
                                val timeStr = sdf.format(calendar.time)
                                updateTaskMap["TimestampCompleted"] = "$dateStr $timeStr"
                                updateTaskMap["Status"] = task_status
                                UserColRef.document(model.documentID)
                                    .set(updateTaskMap, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Toasty.success(
                                            context,
                                            "Todo-task marked as incomplete"
                                        ).show()
                                    }.addOnFailureListener {
                                        Toasty.error(
                                            context,
                                            "Something went wrong"
                                        ).show()
                                    }
                                //
                            }
                        })
                        .setNegativeButton("No", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                holder.customCheckbox.isChecked = true
                            }
                        })
                        .setCancelable(false)
                        .show()
                }
            }
        })
        holder.cardView.setOnClickListener { v: View ->
            Log.i("onclick", "card")
            if ((MasterTodoListActivity.purchaseCode == "0") && position + 1 > (LoginActivity.userAccountDetails.get(
                    1
                ).toInt())
            ) {
                Log.d("subscriptionFeature", "subscription expired!")
                Toasty.warning(
                    context,
                    "Your subscription expired! Renew subscription to continue using premium features",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(context, AppUpgradeActivity2::class.java)
                //                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                context.startActivity(intent)
            } else {
                val doc_id: String = model.documentID.toString()
                val todoTask = TodoTaskFirestore(
                    model.description,
                    model.priority,
                    model.dueDate,
                    model.documentID,
                    model.status,
                    model.category,
                    2,
                    model.benefits,
                    model.benefitsString,
                    model.timestampCompleted
                )
                val intent = Intent(context, AddOrEditTaskActivity2::class.java)
                intent.putExtra("Adding or editing", "Edit Task")
                intent.putExtra("Todo", todoTask)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListFirestoreHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.item_todo_list,
            parent, false
        )
        return TodoListFirestoreHolder(v)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 1) {
            showPlaceHolder()
        } else {
            hidePlaceHolder()
        }
    }

    private fun getMagnitudeColor(pos: Int): Int {
        val magnitudeColorResourceId: Int = when (pos) {
            0 -> R.color.circle8
            1 -> R.color.circle1
            2 -> R.color.circle2
            3 -> R.color.circle3
            4 -> R.color.circle4
            5 -> R.color.circle5
            6 -> R.color.circle6
            7 -> R.color.circle7
            8 -> R.color.circle8
            9 -> R.color.circle9
            else -> R.color.circle1
        }
        return ContextCompat.getColor(context, magnitudeColorResourceId)
    }

    inner class TodoListFirestoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTextDesc: TextView = itemView.findViewById(R.id.tv_todo_desc)
        var circleDot: TextView = itemView.findViewById(R.id.circle_per_item)
        var tvBenefits: TextView = itemView.findViewById(R.id.todo_benefits)
        var cardView: CardView = itemView.findViewById(R.id.materialCard)
        var customCheckbox: CheckBox = itemView.findViewById(R.id.checkb)

    }
}