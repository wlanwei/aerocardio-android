package com.uteamtec.heartcool.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.views.widget.CheckIconView;
import com.uteamtec.heartcool.views.widget.RunningIconView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wd
 */
public class DeviceListAdapter extends ArrayAdapter<UserDevice> {

    private List<UserDevice> devices;
    private Map<String, Integer> maps = new HashMap<>();

    private int resource;

    private LayoutInflater inflater;

    public DeviceListAdapter(Context context, int resource, List<UserDevice> devices) {
        super(context, resource, devices);
        this.resource = resource;
        this.devices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return devices.size();
    }

    @Override
    public UserDevice getItem(int position) {
        if (position >= 0 && position < getCount()) {
            return devices.get(position);
        }
        return null;
    }

    public void clear() {
        devices.clear();
        maps.clear();
    }

    public synchronized void add(UserDevice d) {
        if (d == null || TextUtils.isEmpty(d.getMacAddr())) {
            return;
        }
        Integer i = maps.get(d.getMacAddr());
        if (i == null) {
            devices.add(d);
            maps.put(d.getMacAddr(), devices.size() - 1);
        } else {
            devices.set(i, d);
        }
    }

    public void setState(int position, int state) {
//        L.e("DeviceListAdapter-> setState: " + position + "-" + state);
        UserDevice dev = getItem(position);
        if (dev != null) {
            dev.setState(state);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(resource, null);
            holder.devIcon = (ImageView) convertView.findViewById(R.id.icon_dev);
            holder.devName = (TextView) convertView.findViewById(R.id.dev_name);
            holder.devMac = (TextView) convertView.findViewById(R.id.dev_mac);
            holder.runningIcon = (RunningIconView) convertView.findViewById(R.id.iconRunning);
            holder.checkIcon = (CheckIconView) convertView.findViewById(R.id.iconCheck);
            holder.connectedIcon = (ImageView) convertView.findViewById(R.id.iconConnected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserDevice dev = devices.get(position);

        holder.devName.setText(dev.getName());
        holder.devMac.setText(dev.getMacAddr());

//        L.e("DeviceListAdapter-> getView: " + position + "-" + dev.getState());

        switch (dev.getState()) {
            case UserDevice.STATE_CONNECTED:// connected update device list
                holder.devIcon.setImageResource(R.drawable.dev_on);
                holder.checkIcon.setVisibility(View.VISIBLE);
                holder.checkIcon.startInitAnimation();

                holder.connectedIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.clearAnimation();
                break;
            case UserDevice.STATE_ON:// device online
                holder.devIcon.setImageResource(R.drawable.dev_on);

                holder.checkIcon.setVisibility(View.INVISIBLE);
                holder.checkIcon.clearAnimation();
                holder.connectedIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.clearAnimation();
                break;
            case UserDevice.STATE_OFF:// device off
                holder.devIcon.setImageResource(R.drawable.dev_off);

                holder.checkIcon.setVisibility(View.INVISIBLE);
                holder.checkIcon.clearAnimation();

                holder.connectedIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.setVisibility(View.INVISIBLE);
                holder.runningIcon.clearAnimation();
                break;
            case UserDevice.STATE_CONNECTING:// device connecting
                holder.devIcon.setImageResource(R.drawable.dev_on);

                holder.checkIcon.clearAnimation();
                holder.checkIcon.setVisibility(View.INVISIBLE);

                holder.connectedIcon.setVisibility(View.INVISIBLE);

                holder.runningIcon.setVisibility(View.VISIBLE);
                holder.runningIcon.startInitAnimation();
                break;
        }
        return convertView;
    }

    private final static class ViewHolder {
        TextView devName;
        TextView devMac;
        ImageView devIcon;
        RunningIconView runningIcon;
        CheckIconView checkIcon;
        ImageView connectedIcon;
    }

}