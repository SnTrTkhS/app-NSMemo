package com.example.simplememo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {

    // データベース名
    private static final String _DB_NAME = "MemoList.db";
    // データベースのバージョン（2，3と挙げていくとonUpgradeメソッドが実行される）
    private static final int _DB_VERSION = 1;

    // コンストラクタ
    public MyOpenHelper(Context context) {
        super(context, _DB_NAME, null, _DB_VERSION);
    }

    // データベースが作成された時に実行される処理
    // データベースはアプリを開いた時に存在しなかったら作成され、すでに存在していれば何もしない
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 処理を記述
        /**
         * テーブルを作成する
         * execSQLメソッドにCREATE TABLE命令を文字列として渡すことで実行される
         * PRIMARY KEY:テーブル内の行で重複無し, AUTOINCREMENT:1から順番に振っていく
         */
        sqLiteDatabase.execSQL("CREATE TABLE simple_memo_lists (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "title TEXT, " +
                                "body TEXT, " +
                                "create_datetime DATETIME, " +
                                "update_datetime DATETIME, " +
                                "trash_flag INTEGER DEFAULT 0 );");


        sqLiteDatabase.execSQL("CREATE TABLE simple_memo_categorys (" +
                                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "category_name TEXT);");


        sqLiteDatabase.execSQL("INSERT INTO simple_memo_categorys(category_name) VALUES ('ゴミ箱');");
        sqLiteDatabase.execSQL("INSERT INTO simple_memo_categorys(category_name) VALUES ('全て表示');");


        sqLiteDatabase.execSQL("CREATE TABLE simple_memo_item_category_ids (" +
                                "id INTEGER, " +
                                "category_id INTEGER);");


    }

    // データベースをバージョンアップした時に実行される処理
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        // 処理を記述
        /**
         * テーブルを削除する
         */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS simple_memo_lists");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS simple_memo_categorys");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS simple_memo_item_category_ids");

        // 新しくテーブルを作成する
        onCreate(sqLiteDatabase);
    }

//    // データベースが開かれた時に実行される処理
//    @Override
//    public void onOpen(SQLiteDatabase db) {
//        super.onOpen(db);
//    }

}
