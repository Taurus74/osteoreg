package com.aconst.spinareg.services;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.OsteoService;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.model.ServiceItemRealm;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class NewEditServiceActivity extends AppCompatActivity {
    private int currency;
    private OsteoService osteoService= new OsteoService(this);

    private final static int MODE_NEW_SERVICE = 0;
    private final static int MODE_EDIT_SERVICE = 1;
    private int mode;
    private int serviceId;
    private ServiceItemRealm serviceItem;
    private String[] currencies;

    private TextView etTitle;
    private TextView etDescription;
    private TextView etDuration;
    private TextView etPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        Intent intent = getIntent();
        if (intent.hasExtra("serviceId")) {
            mode = MODE_EDIT_SERVICE;
            serviceId = intent.getIntExtra("serviceId", 0);
            serviceItem = new ServiceController().getService(serviceId).blockingGet();
            setTitle(R.string.title_edit_service);

        }
        else {
            mode = MODE_NEW_SERVICE;
            setTitle(R.string.title_add_service);
        }

        currencies = getResources().getStringArray(R.array.currency);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = findViewById(R.id.spCurrency);
        spinner.setAdapter(adapter);
        if (mode == MODE_NEW_SERVICE)
            spinner.setSelection(0);
        else
            spinner.setSelection(adapter.getPosition(serviceItem.getCurrency()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);

        if (mode == MODE_EDIT_SERVICE) {
            etTitle.setText(serviceItem.getTitle());
            etDescription.setText(serviceItem.getDescription());
            etDuration.setText(String.format(Locale.getDefault(), "%d",
                    serviceItem.getDuration()));
            etPrice.setText(String.format(Locale.getDefault(), "%.2f", serviceItem.getPrice()));
        }

        Button btnServiceSave = findViewById(R.id.btnServiceSave);
        btnServiceSave.setOnClickListener(onServiceSaveListener);
    }

    View.OnClickListener onServiceSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();
            View view = checkFields();
            if (view == null) {
                PrefsHelper prefsHelper = new PrefsHelper(context);
                String token = prefsHelper.getPref("token");

                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                int duration = Integer.parseInt(etDuration.getText().toString());
                float price = Float.parseFloat(
                        etPrice.getText().toString().replace(",", "."));

                if (token.equals("")) {
                    ServiceController controller = new ServiceController();
                    int servId;
                    ServiceItemRealm serviceItemRealm = new ServiceItemRealm();
                    if (mode == MODE_NEW_SERVICE)
                        servId = controller.getNextId().blockingGet();
                    else
                        servId = serviceId;
                    serviceItemRealm.setServID(servId);
                    serviceItemRealm.setSid(-1);
                    serviceItemRealm.setTitle(title);
                    serviceItemRealm.setDescription(description);
                    serviceItemRealm.setDuration(duration);
                    serviceItemRealm.setCurrency(currencies[currency - 1]);
                    serviceItemRealm.setPrice(price);

                    List<ServiceItemRealm> serviceItemRealmList = new LinkedList<>();
                    serviceItemRealmList.add(serviceItemRealm);
                    controller.updateServices(serviceItemRealmList).subscribe();
                    setResult(RESULT_OK);
                }
                else {
                    if (mode == MODE_NEW_SERVICE)
                        osteoService.addService(token,
                                title, description, duration, price, currency);
                    else
                        osteoService.updService(token,
                                serviceId, title, description, duration, price, currency);
                    setResult(RESULT_OK);
                }
                finish();
            } else {
                view.requestFocus();
            }
        }
    };

    private View checkFields() {
        TextView tv = findViewById(R.id.etTitle);
        if (tv.getText().toString().isEmpty()) {
            Toast.makeText(this, "Заполните поле: " + tv.getHint().toString(),
                    Toast.LENGTH_SHORT).show();
            return tv;
        }

        tv = findViewById(R.id.etDescription);
        if (tv.getText().toString().isEmpty()) {
            Toast.makeText(this, "Заполните поле: " + tv.getHint().toString(),
                    Toast.LENGTH_SHORT).show();
            return tv;
        }

        tv = findViewById(R.id.etDuration);
        if (tv.getText().toString().isEmpty()) {
            Toast.makeText(this, "Заполните поле: " + tv.getHint().toString(),
                    Toast.LENGTH_SHORT).show();
            return tv;
        }

        tv = findViewById(R.id.etPrice);
        if (tv.getText().toString().isEmpty()) {
            Toast.makeText(this, "Заполните поле: " + tv.getHint().toString(),
                    Toast.LENGTH_SHORT).show();
            return tv;
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        osteoService.dispose();
    }
}
