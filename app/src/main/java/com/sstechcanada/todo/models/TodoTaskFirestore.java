package com.sstechcanada.todo.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class TodoTaskFirestore implements Parcelable {
    private String description;
    private int priority;
    private long dueDate;
    private String documentID;
    private int completed;
    private String category;
    private int category_count;
    private ArrayList<String> Benefits;
    private String benefitsString;


    public TodoTaskFirestore(){}

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void setBenefitsString(String benefitsString) {
        this.benefitsString = benefitsString;
    }

    public TodoTaskFirestore(String description, int priority, long dueDate, int id, int completed, String category, int category_count, ArrayList<String> Benefits) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
        this.category = category;
        this.category_count = category_count;
        this.Benefits = Benefits;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public long getDueDate() {
        return dueDate;
    }

    @Exclude
    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public int getCompleted() {
        return completed;
    }

    public String getCategory() {
        return category;
    }

    public int getCategory_count() {
        return category_count;
    }

    public ArrayList<String> getBenefits() {
        return Benefits;
    }
    public String getBenefitsString(){

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < Benefits.size(); i++) {
            stringBuilder.append(Benefits.get(i)+", ");
        }
        return benefitsString = stringBuilder.toString();


    }

    public TodoTaskFirestore(String description, int priority, long dueDate, String documentID, int completed,String benefitsString,ArrayList<String> Benefits) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.documentID = documentID;
        this.completed = completed;
        this.benefitsString = benefitsString;
        this.Benefits=Benefits;
    }

    @Override
    public int describeContents() {
        return 0;
    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }
    private TodoTaskFirestore(Parcel in) {
        description = in.readString();
        priority = in.readInt();
        dueDate = in.readLong();
        documentID = in.readString();
        completed = in.readInt();
        benefitsString= in.readString();
//        Benefits=in.readArrayList(new ArrayList[]);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(description);
        parcel.writeInt(priority);
        parcel.writeLong(dueDate);
        parcel.writeString(documentID);
        parcel.writeInt(completed);
        parcel.writeString(benefitsString);
        parcel.writeArray(new ArrayList[]{Benefits});
    }


    //creator - used when un-parceling our parcle (creating the object)
    public static final Parcelable.Creator<TodoTaskFirestore> CREATOR
            = new Parcelable.Creator<TodoTaskFirestore>() {
        @Override
        public TodoTaskFirestore createFromParcel(Parcel in) {
            return new TodoTaskFirestore(in);
        }

        @Override
        public TodoTaskFirestore[] newArray(int size) {
            return new TodoTaskFirestore[size];
        }
    };






}
