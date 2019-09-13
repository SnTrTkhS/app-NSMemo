package com.example.simplememo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import java.sql.SQLException;
import java.util.List;

public class ListLongClickDialogFragment extends DialogFragment {

    private int _memoId;                            // ロングタップされたメモID。
    private int _trashFlag;                         // ゴミ箱フラグ。
    private View _listLongClickDialogFragmentView;  // ListLongClickDialogFragmentのビュー。
    private AutoCompleteTextView _atvCategories;    // カテゴリ選択欄のビュー。
    private MyOpenHelper _helper;                   // データベース・オープンヘルパー。


    ListLongClickDialogFragment(int memoId, int trashFlag) {
        _memoId = memoId;         // ロングタップされたメモID取得。
        _trashFlag = trashFlag;   // ゴミ箱フラグ取得。
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        _listLongClickDialogFragmentView = inflater.inflate(R.layout.layout_list_long_click_dialog_fragment, null, false);  // ListLongClickDialogFragmentのビュー取得。
        _helper = new MyOpenHelper(getActivity());        // データベース・オープンヘルパー取得。

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());   // ダイアログビルダー取得。
//        builder.setTitle(R.string.list_long_click_dialog_fragment_header);                                            // ビルダーにタイトルをセット。
        builder.setView(_listLongClickDialogFragmentView);                      // ビルダーにListLongClickDialogFragmentのビューをセット。
        AlertDialog dialog = builder.create();                                  // ダイアログ生成。


        try(CategoryDao categoryDao = new CategoryDao(_helper)) {
            List<Category> categoryList = categoryDao.getCategoryList();                                         // カテゴリリスト取得。
            _atvCategories = _listLongClickDialogFragmentView.findViewById(R.id.atvCategories);                  // カテゴリ選択欄取得。
            setAutoCompleteTextView(categoryList);                                                               // カテゴリ選択欄にカテゴリリストセット。
            if(categoryList.size() > 7) {
                float ratioOfDpToPx = getResources().getDisplayMetrics().density;        // dpからpxへ変換するための比率。
                _atvCategories.setDropDownHeight( (int)(384 * ratioOfDpToPx + 0.5f) );   // px値でサイズ設定。
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btCategorySet = _listLongClickDialogFragmentView.findViewById(R.id.btCategorySet);                // [ 登録 ] ボタン取得。
        Button btCategoryRemove = _listLongClickDialogFragmentView.findViewById(R.id.btCategoryRemove);          // [ 解除 ] ボタン取得。
        Button btBodyCopy = _listLongClickDialogFragmentView.findViewById(R.id.btBodyCopy);                      // [ 本文コピー ]　ボタン取得。
        Button btDuplication = _listLongClickDialogFragmentView.findViewById(R.id.btDuplication);                // [ 複製 ]　ボタン取得。

        Button btTrash = _listLongClickDialogFragmentView.findViewById(R.id.btTrash);                            // [ ゴミ箱へ移動 ] ボタン取得。
        Button btDelete = _listLongClickDialogFragmentView.findViewById(R.id.btDelete);                          // [ 完全に削除 ] ボタン取得。

        if (_trashFlag == 0) {                                                                                   // ゴミ箱外の時・・・・・・
        btDelete.setVisibility(View.GONE);                                                                       // [ 完全に削除 ] ボタンを隠す。
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)btTrash.getLayoutParams();   // [ ゴミ箱へ移動 ] ボタンの制約を編集。
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;                                   // 下部の制約を親レイアウトと繋げる設定をする。
        btTrash.setLayoutParams(layoutParams);                                                                   // 設定をボタンにセット。
        } else {                                                                                                 // ゴミ箱内の時・・・・・・
            btTrash.setText(getResources().getText(R.string.list_long_click_dialog_fragment_bt_rescue));                                                                     // [ ゴミ箱へ移動 ] ボタンを [ ゴミ箱から出す ] ボタンへ変更。
        }

        ButtonOnClickListener buttonOnClickListener = new ButtonOnClickListener();                               // ボタンクリックリスナ取得。
        btCategorySet.setOnClickListener(buttonOnClickListener);                                                 // [ 登録 ] ボタンにリスナセット。
        btCategoryRemove.setOnClickListener(buttonOnClickListener);                                              // [ 解除 ] ボタンにリスナセット。
        btBodyCopy.setOnClickListener(buttonOnClickListener);                                                    // [ 本文コピー ] ボタンにリスナセット。
        btDuplication.setOnClickListener(buttonOnClickListener);                                                 // [ 複製 ] ボタンにリスナセット。
        btTrash.setOnClickListener(buttonOnClickListener);                                                       // [ ゴミ箱へ ] ボタンにリスナセット。
        btDelete.setOnClickListener(buttonOnClickListener);                                                      // [ 完全に削除 ] ボタンにリスナセット。

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    private class ButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                /** カテゴリ登録 **/
                case R.id.btCategorySet:
                    try(CategoryDao categoryDao = new CategoryDao(_helper);
                        ItemCategoryIdDao itemCategoryIdDao = new ItemCategoryIdDao(_helper)) {

                        int categoryId = categoryDao.getCategoryIdByName(_atvCategories.getText().toString());   // カテゴリ選択欄のカテゴリ名からカテゴリIDを取得。
                        itemCategoryIdDao.addItemCategoryId(new ItemCategoryId(categoryId, _memoId));            // タップされたメモIDとカテゴリIDの組み合わせをデータベースに登録。

                        StringBuilder stringBuilder = new StringBuilder(25);
                        stringBuilder.append(getString(R.string.list_long_click_dialog_fragment_toast_categiryset));          // 文字列結合。
                        stringBuilder.append(_atvCategories.getText().toString());
                        stringBuilder.append(getString(R.string.list_long_click_dialog_fragment_toast_categiryset_set));
                        Toast.makeText(getActivity(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /** カテゴリ解除 **/
                case R.id.btCategoryRemove:
                    try(CategoryDao categoryDao = new CategoryDao(_helper);
                        ItemCategoryIdDao itemCategoryIdDao = new ItemCategoryIdDao(_helper)) {

                        int categoryId = categoryDao.getCategoryIdByName(_atvCategories.getText().toString());    // カテゴリ選択欄のカテゴリ名からカテゴリIDを取得。
                        itemCategoryIdDao.removeItemCategoryIdByMemoId(new ItemCategoryId(categoryId, _memoId));  // タップされたメモIDとカテゴリIDの組み合わせをデータベースから削除。

                        StringBuilder stringBuilder = new StringBuilder(255);
                        stringBuilder.append(getString(R.string.list_long_click_dialog_fragment_toast_categiryset));          // 文字列結合。
                        stringBuilder.append(_atvCategories.getText().toString());
                        stringBuilder.append(getString(R.string.list_long_click_dialog_fragment_toast_categiryset_remove));
                        Toast.makeText(getActivity(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /** 本文コピー **/
                case R.id.btBodyCopy:
                    try(MemoDao memoDao = new MemoDao(_helper, getResources())) {

                        String memoBody = memoDao.getMemoById(_memoId).getBody();
                        ClipData.Item item = new ClipData.Item(memoBody);               //クリップボードに格納するItemを作成
                        String[] mimeType = new String[1];                              //MIMETYPEの作成
                        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                        ClipData clipData = new ClipData(new ClipDescription("text_data", mimeType), item);                           //クリップボードに格納するClipDataオブジェクトの作成
                        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);    //クリップボードにデータを格納
                        clipboardManager.setPrimaryClip(clipData);

                        Toast.makeText(getActivity(), R.string.list_long_click_dialog_fragment_toast_body_copy, Toast.LENGTH_SHORT).show();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /** 複製 **/
                case R.id.btDuplication:
                    try(MemoDao memoDao = new MemoDao(_helper, getResources());
                        ItemCategoryIdDao itemCategoryIdDao = new ItemCategoryIdDao(_helper)) {

                        int newMemoId = memoDao.addMemo(memoDao.getMemoById(_memoId));                                  // タップされたメモIDからメモデータを取得し、新規にメモとしてデータベースに登録し、新規メモIDを取得。
                        List<Integer> itemCategoryIdList = itemCategoryIdDao.getItemCategoryIdListByMemoId(_memoId);    // タップされたメモIDから登録中のカテゴリIDリスト取得。
                        for(int categoryId : itemCategoryIdList ) {
                            itemCategoryIdDao.addItemCategoryId( new ItemCategoryId(categoryId, newMemoId) );           // 各カテゴリIDと新規メモIDの組み合わせをデータベースに登録。
                        }
                        Toast.makeText(getActivity(), R.string.list_long_click_dialog_fragment_toast_duplication, Toast.LENGTH_SHORT).show();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /** ゴミ箱 **/
                case R.id.btTrash:
                    try(MemoDao memoDao = new MemoDao(_helper)) {

                        if (_trashFlag == 1) {                          // ゴミ箱内の時・・・・・・
                            memoDao.setTrashFlag(_memoId, 0);  // タップされたメモデータのゴミ箱フラグを0にセット。（ゴミ箱から出す）
                            Toast.makeText(getActivity(), R.string.list_long_click_dialog_fragment_toast_rescue, Toast.LENGTH_SHORT).show();
                        } else {                                        // ゴミ箱外の時・・・・・・
                            memoDao.setTrashFlag(_memoId, 1);  // タップされたメモデータのゴミ箱フラグを1にセット。（ゴミ箱へ移動）
                            Toast.makeText(getActivity(), R.string.list_long_click_dialog_fragment_toast_trash, Toast.LENGTH_SHORT).show();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                /** 完全に削除 **/
                case R.id.btDelete:
                    DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment(_helper, _memoId);             // 削除ダイアログ生成。
                    deleteDialogFragment.show(getActivity().getSupportFragmentManager(), "DeleteDialogFragment");   // 削除ダイアログ表示。
                    break;
            }
            ((MainActivity)getActivity()).createMemoList();             // 一覧画面更新。
            dismiss();                                                  // ListLongClickDialogFragment終了。
        }
    }


    /** カテゴリ選択欄生成 **/
    private void setAutoCompleteTextView(List<Category> categoryList){
        String[] categoryStrs = new String[categoryList.size()-2];                // 「全て表示」と「ゴミ箱」分引いたカテゴリ数で、String配列生成。
        for (int i = 0 ; i < categoryStrs.length ; i++) {
            categoryStrs[i] = categoryList.get(i+1).getCategoryTitle();           // 最初の「全て表示」と最後の「ゴミ箱」以外のタイトルを配列に代入。
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.select_dialog_item, categoryStrs);     // カテゴリタイトル配列をアダプタにセット。
        _atvCategories.setAdapter(adapter);                                                                                    // アダプタをセット。
        _atvCategories.setThreshold(1);                                                                                        // 入力補完を開始する文字数。(1以上なので1)
        // カテゴリ選択欄にフォーカスチェンジリスナセット。
        _atvCategories.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {                                         // カテゴリ選択欄にフォーカスが合った時・・・・・・
                    _atvCategories.showDropDown();                      // カテゴリリスト表示。
                }
            }
        });
        // カテゴリ選択欄にクリックリスナセット。
        _atvCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _atvCategories.showDropDown();                          // カテゴリ選択欄タップでカテゴリリスト表示。
            }
        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
