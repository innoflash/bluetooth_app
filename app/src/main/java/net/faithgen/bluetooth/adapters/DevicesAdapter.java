package net.faithgen.bluetooth.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.faithgen.bluetooth.R;
import net.faithgen.bluetooth.holders.DeviceViewHolder;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
    private Context context;
    private List<BluetoothDevice> bluetoothDevices;
    private LayoutInflater layoutInflater;

    public DevicesAdapter(Context context, List<BluetoothDevice> bluetoothDevices) {
        this.context = context;
        this.bluetoothDevices = bluetoothDevices;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(layoutInflater.inflate(R.layout.list_item_bluetooth_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.getDeviceHolder().setHeader(bluetoothDevices.get(position).getName());
        holder.getDeviceHolder().setContent(bluetoothDevices.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }
}
