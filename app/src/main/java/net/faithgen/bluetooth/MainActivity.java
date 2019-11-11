package net.faithgen.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.faithgen.bluetooth.adapters.DevicesAdapter;
import net.faithgen.bluetooth.interfaces.DialogListener;
import net.faithgen.bluetooth.utils.Constants;
import net.faithgen.bluetooth.utils.Dialogs;
import net.faithgen.bluetooth.utils.Progress;
import net.faithgen.bluetooth.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements MultiplePermissionsListener {

    private UUID UUID_Service;
    private UUID UUID_characteristic;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bleGatt;
    private Intent enableBtIntent;
    private Handler handler;
    private ScanFilter scanFilter;
    private ScanSettings settings;
    private List<BluetoothDevice> bluetoothDevices;
    private RecyclerView devicesView;
    private DevicesAdapter devicesAdapter;
    private ScanCallback scanCallback;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     /*   UUID_Service = UUID.fromString("19fc95c0-c111–11e3–9904–0002a5d5c51b");
        UUID_characteristic = UUID.fromString("21fac9e0-c111–11e3–9246–0002a5d5c51b");*/

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        devicesView = findViewById(R.id.devicesView);
        devicesView.setLayoutManager(new LinearLayoutManager(this));

        scanCallback = initScanCallback();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback initScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                runOnUiThread(() -> {
                    bluetoothDevices = Utils.getUniqueDevices(bluetoothDevices, result.getDevice());
                    if (devicesAdapter == null || devicesAdapter.getItemCount() == 0) {
                        devicesAdapter = new DevicesAdapter(MainActivity.this, bluetoothDevices);
                        devicesView.setAdapter(devicesAdapter);
                    } else devicesAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
           //     Dialogs.showOkDialog(MainActivity.this, Constants.SCAN_FAILED, false);
            }
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
                    scanLeDevices();
                } else Dialogs.showOkDialog(this, Constants.FAILED_BT_SWITCH_ON, false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevices() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            Dialogs.confirmDialog(this, Constants.ATTENTION, Constants.LOCATION_REQUIRED, new DialogListener() {
                @Override
                public void onYes() {
                    Utils.openSettings(MainActivity.this);
                }

                @Override
                public void onNope() {
                    super.onNope();
                    Dialogs.showOkDialog(MainActivity.this, Constants.CANT_SCAN, false);
                }
            });
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (bleScanner != null) {
                    Progress.showToast(MainActivity.this, Constants.SCANNING_DEVICES);
                    scanFilter = new ScanFilter.Builder().build();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    bleScanner.startScan(Arrays.asList(scanFilter), settings, scanCallback);

                    if (handler == null) handler = new Handler();
                    handler.postDelayed(() -> {
                        bleScanner.stopScan(scanCallback);
                        Progress.showToast(MainActivity.this, Constants.SCAN_COMPLETE);
                        if (bluetoothDevices.size() == 0)
                            Dialogs.showOkDialog(MainActivity.this, Constants.NO_DEVICES_FOUND, false);
                    }, SCAN_PERIOD);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onStart() {
        super.onStart();
        bluetoothDevices = new ArrayList<>();
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(this)
                .check();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) scanLeDevices();
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        scanLeDevices();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

    }
}
