package net.faithgen.bluetooth.utils;

import android.content.Context;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

public class Progress {
    public static KProgressHUD progressHUD;

    public static KProgressHUD getProgressHUD() {
        return progressHUD;
    }

    public static void showProgress(Context context, String message) {
        progressHUD = KProgressHUD.create(context)
                .setAutoDismiss(false)
                .setCancellable(false)
                .setLabel(message == null ? Constants.PLEASE_WAIT : message)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDimAmount(0.5f)
                .show();
    }

    public static void dismissProgress() {
        getProgressHUD().dismiss();
    }

    public static void showToast(Context context,String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
