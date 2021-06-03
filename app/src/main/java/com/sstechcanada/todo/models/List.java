package com.sstechcanada.todo.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.sstechcanada.todo.activities.MasterTodoListActivity;

@IgnoreExtraProperties
public class List {
    private String ListId;
    private String ListName;
    private String ListDescription;
    private int Image;
    int positionImage;

    public List() {}


    public List(String ListId, String ListName, int Image, String ListDescription) {
        this.ListId = ListId;
        this.ListName = ListName;
        this.Image = Image;
        this.ListDescription = ListDescription;
    }

    public List(String ListId, String ListName, int positionImage,int Image, String ListDescription) {
        this.ListId = ListId;
        this.ListName = ListName;
        this.Image = Image;
        this.ListDescription = ListDescription;
    }

    public List(String ListId, String ListName, String ListDescription) {
        this.ListId = ListId;
        this.ListName = ListName;
        this.ListDescription = ListDescription;
    }

    public String getListName() {
        return ListName;
    }

    public String getListId() {
        return ListId;
    }

    public String getListDescription() {
        return ListDescription;
    }

    public void setListDescription(String listDescription) {
        ListDescription = listDescription;
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
