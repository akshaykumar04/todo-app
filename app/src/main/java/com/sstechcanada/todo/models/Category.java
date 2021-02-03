package com.sstechcanada.todo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Category {
    private String categoryId;
    private String categoryName;

    public Category() {}

    public Category(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
