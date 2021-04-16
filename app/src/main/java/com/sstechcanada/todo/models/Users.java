package com.sstechcanada.todo.models;

import java.util.List;

public class Users {
    private String name;
    private String email;
    private String purchase_code;
    private String purchase_type;
    private List<String> category;

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public Users(){}
    public Users(String name, String email, String purchse_code,String purchase_type) {
        this.name = name;
        this.email = email;
        this.purchase_code = purchse_code;
        this.purchase_type = purchase_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPurchse_code() {
        return purchase_code;
    }

    public void setPurchse_code(String purchse_code) {
        this.purchase_code = purchse_code;
    }

    public String getPurchase_type() {
        return purchase_type;
    }

    public void setPurchase_type(String purchase_type) {
        this.purchase_type = purchase_type;
    }
}
