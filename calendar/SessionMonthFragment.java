package com.aconst.spinareg.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.model.Vacation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SessionMonthFragment extends Fragment
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String ARG_PARAM1 = "time";
    private static final String ARG_PARAM2 = "param2";

    private List<Vacation> vacations;
    private long time;
    private PrefsHelper prefsHelper;

    private OnFragmentInteractionListener mListener;

    private class DayTag {
        private int count;
        private Date date;

        public DayTag(int count, Date date) {
            this.count = count;
            this.date = date;
        }
    }

    public SessionMonthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsHelper = new PrefsHelper(getContext());
        if (getArguments() != null) {
            time = getArguments().getLong(ARG_PARAM1);
        } else {
            time = prefsHelper.getPrefLong("monthTime", 0);
            if (time == 0)
                time = Calendar.getInstance().getTimeInMillis();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vacations = new SessionController().getVacations().blockingGet();

        View view = inflater.inflate(R.layout.fragment_events_month, container, false);
        TextView tvPeriod = view.findViewById(R.id.tvPeriod);
        tvPeriod.setText(CalendarHelper.dateToString(time, "LLLL"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        final int viewedYear = calendar.get(Calendar.YEAR);
        final int viewedMonth = calendar.get(Calendar.MONTH);
        int[] viewedDays = CalendarHelper.getMonthArray(viewedYear, viewedMonth);

        Date date = CalendarHelper.getDate(CalendarHelper.getDate(viewedYear, viewedMonth, 1));
        SessionController controller = new SessionController();
        List<SessionItemRealm> sessionItemRealms = controller.getPeriodSessions(
                CalendarHelper.monthStart(date), CalendarHelper.monthEnd(date),
                SessionController.SESSION_WO_VACATIONS).blockingGet();
        HashMap<Date, Integer> sessionDays = getSessionDays(sessionItemRealms);

        drawCalendar(viewedDays, viewedYear, viewedMonth, view, sessionDays);

        view.findViewById(R.id.ibLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = CalendarHelper.addMonth(time, -1);
                prefsHelper.setPrefLong("monthTime", time);
                updateFragment();
            }
        });

        view.findViewById(R.id.ibRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = CalendarHelper.addMonth(time, 1);
                prefsHelper.setPrefLong("monthTime", time);
                updateFragment();
            }
        });

        return view;
    }

    private void updateFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(SessionMonthFragment.this).attach(SessionMonthFragment.this).commit();
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
    public void onClick(View v) {
        if (v.getTag() != null) {
            Date date = ((DayTag) v.getTag()).date;
            if (date != null) {
                prefsHelper.setPrefLong("monthClickedDate", date.getTime());

                ViewPager viewPager = getActivity().findViewById(R.id.calendar_pager);
                viewPager.setCurrentItem(0);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Date date = ((DayTag) v.getTag()).date;
        FreeWorkTimeDialog dialog = new FreeWorkTimeDialog(getContext());
        dialog.onCreate(date).show();
        return true;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void drawCalendar(int[] viewedDays, int viewedYear, int viewedMonth, View view,
                              HashMap<Date, Integer> sessionDays) {
        int mYear = viewedYear;
        int mMonth = viewedMonth;
        if (--mMonth < 0) {
            mMonth = 11;
            mYear--;
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int resId = getResources().getIdentifier("tv" + row + col, "id",
                                getActivity().getPackageName());
                int i = row * 7 + col;

                if (viewedDays[i] == 1) {
                    mMonth++;
                    if (mMonth > 11) {
                        mMonth = 0;
                        mYear++;
                    }
                }

                TextView tv = view.findViewById(resId);
                String mDay = String.format(Locale.getDefault(), "%d", viewedDays[i]);
                tv.setOnClickListener(this);
                tv.setText(mDay);
                if (viewedYear == mYear && viewedMonth == mMonth) {
                    Date date = CalendarHelper.getDate(
                            CalendarHelper.getDate(mYear, mMonth, viewedDays[i]));

                    Integer count = 0;
                    if (!sessionDays.isEmpty())
                        count = sessionDays.get(date);
                    if (count == null || count == 0) {
                        tv.setTextColor(getResources().getColor(R.color.colorCalendarTextMain));
                        tv.setTag(new DayTag(0, date));
                        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                                getResources().getDrawable(R.drawable.dot_empty));

                    } else {
                        tv.setTextColor(getResources().getColor(R.color.colorCalendarTextMain));
//                        tv.setTextColor(getResources().getColor(R.color.colorCalendarTextEvent));
//                        tv.setBackgroundResource(R.drawable.events_cell_bg);
                        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                                getResources().getDrawable(R.drawable.dot_blue));
                        tv.setTag(new DayTag(count, date));
                    }
                    if (isVacation(date) != null)
                        tv.setBackgroundColor(getResources().getColor(R.color.colorFreetime));

                    if (CalendarHelper.isToday(mYear, mMonth, viewedDays[i])) {
                        tv.setTextColor(getResources().getColor(R.color.colorDark));
                        SpannableString spanString = new SpannableString(mDay);
                        spanString.setSpan(
                                new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                        tv.setText(spanString);
                    }
                    tv.setOnLongClickListener(this);

                } else {
                    tv.setTextColor(getResources().getColor(R.color.colorCalendarTextSecond));
                    tv.setTag(new DayTag(0, null));
                }
            }
        }
    }

    private HashMap<Date, Integer> getSessionDays(List<SessionItemRealm> sessionItemRealms) {
        HashMap<Date, Integer> result = new HashMap<>();
        for (SessionItemRealm session : sessionItemRealms) {
            if (session.getClientId() != 0) {
                Integer count = result.get(session.getSessionDate());
                result.put(session.getSessionDate(), (count == null? 1: ++count));
            }
        }
        return result;
    }

    private SessionItemRealm isVacation(Date date) {
        for (Vacation vacation : vacations)
            if ((vacation.getDateFrom().before(date) || vacation.getDateFrom().equals(date))
                    && (vacation.getDateTo().after(date) || vacation.getDateTo().equals(date)))
                return new SessionController().getSession(vacation.getSessionId()).blockingGet();
        return null;
    }
}
