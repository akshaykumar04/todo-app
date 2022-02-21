package com.sstechcanada.todo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Category {

    private String categoryId;
    private String category_name;


    public Category() {}

    public Category(String categoryId, String category_name) {
        this.categoryId = categoryId;
        this.category_name = category_name;
    }


    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public Category( String category_name) {
        this.category_name = category_name;
    }

    public String getCategoryId() {
        return categoryId;
    }


    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }


}
