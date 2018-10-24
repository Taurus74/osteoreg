package com.aconst.spinareg.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.services.ConfirmDelServiceDialog;
import com.aconst.spinareg.services.GetServicesActivity;
import com.aconst.spinareg.services.NewEditServiceActivity;
import com.aconst.spinareg.sessions.EditSessionActivity;
import com.aconst.spinareg.sessions.NewSession2Activity;

import java.util.List;
import java.util.Locale;

import static com.aconst.spinareg.Common.RC_SELECT_SERVICE;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder> {
    private Context context;
    private List<ServiceItemRealm> serviceItems;
    private boolean editable;

    public ServicesAdapter(Context context, List<ServiceItemRealm> serviceItems, boolean editable) {
        this.context = context;
        this.serviceItems = serviceItems;
        this.editable = editable;
    }

    public void setServiceItems(List<ServiceItemRealm> serviceItems) {
        this.serviceItems = serviceItems;
    }

    @Override
    public ServicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.service_item_add, parent, false);
            return new ViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.service_item_selected, parent, false);
            return new ViewHolderService(v);
        }
    }

    @Override
    public void onBindViewHolder(ServicesAdapter.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ServiceItemRealm item = getItem(position);
            ViewHolderService holderService = (ViewHolderService) holder;
            holderService.tvServiceName.setText(item.getTitle());
            holderService.tvDuration.setText(String.format(Locale.getDefault(), "%d мин.",
                    item.getDuration()));
            holderService.tvCost.setText(String.format(Locale.getDefault(), "%.2f %s",
                    item.getPrice(), item.getCurrency()));
        }
    }

    @Override
    public int getItemCount() {
        return serviceItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (editable? (position == 0? 0: 1): 1);
    }

    public ServiceItemRealm getItem(int position) {
        return serviceItems.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ViewHolderService extends ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        ImageView ivPencil;
        public TextView tvServiceName;
        public TextView tvDuration;
        public TextView tvCost;

        ViewHolderService(View itemView) {
            super(itemView);
            ivPencil = itemView.findViewById(R.id.ivPencil);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCost = itemView.findViewById(R.id.tvCost);

            ivPencil.setOnClickListener(this);

            tvServiceName.setOnClickListener(this);
            tvServiceName.setOnLongClickListener(this);

            tvDuration.setOnClickListener(this);
            tvDuration.setOnLongClickListener(this);

            tvCost.setOnClickListener(this);
            tvCost.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            ServiceItemRealm serviceItem = getItem(getAdapterPosition());
            if (v.getId() == R.id.ivPencil) {
                // Редактировать выбранную услугу
                Intent intent = new Intent(context, NewEditServiceActivity.class);
                intent.putExtra("serviceId", serviceItem.getServID());
                context.startActivity(intent);
            }
            else {
                // Заменить выбранную услугу на другую
                Intent intent = new Intent(context, GetServicesActivity.class);
                // Передать id услуги
                intent.putExtra("selectedServId", serviceItem.getServID());
                if (context instanceof NewSession2Activity)
                    ((NewSession2Activity) context).startActivityForResult(intent, RC_SELECT_SERVICE);
                else if (context instanceof EditSessionActivity) {
                    ((EditSessionActivity) context).startActivityForResult(intent, RC_SELECT_SERVICE);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            ConfirmDelServiceDialog delServiceDialog = new ConfirmDelServiceDialog();
            delServiceDialog.onCreateDialog(context, getAdapterPosition()).show();
            return false;
        }
    }

}
