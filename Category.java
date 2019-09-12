package com.example.simplememo;

public class Category {

    private int id;
    private String name;

    public Category() {
        //super();
    }

    public Category(int id, String name) {
        //super();
        this.id = id;
        this.name = name;
    }

    public int getCategoryId() {
        return id;
    }

    public String getCategoryTitle() {
        return name;
    }

}
