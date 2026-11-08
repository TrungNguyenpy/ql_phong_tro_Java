package com.example.manager_room.util;

import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {

    // Hộp thoại nhập liệu: không đóng khi bấm ra ngoài, co lại khi bàn phím hiện lên
    public static AlertDialog showForm(MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = builder.setCancelable(false).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
        return dialog;
    }
}