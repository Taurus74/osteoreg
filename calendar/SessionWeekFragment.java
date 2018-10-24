package com.aconst.spinareg.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.SessionHelper;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.EmptyPeriod;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.model.Vacation;
import com.aconst.spinareg.model.WeekDay;
import com.aconst.spinareg.options.VacationsActivity;
import com.aconst.spinareg.sessions.EditSessionActivity;
import com.aconst.spinareg.sessions.NewSessionActivity;
import com.aconst.spinareg.sessions.TimeoutActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SessionWeekFragment extends Fragment
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int INTERVAL = 15;

    private PrefsHelper prefsHelper;
    private long time;
    private int mYear;
    private int mMonth;
    private int mDay;
    List<SessionItemRealm> sessionItemRealms;
    private List<Vacation> vacations;
    private ArrayList<WeekDay> weekDays;
    private ArrayList<Integer> periods;
    SparseArray<List<SessionItemRealm>> sessionItemByDay;

    private OnFragmentInteractionListener mListener;
//    private int cellWidth = 0, baseCellWidth = 0;
//    private int cellHeight = 0;
//
//    private int[] cellsWidth = new int[8];

    public SessionWeekFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsHelper = new PrefsHelper(getContext());
        if (getArguments() != null) {
            time = getArguments().getLong(ARG_PARAM1);
        } else {
            time = prefsHelper.getPrefLong("weekTime", 0);
            if (time == 0)
                time = Calendar.getInstance().getTimeInMillis();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vacations = new SessionController().getVacations().blockingGet();

        // Инициализация данных
        // - дни недели
        weekDays = CalendarHelper.getWeekDays(mYear, mMonth, mDay);
        // - интервалы
        SessionHelper sessionHelper = new SessionHelper(getContext());
        periods = sessionHelper.getIntervals();

        // - сеансы за неделю
        sessionItemRealms = new SessionController().getPeriodSessions(
                CalendarHelper.weekStart(mYear, mMonth, mDay),
                CalendarHelper.weekEnd(mYear, mMonth, mDay),
                SessionController.SESSION_WO_VACATIONS).blockingGet();

        sessionItemByDay = getSessions(sessionItemRealms);

        View view = inflater.inflate(R.layout.fragment_events_week, container, false);
        view = processTableLayout(view);

//        /*if (baseCellWidth == 0)*/ {
//            // Расчет базового отступа
//            // ToDo
//            TableLayout tableLayout = view.findViewById(R.id.tlWeek);
//            TableRow row = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
//
//            TextView cell = (TextView) row.getChildAt(0);
//            cell.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//
//            // ToDo
////            baseCellWidth = cell.getMeasuredWidth() + 25;
////            cellHeight = cell.getMeasuredHeight();
////
////            cellsWidth[0] = cell.getMeasuredWidth();
//
//            row = (TableRow) tableLayout.getChildAt(0);
//            // ToDo
////            cell = (TextView) row.getChildAt(1);
////            cell.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
////            cellWidth = (int) (cell.getMeasuredWidth() * 1.41);
//
//            int screenWidth = prefsHelper.getPref("screenWidth", 0);
//
////            int width = 0;
////            for (int i = 1; i < 8; i++) {
////                cell = (TextView) row.getChildAt(i);
////                cell.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
////                cellsWidth[i] = cell.getMeasuredWidth();
////                width += cellsWidth[i];
////            }
////            width += cellsWidth[0];
////            float coeff = screenWidth / (float) width;
////            for (int i = 0; i < 8; i++)
////                cellsWidth[i] = (int) (cellsWidth[i] * coeff);
//        }

        return view;
    }

    private View processTableLayout(View view) {
//        // Инициализация данных
//        // - дни недели
//        ArrayList<WeekDay> weekDays = CalendarHelper.getWeekDays(mYear, mMonth, mDay);
//        // - интервалы
//        SessionHelper sessionHelper = new SessionHelper(getContext());
//        ArrayList<Integer> periods = sessionHelper.getIntervals();
//
//        // - сеансы за неделю
//        sessionItemRealms = new SessionController().getPeriodSessions(
//                CalendarHelper.weekStart(mYear, mMonth, mDay),
//                CalendarHelper.weekEnd(mYear, mMonth, mDay)
//        ).blockingGet();
//
//        SparseArray<List<SessionItemRealm>> sessionItemByDay = getSessions(sessionItemRealms);

        // Основные настройки
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        // Заполнение данными
        RelativeLayout rlWeek = view.findViewById(R.id.rlWeek);
        TableLayout tableLayout = view.findViewById(R.id.tlWeek);
        tableLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        tableLayout.setStretchAllColumns(true);
        tableLayout.setShrinkAllColumns(true);

        // Строка заголовка
        TableRow tableRow = getHeader(getContext(), weekDays, tableParams, rowParams);
        tableLayout.addView(tableRow);

        Drawable drawable = getResources().getDrawable(R.drawable.ic_hourglass);
        int cellHeight = drawable.getMinimumHeight();

        tableRow.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        cellHeight = tableRow.getMeasuredHeight();

        // События по интервалам
        for (int begin : periods) {
            TableRow tableRowPeriod = new TableRow(getContext());
            tableRow.setLayoutParams(tableParams);

            TextView tvPeriod = getPeriod(getContext(), begin, rowParams);
            tvPeriod.setHeight(cellHeight);
            tableRowPeriod.addView(tvPeriod);
//            if (cellWidth == 0 ) {
//                tvPeriod.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                cellWidth = tvPeriod.getMeasuredWidth();
//                cellHeight = v.getMeasuredHeight();
//            }

//            // Перебор событий
            float scale = 1f;
            List<SessionItemRealm> list = sessionItemByDay.get(begin);
            // Подготовка строки
            SparseArray<View> weekDaysRow = new SparseArray<>();
            for (int i = 0; i < 7; i++) {
                Date date = CalendarHelper.getDate(CalendarHelper.getDate(
                        weekDays.get(i).getYear(),
                        weekDays.get(i).getMonth(),
                        weekDays.get(i).getDay()));

                // Проверка на отпуск
                SessionItemRealm item = isVacation(date);
                if (item == null) {
                    EmptyPeriod emptyPeriod = new EmptyPeriod(date, begin, begin + INTERVAL);
                    View v = getEmpty(getContext(), emptyPeriod, rowParams);
                    v.setOnLongClickListener(this);
                    weekDaysRow.put(i, v);
                }
                else
                    weekDaysRow.put(i, getEvent(getContext(), item, rowParams, scale));

//                if (cellHeight == 0 ) {
//                    v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                    cellWidth = v.getMeasuredWidth();
//                    cellHeight = v.getMeasuredHeight();
//                }
            }

            if (list != null) {
                // Заполнение событиями
                for (SessionItemRealm item: list) {
//                    int stopTime = item.getStartTime() + item.getDuration();
//                    if (stopTime > begin && stopTime < begin + 15)
//                        scale = (stopTime - begin) / 15f;
                    weekDaysRow.put(CalendarHelper.getWeekDay(item.getSessionDate()),
                            getEvent(getContext(), item, rowParams, scale));
                }
            }

            // Вывод сетки
            for (int i = 0; i < 7; i++)
                tableRowPeriod.addView(weekDaysRow.get(i));
            tableLayout.addView(tableRowPeriod);

//            // Заполнение событиями
//            // ToDo
//            for (SessionItemRealm session : sessionItemRealms) {
//                rlWeek.addView(drawEvent(CalendarHelper.getWeekDay(session.getSessionDate()),
//                        (session.getStartTime() - 480) / 15,
//                        session.getDuration() / 15f));
//            }
        }

        return view;
    }

//    private ImageView drawEvent(int x, int y, float scale) {
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                /*cellWidth*/cellsWidth[x + 1] - 1, (int) (cellHeight * scale));
//        int left = 0;
//        for (int i = 0; i <= x; i++)
//            left += cellsWidth[i];
//        params.setMargins(/*x * (cellWidth + 1) + baseCellWidth*/left,
//                (y + 1) * (cellHeight + 1), // добавляется толщина рамки
//                0 ,0);
//        ImageView overlappingImage = new ImageView(getContext());
//        overlappingImage.setScaleType(ImageView.ScaleType.FIT_XY);
//        overlappingImage.setLayoutParams(params);
//        overlappingImage.setImageResource(R.drawable.splash_bg);
//        return overlappingImage;
//    }

    private SessionItemRealm isVacation(Date date) {
        for (Vacation vacation : vacations)
            if ((vacation.getDateFrom().before(date) || vacation.getDateFrom().equals(date))
                    && (vacation.getDateTo().after(date) || vacation.getDateTo().equals(date)))
                return new SessionController().getSession(vacation.getSessionId()).blockingGet();
        return null;
    }

    private TableRow getHeader(Context context, ArrayList<WeekDay> weekDays,
                               TableLayout.LayoutParams tableParams,
                               TableRow.LayoutParams rowParams) {
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(tableParams);
        tableRow.setBackgroundColor(getResources().getColor(R.color.colorWeekHeaderBg));

        // Угловой элемент
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.ic_hourglass);
        rowParams.width = 120;
        imageView.setLayoutParams(rowParams);
        imageView.setBackgroundColor(getResources().getColor(R.color.colorCellBgFill));

        tableRow.addView(imageView);

        int colWidth = (prefsHelper.getPref("screenWidth", 0) - 120) / 7 - 1;

        // Дни недели
        for (WeekDay weekDay : weekDays) {
            TextView tvDate = new TextView(getContext());
            tvDate.setGravity(Gravity.CENTER);
            rowParams.setMargins(1, 1,0, 0);
            rowParams.width = colWidth;
            tvDate.setLayoutParams(rowParams);
            tvDate.setTextSize(9);
            long mDate = CalendarHelper.getDate(
                    weekDay.getYear(), weekDay.getMonth(), weekDay.getDay());
            String date = getResources().getString(
                    getResources().getIdentifier(
                            "tvDay" + weekDay.getWeekDay(),
                            "string", getActivity().getPackageName()))
                    + "\n" + CalendarHelper.dateToString(mDate, "d MMMM");
            if (CalendarHelper.isToday(weekDay.getYear(), weekDay.getMonth(), weekDay.getDay())) {
                tvDate.setTextColor(getResources().getColor(R.color.colorDark));
                SpannableString spanString = new SpannableString(date);
                spanString.setSpan(
                        new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                tvDate.setText(spanString);

            } else {
                tvDate.setTextColor(getResources().getColor(R.color.colorCalendarNavigatorText));
                tvDate.setText(date);
            }
            tvDate.setTag(CalendarHelper.getDate(mDate));
            tvDate.setBackgroundColor(getResources().getColor(R.color.colorCalendarNavigator));
            tvDate.setOnLongClickListener(this);
            tableRow.addView(tvDate);
        }
        tableRow.getChildAt(1).setOnClickListener(this);
        tableRow.getChildAt(1).setBackground(getResources().getDrawable(R.drawable.week_header_bg_left));
        tableRow.getChildAt(7).setOnClickListener(this);
        tableRow.getChildAt(7).setBackground(getResources().getDrawable(R.drawable.week_header_bg_right));

        return tableRow;
    }

    private TextView getPeriod(Context context, int begin, TableRow.LayoutParams rowParams) {
        SessionHelper sessionHelper = new SessionHelper(getContext());
        TextView period = new TextView(context);
        period.setGravity(Gravity.CENTER);
        period.setLayoutParams(rowParams);
        period.setTextSize(9);
        period.setText(CalendarHelper.getMinutes(begin, begin + sessionHelper.getInterval()));
//                getPeriod(begin, begin + sessionHelper.getInterval()));
        period.setTextColor(getResources().getColor(R.color.colorCalendarTextEvent));
        period.setBackgroundColor(getResources().getColor(R.color.colorCellBgFill));
        period.setPadding(5, 5, 5, 5);

        return period;
    }

    private String getPeriod(int begin, int end) {
        int beginHour = begin / 60;
        int beginMin = begin - beginHour * 60;
        int endHour = end / 60;
        int endMin = end - endHour * 60;
        return String.format(Locale.getDefault(), "%d:%02d - %d:%02d",
                beginHour, beginMin, endHour, endMin);
    }

    private View getEvent(Context context, SessionItemRealm item, TableRow.LayoutParams rowParams,
                          float scale) {
        if (item.getClientId() == 0) {
            // Перерыв
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(rowParams);
            imageView.setBackgroundColor(getResources().getColor(R.color.colorFreetime));
            imageView.setOnClickListener(this);
            imageView.setTag(item);

            return imageView;

        } else if (CalendarHelper.dateInPast(item.getSessionDate(), item.getStopTime())) {
            // Прошедший сеанс
            rowParams.setMargins(1, 1, 1, 1);
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(rowParams);
            if (scale == 1f) {
                imageView.setImageResource(R.drawable.ic_check);
            } else {
                imageView.setScaleY(scale);
            }
            imageView.setBackgroundColor(getResources().getColor(R.color.colorWeekPast));
            imageView.setOnClickListener(this);
            imageView.setTag(item);

            return imageView;

        } else if (CalendarHelper.dateInPast(item.getSessionDate(), item.getStartTime())) {
            // Текущий сеанс
            TextView tvEvent = new TextView(context);
            tvEvent.setGravity(Gravity.CENTER);
            rowParams.setMargins(1, 1, 0, 0);
            tvEvent.setLayoutParams(rowParams);
            tvEvent.setTextSize(9);
            tvEvent.setText("Сеанс");
            tvEvent.setBackgroundColor(getResources().getColor(R.color.colorWeekNow));
            tvEvent.setTextColor(getResources().getColor(R.color.colorCalendarTextEvent));
            tvEvent.setPadding(5, 5, 5, 5);
            tvEvent.setOnClickListener(this);
            tvEvent.setTag(item);

            return tvEvent;

        } else {
            // Будущий сеанс
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.ic_round_arrow);
            imageView.setLayoutParams(rowParams);
            imageView.setBackgroundColor(getResources().getColor(R.color.colorWeekFuture));
            imageView.setOnClickListener(this);
            imageView.setTag(item);

            return imageView;
        }

    }

    private TextView getEmpty(Context context, EmptyPeriod emptyPeriod, TableRow.LayoutParams rowParams) {
        TextView empty = new TextView(context);
        empty.setGravity(Gravity.CENTER);
        empty.setLayoutParams(rowParams);
        empty.setTextSize(9);
        empty.setText(" ");
        empty.setBackgroundColor(getResources().getColor(R.color.colorCalendarTextEvent));
        empty.setOnClickListener(this);
        empty.setTag(emptyPeriod);

        return empty;
    }

    private SparseArray<List<SessionItemRealm>> getSessions(List<SessionItemRealm> sessionItemRealms) {
        SparseArray<List<SessionItemRealm>> result = new SparseArray<>();

        for (SessionItemRealm item : sessionItemRealms) {
            // Приводим время начала к стандартному времени начала (кратно 15 мин.)
            int startMin = item.getStartTime() / 15 * 15;
            while (startMin < item.getStartTime() + item.getDuration()) {
                List<SessionItemRealm> list = result.get(startMin);
                if (list == null)
                    list = new LinkedList<>();
                list.add(item);
                result.put(startMin, list);
                startMin += 15;
            }
        }

        return result;
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
        if (v.getTag() instanceof SessionItemRealm) {
            SessionItemRealm itemRealm = (SessionItemRealm) v.getTag();
            if (itemRealm != null) {
                Intent intent;
                if (itemRealm.getClientId() == 0) {
                    if (itemRealm.getDuration() < 24 * 60) {
                        // Изменить перерыв
                        intent = new Intent(getContext(), TimeoutActivity.class);
                        intent.putExtra("editTimeout", itemRealm.getId());
                        intent.putExtra("sessionDate", itemRealm.getSessionDate().getTime());
                        intent.putExtra("sessionMinuteStart", itemRealm.getStartTime());
                        intent.putExtra("sessionMinuteStop", itemRealm.getStopTime());
                    }
                    else {
                        // Изменить отпуск
                        intent = new Intent(getContext(), VacationsActivity.class);
                        intent.putExtra("sessionId", itemRealm.getId());
                    }

                } else {
                    intent = new Intent(getContext(), EditSessionActivity.class);
                    intent.putExtra("sessionId", itemRealm.getId());
                }
                startActivity(intent);
            }

        } else if (v.getTag() instanceof EmptyPeriod) {
            EmptyPeriod tag = (EmptyPeriod) v.getTag();

            Intent intent = new Intent(getContext(), NewSessionActivity.class);
            intent.putExtra("sessionDate", tag.getDate().getTime());
            intent.putExtra("sessionMinuteStart", tag.getStartTime());
            intent.putExtra("sessionMinuteStop", tag.getStopTime());
            startActivity(intent);

        } else if (v.getTag() instanceof Date) {
            Date date = (Date) v.getTag();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int inc = (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY? -1: 1);
            calendar.setTimeInMillis(time);
            calendar.add(Calendar.WEEK_OF_YEAR, inc);
            time = calendar.getTimeInMillis();
            prefsHelper.setPrefLong("weekTime", time);
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            updateFragment();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() instanceof Date) {
            FreeWorkTimeDialog dialog = new FreeWorkTimeDialog(getContext());
            dialog.onCreate((Date) v.getTag()).show();

        } else if (v.getTag() instanceof EmptyPeriod) {
            // Перерыв
            EmptyPeriod period = (EmptyPeriod) v.getTag();
            Intent intent = new Intent(getContext(), TimeoutActivity.class);
            intent.putExtra("newTimeout", 0);
            intent.putExtra("sessionDate", period.getDate().getTime());
            intent.putExtra("sessionMinuteStart", period.getStartTime());
            intent.putExtra("sessionMinuteStop", period.getStopTime());
            startActivity(intent);
        }
        return false;
    }

    private void updateFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(SessionWeekFragment.this).attach(SessionWeekFragment.this).commit();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
