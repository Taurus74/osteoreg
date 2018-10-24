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
import com.aconst.spinareg.ConfirmDelPhotoDialog;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.PhotosAdapter;
import com.aconst.spinareg.adapters.ServicesAdapter;
import com.aconst.spinareg.api.OsteoService;
import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.ServiceItem;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.services.GetServicesActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class NewSession2Activity extends AppCompatActivity
        implements View.OnClickListener, ConfirmDelPhotoDialog.NoticeDialogListener {
    private Session session = new Session(this);
    private OsteoService osteoService = new OsteoService(this);

    private static final int SESSION_MODE_NEW = 0;
    private static final int SESSION_MODE_EDIT = 1;

    private long now = Calendar.getInstance().getTimeInMillis();
    private int sessionMode;

    private List<Date> sessionDate = new ArrayList<>();
    private int sessionMinuteStart;
    private int sessionMinuteStop;
    private int duration;
    private int sessionId = 0;
    private int clientId;
    private int cardId;
    private int specId;
    private boolean repeatedSession = false;
    private List<ServiceItem> serviceItems = new ArrayList<>();
    private List<String> photos = new LinkedList<>();
    private RecyclerView sessionPhotos;
    private int delPhoto = 0;

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
                serviceItems = parseServices(sessionItemRealm.getService());
                // ToDo - место проведения сеанса
//                ((TextView) findViewById(R.id.tvSessionPlace)).setText(sessionItemRealm.);
                ((TextView) findViewById(R.id.etSessionComment)).setText(sessionItemRealm.getSpecComment());

            } else
                Toast.makeText(this, "Ошибка получения данных о сеансе", Toast.LENGTH_SHORT).show();
        }

        clientId = intent.getIntExtra("clientId", 2);   // Тестовый клиент
        cardId = intent.getIntExtra("cardId", 1);       // Тестовый id карточки клиента

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
        ServiceItem firstItem = new ServiceItem();
        serviceItems.add(0, firstItem);
        ServicesAdapter servicesAdapter = new ServicesAdapter(serviceItems,true);

        RecyclerView sessionServices = findViewById(R.id.SessionServices);
        sessionServices.setAdapter(servicesAdapter);

        sessionServices.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
                                startActivityForResult(intent, 0);
                            }
                            else {
                                ServicesAdapter servicesAdapter = (ServicesAdapter) rv.getAdapter();
                                ServiceItem selected = servicesAdapter.getItem(position);
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        // Список фото
        photos.add("");
        PhotosAdapter photosAdapter = new PhotosAdapter(photos, getCacheDir() + "/", true);

        sessionPhotos = findViewById(R.id.SessionPhotos);
        sessionPhotos.setAdapter(photosAdapter);

        sessionPhotos.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
                                // Выбор фото
                                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, 1);
                            }
                            else {
                                delPhoto = position;
                                ConfirmDelPhotoDialog dialog = new ConfirmDelPhotoDialog();
                                dialog.onCreateDialog(NewSession2Activity.this).show();
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

    private List<ServiceItem> parseServices(String services) {
        List<ServiceItem> result = new ArrayList<>();
        if (services != null && !services.isEmpty()) {
            String[] data = services.split(",");
            ServiceController controller = new ServiceController();
            for (String s : data) {
                // ToDo - избавиться от класса ServiceItem
                ServiceItemRealm serviceItemRealm =
                        controller.getService(Integer.parseInt(s)).blockingGet();
                result.add(new ServiceItem(
                        serviceItemRealm.getServID(),
                        serviceItemRealm.getTitle(),
                        serviceItemRealm.getPrice(),
                        serviceItemRealm.getCurrency(),
                        serviceItemRealm.getDuration(),
                        serviceItemRealm.getDescription()));
            }
        }
        return result;
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
                        // ToDo - тестировать
                        session.updSession( // - обновление сеанса
                                token,
                                sessionId,
                                clientId,
                                cardId,
                                specId,
                                joinIds(", ", serviceItems),
                                // ToDo
                                CalendarHelper.dateToString(sessionDate.get(0), "yyyy-MM-dd"),
                                getTime(sessionMinuteStart),
                                sumDuration(serviceItems),
                                comment,
                                sumCost(serviceItems),
                                "wait",
                                photos
                        );
                    } else {    // - новый сеанс
                        String dir = getCacheDir().getPath();
                        for (int i = 0; i < photos.size(); i++)
                            if (!photos.get(i).isEmpty())
                                photos.set(i, dir + "/" + photos.get(i));
                        session.newSession(
                                token,
                                clientId,
                                cardId,
                                specId,
                                joinIds(",", serviceItems),
                                // ToDo
                                CalendarHelper.dateToString(sessionDate.get(0), "yyyy-MM-dd"),
                                getTime(sessionMinuteStart),
                                sumDuration(serviceItems),
                                comment,
                                sumCost(serviceItems),
                                photos
                        );
                    }

                } else
                    Toast.makeText(this, "Добавьте услуги", Toast.LENGTH_SHORT).show();
                break;

//            case R.id.photoItemAdd :
//                Toast.makeText(this, "Добавить фото...", Toast.LENGTH_SHORT).show();
//                break;

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
                    if (!repeatedSession)
                        sessionDate.clear();
                    if (selectedDate != null)
                        sessionDate.add(selectedDate);
                    drawMonth(0);
                }
                break;
        }
    }

    private String getTime(int minute) {
        int hour = minute / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute - hour * 60);
    }

    private String joinIds(String delimiter, List<ServiceItem> itemList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StringJoiner result = new StringJoiner(delimiter);
            for (ServiceItem item : itemList)
                if (item.getId() != 0)
                    result.add(Integer.toString(item.getId()));
            return result.toString();

        } else {
            List<String> data = new ArrayList<>();
            for (ServiceItem item : itemList)
                if (item.getId() != 0)
                    data.add(Integer.toString(item.getId()));
            return TextUtils.join(delimiter, data);
        }
    }

    private int sumDuration(List<ServiceItem> itemList) {
        int result = 0;
        for (ServiceItem item : itemList)
            result += item.getDuration();
        return result;
    }

    private float sumCost(List<ServiceItem> itemList) {
        float result = 0;
        for (ServiceItem item : itemList)
            result += item.getPrice();
        return result;
    }

    private void drawMonth(int inc) {
        Calendar calendar = Calendar.getInstance();
        if (sessionDate.size() > 0)
            calendar.setTime(sessionDate.get(0));
        else
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

        places.add("г. Москва, ул. Тверская, д.1");
        places.add("г. Москва, ул. Лубянка, д.8");
        places.add("г. Москва, ул. Знаменка, д.2");

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
        if (data != null) {
            if (requestCode == 0) {
                int servId = data.getIntExtra("servId", 0);
                ServiceItemRealm item = new ServiceController().getService(servId).blockingGet();

                ServiceItem serviceItem = new ServiceItem(item.getServID(), item.getTitle(),
                        item.getPrice(), item.getCurrency(),
                        item.getDuration(), item.getDescription());
                serviceItems.add(serviceItem);
                RecyclerView sessionServices = findViewById(R.id.SessionServices);
                sessionServices.getAdapter().notifyDataSetChanged();

                duration = sumDuration(serviceItems);
                sessionMinuteStop = sessionMinuteStart + duration;
                ((TextView) findViewById(R.id.etStopTime)).setText(
                        CalendarHelper.getMinute(sessionMinuteStop));

            } else if (requestCode == 1) {
                // Сохранить фото
                if (resultCode == RESULT_OK) {
                    // Получить выбранное фото
                    Uri photo = data.getData();
                    // Записать во временный файл
                    // ToDo
                    String file = Common.saveTempFile(this, photo);
                    // Загрузить в список фото
//                    byte[] photoData = Common.readFile(file);
//                    photos.add(BitmapFactory.decodeByteArray(photoData, 0, photoData.length));
                    photos.add(file);
                    // Отобразить изменения на экране
                    RecyclerView sessionPhotos = findViewById(R.id.SessionPhotos);
                    sessionPhotos.getAdapter().notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (delPhoto > 0) {
            photos.remove(delPhoto);
//            PrefsHelper prefsHelper = new PrefsHelper(this);
//            String token = prefsHelper.getPref("token");
//            session.deleteFile(token, sessionId, delPhoto - 1);
            sessionPhotos.getAdapter().notifyDataSetChanged();
        }
        delPhoto = 0;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        delPhoto = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.dispose();
        osteoService.dispose();
    }
}
