package net.faithgen.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.faithgen.bluetooth.adapters.DevicesAdapter;
import net.faithgen.bluetooth.utils.Constants;
import net.faithgen.bluetooth.utils.Dialogs;
import net.faithgen.bluetooth.utils.Progress;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements MultiplePermissionsListener {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private Intent enableBtIntent;
    private Handler handler;
    private List<BluetoothDevice> bluetoothDevices;
    private RecyclerView devicesView;
    private DevicesAdapter devicesAdapter;
    private boolean isScanning;
    private BluetoothAdapter.LeScanCallback leScanCallback = (bluetoothDevice, i, bytes) -> {
        runOnUiThread(() -> {
            bluetoothDevices.add(bluetoothDevice);
            if (devicesAdapter == null || devicesAdapter.getItemCount() == 0) {
                devicesAdapter = new DevicesAdapter(MainActivity.this, bluetoothDevices);
                devicesView.setAdapter(devicesAdapter);
            } else devicesAdapter.notifyDataSetChanged();
        });
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothDevices = new ArrayList<>();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        devicesView = findViewById(R.id.devicesView);
        devicesView.setLayoutManager(new LinearLayoutManager(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback initScanCallback() {
        return (bluetoothDevice, i, bytes) -> {

        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Progress.showToast(this, Constants.BLUETOOTH_SWITCHED_ON);
                    scanLeDevice(true);
                } else Dialogs.showOkDialog(this, Constants.FAILED_BT_SWITCH_ON, false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            isScanning = true;
            Progress.showToast(MainActivity.this, Constants.SCANNING_DEVICES);
            bluetoothAdapter.startLeScan(leScanCallback);

            // Stops scanning after a pre-defined scan period.
            if (handler == null) handler = new Handler();
            handler.postDelayed(() -> {
                isScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
                Progress.showToast(MainActivity.this, Constants.SCAN_COMPLETE);
            }, SCAN_PERIOD);


        } else {
            isScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onStart() {
        super.onStart();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) scanLeDevice(true);
/*        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(this)
                .check();*/
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {

    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

    }
}
