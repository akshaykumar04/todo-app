package com.sstechcanada.todo.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.models.Category;

import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.AddCategoryActivity2.hideProgressbar;
import static com.sstechcanada.todo.activities.AddCategoryActivity2.showProgressbar;

public class CategoryFirestoreAdapter extends FirestoreRecyclerAdapter<Category, CategoryFirestoreAdapter.CategoryFirestoreHolder> {

    Context context;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID = user.getUid();
    CollectionReference benefitCollectionRef=db.collection("Users").document(userID).collection("Benefits");
    CollectionReference UserColRef=db.collection("Users").document(userID).collection("Lists");
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CategoryFirestoreAdapter(@NonNull FirestoreRecyclerOptions<Category> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull CategoryFirestoreHolder holder, int position, @NonNull Category model) {
        holder.textViewName.setText(model.getCategory_name());
        DocumentSnapshot documentSnapshot=getSnapshots().getSnapshot(position);
        model.setCategoryId(documentSnapshot.getId());


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog(model.getCategoryId(),model.getCategory_name(),v);
            }
        });

        holder.catDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String doc_id = String.valueOf(model.getCategoryId());

                Log.i("onclick", "delete");
                androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(context);
                alert.setTitle(R.string.delete_benefit);
                alert.setMessage(R.string.delete_benefit_message);
                alert.setPositiveButton(
                        R.string.yes,
                        (dialog, id) -> deleteCategory(doc_id, model.getCategory_name()));

                alert.setNegativeButton(
                        R.string.no,
                        (dialog, id) -> dialog.dismiss());
                alert.show();

            }
        });
    }

    private void showUpdateDialog(final String categoryId, String categoryName,View v) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getRootView().getContext());
        View dialogView =  LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateCategory);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteCategory);

        dialogBuilder.setTitle(categoryName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                updateCategory(categoryId, name, categoryName);
                b.dismiss();
            } else {
                Toasty.warning(context, "Please enter a Benefit name", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDelete.setOnClickListener(view -> {
            androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(context);
            alert.setTitle(R.string.delete_benefit);
            alert.setMessage(R.string.delete_benefit_message);
            alert.setPositiveButton(
                    R.string.yes,
                    (dialog, id) -> deleteCategory(categoryId, categoryName));
            b.dismiss();

            alert.setNegativeButton(
                    R.string.no,
                    (dialog, id) -> dialog.dismiss());
            alert.show();
        });
    }

    private void updateCategory(String id, String name,String oldName) {
        showProgressbar();
        ((Activity)context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //getting the specified category reference
        DocumentReference documentReferenceBenefitReference=benefitCollectionRef.document(id);

//      DatabaseReference dR = FirebaseDatabase.getInstance().getReference("categories").child(id);
        //updating category
        //getting the specified category reference

        //removing category
        documentReferenceBenefitReference.update("category_name",name).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toasty.success(getApplicationContext(), "Benefit Updated", Toast.LENGTH_SHORT).show();
                updateCategoryFromEachTodo(name,oldName);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
//        Category category = new Category(id, name);
//        documentReferenceBenefitReference.update("category_name",name);
//        Toasty.success(getApplicationContext(), "Benefits Updated", Toast.LENGTH_SHORT).show();
    }

    private void updateCategoryFromEachTodo(String categoryNameToBeUpdated, String oldCategoryName) {

        UserColRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots){

                    UserColRef.document(documentSnapshot.getId()).collection("Todo").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(DocumentSnapshot documentSnapshotInner:queryDocumentSnapshots){
                                UserColRef.document(documentSnapshot.getId()).collection("Todo").document(documentSnapshotInner.getId()).update("Benefits", FieldValue.arrayRemove(oldCategoryName),"Benefits",FieldValue.arrayUnion(categoryNameToBeUpdated)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
                Toasty.success(context, "Benefit Updated", Toast.LENGTH_SHORT).show();
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }


    private void deleteCategory(String id,String categoryName) {
        showProgressbar();
        ((Activity)context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //getting the specified category reference
        DocumentReference documentReferenceBenefitReference=benefitCollectionRef.document(id);
        //removing category
        documentReferenceBenefitReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleteCategoryFromEachTodo(categoryName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

    }

    private void deleteCategoryFromEachTodo(String categoryToBeDeletedName) {

        UserColRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                    Log.i("DeletionLogs", "B"+documentSnapshot.getId());
                    UserColRef.document(documentSnapshot.getId()).collection("Todo").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(DocumentSnapshot documentSnapshotInner:queryDocumentSnapshots){
                                List<String> benefitsList=(List<String>)documentSnapshotInner.get("Benefits");
                                if(benefitsList.contains(categoryToBeDeletedName)){

                                    UserColRef.document(documentSnapshot.getId()).collection("Todo").document(documentSnapshotInner.getId()).update("Benefits", FieldValue.arrayRemove(categoryToBeDeletedName),"priority",FieldValue.increment(-1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("DeletionLogs", "Benefit Deleted From Each To-Do!");

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("DeletionLogs","Benefit Not Deleted From Each To-Do!");
                                        }
                                    });

                                }



                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("DeletionLogs", "Bnoasdjfoad");
                        }
                    });
                }
                Toasty.success(context, "Benefit Deleted", Toast.LENGTH_SHORT).show();
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressbar();
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    @NonNull
    @Override
    public CategoryFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_artist_list, parent, false);

        return new CategoryFirestoreHolder(v);
    }


    static class CategoryFirestoreHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        ImageView catDelete,catEdit;
        MaterialCardView cardView;
//        ConstraintLayout constraintLayoutCat;

        public CategoryFirestoreHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.catCard);
            catDelete=itemView.findViewById(R.id.catDelete);
            catEdit=itemView.findViewById(R.id.catEdit);
            textViewName = itemView.findViewById(R.id.textViewName);
//            constraintLayoutCat=itemView.findViewById(R.id.constraintLayoutCat);

        }
    }
}


