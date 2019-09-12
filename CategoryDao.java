package com.example.simplememo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao extends AppCompatActivity implements AutoCloseable {

    private MyOpenHelper _helper;


    public CategoryDao(MyOpenHelper helper) throws ClassNotFoundException, SQLException {
        //super();
        _helper = helper;
    }


    public int getCategoryIdByName(String categoryName) {
        int categoryId;
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT category_id, category_name " +
                    "FROM simple_memo_categorys WHERE category_name = '" + categoryName +"' " , null);
            cursor.moveToNext();
            categoryId = cursor.getInt(0);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (sqLiteDatabase != null) {
                    sqLiteDatabase.close();
                }
            }
        }
        return categoryId;
    }



    public List<Category> getCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Cursor cursor = null;

        try {
            // SELECT及びrawQueryでデータを cursorに取得。
            cursor = sqLiteDatabase.rawQuery("SELECT category_id, category_name " +
                    "FROM simple_memo_categorys WHERE category_id != 1 " , null);
            // cursorの先頭行があるかどうか確認。 取得した全ての行を取得。
            while (cursor.moveToNext()) {
                // 取得したカラム郡の番号と型を指定してデータを取得。
                int categoryId = cursor.getInt(0);
                String categoryName = cursor.getString(1);                                                                    // 1番本文
                categoryList.add(new Category(categoryId,categoryName));
            }
            categoryList.add(getTrash());
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (sqLiteDatabase != null) {
                    sqLiteDatabase.close();
                }
            }
        }
        return categoryList ;
    }

    private Category getTrash() {
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Cursor cursor = null;
        Category trash;

        try {
            // SELECT及びrawQueryでデータを cursorに取得。
            cursor = sqLiteDatabase.rawQuery("SELECT category_id, category_name " +
                    "FROM simple_memo_categorys WHERE category_id == 1 " , null);
            // cursorの先頭行があるかどうか確認。 取得した全ての行を取得。
            cursor.moveToNext();
            // 取得したカラム郡の番号と型を指定してデータを取得。
            int categoryId = cursor.getInt(0);
            String categoryName = cursor.getString(1);                                                                    // 1番本文
            trash = new Category(categoryId,categoryName);

        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (sqLiteDatabase != null) {
                    sqLiteDatabase.close();
                }
            }                                 // SQLiteデータベースをclose。
        }
        return trash ;
    }



    public void addCategory(String string) {
        if( !checkCategory(string) ) {
            SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
            try {
                sqLiteDatabase.execSQL("INSERT INTO simple_memo_categorys(category_name) VALUES ('" + string + "');");
            } finally {
                sqLiteDatabase.close();
            }
        }
    }


    public void deleteCategory(String string) {
        if( checkCategory(string) ) {
            SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
            SQLiteStatement stmt = null;
            try {

                stmt = sqLiteDatabase.compileStatement("DELETE FROM simple_memo_categorys WHERE category_name = ? " );
                stmt.bindString(1, string);

                stmt.executeUpdateDelete();

            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } finally {
                    if (sqLiteDatabase != null) {
                        sqLiteDatabase.close();
                    }
                }
            }
        }

    }




    /** カテゴリが、登録済みかチェックする。 **/
    private boolean checkCategory(String categoryName) {
        boolean checkker;
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT category_name " +
                    "FROM simple_memo_categorys " +
                    "WHERE category_name = '" + categoryName + "' " , null);

            checkker = cursor.moveToNext();

        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (sqLiteDatabase != null) {
                    sqLiteDatabase.close();
                }
            }
        }
        return checkker;
    }




    @Override
    public void close() throws Exception {

    }

}
