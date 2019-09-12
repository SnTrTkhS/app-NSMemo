package com.example.simplememo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Intent _intent;                                      // 画面遷移用。
    private Spinner _spTextSizeMainTitle;                        // 文字サイズ　一覧画面　タイトル。
    private Spinner _spTextSizeMainBody;                         // 文字サイズ　一覧画面　本文。
    private Spinner _spTextSizeMainDatetime;                     // 文字サイズ　一覧画面　日付・時刻。
    private Spinner _spTextSizeCreateTitle;                      // 文字サイズ　編集画面　タイトル。
    private Spinner _spTextSizeCreateBody;                       // 文字サイズ　編集画面　本文。
    private Spinner _spTextSizeCreateDatetime;                   // 文字サイズ　編集画面　日付・時刻。

    private Spinner _spMaxLinesMainBody;                          // 一覧画面の本文最大表示行。
    private Spinner _spUndoMax;                                  // やり直し最大回数。

    private SharedPreferences _prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle(R.string.setting_title);

        ActionBar actionBar = getSupportActionBar();               // 戻る(Home)ボタンを表示。
        actionBar.setDisplayHomeAsUpEnabled(true);

        _intent = this.getIntent();                                // インテントを取得。

        _prefs = getSharedPreferences("SaveData", Context.MODE_PRIVATE);             // SharedPreferencesを取得。
        SettingValues settingValues = new SettingValues();

        _spTextSizeMainTitle = findViewById(R.id.spSettingTextSizeMainTitle);
        _spTextSizeMainTitle.setSelection( _prefs.getInt("TextSizeMainTitle",settingValues.DEFAULT_TEXTSIZE_MAIN_TITLE) );
        _spTextSizeMainBody = findViewById(R.id.spSettingTextSizeMainBody);
        _spTextSizeMainBody.setSelection( _prefs.getInt("TextSizeMainBody",settingValues.DEFAULT_TEXTSIZE_MAIN_BODY) );
        _spTextSizeMainDatetime = findViewById(R.id.spSettingTextSizeMainDatetime);
        _spTextSizeMainDatetime.setSelection( _prefs.getInt("TextSizeMainDatetime",settingValues.DEFAULT_TEXTSIZE_MAIN_DATETIME) );

        _spTextSizeCreateTitle = findViewById(R.id.spSettingTextSizeCreateTitle);
        _spTextSizeCreateTitle.setSelection( _prefs.getInt("TextSizeCreateTitle",settingValues.DEFAULT_TEXTSIZE_CREATE_TITLE) );
        _spTextSizeCreateBody = findViewById(R.id.spSettingTextSizeCreateBody);
        _spTextSizeCreateBody.setSelection( _prefs.getInt("TextSizeCreateBody",settingValues.DEFAULT_TEXTSIZE_CREATE_BODY) );
        _spTextSizeCreateDatetime = findViewById(R.id.spSettingTextSizeCreateDatetime);
        _spTextSizeCreateDatetime.setSelection( _prefs.getInt("TextSizeCreateDatetime",settingValues.DEFAULT_TEXTSIZE_CREATE_DATETIME) );

        _spMaxLinesMainBody = findViewById(R.id.spSettingMaxLinesMainBody);
        _spMaxLinesMainBody.setSelection( _prefs.getInt("MaxLinesMainBody",settingValues.DEFAULT_MAXLINES_MAIN_BODY) );

        _spUndoMax = findViewById(R.id.spSettingUndoMax);
        _spUndoMax.setSelection( _prefs.getInt("UndoMax",settingValues.DEFAULT_UNDOMAX) );

    }


    /** 戻るボタン処理 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK,_intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /** 保存ボタン処理 */
    public void saveSettings(View view) {

        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt("TextSizeMainTitle", _spTextSizeMainTitle.getSelectedItemPosition() );
        editor.putInt("TextSizeMainBody", _spTextSizeMainBody.getSelectedItemPosition() );
        editor.putInt("TextSizeMainDatetime", _spTextSizeMainDatetime.getSelectedItemPosition() );
        editor.putInt("TextSizeCreateTitle", _spTextSizeCreateTitle.getSelectedItemPosition() );
        editor.putInt("TextSizeCreateBody", _spTextSizeCreateBody.getSelectedItemPosition() );
        editor.putInt("TextSizeCreateDatetime", _spTextSizeCreateDatetime.getSelectedItemPosition() );
        editor.putInt("MaxLinesMainBody", _spMaxLinesMainBody.getSelectedItemPosition() );
        editor.putInt("UndoMax", _spUndoMax.getSelectedItemPosition() );
        editor.apply();

        Toast.makeText( SettingsActivity.this , "保存しました。", Toast.LENGTH_LONG).show();
    }


    /** 初期値ボタン処理 */
    public void clearSettings(View view) {
        _prefs.edit().clear().commit();

        Toast.makeText( SettingsActivity.this , "初期値に戻りました。", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
