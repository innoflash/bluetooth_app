package net.faithgen.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.faithgen.bluetooth.utils.Constants;
import net.faithgen.bluetooth.utils.Dialogs;
import net.faithgen.bluetooth.utils.Progress;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MultiplePermissionsListener {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private Intent enableBtIntent;
    private Handler handler;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private boolean isScanning;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leScanCallback = initScanCallback();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback initScanCallback() {
        return (bluetoothDevice, i, bytes) -> {

        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK)
                    Progress.showToast(this, Constants.BLUETOOTH_SWITCHED_ON);
                else Dialogs.showOkDialog(this, Constants.FAILED_BT_SWITCH_ON, false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(() -> {
                isScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
            }, SCAN_PERIOD);

            isScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            isScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBluetoothStatus();
/*        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(this)
                .check();*/
    }

    private void checkBluetoothStatus() {
        if (bluetoothAdapter != null)
            if (bluetoothAdapter.isEnabled()) {
                //todo when bluetooth enabled
            } else {
            }
        // Dialogs.confirmDialog(this, Constants.ATTENTION, Constants.BT_OPEN_QUERY);
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {

    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

    }
}
