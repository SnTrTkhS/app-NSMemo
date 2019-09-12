package com.example.simplememo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.SQLException;

public class DeleteDialogFragment extends DialogFragment {
    private MyOpenHelper _helper;
    private int _memoId;

    DeleteDialogFragment(MyOpenHelper helper,int memoId) {
        _helper = helper;
        _memoId = memoId;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // ダイアログ建設者、ビルダーを呼ぶ。
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // ビルダーにタイトルを渡す。
        builder.setTitle(R.string.delete_dialog_fragment_header);
        //// ビルダーにメッセージを渡す。
        //builder.setMessage();
        // ビルダーにボタン設定を渡す。
        builder.setPositiveButton(R.string.delete_dialog_fragment_ok, new DialogButtonClickListener());
        builder.setNegativeButton(R.string.delete_dialog_fragment_cancel, new DialogButtonClickListener());
        AlertDialog dialog = builder.create();

        return dialog;
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    try(MemoDao memoDao = new MemoDao(_helper)) {
                        memoDao.deleteMemo(_memoId);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ((MainActivity)getActivity()).createMemoList();
                    Toast.makeText(getActivity(), R.string.delete_dialog_fragment_toast_ok, Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(getActivity(), R.string.delete_dialog_fragment_toast_cancel, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}
