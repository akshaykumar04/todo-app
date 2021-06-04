package com.sstechcanada.todo.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.adapters.CategoryAdapter
import com.sstechcanada.todo.models.Category
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.update_dialog.view.*
import java.util.*

class AddCategoryActivity : AppCompatActivity() {
    //view objects
    var categories: MutableList<Category?>? = null
    private var databaseCategories: DatabaseReference? = null
    var userID: String? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid


        databaseCategories = FirebaseDatabase.getInstance().getReference(userID!!).child("benefits")
        arrow_back.visibility = View.VISIBLE
        arrow_back.setOnClickListener { super.onBackPressed() }
        this.categories = ArrayList()
        toolbarTitle.text = getString(R.string.title_add_benefit)
        profile_toolbar.setOnClickListener { startActivity(Intent(this@AddCategoryActivity, LoginActivity::class.java)) }
        buttonAddCategory.setOnClickListener { addCategory() }
        listViewCategory.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
            val category = (categories as ArrayList<Category?>)[i]
            showUpdateDialog(category!!.categoryId, category.categoryName)
        }
        listViewCategory.onItemLongClickListener = OnItemLongClickListener { _, _, i, _ ->
            val category = (categories as ArrayList<Category?>)[i]
            showUpdateDialog(category!!.categoryId, category.categoryName)
            true
        }
        if (MasterTodoListActivity.purchaseCode == "0") {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    /*
     * This method is saving a new category to the
     * Firestore Database
     * */
    private fun addCategory() {
        //getting the values to save
        var name = editTextName!!.text.toString().trim { it <= ' ' }

        //checking if the value is provided
        if (name.isNotEmpty()) {


            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Category
            val id = databaseCategories!!.push().key

            //Making first word capital
            name = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1)
            //creating an Category Object
            val category = Category(id, name)

            //Saving the Category
            databaseCategories!!.child(id!!).setValue(category)

            //setting edittext to blank again
            editTextName!!.setText("")

            //displaying a success toast
            Toasty.success(this, "Benefit added", Toast.LENGTH_SHORT).show()
        } else {
            //if the value is not given displaying a toast
            Toasty.warning(this, "Please enter a name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        //attaching value event listener
        databaseCategories!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //clearing the previous category list
                categories!!.clear()

                //iterating through all the nodes
                for (postSnapshot in dataSnapshot.children) {
                    val category = postSnapshot.getValue(Category::class.java)
                    categories!!.add(category)
                }

                //creating adapter
                val categotyAdapter = CategoryAdapter(this@AddCategoryActivity, categories)
                //attaching adapter to the listview
                listViewCategory!!.adapter = categotyAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showUpdateDialog(categoryId: String, categoryName: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.update_dialog, null)
        dialogBuilder.setView(dialogView)

        dialogBuilder.setTitle(categoryName)
        val b = dialogBuilder.create()
        b.show()
        dialogView.buttonUpdateCategory.setOnClickListener {
            val name = editTextName.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(name)) {
                updateCategory(categoryId, name)
                b.dismiss()
            } else {
                Toasty.warning(this, "Please enter a Benefit name", Toast.LENGTH_SHORT).show()
            }
        }
        dialogView.buttonDeleteCategory.setOnClickListener {
            deleteCategory(categoryId)
            b.dismiss()
        }
    }

    private fun updateCategory(id: String, name: String) {
        //getting the specified category reference
        val dR = FirebaseDatabase.getInstance().getReference(userID!!).child("benefits").child(id)
        //        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //updating category
        val category = Category(id, name)
        dR.setValue(category)
        Toasty.success(applicationContext, "Benefits Updated", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCategory(id: String) {
        //getting the specified category reference
        val dR = FirebaseDatabase.getInstance().getReference(userID!!).child("benefits").child(id)
        //        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //removing category
        dR.removeValue()

        //getting the tracks reference for the specified category
        Toasty.error(applicationContext, "Benefits Deleted", Toast.LENGTH_SHORT).show()
    }
}