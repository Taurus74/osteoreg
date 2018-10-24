package com.aconst.spinareg.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.model.ServiceItemRealm;

import java.util.List;
import java.util.Locale;

public class CliestServiceAdapter extends RecyclerView.Adapter<CliestServiceAdapter.ViewHolder> {
    private List<ServiceItemRealm> services;

    public CliestServiceAdapter(List<ServiceItemRealm> services) {
        this.services = services;
    }

    @Override
    public CliestServiceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_service_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CliestServiceAdapter.ViewHolder holder, int position) {
        ServiceItemRealm service = getItem(position);
        holder.tvServiceName.setText(service.getTitle());
        holder.tvDuration.setText(String.format(Locale.getDefault(), "%d мин.", service.getDuration()));
        // ToDo - вывод валюты учета
        holder.tvCost.setText(String.format(Locale.getDefault(), "%.2f руб.", service.getPrice()));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    private ServiceItemRealm getItem(int position) {
        return services.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvServiceName;
        public TextView tvDuration;
        public TextView tvCost;

        public ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCost = itemView.findViewById(R.id.tvCost);
        }
    }
}
