package com.aconst.spinareg.options;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ExclusionAdapter;
import com.aconst.spinareg.adapters.WeekdaysAdapter;
import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.OptionsWeekDay;
import com.aconst.spinareg.model.SessionItemRealm;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class FreetimeActivity extends AppCompatActivity implements View.OnClickListener {
    private Session session = new Session(this);
    private RecyclerView rvExclusions;
    private Date date = null;
    private int timeStart = -1;
    private int timeStop = -1;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ExclusionAdapter adapter = (ExclusionAdapter) rvExclusions.getAdapter();
            adapter.setExclusions(getExclusions());
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freetime);
        setTitle(R.string.option_btn_free_time);

        List<OptionsWeekDay> optionsWeekDays = getOptionsWeekDays();

        RecyclerView rvWeekDays = findViewById(R.id.rvWeekDays);
        WeekdaysAdapter weekdaysAdapter = new WeekdaysAdapter(optionsWeekDays);
        rvWeekDays.setAdapter(weekdaysAdapter);
        rvWeekDays.setLayoutManager(new LinearLayoutManager(this));

        List<SessionItemRealm> exclusions = getExclusions();

        rvExclusions = findViewById(R.id.rvExclusions);
        ExclusionAdapter exclusionAdapter = new ExclusionAdapter(exclusions);
        rvExclusions.setAdapter(exclusionAdapter);
        rvExclusions.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.tvAddDate).setOnClickListener(this);
        findViewById(R.id.tvAddTimeStart).setOnClickListener(this);
        findViewById(R.id.tvAddTimeStop).setOnClickListener(this);
        findViewById(R.id.tvAddExclusion).setOnClickListener(this);

        registerReceiver(receiver, new IntentFilter("Update_session_UI"));
    }

    private String[] getWeekdayNames() {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] buffer = dfs.getWeekdays();
        String[] result = new String[7];
        System.arraycopy(buffer, 2, result, 0, 6);
        result[6] = buffer[1];
        return result;
    }

    private List<OptionsWeekDay> getOptionsWeekDays() {
        List<OptionsWeekDay> optionsWeekDays = new LinkedList<>();
        PrefsHelper prefsHelper = new PrefsHelper(this);
        String options = prefsHelper.getPref("optionsWeekDays");
        if (options.isEmpty()) {
            String[] weekdayNames = getWeekdayNames();
            for (int i = 0; i < weekdayNames.length; i++)
                optionsWeekDays.add(new OptionsWeekDay(
                        weekdayNames[i], false, -1, -1));
        }

        else {
            try {
                JSONObject jsonObject = new JSONObject().getJSONObject(options);
                optionsWeekDays = (List<OptionsWeekDay>) jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return optionsWeekDays;
    }

    private List<SessionItemRealm> getExclusions() {
        return new SessionController().getFreeSessions().blockingGet();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddDate :
                getDateDialog(date).show();
                break;

            case R.id.tvAddTimeStart :
                openTimeDialog(v, timeStart);
                break;

            case R.id.tvAddTimeStop :
                openTimeDialog(v, timeStop);
                break;

            case R.id.tvAddExclusion :
                if (date == null || timeStart == -1 || timeStop == -1) {
                    String message = "";
                    if (date == null)
                        message = "дату";
                    if (timeStart == -1)
                        message += (message.isEmpty()? "": ", ") + "время начала";
                    if (timeStop == -1)
                        message += (message.isEmpty()? "": ", ") + "время окончания";

                    message = "Для добавления исключения заполните " + message;
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                else {
                    PrefsHelper prefsHelper = new PrefsHelper(this);
                    session.newSession(prefsHelper.getPref("token"), 0, 1,
                            prefsHelper.getPref("specId", 0), "0",
                            CalendarHelper.dateToString(date, "yyyy-MM-dd"),
                            getTime(timeStart), timeStop - timeStart,
                            "Исключение", 0, null);
                }

                break;
        }
    }

    private Dialog getDateDialog(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            long time = date.getTime();
            if (time > 0) {
                cal.setTimeInMillis(time);
            }
        }
        return new DatePickerDialog(FreetimeActivity.this, dateSetListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            date = CalendarHelper.getDate(CalendarHelper.getDate(year, month, dayOfMonth));
            ((TextView) findViewById(R.id.tvAddDate)).setText(CalendarHelper.dateToString(
                    date, Common.DATE_FORMAT_SHORT));
        }
    };

    private String getTime(int minute) {
        int hour = minute / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute - hour * 60);
    }

    private void openTimeDialog(final View v, int minute) {
        int hour = 0;
        int min = 0;
        if (minute > -1) {
            hour = minute / 60;
            min = minute - hour * 60;
        }
        new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (v.getId() == R.id.tvAddTimeStart) {
                    timeStart = hourOfDay * 60 + minute;
                    ((TextView) v).setText(CalendarHelper.getMinute(timeStart));
                }
                else {
                    if (hourOfDay == 0 && minute == 0) {
                        hourOfDay = 24;
                    }
                    timeStop = hourOfDay * 60 + minute;
                    ((TextView) v).setText(CalendarHelper.getMinute(timeStop));
                }
            }
        }, hour, min, true).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.dispose();
    }
}
