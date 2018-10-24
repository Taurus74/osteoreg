package com.aconst.spinareg.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.calendar.Calendars;
import com.aconst.spinareg.calendar.ConfirmSessionCancelingDialog;
import com.aconst.spinareg.calendar.SessionExport;
import com.aconst.spinareg.controllers.ClientsController;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.Client;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.sessions.ConfirmDelTimeoutDialog;
import com.aconst.spinareg.sessions.EditSessionActivity;
import com.aconst.spinareg.sessions.NewSessionActivity;
import com.aconst.spinareg.sessions.TimeoutActivity;

import java.util.List;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.ViewHolderSession> {

    private static final int SESSION_EMPTY = 0;
    private static final int SESSION_PAST = 1;
    private static final int SESSION_FUTURE = 2;
    private static final int SESSION_TIMEOUT = 3;

    public final static int DATE_DAY = 0;
    public final static int DATE_LIST = 1;

    private List<SessionItemRealm> eventItemList;
    private int date_type;

    public SessionListAdapter(List<SessionItemRealm> eventItemList, int date_type) {
        this.eventItemList = eventItemList;
        this.date_type = date_type;
    }

    public void setEventItemList(List<SessionItemRealm> eventItemList) {
        this.eventItemList = eventItemList;
    }

    @Override
    public ViewHolderSession onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case SESSION_EMPTY:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_empty, parent, false);
                return new ViewHolderSession(view);

            case SESSION_TIMEOUT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_timeout, parent, false);
                return new ViewHolderSession(view);

            case SESSION_PAST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_past, parent, false);
                return new ViewHolderPastSession(view);

            case SESSION_FUTURE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_future, parent, false);
                return new ViewHolderFutureSession(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolderSession holder, int position) {
        SessionItemRealm item = eventItemList.get(position);

        String time1, time2;
        if (date_type == DATE_DAY) {
            time1 = CalendarHelper.getMinute(item.getStartTime());
            time2 = CalendarHelper.getMinute(item.getStopTime());

        } else {
            time1 = CalendarHelper.dateToString(item.getSessionDate(), Common.DATE_FORMAT_SHORT);
            time2 = CalendarHelper.getMinute(item.getStartTime())
                    + "-" + CalendarHelper.getMinute(item.getStopTime());
        }

        Client client = new Client();
        boolean noPhoneNumber = false;
        if (holder.getItemViewType() == SESSION_PAST
                || holder.getItemViewType() == SESSION_FUTURE) {
            client = new ClientsController().getClient(item.getClientId()).blockingGet();
            if (client.getPhone() == null || client.getPhone().isEmpty())
                noPhoneNumber = true;
        }
        switch (holder.getItemViewType()) {
            case SESSION_EMPTY :
            case SESSION_TIMEOUT :
                holder.tvTimeStart.setText(time1);
                holder.tvTimeStop.setText(time2);
                break;

            case SESSION_PAST :
                final ViewHolderPastSession viewHolderPastEvent = (ViewHolderPastSession) holder;
                viewHolderPastEvent.tvName.setText(client.getFullName());
                viewHolderPastEvent.tvSession.setText(getClientServices(item.getService()));
                viewHolderPastEvent.tvTimeStart.setText(time1);
                viewHolderPastEvent.tvTimeStop.setText(time2);
                viewHolderPastEvent.btnRepeatSession.setTag(item.getId());

                if (item.getStatus().equalsIgnoreCase("wait"))
                    viewHolderPastEvent.ivLabel.setImageResource(R.mipmap.label_future);
                else
                    viewHolderPastEvent.ivLabel.setImageResource(R.mipmap.label_checked);
                if (noPhoneNumber)
                    viewHolderPastEvent.ivPhone.setVisibility(View.GONE);
                break;

            case SESSION_FUTURE :
                ViewHolderFutureSession viewHolderFutureEvent = (ViewHolderFutureSession) holder;
                viewHolderFutureEvent.tvName.setText(client.getFullName());
                viewHolderFutureEvent.tvSession.setText(getClientServices(item.getService()));
                viewHolderFutureEvent.tvTimeStart.setText(time1);
                viewHolderFutureEvent.tvTimeStop.setText(time2);
                viewHolderFutureEvent.btnEditSession.setTag(item.getId());
                viewHolderFutureEvent.btnCancelSession.setTag(item.getId());
                viewHolderFutureEvent.btnAddToGooCal.setTag(item.getId());
                if (noPhoneNumber)
                    viewHolderFutureEvent.ivPhone.setVisibility(View.GONE);
                break;
        }
    }

    private String getClientServices(String services) {
        String[] serviceList = services.split(",");
        StringBuilder result = new StringBuilder();
        ServiceController controller = new ServiceController();
        for (String s : serviceList) {
            if (!s.isEmpty()) {
                s = controller.getService(Integer.parseInt(s.trim())).blockingGet().getTitle();
                if (result.length() > 0) {
                    result.append(", ");
                    s = s.toLowerCase();
                }
                result.append(s);
            }
        }
        return result.toString();
    }

    @Override
    public int getItemCount() {
        return eventItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        SessionItemRealm item = eventItemList.get(position);
        if (item.getService() == null || item.getService().isEmpty())
            return SESSION_EMPTY;

        else if (item.getClientId() == 0)
            return SESSION_TIMEOUT;

        else if (CalendarHelper.dateInPast(item.getSessionDate(), item.getStartTime()))
            return SESSION_PAST;

        else
            return SESSION_FUTURE;
    }

    public SessionItemRealm getItem(int position) {
        return eventItemList.get(position);
    }

    class ViewHolderSession extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public RelativeLayout layEmpty;
        public TextView tvTimeStart;
        public TextView tvTimeStop;
        public ImageView ivLabel;
        private long calID;

        ViewHolderSession(View itemView) {
            super(itemView);

            layEmpty = itemView.findViewById(R.id.layEmpty);
            tvTimeStart = itemView.findViewById(R.id.tvTimeStart);
            tvTimeStop = itemView.findViewById(R.id.tvTimeStop);
            ivLabel = itemView.findViewById(R.id.ivLabel);

            if (layEmpty != null) {
                layEmpty.setOnClickListener(this);
                layEmpty.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            SessionItemRealm itemRealm = SessionListAdapter.this.getItem(getAdapterPosition());
            int id = itemRealm.getId();
            Intent intent;
            switch (v.getId()) {
                case R.id.btnRepeatSession:
                    intent = new Intent(context, EditSessionActivity.class);
                    intent.putExtra("repeatId", id);
                    context.startActivity(intent);
                    break;

                case R.id.layText :
                case R.id.btnEdit :
                    intent = new Intent(context, EditSessionActivity.class);
                    intent.putExtra("sessionId", id);
                    context.startActivity(intent);
                    break;

                case R.id.btnCancel :
                    ConfirmSessionCancelingDialog dialog = new ConfirmSessionCancelingDialog();
                    PrefsHelper prefsHelper = new PrefsHelper(context);
                    String token = prefsHelper.getPref("token");
                    dialog.onCreateDialog(context, id, token).show();
                    break;

                case R.id.btnAddToGooCal :
                    exportToGoogleCalendar(context, id);
                    break;

                case R.id.layEmpty :
                    if (itemRealm.getId() == 0) {
                        // Создать новый сеанс
                        intent = new Intent(context, NewSessionActivity.class);
                        intent.putExtra("sessionDate", itemRealm.getSessionDate().getTime());
                        intent.putExtra("sessionMinuteStart", itemRealm.getStartTime());
                        intent.putExtra("sessionMinuteStop", itemRealm.getStopTime());
                        intent.putExtra("clientId", itemRealm.getClientId());
                        context.startActivity(intent);
                    }

                    else if (itemRealm.getClientId() == 0) {
                        // Изменить перерыв
                        intent = new Intent(context, TimeoutActivity.class);
                        intent.putExtra("editTimeout", itemRealm.getId());
                        intent.putExtra("sessionDate", itemRealm.getSessionDate().getTime());
                        intent.putExtra("sessionMinuteStart", itemRealm.getStartTime());
                        intent.putExtra("sessionMinuteStop", itemRealm.getStopTime());
                        context.startActivity(intent);

                    } else {
                        intent = new Intent(context, EditSessionActivity.class);
                        intent.putExtra("sessionId", itemRealm.getId());
                        context.startActivity(intent);
                    }
                    break;
            }

        }

        @Override
        public boolean onLongClick(View v) {
            SessionItemRealm itemRealm = SessionListAdapter.this.getItem(getAdapterPosition());
            if (itemRealm.getId() == 0) {
                // Перерыв
                Intent intent = new Intent(v.getContext(), TimeoutActivity.class);
                intent.putExtra("newTimeout", 0);
                intent.putExtra("sessionDate", itemRealm.getSessionDate().getTime());
                intent.putExtra("sessionMinuteStart", itemRealm.getStartTime());
                intent.putExtra("sessionMinuteStop", itemRealm.getStopTime());
                v.getContext().startActivity(intent);

            } else if (itemRealm.getClientId() == 0) {
                ConfirmDelTimeoutDialog dialog = new ConfirmDelTimeoutDialog();
                dialog.onCreateDialog(v.getContext(), itemRealm.getId()).show();
            }
            return true;
        }

        private void exportToGoogleCalendar(Context context, int sessionId) {
            LongSparseArray<String> calendars = new Calendars(context).getCalendars();
            if (calendars.size() == 0) {
                Toast.makeText(context, "Для экспорта сеанса в Google Calendar войдите "
                        + "в учетную запись Google", Toast.LENGTH_SHORT).show();

            } else if (calendars.size() == 1) {
                calID = calendars.keyAt(0);
                doExport(context, sessionId);

            } else {
                // Выбор календаря
                Dialog dialog = onCreateDialog(context, calendars, sessionId);
                dialog.show();
            }
        }

        Dialog onCreateDialog(final Context context, final LongSparseArray<String> calendars,
                              final int sessionId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            CharSequence[] titles = new CharSequence[calendars.size()];
            for (int i = 0; i < titles.length; i++)
                titles[i] = calendars.valueAt(i);
            builder.setTitle("Выберите календарь")
                    .setItems(titles, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            calID = calendars.keyAt(which);
                            doExport(context, sessionId);
                        }
                    });

            return builder.create();
        }

        private void doExport(Context context, int sessionId) {
            SessionController controller = new SessionController();
            SessionItemRealm itemRealm = controller.getSession(sessionId).blockingGet();
            long eventId = new SessionExport(context).createEvent(itemRealm, calID);
            Toast.makeText(context, "Сеанс скопирован в Google Calendar",
                    Toast.LENGTH_SHORT).show();
            // Для удаления события из календаря Google нужно сохранять id в базе.
        }
    }

    class ViewHolderPastSession extends ViewHolderSession {
        public LinearLayout layText;
        public TextView tvName;
        public TextView tvSession;
        public Button btnRepeatSession;
        public ImageView ivPhone;

        ViewHolderPastSession(View itemView) {
            super(itemView);

            layText = itemView.findViewById(R.id.layText);
            tvName = itemView.findViewById(R.id.tvName);
            tvSession = itemView.findViewById(R.id.tvSession);

            layText.setOnClickListener(this);
            btnRepeatSession = itemView.findViewById(R.id.btnRepeatSession);
            btnRepeatSession.setOnClickListener(this);

            ivPhone = itemView.findViewById(R.id.ivPhone);
        }
    }

    class ViewHolderFutureSession extends ViewHolderSession {
        public LinearLayout layText;
        public TextView tvName;
        public TextView tvSession;
        Button btnEditSession;
        Button btnCancelSession;
        public Button btnAddToGooCal;
        public ImageView ivPhone;

        ViewHolderFutureSession(View itemView) {
            super(itemView);

            layText = itemView.findViewById(R.id.layText);
            tvName = itemView.findViewById(R.id.tvName);
            tvSession = itemView.findViewById(R.id.tvSession);

            btnEditSession = itemView.findViewById(R.id.btnEdit);
            btnCancelSession = itemView.findViewById(R.id.btnCancel);
            btnAddToGooCal =  itemView.findViewById(R.id.btnAddToGooCal);

            layText.setOnClickListener(this);
            btnEditSession.setOnClickListener(this);
            btnCancelSession.setOnClickListener(this);
            btnAddToGooCal.setOnClickListener(this);

            ivLabel.setImageResource(R.mipmap.label_future);

            ivPhone = itemView.findViewById(R.id.ivPhone);
        }
    }
}
