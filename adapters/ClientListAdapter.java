package com.aconst.spinareg.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.model.Client;

import java.io.IOException;
import java.util.List;

public class ClientsAdapter extends BaseAdapter {
    private List<Client> clients;
    private Context context;

    public ClientsAdapter(List<Client> clients, Context context) {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public int getCount() {
        return clients.size();
    }

    @Override
    public Object getItem(int position) {
        return clients.get(position);
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
                    R.layout.client_list_item, parent, false);
        } else {
            view = convertView;
        }

        Client client = (Client) getItem(position);

        ImageView ivPhoto = view.findViewById(R.id.ivClientPhoto);
        try {
            Uri uri = Uri.parse(client.getAvatar());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), uri);
            ivPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView tvName = view.findViewById(R.id.tvClientName);
        tvName.setText(client.toString());

        return view;
    }
}
