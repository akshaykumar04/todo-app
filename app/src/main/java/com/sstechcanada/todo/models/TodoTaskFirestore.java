package com.sstechcanada.todo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class TodoTaskFirestore implements Parcelable {
    private String description;


    private int priority;
    private long dueDate;
    private String documentID;
//    private int completed;
    private String Status;
    private String category;
    private int category_count;
    private ArrayList<String> Benefits;
    private String benefitsString;

        public TodoTaskFirestore(){}


    protected TodoTaskFirestore(Parcel in) {
        description = in.readString();
        priority = in.readInt();
        dueDate = in.readLong();
        documentID = in.readString();
        Status=in.readString();
//        completed = in.readInt();
        category = in.readString();
        category_count = in.readInt();
        Benefits = in.createStringArrayList();
        benefitsString = in.readString();
    }

    public static final Creator<TodoTaskFirestore> CREATOR = new Creator<TodoTaskFirestore>() {
        @Override
        public TodoTaskFirestore createFromParcel(Parcel in) {
            return new TodoTaskFirestore(in);
        }

        @Override
        public TodoTaskFirestore[] newArray(int size) {
            return new TodoTaskFirestore[size];
        }
    };

    public void setBenefitsString(String benefitsString) {
        this.benefitsString = benefitsString;
    }

    public TodoTaskFirestore(String description, int priority, long dueDate, String documentID,  String Status, String category, int category_count, ArrayList<String> Benefits,String benefitsString) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.documentID=documentID;
        this.Status=Status;
//        this.completed = completed;
        this.category = category;
        this.category_count = category_count;
        this.Benefits = Benefits;
        this.benefitsString=benefitsString;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
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

//    public int getCompleted() {
//        return completed;
//    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeInt(priority);
        parcel.writeLong(dueDate);
        parcel.writeString(documentID);
        parcel.writeString(Status);
//        parcel.writeInt(completed);
        parcel.writeString(category);
        parcel.writeInt(category_count);
        parcel.writeStringList(Benefits);
        parcel.writeString(benefitsString);
    }
//
//    public TodoTaskFirestore(String description, int priority, long dueDate, String documentID, int completed,String benefitsString,ArrayList<String> Benefits) {
//        this.description = description;
//        this.priority = priority;
//        this.dueDate = dueDate;
//        this.documentID = documentID;
//        this.completed = completed;
//        this.benefitsString = benefitsString;
//        this.Benefits=Benefits;
//    }


//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }



    //creator - used when un-parceling our parcle (creating the object)






}
