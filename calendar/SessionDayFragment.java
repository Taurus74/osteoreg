package com.aconst.spinareg.calendar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.SessionHelper;
import com.aconst.spinareg.adapters.ClickListener;
import com.aconst.spinareg.adapters.SessionListAdapter;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.EventItem;
import com.aconst.spinareg.model.Vacation;
import com.aconst.spinareg.sessions.EditSessionActivity;
import com.aconst.spinareg.sessions.NewSession2Activity;
import com.aconst.spinareg.sessions.NewSessionActivity;
import com.aconst.spinareg.sessions.TimeoutActivity;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SessionDayFragment extends Fragment implements View.OnLongClickListener {
    private static final String ARG_PARAM1 = "time";

    public long time;
    private Date date;
    private PrefsHelper prefsHelper;

    List<SessionItemRealm> sessionItemRealms;
    private RecyclerView recyclerView;
    private OnFragmentInteractionListener mListener;

    public SessionDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsHelper = new PrefsHelper(getContext());
        if (getArguments() != null) {
            time = getArguments().getLong(ARG_PARAM1);
        } else {
            time = prefsHelper.getPrefLong("dayTime", 0);
            if (time == 0)
                time = Calendar.getInstance().getTimeInMillis();
        }
        date = CalendarHelper.getDate(time);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        long timeFromMonth = prefsHelper.getPrefLong("monthClickedDate", 0L);
        if (timeFromMonth > 0) {
            prefsHelper.setPrefLong("monthClickedDate", 0L);
            time = timeFromMonth;
            prefsHelper.setPrefLong("dayTime", time);
            date = CalendarHelper.getDate(time);
        }

        View view = inflater.inflate(R.layout.fragment_events_day, container, false);
        TextView tvPeriod = view.findViewById(R.id.tvPeriod);
        tvPeriod.setText(CalendarHelper.dateToString(time, "EEEE, d MMMM"));
        tvPeriod.setTag(time);
        tvPeriod.setOnLongClickListener(this);

        view.findViewById(R.id.ibLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = CalendarHelper.addDay(time, -1);
                prefsHelper.setPrefLong("dayTime", time);
                date = CalendarHelper.getDate(time);
                updateFragment();
            }
        });

        view.findViewById(R.id.ibRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = CalendarHelper.addDay(time, 1);
                prefsHelper.setPrefLong("dayTime", time);
                date = CalendarHelper.getDate(time);
                updateFragment();
            }
        });

        SessionItemRealm itemRealm = isVacation(date);
        if (itemRealm != null) {
            itemRealm.setDuration(24 * 60);
            sessionItemRealms = new LinkedList<>();
            sessionItemRealms.add(itemRealm);
        }
        else
            sessionItemRealms = new SessionController().getDaySessions(date).blockingGet();
        // ToDo -1
//        int firstSessionTime = 0;
//        if (CalendarHelper.isToday(date)) {
//            firstSessionTime = CalendarHelper.getMinute(new Date());
//        }
//        else if (sessionItemRealms.size() > 0)
//            firstSessionTime = sessionItemRealms.get(0).getStartTime();

        SessionListAdapter adapter = new SessionListAdapter(addEmptyPeriods(sessionItemRealms, date),
                SessionListAdapter.DATE_DAY);

        recyclerView = view.findViewById(R.id.rvEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getContext(), DividerItemDecoration.VERTICAL));
        // ToDo -2
//        int scrollTo = getPosition(firstSessionTime);
//        recyclerView.smoothScrollToPosition(scrollTo);
//
//        String time = "";
//        if (firstSessionTime > 0)
//            time = ", time = " + CalendarHelper.getMinute(firstSessionTime);
//        Toast.makeText(getContext(), "Scroll to position: " + scrollTo + time, Toast.LENGTH_SHORT).show();

//        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            private static final int MAX_CLICK_DURATION = 500;
//            private long startClickTime;
//
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN :
//                        startClickTime = Calendar.getInstance().getTimeInMillis();
//                        break;
//                    case MotionEvent.ACTION_UP :
//                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
//                        int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
//                        SessionListAdapter sessionListAdapter = (SessionListAdapter) rv.getAdapter();
//                        SessionItemRealm sessionItemRealm = sessionListAdapter.getItem(position);
//
//                        if (clickDuration < MAX_CLICK_DURATION) {
//                            if (sessionItemRealm.getId() == 0) {
//                                // ToDo
//                                int clientId = 2;   // Тестовый клиент
//
//                                // Создать новый сеанс
//                                Intent intent = new Intent(getContext(), NewSessionActivity.class);
//                                intent.putExtra("sessionDate", sessionItemRealm.getSessionDate().getTime());
//                                intent.putExtra("sessionMinuteStart", sessionItemRealm.getStartTime());
//                                intent.putExtra("sessionMinuteStop", sessionItemRealm.getStopTime());
//                                intent.putExtra("clientId", clientId);
//                                startActivity(intent);
//                            }
//
//                            else if (sessionItemRealm.getService().equals("0")) {
//
//                            } else {
//                                Intent intent = new Intent(getContext(), EditSessionActivity.class);
//                                intent.putExtra("sessionId", sessionItemRealm.getId());
//                                startActivity(intent);
//                            }
//
//                        } else if (clickDuration > MAX_CLICK_DURATION * 2) {
//                            // Создать перерыв
//                            Intent intent = new Intent(getContext(), TimeoutActivity.class);
//                            intent.putExtra("newTimeout", 0);
//                            intent.putExtra("sessionDate", date.getTime());
//                            intent.putExtra("sessionMinuteStart", sessionItemRealm.getStartTime());
//                            intent.putExtra("sessionMinuteStop", sessionItemRealm.getStopTime());
//                            startActivity(intent);
//                        }
//                        break;
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(RecyclerView rv, MotionEvent e) { }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
//        });

        return view;
    }

    private SessionItemRealm isVacation(Date date) {
        List<Vacation> vacations = new SessionController().getVacations().blockingGet();
        for (Vacation vacation : vacations)
            if ((vacation.getDateFrom().before(date) || vacation.getDateFrom().equals(date))
                    && (vacation.getDateTo().after(date) || vacation.getDateTo().equals(date)))
                return new SessionController().getSession(vacation.getSessionId()).blockingGet();
        return null;
    }

    private int getPosition(int time) {
        if (time == 0)
            return 0;

        int dayBegin = prefsHelper.getPref("dayBegin", 8 * 60);
        int interval = prefsHelper.getPref("interval", 15);
        return (time - dayBegin) / interval;
    }

    private List<SessionItemRealm> addEmptyPeriods(List<SessionItemRealm> sessionItemRealms, Date date) {
        // ToDo
        SessionHelper sessionHelper = new SessionHelper(getContext());
        List<EventItem> eventItemList = sessionHelper.getEventItemList(date);
        for (EventItem item : eventItemList) {
            boolean condition = true;
            for (SessionItemRealm itemRealm : sessionItemRealms) {
                if (itemRealm.getStartTime() <= item.getStartTime()
                        && itemRealm.getStopTime() >= item.getStopTime()) {
                    condition = false;
                    break;
                }

                else if (itemRealm.getStopTime() > item.getStartTime()
                        && itemRealm.getStopTime() <= item.getStopTime()) {
                    item.setStartTime(itemRealm.getStopTime());
                }
                else if (itemRealm.getStartTime() >= item.getStartTime()
                        && itemRealm.getStartTime() < item.getStopTime()) {
                    item.setStopTime(itemRealm.getStartTime());
                }
            }

            if (condition) {
                SessionItemRealm sessionItemRealm = new SessionItemRealm();
                sessionItemRealm.setSessionDate(date);
                sessionItemRealm.setSessionTime(item.getStartTime());
                sessionItemRealm.setDuration(item.getStopTime() - item.getStartTime());
                sessionItemRealms.add(sessionItemRealm);
            }
        }

        Collections.sort(sessionItemRealms, new Comparator<SessionItemRealm>() {
            @Override
            public int compare(SessionItemRealm o1, SessionItemRealm o2) {
                return o1.getStartTime() - o2.getStartTime();
            }
        });

        return sessionItemRealms;
    }

    private void updateFragment() {
        SessionListAdapter adapter = (SessionListAdapter) recyclerView.getAdapter();

        SessionItemRealm itemRealm = isVacation(date);
        if (itemRealm != null) {
            sessionItemRealms.clear();
            sessionItemRealms.add(itemRealm);
        }
        else
            sessionItemRealms = new SessionController().getDaySessions(date).blockingGet();
        adapter.setSessionItemRealms(sessionItemRealms);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(SessionDayFragment.this).attach(SessionDayFragment.this).commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onLongClick(View v) {
        FreeWorkTimeDialog dialog = new FreeWorkTimeDialog(getContext());
        dialog.onCreate(date).show();
        return true;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
