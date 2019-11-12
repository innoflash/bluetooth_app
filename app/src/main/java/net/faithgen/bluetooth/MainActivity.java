package net.faithgen.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

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
import net.faithgen.bluetooth.dialogs.ChatDialog;
import net.faithgen.bluetooth.interfaces.ChatDialogListener;
import net.faithgen.bluetooth.interfaces.DialogListener;
import net.faithgen.bluetooth.utils.Constants;
import net.faithgen.bluetooth.utils.Dialogs;
import net.faithgen.bluetooth.utils.Progress;
import net.faithgen.bluetooth.utils.Utils;
import net.innoflash.iosview.recyclerview.RecyclerTouchListener;
import net.innoflash.iosview.recyclerview.RecyclerViewClickListener;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements MultiplePermissionsListener, RecyclerViewClickListener {

    private UUID UUID_Service;
    private UUID UUID_characteristic;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final String serviceUUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String characteristicUUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bleGatt;
    private BluetoothDevice bluetoothDevice;
    private Intent enableBtIntent;
    private Intent broadcastIntent;
    private Handler handler;
    private ScanFilter scanFilter;
    private ScanSettings settings;
    private List<BluetoothDevice> bluetoothDevices;
    private RecyclerView devicesView;
    private DevicesAdapter devicesAdapter;
    private ScanCallback scanCallback;
    private BluetoothGattCallback bluetoothGattCallback;
    private BroadcastReceiver connectionReceiver;
    private IntentFilter connectionFilter;
    private ChatDialog chatDialog;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothGattDescriptor bluetoothGattDescriptor;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionFilter = new IntentFilter();
        connectionFilter.addAction(Constants.DEVICES_CONNECTED);
        connectionFilter.addAction(Constants.DEVICES_DISCONNECTED);
        connectionFilter.addAction(Constants.WRITABLE_CHARACTERISTIC_FOUND);
        connectionFilter.addAction(Constants.MESSAGE_SENT);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        devicesView = findViewById(R.id.devicesView);
        devicesView.setLayoutManager(new LinearLayoutManager(this));
        devicesView.addOnItemTouchListener(new RecyclerTouchListener(this, devicesView, this));

        scanCallback = initScanCallback();
        bluetoothGattCallback = initGattCallback();
        connectionReceiver = initConnectionReceiver();

        registerReceiver(connectionReceiver, connectionFilter);
    }

    private BroadcastReceiver initConnectionReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case Constants.DEVICES_CONNECTED:
                        Progress.showToast(MainActivity.this, Constants.DISCOVERING_SERVICES);
                        break;
                    case Constants.DEVICES_DISCONNECTED:
                        Dialogs.showOkDialog(MainActivity.this, Constants.FAILED_TO_CONNECT, false);
                        bleGatt.disconnect();
                        break;
                    case Constants.WRITABLE_CHARACTERISTIC_FOUND:
                        chatDialog = new ChatDialog();
                        chatDialog.setChatDialogListener(new ChatDialogListener() {
                            @Override
                            public void onOpened() {

                            }

                            @Override
                            public void onClose() {
                                if (bleGatt != null)
                                    bleGatt.disconnect();
                            }

                            @Override
                            public void onMessageSent(String message) {
                                byte[] strBytes = message.getBytes();
                                byte[] bytes = bluetoothGattCharacteristic.getValue();
                                bluetoothGattCharacteristic.setValue(strBytes);
                                bleGatt.writeCharacteristic(bluetoothGattCharacteristic);
                            }
                        });
                        chatDialog.show(MainActivity.this.getSupportFragmentManager(), ChatDialog.DIALOG_TAG);
                        break;
                    case Constants.MESSAGE_SENT:
                        bleGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                        bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptors().get(0);
                        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bleGatt.writeDescriptor(bluetoothGattDescriptor);
                        break;
                }
            }
        };
    }


    private BluetoothGattCallback initGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Progress.dismissProgress();
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    broadcastIntent = new Intent(Constants.DEVICES_CONNECTED);
                    sendBroadcast(broadcastIntent);
                    gatt.discoverServices();
                    Log.d("Tag", "onConnectionStateChange: device connected");
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    broadcastIntent = new Intent(Constants.DEVICES_DISCONNECTED);
                    sendBroadcast(broadcastIntent);
                } else
                    Log.d("state", "onConnectionStateChange: " + newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Progress.dismissProgress();
                bluetoothGattService = gatt.getService(UUID.fromString(serviceUUID));
                if (bluetoothGattService != null) {
                    bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristicUUID));
                    if (bluetoothGattCharacteristic != null) {
                        bleGatt = gatt;
                        broadcastIntent = new Intent(Constants.WRITABLE_CHARACTERISTIC_FOUND);
                        sendBroadcast(broadcastIntent);
                    } else bleGatt.disconnect();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                chatDialog.setReceivedMessage(new String(characteristic.getValue(), Charset.defaultCharset()));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                broadcastIntent = new Intent(Constants.MESSAGE_SENT);
                sendBroadcast(broadcastIntent);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback initScanCallback() {
        return new ScanCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
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
        };
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(connectionReceiver);
        if (bleGatt != null) {
            bleGatt.close();
            bleGatt = null;
        }
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

                    if (handler == null) handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        bleScanner.stopScan(scanCallback);
                        Progress.showToast(MainActivity.this, Constants.SCAN_COMPLETE);
                        if (bluetoothDevices.size() == 0)
                            Dialogs.confirmDialog(MainActivity.this, Constants.ATTENTION, Constants.NO_DEVICES_FOUND, new DialogListener() {
                                @Override
                                public void onYes() {
                                    scanLeDevices();
                                }
                            });
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view, int position) {
        if (bleScanner != null) bleScanner.stopScan(scanCallback);
        Progress.showProgress(this, Constants.CONNECTING);
        bleGatt = bluetoothDevices.get(position).connectGatt(this, false, bluetoothGattCallback);
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onBackPressed() {
        if (Progress.getProgressHUD().isShowing()) Progress.dismissProgress();
        else super.onBackPressed();
    }
}
