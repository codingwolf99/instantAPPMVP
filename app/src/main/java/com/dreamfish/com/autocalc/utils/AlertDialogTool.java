package com.dreamfish.com.autocalc.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dreamfish.com.autocalc.R;

import androidx.appcompat.app.AlertDialog;

public class AlertDialogTool {

    public static AlertDialog buildCustomBottomPopupDialog(Context context, View v) {
        return buildCustomStylePopupDialogGravity(context, v, Gravity.BOTTOM, R.style.DialogBottomPopup);
    }
    public static AlertDialog buildCustomStylePopupDialogGravity(Context context, View v, int gravity, int anim) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.WhiteRoundDialog)
                .setView(v)
                .setCancelable(true)
                .create();

        Window window = dialog.getWindow();
        window.setGravity(gravity);
        window.getDecorView().setPadding(0, 0, 0, 0);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(lp);
        window.setWindowAnimations(anim);

        return dialog;
    }
    public static AlertDialog.Builder buildBottomPopupDialogBuilder(Context context) {
        return new AlertDialog.Builder(context, R.style.WhiteRoundDialog);
    }
    public static AlertDialog buildBottomPopupDialog(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(lp);
        window.setWindowAnimations(R.style.DialogBottomPopup);

        return dialog;
    }
}
