package com.example.simplememo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.SQLException;
import java.util.List;

public class EditCategoryDialogFragment extends DialogFragment {

    private View _view;
    private SubMenu _subMenu;
    private AutoCompleteTextView _atvCategories;


//    EditCategoryDialogFragment() {
//    }

    EditCategoryDialogFragment(SubMenu subMenu) {
        //super();
        _subMenu = subMenu;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        _view = inflater.inflate(R.layout.layout_edit_category_dialog_fragment, null, false);

        // ダイアログ建設者、ビルダーを呼ぶ。
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ビルダーにタイトルを渡す。
//        builder.setTitle(R.string.edit_category_dialog_fragment_tv_header);

        //// ビルダーにViewを渡す。
        builder.setView(_view);
        AlertDialog dialog = builder.create();

        // カテゴリセット
        try(CategoryDao categoryDao = new CategoryDao(new MyOpenHelper(getActivity()))) {
            List<Category> categoryList = categoryDao.getCategoryList();
            _atvCategories = _view.findViewById(R.id.atvCategories2);
            setAutoCompleteTextView(categoryList);
            if(categoryList.size() > 5) {
                float ratioOfDpToPx = getResources().getDisplayMetrics().density;        // dpからpxへ変換するための比率。
                _atvCategories.setDropDownHeight( (int)(320 * ratioOfDpToPx + 0.5f) );   // px値でサイズ設定。
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        OnButtonClickListener onButtonClickListener = new OnButtonClickListener();
        _view.findViewById(R.id.btEditCategoryCreate).setOnClickListener(onButtonClickListener);   // 新規カテゴリ作成ボタン
        _view.findViewById(R.id.btEditCategoryDelete).setOnClickListener(onButtonClickListener);   // カテゴリ削除ボタン
        _view.findViewById(R.id.btEditCategoryCancel).setOnClickListener(onButtonClickListener);   // キャンセルボタン

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }


    public class OnButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btEditCategoryCreate) {
                addCategory();
                Toast.makeText(getActivity(), R.string.edit_category_dialog_fragment_toast_add, Toast.LENGTH_SHORT).show();

            } else if(view.getId() == R.id.btEditCategoryDelete) {
                deleteCategory();
                Toast.makeText(getActivity(), R.string.edit_category_dialog_fragment_toast_delete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.edit_category_dialog_fragment_toast_cancel, Toast.LENGTH_SHORT).show();
            }
            ((MainActivity)getActivity()).createCategoryMenu();
            dismiss();
        }

    }


    public void addCategory() {
        try(CategoryDao categoryDao = new CategoryDao(new MyOpenHelper(getActivity())) ){
            if ( !(_atvCategories.getText().toString().equals("")) ) {
                categoryDao.addCategory(_atvCategories.getText().toString());          // 入力された文字列をカテゴリテーブルに登録する。
                _subMenu.add(_atvCategories.getText().toString());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deleteCategory() {
        try(CategoryDao categoryDao = new CategoryDao(new MyOpenHelper(getActivity()));
            ItemCategoryIdDao itemCategoryIdDao = new ItemCategoryIdDao(new MyOpenHelper(getActivity()))){
            if ( !(_atvCategories.getText().toString().equals("")) ) {
                String deleteCategoryName = _atvCategories.getText().toString();
                int deleteCategoryId = categoryDao.getCategoryIdByName(deleteCategoryName);
                itemCategoryIdDao.removeItemCategoryIdByMemoId(new ItemCategoryId(deleteCategoryId,0));
                categoryDao.deleteCategory(deleteCategoryName);          // 入力されたカテゴリ名のレコードをカテゴリテーブルから削除する。

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private void setAutoCompleteTextView(List<Category> categoryList) {
        String[] categoryStrs = new String[categoryList.size() - 2];                // 「全て表示」と「ゴミ箱」分引いたカテゴリ数で、String配列生成。
        for (int i = 0; i < categoryStrs.length; i++) {
            categoryStrs[i] = categoryList.get(i + 1).getCategoryTitle();           // 最初の「全て表示」と最後の「ゴミ箱」以外のタイトルを代入
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.select_dialog_item, categoryStrs);     // カテゴリタイトル配列をアダプタにセット。
        _atvCategories.setAdapter(adapter);                                                                                    // スピナーにアダプタをセット。
        _atvCategories.setThreshold(1);
        _atvCategories.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    _atvCategories.showDropDown();
                }
            }
        });

        _atvCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _atvCategories.showDropDown();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
