package com.example.simplememo;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoDao extends AppCompatActivity implements AutoCloseable {

    private MyOpenHelper _helper;   // データベース・オープンヘルパー参照フィールド。
    private Resources _resources;   // リソース参照フィールド。


    public MemoDao(MyOpenHelper helper) {
        //super();
        this(helper, null);
    }

    public MemoDao(MyOpenHelper helper, Resources resources) {
        //super();
        _helper = helper;
        _resources = resources;
    }


    /** 新規メモをデータベースへ登録 **/
    public int addMemo(Memo memo) {
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        SQLiteStatement stmt = null;                                    // ステートメント参照変数初期化。
        try {
            // 新規作成記事のINSERT INTO
            stmt = sqLiteDatabase.compileStatement("INSERT INTO simple_memo_lists(title, body, create_datetime, update_datetime) " +
                                                         "VALUES(?, ?, ?, ?)");
            stmt.bindString(1, memo.getTitle());
            stmt.bindString(2, memo.getBody());
            stmt.bindString(3, memo.getCreateDatetime());
            stmt.bindString(4, memo.getUpdateDatetime());

            memo.setId( (int)stmt.executeInsert() );      // 2回目以降の保存は既存記事としてUPDATEする為、新規作成したメモIDをセットする。

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
        return memo.getId();    // 新規作成したメモIDを返す。
    }


    /** 更新メモでデータベースを更新 **/
    public void updateMemo(Memo memo) {
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        SQLiteStatement stmt = null;                                    // ステートメント参照変数初期化。
        try {
            stmt = sqLiteDatabase.compileStatement("UPDATE simple_memo_lists SET title = ?, body = ?, update_datetime = ?" +
                                                        "WHERE id = ?");

            stmt.bindString(1, memo.getTitle());
            stmt.bindString(2, memo.getBody());
            stmt.bindString(3, memo.getUpdateDatetime());
            stmt.bindLong(4, memo.getId());
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



    /** 指定メモIDのレコードをデータベースから削除 **/
    public void deleteMemo(int memoId) {
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        SQLiteStatement stmt = null;                                    // ステートメント参照変数初期化。
        try {
            stmt = sqLiteDatabase.compileStatement("DELETE FROM simple_memo_lists WHERE id = ?");
            stmt.bindLong(1, memoId);
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



    /** 指定メモIDとtrashからデータベースを更新 **/
    public void setTrashFlag(int memoID, int trashFlag) {
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        SQLiteStatement stmt = null;                                    // ステートメント参照変数初期化。
        try {
            stmt = sqLiteDatabase.compileStatement("UPDATE simple_memo_lists SET trash_flag = ? WHERE id = ?");

            stmt.bindLong(1, trashFlag);
            stmt.bindLong(2, memoID);
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



    /** 指定メモIDのメモ・インスタンスを取得 **/
    public Memo getMemoById(int memoId) {
        Memo memo = null;                                               // メモ・インスタンス用参照変数。
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        Cursor cursor = null;                                           // カーソル用参照変数初期化。
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT id, title, body, create_datetime, update_datetime, " +
                                                  "strftime('" + _resources.getString(R.string.date_f) + "',create_datetime), " +
                                                  "strftime('" + _resources.getString(R.string.week_f) + "',create_datetime), " +
                                                  "strftime('" + _resources.getString(R.string.time_f) + "',create_datetime), " +
                                                  "strftime('" + _resources.getString(R.string.date_f) + "',update_datetime), " +
                                                  "strftime('" + _resources.getString(R.string.week_f) + "',update_datetime), " +
                                                  "strftime('" + _resources.getString(R.string.time_f) + "',update_datetime) " +
                                                  "FROM simple_memo_lists WHERE id = " + memoId , null);

            while (cursor.moveToNext()) {       // 次のレコードがある間ループ。
                memo = getMemoByCursor(cursor); // カーソルを元にメモ・インスタンスを取得。
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
        return memo;                            // メモ・インスタンスを返す。
    }


    /** カーソルを元にメモ・インスタンスを返す **/
    private Memo getMemoByCursor(Cursor cursor) {
        int memoId = cursor.getInt(0);                                                       // メモIDを取得。
        String memoTitle = cursor.getString(1);                                              // タイトルを取得。
        String memoBody = cursor.getString(2);                                               // 本文を取得。
        String memoCreateDatetime = cursor.getString(3);                                     // 作成日時を取得。
        String memoUpdateDatetime = cursor.getString(4);                                     // 更新日時を取得。
        String memoCreateDate = cursor.getString(5);                                         // 作成日を取得。
        int memoCreateWeek = Integer.parseInt(cursor.getString(6));                          // 作成曜日番号を取得。
        String memoCreateTime = cursor.getString(7);                                         // 作成時刻を取得。
        String memoUpdateDate = cursor.getString(8);                                         // 更新日を取得。
        int memoUpdateWeek = Integer.parseInt(cursor.getString(9));                          // 更新曜日番号を取得。
        String memoUpdateTime = cursor.getString(10);                                        // 更新時刻を取得。

        String[] weeks = _resources.getStringArray(R.array.week);                               // 曜日名の配列

        StringBuilder stringBuilder = new StringBuilder(25);                                      // ストリングビルダー生成
        stringBuilder.append(memoCreateDate);          // 文字列結合。
        stringBuilder.append(weeks[memoCreateWeek]);
        stringBuilder.append(memoCreateTime);
        String memoCreateDateWeekTime = stringBuilder.toString();                                   // 作成日時の文字列作成。

        stringBuilder = new StringBuilder(25);
        stringBuilder.append(memoUpdateDate);          // 文字列結合。
        stringBuilder.append(weeks[memoUpdateWeek]);
        stringBuilder.append(memoUpdateTime);
        String memoUpdateDateWeekTime = stringBuilder.toString();    // 更新日時の文字列作成。

        Memo memo = new Memo(memoId, memoTitle, memoBody, memoCreateDatetime, memoUpdateDatetime, memoCreateDateWeekTime, memoUpdateDateWeekTime);  // 各値を元にメモ・インスタンスを生成。
        return memo;                                                                                // メモ・インスタンスを返す。
    }


    /** メモリストを返す **/
    public List<Map<String,Object>> getMemoList(int categoryId, int trashFlag) {
        List<Map<String,Object>> memos = new ArrayList<>();             // メモリスト用マップリスト。
        SQLiteDatabase sqLiteDatabase = _helper.getWritableDatabase();  // データベース取得。
        Cursor cursor = null;                                           // カーソル用参照変数。
        try{
            // SELECT及びrawQueryでデータを cursorに取得。
            cursor = sqLiteDatabase.rawQuery("SELECT m_lists.id, title, body, create_datetime, update_datetime, " +
                                                    "strftime('" + _resources.getString(R.string.date_f) + "',create_datetime), " +
                                                    "strftime('" + _resources.getString(R.string.week_f) + "',create_datetime), " +
                                                    "strftime('" + _resources.getString(R.string.time_f) + "',create_datetime), " +
                                                    "strftime('" + _resources.getString(R.string.date_f) + "',update_datetime), " +
                                                    "strftime('" + _resources.getString(R.string.week_f) + "',update_datetime), " +
                                                    "strftime('" + _resources.getString(R.string.time_f) + "',update_datetime) " +
                                                    "FROM simple_memo_lists AS m_lists " +
                                                    "INNER JOIN simple_memo_item_category_ids AS c_ids ON m_lists.id = c_ids.id " +
                                                    "WHERE category_id =" + categoryId + " AND trash_flag =" + trashFlag + " " +
                                                    "ORDER BY update_datetime DESC", null);

            // cursorの先頭行があるかどうか確認。 取得した全ての行の値を取得。
            while (cursor.moveToNext()) {
                Memo memo = getMemoByCursor(cursor);
                memos.add(memo.getMemoMap());
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
        return memos;
    }



    @Override
    public void close() throws Exception {
    }

}
