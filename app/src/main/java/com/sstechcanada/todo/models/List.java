package com.sstechcanada.todo.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.sstechcanada.todo.activities.MasterTodoListActivity;

@IgnoreExtraProperties
public class List {
    private String ListId;
    private String ListName;
//    private String ListDescription;
    private int Image;
    int positionImage;

    public List() {}


    public List(String ListId, String ListName, int Image) {
        this.ListId = ListId;
        this.ListName = ListName;
        this.Image = Image;
//        this.ListDescription = ListDescription;
    }

    public List(String ListId, String ListName, int positionImage,int Image) {
        this.ListId = ListId;
        this.ListName = ListName;
        this.Image = Image;
    }

    public List(String ListId, String ListName) {
        this.ListId = ListId;
        this.ListName = ListName;
    }

    public String getListName() {
        return ListName;
    }

    public String getListId() {
        return ListId;
    }

    public void setListId(String listId) {
        ListId = listId;
    }

    public void setListName(String listName) {
        ListName = listName;
    }

    public int getPositionImage() {
        return positionImage;
    }

    public void setPositionImage(int positionImage) {
        this.positionImage = positionImage;
    }

    public int getImage() {
        if (positionImage>=0 && MasterTodoListActivity.listDrawable.length>positionImage){
            return MasterTodoListActivity.listDrawable[positionImage];
        }

        return MasterTodoListActivity.listDrawable[0];

    }

    public void setImage(int image) {
        Image = image;
    }
}
