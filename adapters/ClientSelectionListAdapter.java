package com.aconst.spinareg.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.R;
import com.aconst.spinareg.clients.NewEditClientActivity;
import com.aconst.spinareg.controllers.ConfirmDelClientDialog;
import com.aconst.spinareg.model.Client;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolderClient> {
    private List<Client> clients;
    private Context context;

    public ClientListAdapter(List<Client> clients, Context context) {
        this.clients = clients;
        this.context = context;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    public ViewHolderClient onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_list_item, parent, false);
        return new ViewHolderClient(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderClient holder, int position) {
        Client client = clients.get(position);

        String avatar = client.getAvatar();
        String path = context.getCacheDir().getPath();
        if (avatar!= null && !avatar.isEmpty()) {
            byte[] data = Common.readFile(path + "/" + avatar);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.ivClientPhoto.setImageBitmap(bmp);
        }
        // ToDo - отключить в рабочем варианте
        String name = String.format(Locale.getDefault(), "(%d) ", client.getId()) + client.getFullName();
        holder.tvClientName.setText(name);

        Date date = client.getBirthday();
        if (date == null)
            holder.tvAge.setText("");
        else
            holder.tvAge.setText(CalendarHelper.getAge(date, new Date()));

        holder.ivArrowRightCircle.setVisibility(View.GONE);

        if (position % 2 == 0) {
            holder.itemView.setBackground(context.getResources().getDrawable(
                    R.drawable.client_bg_dark));

        } else {
            holder.itemView.setBackground(context.getResources().getDrawable(
                    R.drawable.client_bg_light));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public Client getItem(int position) {
        return clients.get(position);
    }

    class ViewHolderClient extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public ImageView ivClientPhoto;
        public TextView tvClientName;
        public TextView tvAge;
        public ImageView ivArrowRightCircle;

        ViewHolderClient(View itemView) {
            super(itemView);
            ivClientPhoto = itemView.findViewById(R.id.ivClientPhoto);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvAge = itemView.findViewById(R.id.tvAge);
            ivArrowRightCircle = itemView.findViewById(R.id.ivArrowRightCircle);

            ivClientPhoto.setOnClickListener(this);
            ivClientPhoto.setOnLongClickListener(this);

            tvClientName.setOnClickListener(this);
            tvClientName.setOnLongClickListener(this);

            tvAge.setOnClickListener(this);
            tvAge.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Client client = ClientListAdapter.this.getItem(getAdapterPosition());
            if (client != null) {
                Intent intent = new Intent(v.getContext(), NewEditClientActivity.class);
                intent.putExtra("clientId", client.getId());
                intent.putExtra("cardId", client.getCardID());
                v.getContext().startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Client client = ClientListAdapter.this.getItem(getAdapterPosition());
            int delId = client.getId();
            new ConfirmDelClientDialog().onCreateDialog(context, delId).show();
            return true;
        }
    }
}
