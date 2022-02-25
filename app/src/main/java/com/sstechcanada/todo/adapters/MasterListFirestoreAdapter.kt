package com.sstechcanada.todo.adapters

import android.content.Context
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.sstechcanada.todo.adapters.MasterListFirestoreAdapter.MasterListFirestoreHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sstechcanada.todo.activities.MasterTodoListActivity
import com.sstechcanada.todo.activities.auth.LoginActivity
import es.dmoral.toasty.Toasty
import android.widget.Toast
import android.content.Intent
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sstechcanada.todo.activities.TodoListActivity
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.sstechcanada.todo.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.sstechcanada.todo.activities.AppUpgradeActivity
import com.sstechcanada.todo.models.List
import java.lang.Exception

class MasterListFirestoreAdapter(options: FirestoreRecyclerOptions<List?>, var context: Context) :
    FirestoreRecyclerAdapter<List, MasterListFirestoreHolder>(options) {

    private val mAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    var userID = user?.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onBindViewHolder(holder: MasterListFirestoreHolder, position: Int, model: List) {
        try {
            holder.tv_todo_list_name.text = model.listName
            val drawable = context.resources.getDrawable(model.image)
            if (drawable != null) {
                holder.list_default_icon.background = drawable
            }
        } catch (e: Exception) {
            Log.i("TAG", e.message!!.trim { it <= ' ' })
        }
        holder.cardView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (MasterTodoListActivity.purchaseCode == "0" && position + 1 > LoginActivity.userAccountDetails[0].toInt()) {
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
                    val documentSnapshot = snapshots.getSnapshot(position)
                    model.listId = documentSnapshot.id
                    MasterTodoListActivity.listId = model.listId
                    Log.i("ListId", "Firestore: " + MasterTodoListActivity.listId)
                    FirebaseCrashlytics.getInstance()
                        .log(this.javaClass.simpleName + "listId = " + MasterTodoListActivity.listId)
                    val intent = Intent(context, TodoListActivity::class.java)
                    intent.putExtra("ListId", MasterTodoListActivity.listId)
                    if (model.listName != null && model.listName != "") {
                        MasterTodoListActivity.listName = model.listName
                    } else {
                        MasterTodoListActivity.listName = "Untitled List"
                    }
                    intent.putExtra("ListName", MasterTodoListActivity.listName)
                    context.startActivity(intent)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasterListFirestoreHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.item_master_todo_list,
            parent, false
        )
        return MasterListFirestoreHolder(v)
    }

    inner class MasterListFirestoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_todo_list_name: TextView = itemView.findViewById(R.id.tv_todo_list_name)
        var list_default_icon: TextView = itemView.findViewById(R.id.list_default_icon)
        var cardView: CardView = itemView.findViewById(R.id.materialCard)
    }
}