package com.aconst.spinareg.services;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ServiceResponseAdapter;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.controllers.ServiceController;

import java.util.List;

public class GetServicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_services);
        setTitle(R.string.title_service_select);

        final List<ServiceItemRealm> responseItems
                = new ServiceController().getServices().blockingGet();
        Intent intent = getIntent();
        final int selectedServId = intent.getIntExtra("selectedServId", 0);

        ServiceResponseAdapter adapter = new ServiceResponseAdapter(responseItems, this);
        ListView lvServiceList = findViewById(R.id.lvServiceList);
        lvServiceList.setAdapter(adapter);
        lvServiceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("servId", responseItems.get(position).getServID());
                intent.putExtra("selectedServId", selectedServId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
