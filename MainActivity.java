package com.example.simplememo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    public MyOpenHelper _helper = null;         // データベース・オープンヘルパー。

    private Intent _intent;                     // 編集画面遷移インテント。
    private Intent _intentSetting;              // 設定画面遷移インテント。


    private ListView lvMemoList;                // メモリスト表示用リストビュー。
    private List<Map<String,Object>> _memoList; // メモリスト。

    private Menu _navigationViewMenu;           // ナビゲーションメニュー。
    private SubMenu _submenuByCategory;         // ナビゲーションメニュー内カテゴリメニュー群。

    private int _memoId;                        // タップされたメモID。
    private int _categoryId = 2;                // カテゴリID。初期値：2(全て表示)
    private int _trashFlag = 0;                 // ゴミ箱フラグ（0 or 1）。初期値：0(偽)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // メモリスト
        lvMemoList = findViewById(R.id.lvMemoList);                             // メモリスト表示用リストビューを取得。
        createMemoList();                                                       // メモリスト生成。

        lvMemoList.setOnItemClickListener(new onListClickListener());           // メモリストビューにクリックリスナセット
        lvMemoList.setOnItemLongClickListener(new onListLongClickListener());   // メモリストビューにロングクリックリスナセット

        // ナビゲーションビュー
        NavigationView navigationView = findViewById(R.id.nav_view);                                                // ナビゲーションビュー取得。
        _navigationViewMenu = navigationView.getMenu();                                                             // ナビゲーションメニュー取得。
        _submenuByCategory = _navigationViewMenu.getItem(0).getSubMenu();                                        // カテゴリメニュー取得
        createCategoryMenu();                                                                                       // カテゴリメニュー生成。

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {    // ナビゲーションビューにセレクトリスナセット
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // 画面遷移のインテント用意。
        _intent = new Intent(MainActivity.this, com.example.simplememo.CreateMemoActivity.class);       // 編集画面用インテント
        _intentSetting = new Intent(MainActivity.this, com.example.simplememo.SettingsActivity.class);  // 設定画面用インテント
    }


    /** メモリスト生成 **/
    public void createMemoList() {
        // データベースからメモリストを取得。
        if(_helper == null){
            // アプリ起動直後
            _helper = new MyOpenHelper(MainActivity.this);      // データベース・オープンヘルパーを取得。
            // 設定画面用
            String[] strTextSizes = getResources().getStringArray(R.array.sp_setting_text_size);            // テキストサイズ設定用　文字列配列
            String[] strMaxLines = getResources().getStringArray(R.array.sp_setting_maxlines_main_body);    // 本文表示行数設定用　文字列配列
            String[] strUndoMaxs = getResources().getStringArray(R.array.sp_setting_undo_max);              // やり直し回数設定用　文字列配列
            Typeface typefaceBold = Typeface.createFromAsset(getAssets(), getString(R.string.typeface_bold) );
            Typeface typefaceIcon = Typeface.createFromAsset(getAssets(), getString(R.string.typeface_icon) );
            new SettingValues(strTextSizes, strMaxLines, strUndoMaxs, typefaceBold, typefaceIcon);                                      // 設定値用クラスに　各配列をセット。
        }

        _memoList = new ArrayList<>();                                          // メモリスト参照変数を初期化。
        try(MemoDao memoDao = new MemoDao(_helper, getResources())) {
            _memoList = memoDao.getMemoList(_categoryId, _trashFlag);     // カテゴリIDとゴミ箱スイッチを元に、メモリスト取得。
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // メモリスト用アダプタ生成。
        String[] from = {"title","body","updateDateWeekTime"};          // メモリストのアダプタ用キー配列。
        int[] to = {R.id.tvTitle,R.id.tvBody,R.id.tvDate};      // メモリストの各バリューのセット先ID配列。
        SimpleAdapter memoListAdapter = new SimpleAdapter(MainActivity.this, _memoList, R.layout.layout_memo_list, from, to){    // メモリストのアダプタ生成。
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // 各バリューのセット先の設定項目に、設定ファイルの値をセットする。
                View view = super.getView(position, convertView, parent);
                SharedPreferences prefs = getSharedPreferences("SaveData", Context.MODE_PRIVATE);             // 設定ファイル「SaveData」を取得。

                SettingValues settingValues = new SettingValues();      // 初期設定値用クラスのインスタンス生成。
                //int[] clockSizes = settingValues.getClockSizes();       // 時計アイコンサイズ用　設定値配列取得。
                float[] textSizes = settingValues.getTextSizes();       // タイトル＆本文のテキストサイズ用　設定値配列取得。
                int[] maxLines = settingValues.getMaxLines();           // 本文表示行数用　設定値配列取得。

                //float ratioOfDpToPx = getResources().getDisplayMetrics().density;                 // dpからpxへ変換するための比率
                //view.findViewById(R.id.ivClock).getLayoutParams().width = (int)( (clockSizes[prefs.getInt("TextSizeMainDatetime",settingValues.DEFAULT_TEXTSIZE_MAIN_DATETIME)]+4) * ratioOfDpToPx + 0.5f);  // px値で時計サイズ設定。

//                TextView tvClock = view.findViewById(R.id.tvClock);     // タイトルのテキストビュー取得。
//                tvClock.setTextSize( textSizes[prefs.getInt("TextSizeMainDatetime",settingValues.DEFAULT_TEXTSIZE_MAIN_DATETIME)] );   // 日時の文字サイズ設定。
//                tvClock.setTypeface( settingValues.getTypefaceIcon() );               // タイトルのフォントファミリー設定。
//                tvClock.setText(String.valueOf((char)0xe94e));


                ((TextView)view.findViewById(R.id.tvDate)).setTextSize( textSizes[prefs.getInt("TextSizeMainDatetime",settingValues.DEFAULT_TEXTSIZE_MAIN_DATETIME)] );   // 日時の文字サイズ設定。

                TextView tvTitle = view.findViewById(R.id.tvTitle);     // タイトルのテキストビュー取得。
                tvTitle.setTextSize( textSizes[prefs.getInt("TextSizeMainTitle",settingValues.DEFAULT_TEXTSIZE_MAIN_TITLE)] );  // タイトルのテキストサイズ設定。
                tvTitle.setTypeface( settingValues.getTypefaceBold() );               // タイトルのフォントファミリー設定。

                TextView tvBody = view.findViewById(R.id.tvBody);       // 本文のテキストビュー取得。
                tvBody.setTextSize( textSizes[prefs.getInt("TextSizeMainBody",settingValues.DEFAULT_TEXTSIZE_MAIN_BODY)] );     // 本文のテキストサイズ設定。
                tvBody.setMaxLines( maxLines[prefs.getInt("MaxLinesMainBody",settingValues.DEFAULT_MAXLINES_MAIN_BODY)] );      // 本文の表示行数設定。
                
                return view;
            }
        };
        // lvMemoListにアダプタをセット
        lvMemoList.setAdapter(memoListAdapter);  // メモリスト表示用リストビューにメモリスト用アダプタをセット。
        memoListAdapter.notifyDataSetChanged();  // リストビュー更新

    }


    /** アクティビティに戻ってきたとき */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        createMemoList();       // メモリスト再生成。
    }


    /** リストを長押しした時 */
    public class onListLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            _memoId = getMemoIdByPosition(position);      // 長押しされたpositionを元に、メモIDを取得。

            // ダイアログフラグメント生成。
            ListLongClickDialogFragment listLongClickDialogFragment = new ListLongClickDialogFragment(_memoId, _trashFlag); // メモIDを渡してダイアログフラグメント生成。
            listLongClickDialogFragment.show(getSupportFragmentManager(), "ListLongClickDialogFragment");   // ダイアログフラグメント表示。

            return true;
        }
    }


    /** リストをタップしたとき */
    public class onListClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            _memoId = getMemoIdByPosition(position);             // タップされたpositionを元に、メモIDを取得。
            _intent.putExtra("memoId", _memoId);           // メモIDを遷移先に送る。
            startActivityForResult(_intent, 1);       // 遷移先アクティビティ起動。
        }
    }


    /** データベースから、タップされたポジションのメモIDを取得 */
    private int getMemoIdByPosition(int position){
        return (int) _memoList.get(position).get("memoId");
    }



    /**
     * オプションメニュー
     */
    /** オプションメニュー生成 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_option_menu, menu);       // オプションメニュー生成。
        return true;
    }
    /** 記事を新規作成するボタン */
    public void addMemo(MenuItem item) {
        _intent.putExtra("memoId",-1);
        _intent.putExtra("categoryId",_categoryId);
        startActivityForResult(_intent, 0);                 // 編集画面へ遷移。
    }

    /** 設定画面へ遷移するボタン */
    public void settings(MenuItem item) {
        startActivityForResult(_intentSetting, 2);          // 設定画面へ遷移。
    }



    /**
     * ナビゲーション
     */

    private List<Category> _categoryList;

    /** カテゴリメニューの項目生成 */
    public void createCategoryMenu() {
        try(CategoryDao categoryDao = new CategoryDao(_helper)){

            _categoryList = categoryDao.getCategoryList();              // 現在あるカテゴリ郡を取得。
            _submenuByCategory.clear();                                 // 既にあるサブメニューを消す。
            int submenuNumber = 0;                                      // サブメニュー番号カウンタ。
            for(Category category : _categoryList) {
                _submenuByCategory.add(category.getCategoryTitle());    // サブメニューにカテゴリ名を追加。
                _submenuByCategory.getItem(submenuNumber).setOnMenuItemClickListener(new OnMenuItemClickListener());   // 追加したサブメニューにリスナーをセット。
                submenuNumber++;                                        // サブメニュー番号1加算。
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** カテゴリメニューのクリックリスナ **/
    public class OnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            setTitle(menuItem.getTitle().toString());                                                           // タイトルをタップしたカテゴリ名に変更
            if(menuItem.getTitle().equals( getString(R.string.navigation_view_menu_trash) )) {   // タップしたのがゴミ箱だったら・・・・・・
                _trashFlag = 1;                                                                                 // ゴミ箱フラグを1にする。
            }else {                                                                                             // タップしたのがゴミ箱以外だったら・・・・・・
                _trashFlag = 0;                                                                                 // ゴミ箱フラグを0にする。
                try(CategoryDao categoryDao = new CategoryDao(_helper)) {
                    int categoryId = categoryDao.getCategoryIdByName(menuItem.getTitle().toString());           // タップしたカテゴリ名からカテゴリIDを取得。
                    _categoryId = categoryId;                                                                   // カテゴリIDをフィールドに参照させる。

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            createMemoList();                                                                                   // メモリスト更新。
            return false;
        }
    }



    /** カテゴリを新規作成するボタン */
    public void openAddCategoryDialogFragment(MenuItem item) {
        EditCategoryDialogFragment dialogFragment = new EditCategoryDialogFragment(_submenuByCategory);     // カテゴリ編集ダイアログ生成。
        dialogFragment.show(getSupportFragmentManager(), "EditCategoryDialogFragment");                 // カテゴリ編集ダイアログ表示。
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
