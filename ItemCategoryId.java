package com.example.simplememo;

public class ItemCategoryId {

    private int _categoryId;
    private int _memoId;

    public ItemCategoryId() {
        //super();
    }

    public ItemCategoryId(int categoryId, int memoId) {
        //super();
        _categoryId = categoryId;
        _memoId = memoId;
    }


    public int getMemoId() {
        return _memoId;
    }


    public int getCategoryId() {
        return _categoryId;
    }



}
