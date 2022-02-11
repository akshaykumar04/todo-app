package com.sstechcanada.todo.adapters

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.sstechcanada.todo.adapters.BenefitsAdapter.CategoryFirestoreHolder
import com.google.firebase.auth.FirebaseAuth
import com.sstechcanada.todo.R
import android.content.DialogInterface
import android.view.LayoutInflater
import android.text.TextUtils
import es.dmoral.toasty.Toasty
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.*
import com.sstechcanada.todo.models.Category
import kotlinx.android.synthetic.main.layout_artist_list.view.*


class BenefitsAdapter(options: FirestoreRecyclerOptions<Category?>, var context: Context) :
    FirestoreRecyclerAdapter<Category, CategoryFirestoreHolder>(options) {

    private var mCallbacks: Callbacks? = null

    fun setCallbacks(callbacks: Callbacks) {
        mCallbacks = callbacks
    }

    private val mAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    private val db = FirebaseFirestore.getInstance()
    var userID = user!!.uid
    var benefitCollectionRef = db.collection("Users").document(userID).collection("Benefits")
    var UserColRef = db.collection("Users").document(userID).collection("Lists")
    override fun onBindViewHolder(holder: CategoryFirestoreHolder, position: Int, model: Category) {
        holder.textViewName.text = model.category_name
        val documentSnapshot = snapshots.getSnapshot(position)
        model.categoryId = documentSnapshot.id
        holder.cardView.setOnClickListener { v ->
            showUpdateDialog(
                model.categoryId,
                model.category_name,
                v
            )
        }
        holder.catDelete.setOnClickListener {
            val doc_id = model.categoryId.toString()
            Log.i("onclick", "delete")
            val alert = AlertDialog.Builder(
                context
            )
            alert.setTitle(R.string.delete_benefit)
            alert.setMessage(R.string.delete_benefit_message)
            alert.setPositiveButton(
                R.string.yes
            ) { _: DialogInterface?, id: Int ->
                deleteCategory(
                    doc_id,
                    model.category_name
                )
            }
            alert.setNegativeButton(
                R.string.no
            ) { dialog: DialogInterface, id: Int -> dialog.dismiss() }
            alert.show()
        }
    }

    private fun showUpdateDialog(categoryId: String, categoryName: String, v: View) {
        val dialogBuilder = AlertDialog.Builder(v.rootView.context)
        val dialogView =
            LayoutInflater.from(v.rootView.context).inflate(R.layout.update_dialog, null)
        dialogBuilder.setView(dialogView)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val buttonUpdate = dialogView.findViewById<Button>(R.id.buttonUpdateCategory)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDeleteCategory)
        dialogBuilder.setTitle(categoryName)
        val b = dialogBuilder.create()
        b.show()
        buttonUpdate.setOnClickListener {
            val name: String = editTextName.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(name)) {
                updateCategory(categoryId, name, categoryName)
                b.dismiss()
            } else {
                Toasty.warning(context, "Please enter a Benefit name", Toast.LENGTH_SHORT).show()
            }
        }
        buttonDelete.setOnClickListener { view: View? ->
            val alert: AlertDialog.Builder = AlertDialog.Builder(
                context
            )
            alert.setTitle(R.string.delete_benefit)
            alert.setMessage(R.string.delete_benefit_message)
            alert.setPositiveButton(
                R.string.yes
            ) { _: DialogInterface?, _: Int ->
                deleteCategory(
                    categoryId,
                    categoryName
                )
            }
            b.dismiss()
            alert.setNegativeButton(
                R.string.no
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            alert.show()
        }
    }

    private fun updateCategory(id: String, name: String, oldName: String) {
        mCallbacks?.showProgressBar()
        (context as Activity).window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        //getting the specified category reference
        val documentReferenceBenefitReference = benefitCollectionRef.document(id)

//      DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //updating category
        //getting the specified category reference

        //removing category
        documentReferenceBenefitReference.update("category_name", name)
            .addOnSuccessListener { //                Toasty.success(getApplicationContext(), "Benefit Updated", Toast.LENGTH_SHORT).show();
                updateCategoryFromEachTodo(name, oldName)
            }.addOnFailureListener {
                Toasty.error(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                mCallbacks?.hideProgressBar()
                (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        //        Category category = new Category(id, name);
//        documentReferenceBenefitReference.update("category_name",name);
//        Toasty.success(getApplicationContext(), "Benefits Updated", Toast.LENGTH_SHORT).show();
    }

    private fun updateCategoryFromEachTodo(
        categoryNameToBeUpdated: String,
        oldCategoryName: String
    ) {
        UserColRef.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots) {
                UserColRef.document(documentSnapshot.id).collection("Todo")
                    .whereArrayContains("Benefits", oldCategoryName).get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (documentSnapshotInner: DocumentSnapshot in queryDocumentSnapshots) {
                            UserColRef.document(documentSnapshot.id).collection("Todo")
                                .document(documentSnapshotInner.id).update(
                                    "Benefits",
                                    FieldValue.arrayRemove(oldCategoryName),
                                    "Benefits",
                                    FieldValue.arrayUnion(categoryNameToBeUpdated)
                                ).addOnSuccessListener { }
                                .addOnFailureListener { }
                        }
                    }.addOnFailureListener { }
            }
            Toasty.success(context, "Benefit Updated", Toast.LENGTH_SHORT).show()
            mCallbacks?.hideProgressBar()
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }.addOnFailureListener {
            mCallbacks?.hideProgressBar()
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun deleteCategory(id: String, categoryName: String) {
        mCallbacks?.showProgressBar()
        (context as Activity).window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        benefitCollectionRef.document(id).delete()
            .addOnSuccessListener { deleteCategoryFromEachTodo(categoryName) }
            .addOnFailureListener {
                Toasty.error(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                mCallbacks?.hideProgressBar()
                (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
    }

    private fun deleteCategoryFromEachTodo(categoryToBeDeletedName: String) {
        UserColRef.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots) {
                Log.i("DeletionLogs", "B" + documentSnapshot.id)
                UserColRef.document(documentSnapshot.id).collection("Todo")
                    .whereArrayContains("Benefits", categoryToBeDeletedName).get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (documentSnapshotInner: DocumentSnapshot in queryDocumentSnapshots) {
                            //                                List<String> benefitsList=(List<String>)documentSnapshotInner.get("Benefits");
                            //                                if(benefitsList.contains(categoryToBeDeletedName)){
                            UserColRef.document(documentSnapshot.id).collection("Todo")
                                .document(documentSnapshotInner.id).update(
                                    "Benefits",
                                    FieldValue.arrayRemove(categoryToBeDeletedName),
                                    "priority",
                                    FieldValue.increment(-1)
                                ).addOnSuccessListener {
                                    Log.i(
                                        "DeletionLogs",
                                        "Benefit Deleted From Each To-Do!"
                                    )
                                }.addOnFailureListener {
                                    Log.i(
                                        "DeletionLogs",
                                        "Benefit Not Deleted From Each To-Do!"
                                    )
                                }
                        }
                    } //                        }
                    .addOnFailureListener { Log.i("DeletionLogs", "Bnoasdjfoad") }
            }
            Toasty.success(context, "Benefit Deleted", Toast.LENGTH_SHORT).show()
            mCallbacks?.hideProgressBar()
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }.addOnFailureListener { //
            mCallbacks?.hideProgressBar()
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryFirestoreHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_artist_list, parent, false)
        return CategoryFirestoreHolder(v)
    }

    class CategoryFirestoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.textViewName
        var catDelete: ImageView = itemView.catDelete
        var catEdit: ImageView = itemView.catEdit
        var cardView: MaterialCardView = itemView.catCard
    }

    interface Callbacks {
        fun showProgressBar()
        fun hideProgressBar()
    }
}