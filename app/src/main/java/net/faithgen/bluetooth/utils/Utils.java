package net.faithgen.bluetooth.utils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

public class Utils {
    public static void openSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<BluetoothDevice> getUniqueDevices(List<BluetoothDevice> bluetoothDevices, BluetoothDevice bluetoothDevice) {
        boolean itemExists = false;
        if (bluetoothDevices.size() == 0) bluetoothDevices.add(bluetoothDevice);
        else {
            for (BluetoothDevice device :
                    bluetoothDevices) {
                if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                    itemExists = true;
                    break;
                }
            }
            if (!itemExists) bluetoothDevices.add(0, bluetoothDevice);
        }
        return bluetoothDevices;
    }
}
