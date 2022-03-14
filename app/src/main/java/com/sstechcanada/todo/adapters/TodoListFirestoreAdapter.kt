package com.sstechcanada.todo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.AddOrEditTaskActivity
import com.sstechcanada.todo.activities.AppUpgradeActivity
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.sstechcanada.todo.activities.TodoListActivity.Companion.hidePlaceHolder
import com.sstechcanada.todo.activities.TodoListActivity.Companion.showPlaceHolder
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.adapters.TodoListFirestoreAdapter.TodoListFirestoreHolder
import com.sstechcanada.todo.models.TodoTaskFirestore
import es.dmoral.toasty.Toasty
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
    var userID = user!!.uid
    private var userColRef = MasterTodoListActivity.listId?.let {
        db.collection("Users").document(userID).collection("Lists")
            .document(it).collection("Todo")
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(
        holder: TodoListFirestoreHolder,
        position: Int,
        model: TodoTaskFirestore
    ) {
        holder.tvTextDesc.text = model.description

        val colors = intArrayOf(
            R.color.chip_1,
            R.color.chip_2,
            R.color.chip_3,
            R.color.chip_4,
            R.color.chip_5,
            R.color.chip_6,
            R.color.chip_7,
            R.color.chip_8,
            R.color.chip_9,
            R.color.chip_10
        )
        holder.chipGroup.removeAllViews()
        holder.chipGroup.visibility = View.VISIBLE

        (0 until model.benefits.size).forEach { i ->
            val chip = Chip(context)
            val drawable = ChipDrawable.createFromAttributes(
                context,
                null,
                0,
                R.style.ThinnerChip
            )
            chip.setChipDrawable(drawable)

            if (i in 0..9) {
                chip.setChipBackgroundColorResource(colors[i])
            } else if (i >= 10) {
                chip.setChipBackgroundColorResource(colors[i % 10])
            }
            chip.text = model.benefits[i] + ""
            holder.chipGroup.childCount
            chip.maxWidth = 180
            chip.minWidth = 180
            chip.minHeight = 54
            chip.ellipsize = TextUtils.TruncateAt.END
            chip.setTextAppearanceResource(R.style.SmallerText)
            holder.chipGroup.addView(chip)
        }


        holder.customCheckbox.isChecked = (model.status == "Completed")

        val magnitudeCircle = holder.circleDot.background as GradientDrawable
        val magnitudeColor = getMagnitudeColor(position % 9)
        magnitudeCircle.setColor(magnitudeColor)

        val documentSnapshot = snapshots.getSnapshot(position)
        model.documentID = documentSnapshot.id
        holder.customCheckbox.setOnClickListener {
            Log.i("onclick", "checkbox")
            if (holder.customCheckbox.isChecked) {
                AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Confirm Complete")
                    .setMessage("Are you sure you want to mark this task as completed?")
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        val updateTaskMap: MutableMap<String, Any> = HashMap()
                        val taskStatus = "Completed"
                        val calendar = Calendar.getInstance()
                        val dateStr = DateFormat.getDateInstance(DateFormat.FULL)
                            .format(calendar.time)
                        val sdf = SimpleDateFormat("h:mm a")
                        Log.i("dateTime", "TimestampCompleted$dateStr")
                        val timeStr = sdf.format(calendar.time)
                        updateTaskMap["TimestampCompleted"] = "$dateStr $timeStr"
                        updateTaskMap["Status"] = taskStatus
                        userColRef?.document(model.documentID)
                            ?.set(updateTaskMap, SetOptions.merge())?.addOnSuccessListener {
                                Toasty.success(context, "Todo-task marked as completed")
                                    .show()
                            }?.addOnFailureListener {
                                Toasty.error(context, "Something went wrong").show()
                            }
                        //
                    }
                    .setNegativeButton("No"
                    ) { _, _ -> holder.customCheckbox.isChecked = false }
                    .setCancelable(false)
                    .show()
            } else {
                AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Confirm Incomplete")
                    .setMessage("Are you sure you want to mark this task as incomplete?")
                    .setPositiveButton("Yes"
                    ) { _, _ ->
                        val updateTaskMap: MutableMap<String, Any> = HashMap()
                        val taskStatus = "Pending"
                        val calendar = Calendar.getInstance()
                        val dateStr = DateFormat.getDateInstance(DateFormat.FULL)
                            .format(calendar.time)
                        val sdf = SimpleDateFormat("h:mm a")
                        Log.i("dateTime", "TimestampCompleted$dateStr")
                        val timeStr = sdf.format(calendar.time)
                        updateTaskMap["TimestampCompleted"] = "$dateStr $timeStr"
                        updateTaskMap["Status"] = taskStatus
                        userColRef?.document(model.documentID)
                            ?.set(updateTaskMap, SetOptions.merge())
                            ?.addOnSuccessListener {
                                Toasty.success(
                                    context,
                                    "Todo-task marked as incomplete"
                                ).show()
                            }?.addOnFailureListener {
                                Toasty.error(
                                    context,
                                    "Something went wrong"
                                ).show()
                            }
                        //
                    }
                    .setNegativeButton("No"
                    ) { _, _ -> holder.customCheckbox.isChecked = true }
                    .setCancelable(false)
                    .show()
            }
        }
        holder.cardView.setOnClickListener {
            Log.i("onclick", "card")
            if ((MasterTodoListActivity.purchaseCode == "0") && position + 1 > (LoginActivity.userAccountDetails[1].toInt())
            ) {
                Log.d("subscriptionFeature", "subscription expired!")
                Toasty.warning(
                    context,
                    "Your subscription expired! Renew subscription to continue using premium features",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(context, AppUpgradeActivity::class.java)
                //                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                context.startActivity(intent)
            } else {
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
                val intent = Intent(context, AddOrEditTaskActivity::class.java)
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
        var cardView: MaterialCardView = itemView.findViewById(R.id.materialCard)
        var customCheckbox: CheckBox = itemView.findViewById(R.id.checkb)
        var chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

    }
}