package com.aconst.spinareg.profile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.R;

import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_NAME1;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_NAME2;
import static com.aconst.spinareg.profile.Profile1Fragment.PREF_PROFILE_NAME3;

public class ProfileNameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvName1;
    private TextView tvName2;
    private TextView tvName3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_name);

        Intent intent = getIntent();

        tvName1 = findViewById(R.id.tvFirstName);
        tvName1.setText(intent.getStringExtra(PREF_PROFILE_NAME1));

        tvName2 = findViewById(R.id.tvSecondName);
        tvName2.setText(intent.getStringExtra(PREF_PROFILE_NAME2));

        tvName3 = findViewById(R.id.tvLastName);
        tvName3.setText(intent.getStringExtra(PREF_PROFILE_NAME3));

        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if (v.getId() == R.id.btnOk) {
            View emptyView = checkFields();
            if (emptyView == null) {
                intent.putExtra(PREF_PROFILE_NAME1, tvName1.getText().toString());
                intent.putExtra(PREF_PROFILE_NAME2, tvName2.getText().toString());
                intent.putExtra(PREF_PROFILE_NAME3, tvName3.getText().toString());
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
        if (tvName3.getText().toString().isEmpty())
            return findViewById(R.id.tvLastName);
        else if (tvName1.getText().toString().isEmpty())
            return findViewById(R.id.tvFirstName);
        else
            return null;
    }

}
