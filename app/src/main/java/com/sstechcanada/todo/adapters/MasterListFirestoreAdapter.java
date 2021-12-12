package com.sstechcanada.todo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.AppUpgradeActivity2;
import com.sstechcanada.todo.activities.TodoListActivity2;
import com.sstechcanada.todo.models.List;

import es.dmoral.toasty.Toasty;

import static com.sstechcanada.todo.activities.MasterTodoListActivity.listId;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.listName;
import static com.sstechcanada.todo.activities.MasterTodoListActivity.purchaseCode;
import static com.sstechcanada.todo.activities.auth.LoginActivity.userAccountDetails;

public class MasterListFirestoreAdapter extends FirestoreRecyclerAdapter<List, MasterListFirestoreAdapter.MasterListFirestoreHolder> {

    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    String userID=user.getUid();
    Context context;
    private DatabaseReference databaseReference;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference usersColRef=db.collection("Users");

    public MasterListFirestoreAdapter(@NonNull FirestoreRecyclerOptions<List> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MasterListFirestoreHolder holder, int position, @NonNull List model) {

        try {
            holder.tv_todo_list_name.setText(model.getListName());
//            if(model.getListName()!=null && (!model.getListName().equals(""))){
//                listName=model.getListName();
//            }else{
//                listName="Untitled List";
//            }
//            holder.tv_todo_list_desc.setText(model.getListDescription());
            Drawable drawable = context.getResources().getDrawable(model.getImage());
            if(drawable!=null ){
                holder.list_default_icon.setBackground(drawable);
            }

        }catch (Exception e){
            Log.i("TAG",e.getMessage().trim());
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(purchaseCode.equals("0") && position+1>(Integer.parseInt(userAccountDetails.get(0)))){

                    Log.d("subscriptionFeature","subscription expired!");
                    Toasty.warning(context, "Your subscription expired! Renew subscription to continue using premium features", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), AppUpgradeActivity2.class);
//                        intent.putExtra(getString(R.string.intent_adding_or_editing_key), getString(R.string.add_new_task));
                    v.getContext().startActivity(intent);
                }else{
                    DocumentSnapshot documentSnapshot=getSnapshots().getSnapshot(position);
                    model.setListId(documentSnapshot.getId());
                    listId= model.getListId();
                    Log.i("ListId", "Firestore: "+listId);
                    FirebaseCrashlytics.getInstance().log(this.getClass().getSimpleName()+"listId = "+listId);
                    Intent intent = new Intent(v.getContext(), TodoListActivity2.class);
                    intent.putExtra("ListId",listId);
                    if(model.getListName()!=null && (!model.getListName().equals(""))){
                        listName=model.getListName();
                    }else{
                        listName="Untitled List";
                    }
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    @NonNull
    @Override
    public MasterListFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_master_todo_list,
                parent,false);
        return new MasterListFirestoreHolder(v);
    }

    class MasterListFirestoreHolder extends RecyclerView.ViewHolder{

        TextView tv_todo_list_name, list_default_icon;
        CardView cardView;
        public MasterListFirestoreHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.materialCard);
//            tv_todo_list_desc = itemView.findViewById(R.id.tv_todo_list_desc);
            list_default_icon = itemView.findViewById(R.id.list_default_icon);
            tv_todo_list_name = itemView.findViewById(R.id.tv_todo_list_name);

        }
    }

//    @Override
//    public void onDataChanged() {
//        super.onDataChanged();
//        if(getItemCount()<=2){
//            showPlaceHolder();
//        } else{
//            hidePlaceHolder();
//        }
//    }

//
}

