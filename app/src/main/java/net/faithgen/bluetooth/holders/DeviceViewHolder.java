package net.faithgen.bluetooth.holders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.innoflash.iosview.lists.ListItemView2;

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    private ListItemView2 deviceHolder;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);
        deviceHolder = (ListItemView2) itemView;
    }

    public ListItemView2 getDeviceHolder() {
        return deviceHolder;
    }
}
