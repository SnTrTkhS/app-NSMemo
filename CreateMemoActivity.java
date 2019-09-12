package com.example.simplememo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

;


public class CreateMemoActivity extends AppCompatActivity implements TextWatcher {

    private Intent _intent;                                      // 画面遷移インテント用参照変数。
    private boolean _newFlag = false;                            // 新規作成フラグ。初期値：false。
    private int _memoId;                                             // 画面遷移元から受け取るメモID用参照変数。
    private int _categoryId;                                     // 画面遷移元から受け取るカテゴリID用参照変数。
    private MyOpenHelper _helper = null;                         // データベース・オープンヘルパー。初期値：null。

    private int[] _saveEditTitleStr = {R.string.menu_create_title_save, R.string.menu_create_title_edit};     // 保存・編集モード時のタイトル切り替え用文字列。
    private int[] _saveEditIcon = {R.drawable.menu_edit, R.drawable.menu_save};                 // 保存・編集モード時のボタン名切り替え用文字列。
    private static final boolean[] _falseTrue = {false,true};    // 保存・編集時のEditTextのフォーカス切り替え用boolean。[0]==false, [1]==true 。
    private int _zeroOneId = 0;                                  // [0]と[1]の切り替え用。

    private EditText _etTitle;                                   // タイトル欄
    private EditText _etBody;                                    // 本文欄
    private EditText[] _etTitleBody = new EditText[2];           // タイトル欄と本文欄を切り替える為の配列。フォーカス位置によるundo,redo処理対象の変更に使用。
    private int _etTitleBodyID = 0;                              // タイトル欄と本文欄を切り替える為の添え字を入れる変数。

    private SharedPreferences _prefs;                            // 設定ファイル
    private int _undoDataMax;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memo);

        ActionBar actionBar = getSupportActionBar();                        // アクションバー取得。
        actionBar.setDisplayHomeAsUpEnabled(true);                          // 戻る(Home)ボタン表示。

        _etTitle = findViewById(R.id.etTitle);                              // タイトルのエディットテキスト取得。
        _etBody = findViewById(R.id.etBody);                                // 本文のエディットテキスト取得。
        _etTitleBody[0] = _etTitle;                                         // 切り替え用の配列に格納。（undo,redo処理対象の変更に使用）
        _etTitleBody[1] = _etBody;                                          //        〃

        _etTitle.addTextChangedListener(CreateMemoActivity.this);   // タイトルにリスナをセット。
        _etBody.addTextChangedListener(CreateMemoActivity.this);    // 本文にリスナをセット。

        _etTitle.setOnTouchListener(new onTouchListener());                 // フォーカスを監視するリスナをセット。
        _etBody.setOnTouchListener(new onTouchListener());                  //        〃

        TextView tvCreate = findViewById(R.id.tvCreate);                    // 作成日時のテキストビュー取得。
        TextView tvUpdate = findViewById(R.id.tvUpdate);                    // 更新日時のテキストビュー取得。

        _intent = this.getIntent();                                             // 遷移元からインテント取得。
        _memoId = _intent.getIntExtra("memoId", 0);                    // メモIDを遷移元から取得。
        _categoryId = _intent.getIntExtra("categoryId", 0);    // カテゴリIDを遷移元から取得。

        SettingValues settingValues = new SettingValues();                      // 設定値クラスを取得
        float[] textSizes = settingValues.getTextSizes();                       // テキストサイズ用設定値の配列取得

        _prefs = getSharedPreferences("SaveData", Context.MODE_PRIVATE);  // 設定ファイル「SaveData」を取得。

        _etTitle.setTextSize( textSizes[ _prefs.getInt("TextSizeCreateTitle", settingValues.DEFAULT_TEXTSIZE_CREATE_TITLE) ] );        // タイトルのテキストサイズ設定。
        _etTitle.setTypeface( Typeface.createFromAsset(getAssets(), settingValues.DEFAULT_FONTFAMILY_NAME) );                            // タイトルのフォントファミリー設定。
        _etBody.setTextSize( textSizes[ _prefs.getInt("TextSizeCreateBody", settingValues.DEFAULT_TEXTSIZE_CREATE_BODY) ] );           // 本文のテキストサイズ設定。
        tvCreate.setTextSize( textSizes[ _prefs.getInt("TextSizeCreateDatetime", settingValues.DEFAULT_TEXTSIZE_CREATE_DATETIME) ] );  // 作成日時のテキストサイズ設定。
        tvUpdate.setTextSize( textSizes[ _prefs.getInt("TextSizeCreateDatetime", settingValues.DEFAULT_TEXTSIZE_CREATE_DATETIME) ] );  // 更新日時のテキストサイズ設定。

        int[] undoMaxs = settingValues.getUndoMaxs();
        _undoDataMax = undoMaxs[ _prefs.getInt("UndoMax", settingValues.DEFAULT_UNDOMAX)] + 1;

        if(_helper == null){
            _helper = new MyOpenHelper(CreateMemoActivity.this);    // データベースのオープンヘルパーを呼ぶ。
        }

        // 画面表示
        if(_memoId == -1){                                                                                   // 新規作成の場合

            pushCount = 2;                          // テキスト入力状況検知回数
            _newFlag = true;
            setTitle(R.string.menu_main_option_add);                    // ページタイトル変更。
            tvCreate.setVisibility(View.GONE);      // 作成日時非表示。
            tvUpdate.setVisibility(View.GONE);      // 更新日時非表示。
        }else {
            _etTitleBodyID = 1;
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);                   // 最初はキーボードを隠す。

            try(MemoDao memoDao = new MemoDao(_helper, getResources())) {

                Memo memo = memoDao.getMemoById(_memoId);

                _etTitle.setText(memo.getTitle(), TextView.BufferType.NORMAL);
                _etBody.setText(memo.getBody(), TextView.BufferType.NORMAL);

                StringBuilder stringBuilder = new StringBuilder(37);
                stringBuilder.append(tvCreate.getText().toString());
                stringBuilder.append(memo.getCreateDateWeekTime());
                tvCreate.setText(stringBuilder.toString(), TextView.BufferType.NORMAL);

                stringBuilder = new StringBuilder(37);
                stringBuilder.append(tvUpdate.getText().toString());
                stringBuilder.append(memo.getUpdateDateWeekTime());
                tvUpdate.setText(stringBuilder.toString(), TextView.BufferType.NORMAL);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    /**
     * オプションメニュー
     */

    private Menu _menu;        // オプションメニュー用。

    /** オプションメニュー生成 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _menu = menu;                                                     // オプションメニューのアイテムオブジェクトを他メソッドで扱えるよう、フィールドに入れておく。

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_memo_option_menu, menu);           // オプションメニュー生成。

        if (_memoId != -1) {                                                  // 既存記事の場合、
            menu.getItem(2).setIcon(_saveEditIcon[_zeroOneId]);        // 左から3番目(保存･編集ボタン)の名前を「編集」にする。（デフォルトが保存）
            setEditOrSaveMenuOption();                                    // 保存・編集ボタンの処理を、編集に切り替える。（デフォルトが保存）
        }
        setMenuItemAlpha(0,false);
        setMenuItemAlpha(1,false);

        return true;
    }

    /** オプションメニューのアイテムのEnabledを操作 */
    private void setMenuItemEnabled(int menuItemNumber, boolean b) {          // 引数は( [メニューのアイテムナンバー], [true or false] )
        _menu.getItem(menuItemNumber).setEnabled(b);                          // 指定ナンバーのアイテムを取得して、Enabledを操作する。
    }



    /** 保存・編集ボタン処理 */
    public void onSaveEditClick(MenuItem item) {
        if (_zeroOneId == 0) {                                          // 保存ボタンが押された時
            saveSQLitedatabase();                                       // 現在の記事内容をデータベースに保存。
        }
        item.setIcon( _saveEditIcon[_zeroOneId] );                      // ボタンの表示文字列を切り替える。
        setEditOrSaveMenuOption();                                      // ボタンの機能を切り替える。
    }
    // 保存・編集ボタン切り替えメソッド
    public void setEditOrSaveMenuOption() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_etBody.getWindowToken(), 0);          // キーボード非表示。

        _etTitle.setFocusableInTouchMode(_falseTrue[_zeroOneId]);                // タイトル欄の、タッチモードのフォーカス受け取りの切替。
        _etBody.setFocusableInTouchMode(_falseTrue[_zeroOneId]);                 // 記事欄の、　　　　〃
        _etTitle.setFocusable(_falseTrue[_zeroOneId]);                           // タイトル欄の、フォーカス受け取りの切り替え。
        _etBody.setFocusable(_falseTrue[_zeroOneId]);                            // 記事欄の、       〃
        int[] textColors = {getColor(R.color.textColorLight), getColor(R.color.textColorPrimary)};
        _etTitle.setTextColor(textColors[_zeroOneId]);
        _etBody.setTextColor(textColors[_zeroOneId]);


        if (_falseTrue[_zeroOneId] == true) {                                    // 各欄のフォーカスがtrueの場合。
            //etTitle.requestFocus();
            _etBody.requestFocus();                                              // 記事欄にフォーカスを合わせる。
            imm.showSoftInput(_etBody, InputMethodManager.SHOW_IMPLICIT);        // キーボード表示。
            //imm.showSoftInput(_etTitle, 0);       // 　※いらないかも。
        }
//        else if (this.getCurrentFocus() != null){                                // 各欄のフォーカスはfalseだが、どこかにフォーカスがある場合？　※いらないかも。
//            _etBody.clearFocus();                                                // 記事欄からフォーカスを外す。                            ※いらないかも。
//        }

        // タイトル変更
        setTitle(_saveEditTitleStr[_zeroOneId]);                                 // 保存・編集モード時のタイトル切替。
        _zeroOneId = 1-_zeroOneId;                                               // zeroOneIDの値切替。
    }


    /** 完了ボタン処理 */
    public void onFinishClick(MenuItem item) {
        saveSQLitedatabase();                                                    // 記事内容をデータベースに保存。

        // 保存後に一覧へ戻る
        setResult(RESULT_OK);                                                    // 起動元のMainActivityのonActivityResultへ引数を送る。
        finish();                                                                // CreateMemoActivityを終了する。
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


    /**
     * データベース保存
     */
    private void saveSQLitedatabase() {

        String titleStr = _etTitle.getText().toString();                                 // タイトル欄の内容を取得。
        String bodyStr = _etBody.getText().toString();                                   // 記事欄の内容を取得。
        String date = (new SimpleDateFormat(getString(R.string.date_format)).format(new Date()));   // 現在日時を取得。

        Memo newMemo = new Memo(_memoId, titleStr, bodyStr, date, date);


        try(MemoDao memoDao = new MemoDao(_helper);
            ItemCategoryIdDao itemCategoryIdDao = new ItemCategoryIdDao(_helper)) {

            if (_newFlag) {
                // _newFlag が trueの時は新規作成。
                _memoId = memoDao.addMemo(newMemo);
                itemCategoryIdDao.addItemCategoryId(new ItemCategoryId(2, _memoId));        // 必ずアイテムカテゴリに「全て表示」を登録。
                if (_categoryId != 2) {
                    itemCategoryIdDao.addItemCategoryId(new ItemCategoryId(_categoryId, _memoId));    // カテゴリ内一覧から作成した場合はそのカテゴリを登録。
                }

                _newFlag = false;                // 新規から既存記事になるので、フラグをfalseにする。

            } else {
                memoDao.updateMemo(newMemo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * undo,redo処理
     */

    /** EditTextのフォーカスの状態を検知 */

    private ArrayList<String> _undoList = new ArrayList<>();
    private ArrayList<String> _redoList = new ArrayList<>();
    private String _UndoData;
    private int _iBack = -1;

    private boolean _unficxInput = true;

    static int viewIdBack = 0;  // フォーカスしているEditTextのIDを保持し、undoとredoの処理対象変更に使用する。
    private class onTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                if (view.getId() != viewIdBack) {
                    _undoList.clear();
                    setMenuItemEnabled(0,false);
                    setMenuItemAlpha(0,false);
                    _redoList.clear();
                    setMenuItemEnabled(1,false);
                    setMenuItemAlpha(1,false);
                }
                Log.i("CheckCheckCheck" , "view.getMemoId()  " + view.getId());
                if (view.getId() == _etTitle.getId()) {
                    _etTitleBodyID = 0;
                    viewIdBack = _etTitle.getId();
                    Log.i("CheckCheckCheck" , "viewIdBack  " + viewIdBack);
                } else {
                    _etTitleBodyID = 1;
                    viewIdBack = _etBody.getId();
                    Log.i("CheckCheckCheck" , "viewIdBack  " + viewIdBack);
                }
            }
            return false;
        }
    }



    /** テキスト入力検知 */

    private String _beforeTextCharSequence;   // 入力テキスト参照フィールド。
    private int _beforeTextI;                 // 前回入力状況フィールド。
    private int _beforeTextI1;                // 前回入力状況フィールド。
    private int _beforeTextI2;                // 前回入力状況フィールド。

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.i("CheckCheckCheck" , "beforeTextChanged  " + charSequence.toString());
        Log.i("CheckCheckCheck" , "i   " + i);
        Log.i("CheckCheckCheck" , "i1  " + i1);
        Log.i("CheckCheckCheck" , "i2  " + i2);
        _beforeTextCharSequence = charSequence.toString();  // 直前の入力テキスト取得。
        _beforeTextI = i;                                   // 直前の入力状況i取得。
        _beforeTextI1 = i1;                                 // 直前の入力状況i1取得。
        _beforeTextI2 = i2;                                 // 直前の入力状況i2取得。
    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.i("CheckCheckCheck" , "onTextChanged" + charSequence.toString());
    }
    @Override
    public void afterTextChanged(Editable editable) {
        Log.i("CheckCheckCheck" , "afterTextChanged" + editable.toString());
        _unficxInput = unfixInput(editable);                                                // 入力確定前チェック
        push(_beforeTextCharSequence, _beforeTextI, _beforeTextI1, _beforeTextI2);          // 直前の入力テキスト＆状況を元にundoリスト生成。
        Log.i("CheckCheckCheck", "unfixInput afterTextChanged" + _unficxInput);
    }

    /** 入力確定前チェック（確定前：true　確定：false） **/
    private boolean unfixInput(final Editable s) {
        Object[] spanned = s.getSpans(0, s.length(), Object.class);
        if (spanned != null) {
            for (Object obj : spanned) {
                if ((s.getSpanFlags(obj) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
                    Log.i("CheckCheckCheck", "unfixInput  true");
                    return true;
                }
            }
        }
        return false;
    }


    /** undoList生成 */
    int pushCount = 0;  // オプションメニュー生成前にテキスト入力検知から2回呼ばれるので、それをスルーする為のカウント用変数。(setMenuItemEnabledがエラーを出す為)。
    public void push(String str, int i, int i1, int i2) {

        if (i != _iBack || ( i == _iBack && i1 == 0 && i2 > 0 ) || ( i == _iBack && i1 > 0 && i2 == 0 ) ) {
            _UndoData = str;
            Log.i("CheckCheckCheck", "push _UndoData: "+_UndoData);
        }

        Log.i("CheckCheckCheck", "push unfixInput pushCount: " + pushCount + " _unficxInput: " + _unficxInput);
        // ( pushカウント2以上　&& 入力確定時 )
        if ( pushCount > 1 && _unficxInput == false ) {
            _undoList.add(_UndoData);
            Log.i("CheckCheckCheck", "push _undoList.add: " + _UndoData);
            if ( _undoList.size() >= 2 && (_undoList.get(_undoList.size()-1).equals( _undoList.get(_undoList.size()-2) ) ) ){
                _undoList.remove(_undoList.size()-1);
                Log.i("CheckCheckCheck", "push _undoList.remove: " + (_undoList.size()-1) );
            }
            setMenuItemEnabled(0,true);
            setMenuItemAlpha(0,true);
            _redoList.clear();
            setMenuItemEnabled(1,false);
            setMenuItemAlpha(1,false);
            Log.i("CheckCheckCheck", "push _undoList1: " + _undoList.toString());
            if (_undoList.size() == _undoDataMax) {
                _undoList.remove(0);
            }
        }
        Log.i("CheckCheckCheck", "push _undoList2: " + _undoList.toString());

        if (pushCount < 5) {
            pushCount++;        // pushが動いた回数をカウント。上限5。
        }
        _iBack = i;             // 直前の入力状況iの履歴を残す。
    }

    private void setMenuItemAlpha(int menuItemNumber, boolean b) {
        int alpha = b == true ? 255 : 32;
        _menu.getItem(menuItemNumber).getIcon().setAlpha(alpha);
    }

    /** undo */
    public void undoClick(MenuItem item) {
        Log.i("CheckCheckCheck" , "viewIdBack  " + viewIdBack);
        pushCount = 0;
        _redoList.add(_etTitleBody[_etTitleBodyID].getText().toString());                       // undoする時、現在の状態をredoListに追加する。
        setMenuItemEnabled(1,true);                                              // redoが選択可能になる。
        setMenuItemAlpha(1,true);
        _etTitleBody[_etTitleBodyID].setText( _undoList.get( _undoList.size() - 1 ) );          // undo履歴をTextViewにセットする。
        _etTitleBody[_etTitleBodyID].setSelection(_etTitleBody[_etTitleBodyID].getText().length());
        _undoList.remove(_undoList.size() - 1);
        Log.i("CheckCheckCheck" , "_undoList removed  "+ _undoList.toString());

        if ( _undoList.size() == 0){
            setMenuItemEnabled(0,false);                                          // 最大までundoした時、選択不可になる。
            setMenuItemAlpha(0,false);
        }
        pushCount = 2;                                                                          // pushCountを2に戻し、次回入力でundoが選択可能になるようにする。
    }

    /** redo */
    public void redoClick(MenuItem item) {
        pushCount = 0;
        _undoList.add(_etTitleBody[_etTitleBodyID].getText().toString());                       // 現在の状態をundoListに追加する。
        setMenuItemEnabled(0,true);                                              // redoした時、undoが選択可能になる。
        setMenuItemAlpha(0,true);
        Log.i("CheckCheckCheck" , "_redoList   "+ _redoList.toString());
        Log.i("CheckCheckCheck" , "_redoListsize   "+ _redoList.size());
        _etTitleBody[_etTitleBodyID].setText( _redoList.get( _redoList.size() - 1 ) );          // redo履歴をTextViewにセットする。
        _etTitleBody[_etTitleBodyID].setSelection(_etTitleBody[_etTitleBodyID].getText().length());
        _redoList.remove(_redoList.size() - 1 );
        if (_redoList.size() == 0) {
            setMenuItemEnabled(1,false);                                         // _redoListが0番まで利用された時に、redoを選択不可にする。
            setMenuItemAlpha(1,false);
        }
        pushCount = 2;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

