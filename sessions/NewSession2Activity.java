package com.aconst.spinareg.sessions;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.PhotosAdapter;
import com.aconst.spinareg.adapters.ServicesAdapter;
import com.aconst.spinareg.api.OsteoService;
import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.services.ConfirmDelServiceDialog;
import com.aconst.spinareg.services.GetServicesActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import static com.aconst.spinareg.Common.RC_EDIT_SERVICE;
import static com.aconst.spinareg.Common.RC_GET_PHOTO;
import static com.aconst.spinareg.Common.RC_NEW_SERVICE;
import static com.aconst.spinareg.Common.RC_SELECT_SERVICE;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_BLD;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_CITY;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_FLAT;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_STATION;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_STREET;

public class NewSession2Activity extends AppCompatActivity
        implements View.OnClickListener,
        ConfirmDelFileDialog.ConfirmDelFileDialogListener,
        ConfirmDelServiceDialog.ConfirmDelServiceDialogListener {

    private Session session = new Session(this);
    private OsteoService osteoService = new OsteoService(this);

    private static final int SESSION_MODE_NEW = 0;
    private static final int SESSION_MODE_EDIT = 1;

    private long now = Calendar.getInstance().getTimeInMillis();
    private int sessionMode;

    private List<Date> sessionDate = new LinkedList<>();
    private int sessionMinuteStart;
    private int sessionMinuteStop;
    private int duration;
    private int sessionId = 0;
    private int clientId;
    private int cardId;
    private int specId;
    private boolean repeatedSession = false;
    private List<ServiceItemRealm> serviceItems = new LinkedList<>();
    private List<String> photos = new LinkedList<>();
    private RecyclerView rvServices;
    private RecyclerView rvSessionPhotos;

    private int viewedYear;
    private int viewedMonth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session2);

        final PrefsHelper prefsHelper = new PrefsHelper(this);
        specId = prefsHelper.getPref("specId", 0);
        duration = prefsHelper.getPref("duration", 15);

        Intent intent = getIntent();
        if (intent.hasExtra("sessionId")) {
            sessionMode = SESSION_MODE_EDIT;
            setTitle(R.string.title_edit_session);
            sessionId = intent.getIntExtra("sessionId", 0);

        } else {
            sessionMode = SESSION_MODE_NEW;
            setTitle(R.string.title_new_session);
            final long time = intent.getLongExtra("sessionDate", 0);
            if (!repeatedSession)
                sessionDate.clear();
            sessionDate.add(CalendarHelper.getDate(time));
            sessionMinuteStart = intent.getIntExtra("sessionMinuteStart", 0);
            sessionMinuteStop = intent.getIntExtra("sessionMinuteStop", 0);
        }

        if (sessionMode != SESSION_MODE_NEW) {
            SessionItemRealm sessionItemRealm = new SessionController().getSession(sessionId).blockingGet();
            if (sessionItemRealm != null) {
                if (!repeatedSession)
                    sessionDate.clear();
                sessionDate.add(sessionItemRealm.getSessionDate());
                sessionMinuteStart = sessionItemRealm.getStartTime();
                sessionMinuteStop = sessionItemRealm.getStopTime();
                serviceItems = sessionItemRealm.getServices();
                // ToDo - место проведения сеанса
//                ((TextView) findViewById(R.id.tvSessionPlace)).setText(sessionItemRealm.);
                ((TextView) findViewById(R.id.etSessionComment)).setText(sessionItemRealm.getSpecComment());

            } else
                Toast.makeText(this, "Ошибка получения данных о сеансе", Toast.LENGTH_SHORT).show();
        }

        clientId = intent.getIntExtra("clientId", 2);   // Тестовый клиент
        cardId = intent.getIntExtra("cardId", 1);       // Тестовый id карточки клиента

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sessionDate.get(0));
        viewedYear = calendar.get(Calendar.YEAR);
        viewedMonth = calendar.get(Calendar.MONTH);
        drawMonth(0);

        findViewById(R.id.ibLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawMonth(-1);
            }
        });

        findViewById(R.id.ibRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawMonth(1);
            }
        });

        findViewById(R.id.tvSessionPlace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putCharSequenceArray("places", getPlaces());
                bundle.putInt("viewId", R.id.tvSessionPlace);
                Dialog dialog = onCreateDialog(bundle);
                dialog.show();
            }
        });

        TextView etStartTime = findViewById(R.id.etStartTime);
        etStartTime.setText(CalendarHelper.getMinute(sessionMinuteStart));
        etStartTime.setOnClickListener(this);

        TextView etStopTime = findViewById(R.id.etStopTime);
        etStopTime.setText(CalendarHelper.getMinute(sessionMinuteStop));
        etStopTime.setOnClickListener(this);

        SwitchCompat switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                repeatedSession = isChecked;
                if (!isChecked) {
                    Date minDate = sessionDate.get(0);
                    for (Date date : sessionDate)
                        if (date.compareTo(minDate) < 0)
                            minDate = date;
                    sessionDate.clear();
                    sessionDate.add(minDate);
                    drawMonth(0);
                }
            }
        });

        // Список услуг
        ServiceItemRealm firstItem = new ServiceItemRealm();
        serviceItems.add(0, firstItem);
        ServicesAdapter servicesAdapter = new ServicesAdapter(this, serviceItems,true);

        rvServices = findViewById(R.id.SessionServices);
        rvServices.setAdapter(servicesAdapter);

        rvServices.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private static final int MAX_CLICK_DURATION = 500;
            private long startClickTime;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if(clickDuration < MAX_CLICK_DURATION) {
                            int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
                            if (position == 0) {
                                Intent intent = new Intent(getApplicationContext(), GetServicesActivity.class);
                                startActivityForResult(intent, RC_NEW_SERVICE);
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) { }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

        // Список фото
        photos.add("");
        PhotosAdapter photosAdapter = new PhotosAdapter(this, photos,
                getCacheDir() + "/", true);

        rvSessionPhotos = findViewById(R.id.rvSessionPhotos);
        rvSessionPhotos.setAdapter(photosAdapter);

        rvSessionPhotos.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private static final int MAX_CLICK_DURATION = 500;
            private long startClickTime;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if(clickDuration < MAX_CLICK_DURATION) {
                            int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
                            if (position == 0) {
                                // Добавить фото
                                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, RC_GET_PHOTO);
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) { }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

        findViewById(R.id.btnSessionSave).setOnClickListener(this);

        String token = prefsHelper.getPref("token");

        osteoService.getServices(token);
        session.getSessions(token, 0, 20);
    }

    private void drawCalendar(int[] viewedDays, int viewedYear, int viewedMonth, View view) {
        int mYear = viewedYear;
        int mMonth = viewedMonth;
        if (--mMonth < 0) {
            mMonth = 11;
            mYear--;
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int resId = getResources().getIdentifier("tv" + row + col, "id",
                        getPackageName());
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
                tv.setTag(String.format(Locale.getDefault(),
                        "Date %04d-%02d-%02d", mYear, mMonth + 1, viewedDays[i]));
                if (CalendarHelper.isToday(mYear, mMonth, viewedDays[i])) {
                    tv.setTextColor(getResources().getColor(R.color.colorDark));
                    SpannableString spanString = new SpannableString(mDay);
                    spanString.setSpan(
                            new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                    tv.setText(spanString);
                }
                else if (viewedYear == mYear && viewedMonth == mMonth)
                    tv.setTextColor(getResources().getColor(R.color.colorCalendarTextMain));

                else
                    tv.setTextColor(getResources().getColor(R.color.colorCalendarTextSecond));
                tv.setBackground(null);
                if (sessionDate != null && sessionDate.size() > 0) {
                    Date testDate = CalendarHelper.getDate(
                            CalendarHelper.getDate(mYear, mMonth, viewedDays[i]));
                    for (Date date : sessionDate)
                        if (CalendarHelper.isEqualDates(date, testDate))
                            tv.setBackground(getResources().getDrawable(R.drawable.events_cell_bg));
                }
                else
                    tv.setBackground(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etStartTime:  // Выбор времени начала сеанса
                openTimeDialog(v, sessionMinuteStart, duration);
                break;

            case R.id.etStopTime:   // Выбор времени окончания сеанса
                openTimeDialog(v, sessionMinuteStop, duration);
                break;

            case R.id.btnSessionSave :  // Запись сеанса
                if (serviceItems.size() > 1) {
                    PrefsHelper prefsHelper = new PrefsHelper(this);
                    String token = prefsHelper.getPref("token");
                    String comment = ((TextView) findViewById(R.id.etSessionComment)).getText().toString();
                    if (sessionMode == SESSION_MODE_EDIT) {
                        // - обновление сеанса
                        session.updSession(token, sessionId, clientId, cardId, specId,
                                joinIds(", ", serviceItems),
                                CalendarHelper.dateToString(
                                        sessionDate.get(0), "yyyy-MM-dd"),
                                CalendarHelper.getMinuteWithSeconds(sessionMinuteStart),
                                sumDuration(serviceItems),
                                comment, sumCost(serviceItems), "wait", photos
                        );
                    } else {    // - новый сеанс
                        // Подготовка фото к записи
                        String dir = getCacheDir().getPath();
                        for (int i = 0; i < photos.size(); i++)
                            if (!photos.get(i).isEmpty())
                                photos.set(i, dir + "/" + photos.get(i));
                        if (sessionDate.size() > 0) {
                            session.newSession(token, clientId, cardId, specId,
                                    joinIds(",", serviceItems),
                                    CalendarHelper.dateToString(
                                            sessionDate.get(0), "yyyy-MM-dd"),
                                    CalendarHelper.getMinuteWithSeconds(sessionMinuteStart),
                                    sumDuration(serviceItems),
                                    comment, sumCost(serviceItems), photos
                            );
                            for (int i = 1; i < sessionDate.size(); i++)
                                session.newSession(token, clientId, cardId, specId,
                                        joinIds(",", serviceItems),
                                        CalendarHelper.dateToString(
                                                sessionDate.get(i), "yyyy-MM-dd"),
                                        CalendarHelper.getMinuteWithSeconds(sessionMinuteStart),
                                        sumDuration(serviceItems),
                                        comment, sumCost(serviceItems), null
                                );
                        } else
                            Toast.makeText(this, "Выберите дату сеанса",
                                    Toast.LENGTH_SHORT).show();
                    }
                    finish();

                } else
                    Toast.makeText(this, "Добавьте услуги", Toast.LENGTH_SHORT).show();
                break;

            default:    // Выбор даты сеанса
                String tag = (String) v.getTag();
                if (tag != null && tag.substring(0, 5).equals("Date ")) {
                    SimpleDateFormat sdf = new SimpleDateFormat(Common.DATE_FORMAT, Locale.getDefault());
                    Date selectedDate = null;
                    try {
                        selectedDate = sdf.parse(tag.substring(5));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (repeatedSession) {
                        if (selectedDate != null) {
                            boolean del = false;
                            Iterator<Date> iterator = sessionDate.iterator();
                            while (iterator.hasNext()) {
                                if (CalendarHelper.isEqualDates(selectedDate, iterator.next())) {
                                    iterator.remove();
                                    del = true;
                                    break;
                                }
                            }
                            if (!del)
                                sessionDate.add(selectedDate);
                        }

                    } else {
                        sessionDate.clear();
                        if (selectedDate != null)
                            sessionDate.add(selectedDate);
                    }
                    drawMonth(0);
                }
                break;
        }
    }

    private String joinIds(String delimiter, List<ServiceItemRealm> itemList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StringJoiner result = new StringJoiner(delimiter);
            for (ServiceItemRealm item : itemList)
                if (item.getServID() != 0)
                    result.add(Integer.toString(item.getServID()));
            return result.toString();

        } else {
            List<String> data = new LinkedList<>();
            for (ServiceItemRealm item : itemList)
                if (item.getServID() != 0)
                    data.add(Integer.toString(item.getServID()));
            return TextUtils.join(delimiter, data);
        }
    }

    private int sumDuration(List<ServiceItemRealm> itemList) {
        int result = 0;
        for (ServiceItemRealm item : itemList)
            result += item.getDuration();
        return result;
    }

    private float sumCost(List<ServiceItemRealm> itemList) {
        float result = 0;
        for (ServiceItemRealm item : itemList)
            result += item.getPrice();
        return result;
    }

    private void drawMonth(int inc) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.add(Calendar.MONTH, inc);
        now = calendar.getTimeInMillis();

        int viewedYear = calendar.get(Calendar.YEAR);
        int viewedMonth = calendar.get(Calendar.MONTH);
        int[] viewedDays = CalendarHelper.getMonthArray(viewedYear, viewedMonth);

        drawCalendar(viewedDays, viewedYear, viewedMonth, findViewById(R.id.calendar_month));

        TextView tvPeriod = findViewById(R.id.tvPeriod);
        tvPeriod.setText(CalendarHelper.dateToString(now, "LLLL"));
    }

    private void openTimeDialog(final View v, int minute, final int duration) {
        int hour = minute / 60;
        int min = minute - hour * 60;
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                ((TextView) v).setText(CalendarHelper.getMinute(hourOfDay * 60 + minute));
                // ToDo -> RxJava
                if (v.getId() == R.id.etStartTime) {
                    sessionMinuteStart = hourOfDay * 60 + minute;
                    if (sessionMinuteStop < sessionMinuteStart + duration) {
                        sessionMinuteStop = sessionMinuteStart + duration;
                        if (sessionMinuteStop > 24 * 60)
                            sessionMinuteStop = 24 * 60;
                        ((TextView) findViewById(R.id.etStopTime)).setText(
                                CalendarHelper.getMinute(sessionMinuteStop));
                    }
                }
                else if (v.getId() == R.id.etStopTime) {
                    sessionMinuteStop = hourOfDay * 60 + minute;
                    if (sessionMinuteStart > sessionMinuteStop - duration) {
                        sessionMinuteStart = sessionMinuteStop - duration;
                        if (sessionMinuteStart < 0)
                            sessionMinuteStart = 0;
                        ((TextView) findViewById(R.id.etStartTime)).setText(
                                CalendarHelper.getMinute(sessionMinuteStart));
                    }
                }
            }
        }, hour, min, true).show();
    }

    private CharSequence[] getPlaces() {
        ArrayList<String> places = new ArrayList<>();

        PrefsHelper prefsHelper = new PrefsHelper(this);
        String pref_profile_city = prefsHelper.getPref(PREF_PROFILE_CITY);
        String pref_profile_street = prefsHelper.getPref(PREF_PROFILE_STREET);
        String pref_profile_station = prefsHelper.getPref(PREF_PROFILE_STATION);
        String pref_profile_bld = prefsHelper.getPref(PREF_PROFILE_BLD);
        String pref_profile_flat = prefsHelper.getPref(PREF_PROFILE_FLAT);

        places.add(
                (pref_profile_city.isEmpty()? "": "г. " + pref_profile_city)
                        + (pref_profile_street.isEmpty()? "": ", ул. " + pref_profile_street)
                        + (pref_profile_station.isEmpty()? "": ", ст.м. " + pref_profile_station)
                        + (pref_profile_bld.isEmpty()? "": ", д. " + pref_profile_bld)
                        + (pref_profile_flat.isEmpty()? "": ", кв. " + pref_profile_flat));

        return places.toArray(new CharSequence[places.size()]);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final CharSequence[] places = savedInstanceState.getCharSequenceArray("places");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prompt_select_place)
                .setItems(places, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TextView v = findViewById(savedInstanceState.getInt("viewId"));
                        v.setText(places[which].toString());
                    }
                });
        return builder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null)
            switch (requestCode) {
                case RC_NEW_SERVICE :
                case RC_EDIT_SERVICE :
                    // Добавить услугу
                    int servId = data.getIntExtra("servId", 0);
                    ServiceItemRealm item = new ServiceController().getService(servId).blockingGet();
                    if (requestCode == RC_NEW_SERVICE)
                        serviceItems.add(item);
                    else {
                        for (ServiceItemRealm serviceItemRealm : serviceItems) {
                            if (serviceItemRealm.getServID() == servId) {
                                serviceItemRealm.setTitle(item.getTitle());
                                serviceItemRealm.setDescription(item.getDescription());
                                serviceItemRealm.setPrice(item.getPrice());
                                serviceItemRealm.setCurrency(item.getCurrency());
                                serviceItemRealm.setDuration(item.getDuration());
                            }
                        }
                    }

                    ServicesAdapter servicesAdapter = (ServicesAdapter) rvServices.getAdapter();
                    servicesAdapter.setServiceItems(serviceItems);
                    servicesAdapter.notifyDataSetChanged();

                    duration = sumDuration(serviceItems);
                    sessionMinuteStop = sessionMinuteStart + duration;
                    ((TextView) findViewById(R.id.etStopTime)).setText(
                            CalendarHelper.getMinute(sessionMinuteStop));
                    break;

                case RC_GET_PHOTO :
                    // Сохранить фото
                    if (resultCode == RESULT_OK) {
                        // Получить выбранное фото
                        Uri photo = data.getData();
                        // Записать во временный файл
                        String file = Common.saveTempFile(this, photo);
                        // Загрузить в список фото
                        photos.add(file);
                        // Отобразить изменения на экране
                        RecyclerView sessionPhotos = findViewById(R.id.rvSessionPhotos);
                        sessionPhotos.getAdapter().notifyDataSetChanged();
                    }
                    break;

                case RC_SELECT_SERVICE :
                    // Заменить выбранную услугу другой из списка
                    int selectedServId = data.getIntExtra("selectedServId", 0);
                    servId = data.getIntExtra("servId", 0);
                    if (selectedServId != 0 && servId != 0) {
                        Iterator<ServiceItemRealm> iterator = serviceItems.iterator();
                        while (iterator.hasNext()) {
                            ServiceItemRealm itemRealm = iterator.next();
                            if (itemRealm.getServID() == selectedServId) {
                                int position = serviceItems.indexOf(itemRealm);
                                iterator.remove();
                                serviceItems.add(position,
                                        new ServiceController().getService(servId).blockingGet());

                                servicesAdapter = (ServicesAdapter) rvServices.getAdapter();
                                servicesAdapter.setServiceItems(serviceItems);
                                servicesAdapter.notifyDataSetChanged();

                                duration = sumDuration(serviceItems);
                                sessionMinuteStop = sessionMinuteStart + duration;
                                ((TextView) findViewById(R.id.etStopTime)).setText(
                                        CalendarHelper.getMinute(sessionMinuteStop));
                                break;
                            }
                        }
                    }
                    break;
        }
    }

    @Override
    public void onDelFileDialogPositiveClick(DialogFragment dialog, int position) {
        if (position > 0) {
            // Удалить фото из отображаемого списка
            photos.remove(position);
            // Отобразить изменения
            PhotosAdapter adapter = (PhotosAdapter) rvSessionPhotos.getAdapter();
            adapter.setPhotos(photos);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int position) {
        ServicesAdapter servicesAdapter = (ServicesAdapter) rvServices.getAdapter();
        serviceItems.remove(position);
        servicesAdapter.setServiceItems(serviceItems);
        servicesAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.dispose();
        osteoService.dispose();
    }
}
