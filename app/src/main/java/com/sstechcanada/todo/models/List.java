package com.sstechcanada.todo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class List {
    private String ListId;
    private String ListName;
    private String ListDescription;
    private String Image;


    public List() {}


    public List(String ListId, String ListName, String Image, String ListDescription) {
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

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
