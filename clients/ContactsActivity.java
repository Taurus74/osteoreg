package com.aconst.spinareg.clients;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ContactsAdapter;
import com.aconst.spinareg.api.Clients;
import com.aconst.spinareg.controllers.ClientsController;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {
    private ContactsAdapter adapter;
    private Clients clients = new Clients(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setTitle(R.string.title_contacts);

        Intent intent = getIntent();
        boolean singleClient = false;
        if (intent.hasExtra("singleClient")) {
            singleClient = intent.getBooleanExtra("singleClient", false);
        }

        ArrayList<Contact> contacts = ContactsReader.getAll(this);

        ListView listView = findViewById(R.id.contact_list_view);
        adapter = new ContactsAdapter(contacts, this, singleClient);
        listView.setAdapter(adapter);

        if (singleClient) {
            findViewById(R.id.btnCancel).setVisibility(View.GONE);
            findViewById(R.id.btnOk).setVisibility(View.GONE);

        } else {
            findViewById(R.id.btnCancel).setOnClickListener(this);
            findViewById(R.id.btnOk).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk : {
                ArrayList<Contact> contacts = adapter.getCheckedContacts();
                for (int i = 0; i < contacts.size(); i++)
                    addClient(contacts.get(i));
            }

            case R.id.btnCancel :
                finish();
                break;

            case R.id.tvName :
            case R.id.tvPhone :
                int selectedId = (Integer) v.getTag();
                if (selectedId >= 0)
                    addClient((Contact) adapter.getItem(selectedId));
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    private void addClient(Contact contact) {
        int cardId = new ClientsController().getNextCardId().blockingGet();
        PrefsHelper prefsHelper = new PrefsHelper(this);
        String token = prefsHelper.getPref("token");
        String complaint = "Добавлено из контактов: " + contact.getName()
                + ", адрес: " + contact.getAddress();
        String anamnesis = "Добавлено из контактов: " + contact.getPhone();
        String comment = "Добавлено из контактов: " + contact.getEmail();
        int groupId = 1;
        clients.addClientPro(token, cardId, complaint, anamnesis, comment, groupId);
        // ToDo
        // записать аватар в базу
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clients.dispose();
    }
}
