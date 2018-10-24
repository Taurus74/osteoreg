package com.aconst.spinareg.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.clients.Contact;

import java.io.IOException;
import java.util.ArrayList;

public class ContactsAdapter extends BaseAdapter implements View.OnClickListener {
    private ArrayList<Contact> data;
    private SparseBooleanArray mCheckedMap = new SparseBooleanArray();

    private Context context;
    private boolean singleClient;

    public ContactsAdapter(ArrayList<Contact> data, Context context, boolean singleClient) {
        this.data = data;
        this.context = context;
        this.singleClient = singleClient;
        for (int i = 0; i < data.size(); i++)
            mCheckedMap.put(i, false);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null)
            view = LayoutInflater.from(context).inflate(
                    R.layout.contacts_list_item, parent, false);
        else
            view = convertView;

        Contact contact = (Contact) getItem(position);

        ImageView ivPhoto = view.findViewById(R.id.ivPhoto);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), contact.getPhotoUri());
            ivPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CheckedTextView tvName = view.findViewById(R.id.tvName);

        if (singleClient)
            tvName.setCheckMarkDrawable(null);
        else
            tvName.setChecked(mCheckedMap.valueAt(position));
        tvName.setText(contact.getName());
        tvName.setTag(position);

        TextView tvPhone = view.findViewById(R.id.tvPhone);
        tvPhone.setText(contact.getPhone());
        tvPhone.setTag(position);
        if (singleClient) {
            tvName.setOnClickListener((View.OnClickListener) context);
            tvPhone.setOnClickListener((View.OnClickListener) context);

        } else {
            tvName.setOnClickListener(this);
            tvPhone.setOnClickListener(this);
        }

        return view;
    }

    private void toggleChecked(int position) {
        int key = mCheckedMap.keyAt(position);
        mCheckedMap.put(key, !mCheckedMap.get(key));
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        toggleChecked((Integer) v.getTag());
    }

    public ArrayList<Contact> getCheckedContacts() {
        ArrayList<Contact> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (mCheckedMap.valueAt(i))
                result.add(data.get(i));
        }
        return result;
    }
}
