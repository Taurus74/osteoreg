package com.aconst.spinareg.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aconst.spinareg.FirebaseService;
import com.aconst.spinareg.MenuHelper;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Clients;
import com.aconst.spinareg.api.Messages;
import com.aconst.spinareg.api.OsteoService;
import com.aconst.spinareg.api.Profile;
import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.messages.NotificationHelper;
import com.aconst.spinareg.model.Message;
import com.aconst.spinareg.model.Vacation;
import com.aconst.spinareg.sessions.EditSessionActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class CalendarActivity extends AppCompatActivity
        implements SessionDayFragment.OnFragmentInteractionListener,
        SessionWeekFragment.OnFragmentInteractionListener,
        SessionMonthFragment.OnFragmentInteractionListener,
        SessionListFragment.OnFragmentInteractionListener,
        Messages.SetMessageList, Messages.SetMessageArchList {

    private Session session = new Session(this);
    private OsteoService osteoService = new OsteoService(this);
    private Clients clients = new Clients(this);
    private Profile profile = new Profile(this);
    private Messages messages = new Messages(this, this);

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ViewPager viewPager = findViewById(R.id.calendar_pager);
            viewPager.getAdapter().notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startFCMNotification();
        initRealm();

        setContentView(R.layout.activity_calendar);
        setTitle(R.string.menu_calendar);

        PrefsHelper prefsHelper = new PrefsHelper(this);
        String token = prefsHelper.getPref("token");
        // Чтение с сервера в Realm-кэш
        session.getSessions(token, 0, 20);  // Сеансы
        osteoService.getServices(token);                // Услуги
        clients.getClients(token, 0, 20);   // Клиенты
        profile.getSpecialties(token);                  // Специальности
        profile.getUser(token);

        profile.getSpecData(token, prefsHelper.getPref("specId", 0), false);

        TabLayout tabLayout = findViewById(R.id.tab_calendar_layout);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab2));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab3));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab4));

        final ViewPager viewPager = findViewById(R.id.calendar_pager);
        final PagerAdapter adapter = new TabPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        viewPager.setCurrentItem(prefsHelper.getPref("calendarPage", 0));

        startNotificationCheck(token);

        registerReceiver(receiver, new IntentFilter("Update_session_UI"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHelper menuHelper = new MenuHelper(this);
        return menuHelper.processMenu(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        osteoService.dispose();
        session.dispose();
        clients.dispose();
        messages.dispose();
        profile.dispose();
        unregisterReceiver(receiver);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void addMessagesToList(List<Message> messageList) {
    }

    @Override
    public void setMessageList(List<Message> messageList) {
    }

    @Override
    public void addMessagesArchToList(List<Message> messageList) {
    }

    @Override
    public void setMessageArchList(List<Message> messageList) {
    }

    private void startNotificationCheck(String token) {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = createIntent(token);
            pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC,
                    SystemClock.currentThreadTimeMillis(),
                    1000 * 600, pendingIntent);
        }
    }

    private Intent createIntent(String token) {
        Intent intent = new Intent(this, NotificationHelper.class);
        intent.setAction("checkMessages");
        intent.putExtra("token", token);
        return intent;
    }

    private void initRealm() {
        Realm.init(this);

        if (Realm.getDefaultConfiguration() == null)
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                    .name("osteoreg.realm")
                    .deleteRealmIfMigrationNeeded()
                    .build()
            );
    }

    private void startFCMNotification() {
        Intent intent = new Intent(this, FirebaseService.class);
        startService(intent);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Start", "getInstanceId failed", task.getException());
                            return;
                        }
                        PrefsHelper prefsHelper = new PrefsHelper(CalendarActivity.this);
                        prefsHelper.setPref("pushToken", task.getResult().getToken());
                    }
                }
        );
    }
}
