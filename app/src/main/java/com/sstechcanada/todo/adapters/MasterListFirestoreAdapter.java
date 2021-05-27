package com.sstechcanada.todo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sstechcanada.todo.R;
import com.sstechcanada.todo.activities.AddCategoryActivity2;
import com.sstechcanada.todo.activities.AddOrEditTaskActivity2;
import com.sstechcanada.todo.activities.TodoListActivity;
import com.sstechcanada.todo.activities.TodoListActivity2;
import com.sstechcanada.todo.custom_views.PriorityStarImageView;
import com.sstechcanada.todo.models.List;
import com.sstechcanada.todo.models.TodoTaskFirestore;

public class MasterListFirestoreAdapter extends FirestoreRecyclerAdapter<List, MasterListFirestoreAdapter.MasterListFirestoreHolder> {

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
    public MasterListFirestoreAdapter(@NonNull FirestoreRecyclerOptions<List> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MasterListFirestoreHolder holder, int position, @NonNull List model) {

        try {
            holder.tv_todo_list_name.setText(model.getListName());
            holder.tv_todo_list_desc.setText(model.getListDescription());
            String res =model.getImage();
            if(res!=null || res!=""){
                int imageResource = context.getResources().getIdentifier(res, null, context.getPackageName());
                Drawable drawable = context.getResources().getDrawable(imageResource);
                holder.list_default_icon.setBackground(drawable);
            }

        }catch (Exception e){
            Log.i("TAG",e.getMessage().trim());
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentSnapshot documentSnapshot=getSnapshots().getSnapshot(position);
                model.setListId(documentSnapshot.getId());
                String listId= model.getListId();

                Intent intent = new Intent(v.getContext(), TodoListActivity2.class);
                intent.putExtra("ListId",listId);
                v.getContext().startActivity(intent);

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

        TextView tv_todo_list_name, list_default_icon, tv_todo_list_desc;
        CardView cardView;
        public MasterListFirestoreHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.materialCard);
            tv_todo_list_desc = itemView.findViewById(R.id.tv_todo_list_desc);
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

//    private int getMagnitudeColor(int pos) {
//        int magnitudeColorResourceId;
//        int magnitudeFloor = pos;
//        switch (magnitudeFloor) {
//            case 0:
//                magnitudeColorResourceId = R.color.circle8;
//                break;
//            case 1:
//                magnitudeColorResourceId = R.color.circle1;
//                break;
//            case 2:
//                magnitudeColorResourceId = R.color.circle2;
//                break;
//            case 3:
//                magnitudeColorResourceId = R.color.circle3;
//                break;
//            case 4:
//                magnitudeColorResourceId = R.color.circle4;
//                break;
//            case 5:
//                magnitudeColorResourceId = R.color.circle5;
//                break;
//            case 6:
//                magnitudeColorResourceId = R.color.circle6;
//                break;
//            case 7:
//                magnitudeColorResourceId = R.color.circle7;
//                break;
//            case 8:
//                magnitudeColorResourceId = R.color.circle8;
//                break;
//            case 9:
//                magnitudeColorResourceId = R.color.circle9;
//                break;
//            default:
//                magnitudeColorResourceId = R.color.circle1;
//                break;
//        }
//        return ContextCompat.getColor(context, magnitudeColorResourceId);
//    }
}

