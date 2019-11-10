package net.faithgen.bluetooth.utils;

import android.app.Activity;
import android.content.Context;

import com.gdacciaro.iOSDialog.iOSDialogBuilder;

import net.faithgen.bluetooth.interfaces.DialogListener;

public class Dialogs {
    public static void showOkDialog(Context context, String message, boolean closeActivity) {
        new iOSDialogBuilder(context)
                .setTitle(Constants.SERVER_RESPONSE)
                .setSubtitle(message == null ? Constants.SERVER_ERROR : message)
                .setBoldPositiveLabel(true)
                .setCancelable(true)
                .setPositiveListener(Constants.OK, dialog -> {
                    dialog.dismiss();
                    if (closeActivity) {
                        Activity activity = (Activity) context;
                        activity.finish();
                    }
                })
                .build().show();
    }

    public static void confirmDialog(Context context, String title, String message, DialogListener dialogListener) {
        new iOSDialogBuilder(context)
                .setTitle(title != null ? title : "Faith Gen")
                .setSubtitle(message)
                .setBoldPositiveLabel(true)
                .setCancelable(false)
                .setPositiveListener(Constants.YES, dialog -> {
                    dialog.dismiss();
                    if (dialogListener != null)
                        dialogListener.onYes();
                })
                .setNegativeListener(Constants.NOPE, dialog -> {
                    dialog.dismiss();
                    if (dialogListener != null)
                        dialogListener.onNope();
                })
                .build()
                .show();
    }
}
