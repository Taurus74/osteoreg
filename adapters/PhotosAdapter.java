package com.aconst.spinareg.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aconst.spinareg.R;

import java.util.List;

public class PhotosAdapterNew extends RecyclerView.Adapter<PhotosAdapterNew.ViewHolder> {
    private List<String> photos;
    private String path;

    public PhotosAdapterNew(List<String> photos, String path) {
        this.photos = photos;
        this.path = path;
    }

    @Override
    public PhotosAdapterNew.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_item_add, parent, false);
            return new ViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_item, parent, false);
            return new ViewHolderPhoto(v);
        }
    }

    @Override
    public void onBindViewHolder(PhotosAdapterNew.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ViewHolderPhoto viewHolderPhoto = (ViewHolderPhoto) holder;
            Bitmap bm = BitmapFactory.decodeFile(path + photos.get(position));
            viewHolderPhoto.photo.setImageBitmap(bm);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0? 0: 1);
    }

    public String getItem(int position) {
        return photos.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ViewHolderPhoto extends ViewHolder {
        public ImageView photo;

        public ViewHolderPhoto(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.sessionPhoto);
        }
    }
}
