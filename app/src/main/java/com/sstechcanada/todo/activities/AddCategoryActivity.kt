package com.sstechcanada.todo.activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.sstechcanada.todo.adapters.CategoryFirestoreAdapter
import android.os.Bundle
import com.sstechcanada.todo.R
import android.content.Intent
import android.view.View
import android.widget.*
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.google.android.gms.ads.AdView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import com.google.firebase.firestore.Query
import com.sstechcanada.todo.models.Category
import kotlinx.android.synthetic.main.act_bar.*
import kotlinx.android.synthetic.main.activity_category.*
import java.util.HashMap

class AddCategoryActivity : AppCompatActivity(),
    CategoryFirestoreAdapter.Callbacks {
    var userID: String? = null
    private var mAuth: FirebaseAuth? = null
    var db = FirebaseFirestore.getInstance()
    private var benefitCollectionRef: CollectionReference? = null
    private var userColRef: CollectionReference? = null
    private var categoryFirestoreAdapter: CategoryFirestoreAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        benefitCollectionRef = db.collection("Users").document(userID!!).collection("Benefits")
        userColRef = db.collection("Users").document(userID!!).collection("Lists")
        arrow_back.visibility = View.VISIBLE
        arrow_back.setOnClickListener { super.onBackPressed() }
        setUpRecyclerView()
        categoryFirestoreAdapter?.setCallbacks(this@AddCategoryActivity)
        toolbarTitle.text = getString(R.string.add_benefit)
        profile_toolbar.setOnClickListener {
            startActivity(
                Intent(this@AddCategoryActivity, LoginActivity::class.java)
            )
        }
        buttonAddCategory.setOnClickListener { addCategory() }
        if (MasterTodoListActivity.purchaseCode == "0") {
            val adView = findViewById<AdView>(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun setUpRecyclerView() {
        val query: Query? = benefitCollectionRef
        val options = query?.let {
            FirestoreRecyclerOptions.Builder<Category>().setQuery(
                it, Category::class.java
            ).build()
        }
        categoryFirestoreAdapter = options?.let { CategoryFirestoreAdapter(it, this) }
        listViewCategory.setHasFixedSize(true)
        listViewCategory.layoutManager = LinearLayoutManager(this)
        listViewCategory.adapter = categoryFirestoreAdapter
    }

    override fun onStart() {
        super.onStart()
        categoryFirestoreAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        categoryFirestoreAdapter!!.stopListening()
    }

    private fun addCategory() {
        //getting the values to save
        val name = editTextName!!.text.toString().trim { it <= ' ' }

        //checking if the value is provided
        if (name.isNotEmpty()) {
            val documentReferenceCurrentReference = benefitCollectionRef!!.document()
            val category: MutableMap<String, String> = HashMap()
            category["category_name"] = name
            documentReferenceCurrentReference.set(category, SetOptions.merge())
                .addOnSuccessListener {
                    Toasty.success(
                        applicationContext, "Benefit added", Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toasty.error(
                        applicationContext,
                        "Benefit addition Failed: ",
                        Toast.LENGTH_LONG
                    ).show()
                }

//            //Saving the Category
//            databaseCategories.child(id).setValue(category);

            //setting edittext to blank again
            editTextName!!.setText("")

            //displaying a success toast
        } else {
            //if the value is not given displaying a toast
            Toasty.warning(this, "Please enter a name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showProgressBar() {
        progressCat.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressCat.visibility = View.INVISIBLE
    }

}