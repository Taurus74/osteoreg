package com.aconst.spinareg.calendar;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ClickListener;
import com.aconst.spinareg.adapters.SessionListAdapter;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.controllers.SessionController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SessionListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public SessionListFragment() {
        // Required empty public constructor
    }

    public static SessionListFragment newInstance(String param1, String param2) {
        SessionListFragment fragment = new SessionListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        // Сеансы за период
        Date dateStart = CalendarHelper.getDayStart(new Date());
        Date dateStop = CalendarHelper.getDayEnd(
                CalendarHelper.getDate(CalendarHelper.addDay(dateStart.getTime(), 7)));
        SessionController controller = new SessionController();
        List<SessionItemRealm> sessionItemRealms = controller
                .getPeriodSessions(dateStart, dateStop, SessionController.SESSION_WORKTIME)
                .blockingGet();

        RecyclerView recyclerView = view.findViewById(R.id.rvEventsList);
        if (sessionItemRealms.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        }
        else {
            SessionListAdapter adapter = new SessionListAdapter(
                    sessionItemRealms, SessionListAdapter.DATE_LIST);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(
                    getContext(), DividerItemDecoration.VERTICAL));

//            recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//                private static final int MAX_CLICK_DURATION = 500;
//                private long startClickTime;
//
//                @Override
//                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                    switch (e.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            startClickTime = Calendar.getInstance().getTimeInMillis();
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
//                            if (clickDuration < MAX_CLICK_DURATION) {
//                                int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
//                                SessionListAdapter sessionListAdapter = (SessionListAdapter) rv.getAdapter();
//                                SessionItemRealm sessionItemRealm = sessionListAdapter.getItem(position);
//                            if (sessionItemRealm != null)
//                                Toast.makeText(getContext(), "Click on time: ["
//                                        + String.format(Locale.getDefault(), "%d:%02d",
//                                        sessionItemRealm.getStartTime(),
//                                        sessionItemRealm.getStopTime())
//                                        + "]", Toast.LENGTH_SHORT).show();
//                            }
//                            break;
//                    }
//                    return false;
//                }
//
//                @Override
//                public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//                }
//
//                @Override
//                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//                }
//            });
        }

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
