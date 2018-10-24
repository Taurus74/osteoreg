package com.aconst.spinareg.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.model.ServiceItemRealm;

import java.util.List;
import java.util.Locale;

public class ServiceResponseAdapter extends BaseAdapter {
    private List<ServiceItemRealm> serviceItems;
    private Context context;

    public ServiceResponseAdapter(List<ServiceItemRealm> serviceItems, Context context) {
        this.serviceItems = serviceItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return serviceItems.size();
    }

    @Override
    public Object getItem(int position) {
        return serviceItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(
                    R.layout.service_item, parent, false);
        } else {
            view = convertView;
        }

        ServiceItemRealm item = (ServiceItemRealm) getItem(position);

        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        tvServiceName.setText(item.getTitle());

        TextView tvDuration = view.findViewById(R.id.tvDuration);
        tvDuration.setText(String.format(Locale.getDefault(), "%d", item.getDuration()));

        TextView tvCost = view.findViewById(R.id.tvCost);
        tvCost.setText(String.format(Locale.getDefault(), "%.2f %s", item.getPrice(), item.getCurrency()));

        return view;
    }
}
