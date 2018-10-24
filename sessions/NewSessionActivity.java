package com.aconst.spinareg.sessions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ClientsAdapter;
import com.aconst.spinareg.clients.ContactsActivity;
import com.aconst.spinareg.clients.NewEditClientActivity;
import com.aconst.spinareg.controllers.ClientsController;
import com.aconst.spinareg.model.Client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NewSessionActivity extends AppCompatActivity implements View.OnClickListener {
    private Date sessionDate;
    private int sessionMinuteStart;
    private int sessionMinuteStop;

    private List<Client> clients;
    private ListView listView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent(getApplicationContext(), NewSession2Activity.class);
            newIntent.putExtra("sessionDate", sessionDate.getTime());
            newIntent.putExtra("sessionMinuteStart", sessionMinuteStart);
            newIntent.putExtra("sessionMinuteStop", sessionMinuteStop);
            newIntent.putExtra("clientId", intent.getIntExtra("newClientId", 0));

            startActivity(newIntent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session);
        setTitle(R.string.title_new_session);

        EditText etClientSearch = findViewById(R.id.etClientSearch);
        etClientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                ClientsAdapter adapter = (ClientsAdapter) listView.getAdapter();
                if (s.length() == 0)
                    adapter.setClients(clients);
                else {
                    List<Client> filteredClients = new LinkedList<>();
                    for (Client client : clients) {
                        if (client.getFullName().toLowerCase().contains(s.toString().toLowerCase()))
                            filteredClients.add(client);
                    }
                    adapter.setClients(filteredClients);
                }
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.tvNewClient).setOnClickListener(this);
        findViewById(R.id.tvSelectClient).setOnClickListener(this);

        Intent intent = getIntent();
        long time = intent.getLongExtra("sessionDate", 0);
        if (time > 0)
            sessionDate = CalendarHelper.getDate(time);
        sessionMinuteStart = intent.getIntExtra("sessionMinuteStart", 0);
        sessionMinuteStop = intent.getIntExtra("sessionMinuteStop", 0);

        clients = new ClientsController().getClients().blockingGet();

        listView = findViewById(R.id.lvClients);
        final ClientsAdapter adapter = new ClientsAdapter(clients, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Client client = (Client) adapter.getItem(position);

                Intent newIntent = new Intent(getApplicationContext(), NewSession2Activity.class);
                newIntent.putExtra("sessionDate", sessionDate.getTime());
                newIntent.putExtra("sessionMinuteStart", sessionMinuteStart);
                newIntent.putExtra("sessionMinuteStop", sessionMinuteStop);
                newIntent.putExtra("clientId", client.getId());
                newIntent.putExtra("cardId", client.getCardID());

                startActivity(newIntent);
                NewSessionActivity.this.finish();
            }
        });

        registerReceiver(receiver, new IntentFilter("New_client_updater"));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.tvNewClient:
                // Создать клиента для выбора в сеансе
                intent = new Intent(this, NewEditClientActivity.class);
                startActivity(intent);
                break;

            case R.id.tvSelectClient :
                // Выбрать клиента из списка контактов
                intent = new Intent(this, ContactsActivity.class);
                intent.putExtra("singleClient", true);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
