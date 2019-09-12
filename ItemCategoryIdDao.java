package com.example.simplememo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemCategoryIdDao extends AppCompatActivity implements AutoCloseable {

    private MyOpenHelper _helper;


    public ItemCategoryIdDao(MyOpenHelper helper) throws ClassNotFoundException, SQLException {
        //super();
        _helper = helper;
    }



    public void addItemCategoryId(ItemCategoryId itemCategoryId) {
        Log.i("aaaaaaaaa", "itemCategoryId.getMemoId() " + itemCategoryId.getMemoId());
        Log.i("aaaaaaaaa", "itemCategoryId.getCategoryId() " + itemCategoryId.getCategoryId());
        if( !( checkItemCategoryId(itemCategoryId.getMemoId(), itemCategoryId.getCategoryId()) ) ) {
            SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
            SQLiteStatement stmt = null;
            try {
                stmt = sqLiteDatabase.compileStatement("INSERT INTO simple_memo_item_category_ids(id, category_id) VALUES(?,?)");
                // メモIDとカテゴリIDの組み合わせが未登録の時にインサート

                stmt.bindLong(1, itemCategoryId.getMemoId());
                stmt.bindLong(2, itemCategoryId.getCategoryId());
                stmt.executeInsert();

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


    public void removeItemCategoryIdByMemoId(ItemCategoryId itemCategoryId) {
        if( checkItemCategoryId(itemCategoryId.getMemoId(), itemCategoryId.getCategoryId()) ) {
            SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
            SQLiteStatement stmt = null;
            try {

                stmt = sqLiteDatabase.compileStatement("DELETE FROM simple_memo_item_category_ids WHERE category_id = ? " +
                        (itemCategoryId.getMemoId() != 0 ? "AND id = ? ":"") );
                stmt.bindLong(1, itemCategoryId.getCategoryId());
                if (itemCategoryId.getMemoId() != 0) {
                    stmt.bindLong(2, itemCategoryId.getMemoId());
                }
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


    /** メモIDから登録済みカテゴリIDリストを取得。 **/
    public List<Integer> getItemCategoryIdListByMemoId(int memoId) {
        List<Integer> itemCategoryIdList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Log.i("aaaaaaaaa", "btDuplication: 2 - 1");
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT id, category_id " +
                    "FROM simple_memo_item_category_ids " +
                    "WHERE id = '" + memoId + "' " , null);

            while(cursor.moveToNext()) {
                itemCategoryIdList.add(cursor.getInt(1));
            }

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
        Log.i("aaaaaaaaa", "btDuplication: 2 - 2");
        return itemCategoryIdList;
    }




    /** メモIDとカテゴリIDの組み合わせが、登録済みかチェックする。 **/
    private boolean checkItemCategoryId(int memoId,int categoryId) {
        boolean checkker;
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT id, category_id " +
                                                  "FROM simple_memo_item_category_ids " +
                                                  "WHERE id = '" + memoId + "' AND category_id = '" + categoryId + "' " , null);

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
