package com.sstechcanada.todo.models;

import java.util.ArrayList;

public class TodoTaskFirestore {
    private String description;
    private int priority;
    private long dueDate;
    private int id;
    private int completed;
    private String category;
    private int category_count;
    private ArrayList<String> Benefits;
    private String benefitsString;

    public TodoTaskFirestore(){}

    public TodoTaskFirestore(String description, int priority, long dueDate, int id, int completed, String category, int category_count, ArrayList<String> Benefits) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.id = id;
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

    public int getId() {
        return id;
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


}
