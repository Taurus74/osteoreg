package com.aconst.spinareg.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.R;
import com.aconst.spinareg.controllers.ClientsController;
import com.aconst.spinareg.messages.MsgThreadActivity;
import com.aconst.spinareg.model.Client;
import com.aconst.spinareg.model.Message;

import java.util.LinkedList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<Message> messageList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Message message);
    }
    private final OnItemClickListener onItemClickListener;

    public MessagesAdapter(Context context, List<Message> messageList,
                           OnItemClickListener onItemClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.onItemClickListener = onItemClickListener;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessagesAdapter.ViewHolder holder, int position) {
        Message message = getItem(position);
        int clientId = message.getFrom();
        Client client = new ClientsController().getClient(clientId).blockingGet();
        String avatar = client.getAvatar();
        String path = context.getCacheDir().getPath();
        if (avatar != null && !avatar.isEmpty()) {
            byte[] data = Common.readFile(path + "/" + avatar);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.ivAvatar.setImageBitmap(bmp);
        }

        if (message.getStatus().equalsIgnoreCase("wait")) {
            holder.tvClientName.setTextColor(context.getResources().getColor(R.color.colorMessageNameNew));
            SpannableString spanString = new SpannableString(message.getFullName());
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            holder.tvClientName.setText(spanString);
        }
        else {
            holder.tvClientName.setText(message.getFullName());
        }

        holder.tvMessage.setText(message.getMessage());
        holder.tvTimeDate.setText(CalendarHelper.dateToString(
                message.getSentDate(), "HH:mm, dd MMMM yyyy"));
        if (message.getCountNew() == 0)
            holder.tvNewMessagesCount.setVisibility(View.INVISIBLE);
        else
            holder.tvNewMessagesCount.setText(message.getCountNew());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAvatar;
        public TextView tvClientName;
        public TextView tvMessage;
        public TextView tvTimeDate;
        public TextView tvNewMessagesCount;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimeDate = itemView.findViewById(R.id.tvTimeDate);
            tvNewMessagesCount = itemView.findViewById(R.id.tvNewMessagesCount);
        }
    }
}