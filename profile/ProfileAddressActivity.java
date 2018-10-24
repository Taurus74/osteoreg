package com.aconst.spinareg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.R;

import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_BLD;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_CITY;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_FLAT;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_STATION;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_STREET;

public class AddressActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvAddr1;
    private TextView tvAddr2;
    private TextView tvAddr3;
    private TextView tvAddr4;
    private TextView tvAddr5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_address);
        setTitle(R.string.title_address);

        Intent intent = getIntent();

        tvAddr1 = findViewById(R.id.tvAddr1);
        tvAddr1.setText(intent.getStringExtra(PREF_PROFILE_CITY));

        tvAddr2 = findViewById(R.id.tvAddr2);
        tvAddr2.setText(intent.getStringExtra(PREF_PROFILE_STREET));

        tvAddr3 = findViewById(R.id.tvAddr3);
        tvAddr3.setText(intent.getStringExtra(PREF_PROFILE_STATION));

        tvAddr4 = findViewById(R.id.tvAddr4);
        tvAddr4.setText(intent.getStringExtra(PREF_PROFILE_BLD));

        tvAddr5 = findViewById(R.id.tvAddr5);
        tvAddr5.setText(intent.getStringExtra(PREF_PROFILE_FLAT));

        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if (v.getId() == R.id.btnOk) {
            View emptyView = checkFields();
            if (emptyView == null) {
                intent.putExtra(PREF_PROFILE_CITY, tvAddr1.getText().toString());
                intent.putExtra(PREF_PROFILE_STREET, tvAddr2.getText().toString());
                intent.putExtra(PREF_PROFILE_STATION, tvAddr3.getText().toString());
                intent.putExtra(PREF_PROFILE_BLD, tvAddr4.getText().toString());
                intent.putExtra(PREF_PROFILE_FLAT, tvAddr5.getText().toString());
                setResult(RESULT_OK, intent);
                finish();

            } else {
                emptyView.requestFocus();
                Toast.makeText(this, "Необходимое поле не заполнено", Toast.LENGTH_SHORT).show();
            }

        } else {
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private View checkFields() {
        if (tvAddr1.getText().toString().isEmpty())
            return findViewById(R.id.tvAddr1);
        else if (tvAddr2.getText().toString().isEmpty())
            return findViewById(R.id.tvAddr2);
        else if (tvAddr4.getText().toString().isEmpty())
            return findViewById(R.id.tvAddr4);
        else
            return null;
    }
}
